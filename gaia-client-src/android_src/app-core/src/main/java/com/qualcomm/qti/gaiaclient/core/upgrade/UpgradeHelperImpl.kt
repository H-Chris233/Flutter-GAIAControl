/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.upgrade

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.UpgradePublisher
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DeviceInformationSubscriber
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ProtocolSubscriber
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.QTILFeaturesSubscriber
import com.qualcomm.qti.gaiaclient.core.upgrade.data.ChunkSize
import com.qualcomm.qti.gaiaclient.core.upgrade.data.ChunkSizeType
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpdateOptions
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeErrorStatus
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeState
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger
import com.qualcomm.qti.libraries.upgrade.UpgradeListener
import com.qualcomm.qti.libraries.upgrade.UpgradeManagerFactory
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationType
import com.qualcomm.qti.libraries.upgrade.data.EndType
import com.qualcomm.qti.libraries.upgrade.data.ResumePoint
import com.qualcomm.qti.libraries.upgrade.data.UpgradeError
import com.qualcomm.qti.libraries.upgrade.messages.OpCodes
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessageListener
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean

internal class UpgradeHelperImpl(publicationManager: PublicationManager) {
    private var mHelperListener: UpgradeHelperListener? = null
    private var mState: UpgradeState? = null
    private val mUpgradePublisher = UpgradePublisher()
    private val mParameters = UpgradeParameters()
    private val mUploadQueue = ConcurrentLinkedQueue<UploadRequest>()
    private val mIsBlocked = AtomicBoolean(false)
    private val mChunkSize = ChunkSize()

    /**
     * Listen for connection states in order to update the state when reconnecting.
     */
    private val mConnectionSubscriber: ConnectionSubscriber = object : ConnectionSubscriber {
        override fun getExecutionType(): ExecutionType {
            return ExecutionType.BACKGROUND
        }

        override fun onConnectionStateChanged(device: Device, state: ConnectionState) {
            Logger.log(LOG_METHODS, TAG, "Subscriber->onConnectionStateChanged", Pair("state", state))
            if (!isUpgrading) {
                // upgrade is not processing, nothing to do.
                return
            }
            if (state == ConnectionState.DISCONNECTED) {
                mChunkSize.reset() // size is checked during handshake
                if (mState == UpgradeState.VALIDATION) {
                    // expected reboot
                    setState(UpgradeState.REBOOT)
                } else if (!isReconnecting) {
                    // upgrade is ongoing and the disconnection is not an expected reboot
                    setState(UpgradeState.RECONNECTING)
                }
            } else if (state == ConnectionState.CONNECTED) {
                logBytes(mParameters.isLogged)
            }
        }

        override fun onConnectionError(device: Device, reason: BluetoothStatus) {
            // captured by the Upgrade Reconnection Delegate
            if (reason == BluetoothStatus.RECONNECTION_TIME_OUT && isUpgrading) {
                abort(UpgradeFail(UpgradeErrorStatus.RECONNECTION_ERROR))
            }
        }
    }

    /**
     * Listen for the features discovery: if the upgrade feature is not discovered -> not supported
     */
    private val mDeviceInformationSubscriber: DeviceInformationSubscriber = object : DeviceInformationSubscriber {
        override fun getExecutionType(): ExecutionType {
            return ExecutionType.BACKGROUND
        }

        override fun onInfo(info: DeviceInfo, update: Any) {}
        override fun onError(info: DeviceInfo, reason: Reason) {
            Logger.log(
                LOG_METHODS, TAG, "Subscriber->onError",
                Pair("info", info), Pair("reason", reason)
            )
            if (info == DeviceInfo.GAIA_VERSION) {
                abort(UpgradeFail(UpgradeErrorStatus.GAIA_INITIALISATION_ERROR))
            }
        }
    }
    private val mFeatureSubscriber: QTILFeaturesSubscriber = object : QTILFeaturesSubscriber {
        override fun getExecutionType(): ExecutionType {
            return ExecutionType.BACKGROUND
        }

        override fun onFeatureSupported(feature: QTILFeature, version: Int) {
            // nothing to do: upgrade to resume through #onPlugged
        }

        override fun onFeatureNotSupported(feature: QTILFeature, reason: Reason) {
            if (feature == QTILFeature.UPGRADE && isUpgrading) {
                abort(UpgradeFail(UpgradeErrorStatus.GAIA_INITIALISATION_ERROR))
            }
        }

        override fun onError(reason: Reason) {
            if (isUpgrading) {
                abort(UpgradeFail(UpgradeErrorStatus.GAIA_INITIALISATION_ERROR))
            }
        }
    }
    private val mProtocolSubscriber: ProtocolSubscriber = object : ProtocolSubscriber {
        override fun getExecutionType(): ExecutionType {
            return ExecutionType.BACKGROUND
        }

        override fun onSizeInfo(info: SizeInfo, size: Int) {
            when (info) {
                SizeInfo.MAX_RX_PAYLOAD -> onAvailableSizeUpdate(size)
                SizeInfo.OPTIMUM_RX_PAYLOAD -> onOptimumSizeUpdate(size)
            }
        }

        override fun onError(info: Any, reason: Reason) {
            if (info is SizeInfo) {
                when (info) {
                    SizeInfo.MAX_RX_PAYLOAD -> onAvailableSizeUpdate(GaiaPdu.Payload.minLength)
                    SizeInfo.OPTIMUM_RX_PAYLOAD -> onOptimumSizeUpdate(GaiaPdu.Payload.minLength)
                }
            }
        }
    }
    private val mUpgradeListener: UpgradeListener = object : UpgradeListener {
        override fun sendUpgradeMessage(bytes: ByteArray, uploadListener: UpgradeMessageListener?) {
            Logger.log(
                LOG_BYTES, TAG, "Listener->sendUpgradeMessage",
                Pair("plugin", mHelperListener), Pair("data", bytes)
            )
            if (mHelperListener != null && uploadListener != null) {
                prepareUploadRequest(bytes, uploadListener)
            } else if (mHelperListener != null) {
                mHelperListener!!.sendUpgradeMessage(bytes)
            }
        }

        override fun onUpgradeError(error: UpgradeError) {
            Logger.log(LOG_METHODS, TAG, "Listener->onUpgradeError", Pair("error", error))
            resetUpload()
            abort(UpgradeFail(error))
        }

        override fun onResumePointChanged(point: ResumePoint) {
            Logger.log(LOG_METHODS, TAG, "Listener->onResumePointChanged", Pair("point", point))
            setState(matchResumePointToState(point))
        }

        override fun onUpgradeEnd(type: EndType) {
            Logger.log(LOG_METHODS, TAG, "Listener->onUpgradeEnd")
            val state = getState(type)
            setState(state)
            mUpgradePublisher.publishProgress(UpgradeProgress.end(state, type))
        }

        override fun onUploadProgress(resumePoint: ResumePoint, progress: Double) {
            Logger.log(
                LOG_METHODS, TAG, "Listener->onFileUploadProgress",
                Pair("progress", progress)
            )
            mUpgradePublisher.publishProgress(UpgradeProgress.upload(matchResumePointToState(resumePoint), progress))
        }

        override fun onConfirmationRequired(type: ConfirmationType, options: Array<ConfirmationOptions>) {
            Logger.log(
                LOG_METHODS, TAG, "Listener->onConfirmationRequired", Pair("confirmation", type),
                Pair("options", options)
            )
            mUpgradePublisher
                .publishProgress(UpgradeProgress.confirmation(mState, matchTypeToConfirmation(type), options))
        }

        override fun enableUpgradeMode() {
            Logger.log(LOG_METHODS, TAG, "Listener->enableUpgradeMode")
            sendConnectUpgrade()
        }

        override fun disableUpgradeMode() {
            Logger.log(LOG_METHODS, TAG, "Listener->disableUpgradeMode")
            sendDisconnectUpgrade()
        }

        override fun isAborting() {
            Logger.log(LOG_METHODS, TAG, "Listener->isAborting")
            if (mState != UpgradeState.ABORTING && mState != UpgradeState.ABORTED) {
                // prevents multi call to isAborting to update the state
                setState(UpgradeState.ABORTING)
            }
        }

        override fun onAlert(alert: UpgradeAlert, raised: Boolean) {
            Logger.log(LOG_METHODS, TAG, "Listener->onAlert", Pair("alert", alert), Pair("raised", raised))
            mUpgradePublisher.publishAlert(alert, raised)
        }
    }
    private val mUpgradeManager = UpgradeManagerFactory.buildUpgradeManager(
        mUpgradeListener
    ) { task: Runnable?, delayMs: Long ->
        GaiaClientService.getTaskManager().schedule(
            task!!, delayMs
        )
    }

    init {
        publicationManager.register(mUpgradePublisher)
        publicationManager.subscribe(mConnectionSubscriber)
        publicationManager.subscribe(mDeviceInformationSubscriber)
        publicationManager.subscribe(mFeatureSubscriber)
        publicationManager.subscribe(mProtocolSubscriber)
        mUpgradeManager.showDebugLogs(DEBUG.Upgrade.UPGRADE_MANAGER)
    }

    fun confirm(confirmation: UpgradeConfirmation, option: ConfirmationOptions) {
        Logger.log(
            LOG_METHODS, TAG, "onConfirmationRequired",
            Pair("confirmation", confirmation), Pair("option", option)
        )
        mUpgradeManager.onConfirmation(matchConfirmationToType(confirmation), option)
    }

    fun abort() {
        Logger.log(LOG_METHODS, TAG, "abort", Pair("state", mState))
        abort(null)
    }

    fun startUpgrade(context: Context, options: UpdateOptions) {
        Logger.log(
            LOG_METHODS, TAG, "startUpgrade", Pair(
                "file_uri",
                if (options.fileLocation == null) "null" else options.fileLocation.lastPathSegment
            )
        )
        if (isUpgrading) {
            // an upgrade is already in progress: no error as progress will be published
            return
        }

        // getting the data from the Uri
        val file = getBytesFromUri(context, options.fileLocation)
        val md5Checksum = BytesUtils.getMD5FromUri(context, options.fileLocation)
        if (file.isEmpty()) {
            // bytes could not be found for the given uri or the file is empty
            mUpgradePublisher.publishError(UpgradeFail(UpgradeErrorStatus.FILE_ERROR))
            setState(UpgradeState.ABORTED)
            mUpgradePublisher.publishProgress(UpgradeProgress.end(UpgradeState.ABORTED, EndType.ABORTED))
            return
        }

        // setting the options
        var expectedChunkSize = if (0 < options.expectedChunkSize) options.expectedChunkSize else mChunkSize.default
        if (options.useRwcp) {
            expectedChunkSize -= 1
        }

        // Set useRwcp
        mParameters.set(
            expectedChunkSize,
            options.isLogged,
            options.isUploadFlushed,
            options.isUploadAcknowledged && !options.useRwcp,
            options.useRwcp
        )
        logBytes(mParameters.isLogged)
        // init the chunk size for the manager
        setChunkSize()

        // start upgrade
        setState(UpgradeState.INITIALISATION)
        mUpgradeManager.startUpgrade(file, md5Checksum)
    }

    fun onUpgradeMessage(data: ByteArray) {
        Logger.log(LOG_BYTES, TAG, "Listener->onUpgradeMessage", Pair("data", data))
        mUpgradeManager.onUpgradeMessage(data)
    }

    fun onAcknowledged() {
        Logger.log(LOG_METHODS, TAG, "Listener->onAcknowledged")
        onMessageTransferred()
    }

    fun onSendingFailed(ignored: Reason) {
        Logger.log(LOG_METHODS, TAG, "onSendingFailed", Pair("reason", ignored))
        // error is ignored: device is going to be disconnected if an error occurred while sending a packet
    }

    fun onErrorResponse(command: UpgradeGaiaCommand?, status: GaiaErrorStatus) {
        Logger.log(LOG_METHODS, TAG, "onErrorResponse", Pair("status", status))
        if (isUpgrading) {
            when (command) {
                UpgradeGaiaCommand.CONNECT -> onUpgradeModeOnFailed(status)
                UpgradeGaiaCommand.CONTROL -> abort(UpgradeFail(status))
                UpgradeGaiaCommand.DISCONNECT ->                     // if message cannot reach the device it is assumed that the transport is disconnected
                    onUpgradeDisconnected()
            }
            abort(UpgradeFail(status))
        }
    }

    fun onUpgradeConnected() {
        Logger.log(LOG_METHODS, TAG, "onUpgradeConnected")
        if (isUpgrading) {
            mUpgradeManager.onUpgradeModeEnabled()
        }
    }

    fun onUpgradeDisconnected() {
        Logger.log(LOG_METHODS, TAG, "onUpgradeDisconnected")
        mUpgradeManager.onUpgradeModeDisabled()
    }

    fun onPlugged(listener: UpgradeHelperListener?) {
        Logger.log(LOG_METHODS, TAG, "start")
        mHelperListener = listener
        if (isUpgrading) {
            setState(UpgradeState.INITIALISATION)
        }
        mUpgradeManager.start()
    }

    fun onUnplugged() {
        mUpgradeManager.pause()
        resetUpload()
    }

    val isFlushed: Boolean
        get() = mParameters.isFlushed

    fun release() {
        mUpgradeManager.release()
        mHelperListener = null
        mUploadQueue.clear()
    }

    private fun resetUpload() {
        Logger.log(LOG_METHODS, TAG, "resetUpload")
        if (mHelperListener != null) {
            mHelperListener!!.cancelAll()
        }
        mUploadQueue.clear()
        mIsBlocked.set(false)
    }

    private fun abort(error: UpgradeFail?) {
        if (mState != UpgradeState.ABORTING) {
            resetUpload()
            setState(UpgradeState.ABORTING)
            if (error != null) {
                mUpgradePublisher.publishError(error)
            }
            mUpgradeManager.abort()

            // stop the logging of the bytes
            logBytes(DEBUG.Bluetooth.DATA)
        }
    }

    private fun onUpgradeModeOnFailed(status: GaiaErrorStatus) {
        resetUpload()
        mUpgradeManager.forceStop()
        setState(UpgradeState.END)
        mUpgradePublisher.publishError(UpgradeFail(status))
        mUpgradePublisher.publishProgress(UpgradeProgress.end(UpgradeState.END, null))
    }

    private fun sendConnectUpgrade() {
        Logger.log(LOG_METHODS, TAG, "sendConnectUpgrade")
        if (mHelperListener != null) {
            mHelperListener!!.setUpgradeModeOn(mParameters.useRwcp)
        }
    }

    private fun sendDisconnectUpgrade() {
        Logger.log(LOG_METHODS, TAG, "setUpgradeModeOff")
        if (mHelperListener != null) {
            mHelperListener!!.setUpgradeModeOff()
        }
    }

    private fun setState(state: UpgradeState) {
        Logger.log(
            LOG_METHODS, TAG, "setState",
            Pair("previous", mState), Pair("new", state)
        )
        if (state != mState) {
            mState = state
            mUpgradePublisher.publishProgress(UpgradeProgress.state(state))
        }
    }

    private val isUpgrading: Boolean
        get() = mUpgradeManager.isUpgrading
    private val isReconnecting: Boolean
        get() = mState == UpgradeState.REBOOT || mState == UpgradeState.RECONNECTING

    private fun getBytesFromUri(context: Context, uri: Uri): ByteArray {
        return BytesUtils.getBytesFromUri(context, uri)
    }

    private fun onMessageTransferred() {
        // inform the manager that the message was transferred
        mUpgradeManager.onMessageTransferred()
        // handle next upload request if required
        if (mParameters.isUploadAcknowledged) {
            mIsBlocked.set(false)
            processUploadRequest()
        }
    }

    private fun prepareUploadRequest(
        bytes: ByteArray,
        listener: UpgradeMessageListener
    ) {
        val isAcknowledged = mParameters.isUploadAcknowledged
        val request = UploadRequest(bytes, mParameters.isFlushed, isAcknowledged, mParameters.useRwcp, listener)
        if (isAcknowledged) {
            mUploadQueue.add(request)
            processUploadRequest()
        } else {
            sendUploadRequest(request)
        }
    }

    private fun processUploadRequest() {
        if (mUploadQueue.isEmpty() || !mIsBlocked.compareAndSet(false, true)) {
            return
        }
        val request = mUploadQueue.poll()
        request?.let { sendUploadRequest(it) }
            ?: Log.w(TAG, "[processUploadRequest] Unexpected null request.")
    }

    private fun sendUploadRequest(request: UploadRequest) {
        if (mHelperListener == null) {
            return
        }
        if (mState != UpgradeState.UPLOAD) {
            Log.w(TAG, "[sendUploadRequest] helper is not in UPLOAD state.")
            return
        }
        mHelperListener!!.sendUpgradeMessage(
            request.data, request.isAcknowledged,
            request.isFlushed, request.useRwcp,
        ) { onPacketSent(request) }
    }

    private fun onPacketSent(request: UploadRequest) {
        request.upgradeMessageListener.onSent()
        if (!request.isAcknowledged) {
            onMessageTransferred()
        }
    }

    private fun onAvailableSizeUpdate(payloadSize: Int) {
        Logger.log(
            LOG_METHODS, TAG, "onAvailableSizeUpdate", Pair(
                "payloadSize",
                payloadSize
            )
        )
        val newSize = getChunkSizeFromPayloadSize(payloadSize)

        // update the available size
        mChunkSize.available = newSize
        mUpgradePublisher.publishChunkSize(ChunkSizeType.AVAILABLE, mChunkSize.available)
        if (isUpgrading) {
            // must update the upgrade manager size
            setChunkSize()
        }
    }

    private fun onOptimumSizeUpdate(payloadSize: Int) {
        Logger.log(LOG_METHODS, TAG, "onOptimumSizeUpdate", Pair("payloadSize", payloadSize))
        val newSize = getChunkSizeFromPayloadSize(payloadSize)
        mChunkSize.default = newSize
        mUpgradePublisher.publishChunkSize(ChunkSizeType.DEFAULT, newSize)
    }

    /**
     * To set the chunk size, this method checks if the expected size can be used and then set the closest depending on
     * the available size.
     */
    private fun setChunkSize() {
        val expected = mParameters.expectedChunkSize
        val size = mChunkSize.getChunkSize(expected)
        val result = mUpgradeManager.setChunkSize(size)
        Logger.log(
            LOG_METHODS, TAG, "setChunkSize", Pair("expected", expected),
            Pair("available", mChunkSize.available), Pair("size", size),
            Pair("set", result)
        )
        mUpgradePublisher.publishChunkSize(ChunkSizeType.SET, result)
    }

    /**
     *
     * To get the chunk size when having the size of a GAIA payload.
     *
     * @return the payload minus the upgrade message header and the header of UPGRADE_DATA packets that contains the
     * data chunks during the upload. If RWCP is used, the header of the RWCP packet (1 b) is also subtracted.
     */
    private fun getChunkSizeFromPayloadSize(gaiaPayloadSize: Int): Int {
        return gaiaPayloadSize - UpgradeMessage.HEADER_LENGTH - OpCodes.UpgradeData.DATA_HEADER_LENGTH - if (mParameters.useRwcp) 1 else 0
    }

    private fun logBytes(log: Boolean) {
        val manager = GaiaClientService.getTransportManager()
        manager.logBytes(log)
    }

    private fun matchResumePointToState(point: ResumePoint): UpgradeState {
        return when (point) {
            ResumePoint.START -> UpgradeState.UPLOAD
            ResumePoint.PRE_VALIDATE, ResumePoint.PRE_REBOOT -> UpgradeState.VALIDATION
            ResumePoint.POST_REBOOT, ResumePoint.COMMIT, ResumePoint.POST_COMMIT -> UpgradeState.VERIFICATION
            else -> UpgradeState.INITIALISATION
        }
    }

    private fun matchTypeToConfirmation(type: ConfirmationType): UpgradeConfirmation? {
        return when (type) {
            ConfirmationType.BATTERY_LOW_ON_DEVICE -> UpgradeConfirmation.BATTERY_LOW_ON_DEVICE
            ConfirmationType.COMMIT -> UpgradeConfirmation.COMMIT
            ConfirmationType.IN_PROGRESS -> UpgradeConfirmation.IN_PROGRESS
            ConfirmationType.TRANSFER_COMPLETE -> UpgradeConfirmation.TRANSFER_COMPLETE
            ConfirmationType.WARNING_FILE_IS_DIFFERENT -> UpgradeConfirmation.WARNING_FILE_IS_DIFFERENT
            else -> null
        }
    }

    private fun matchConfirmationToType(confirmation: UpgradeConfirmation): ConfirmationType? {
        return when (confirmation) {
            UpgradeConfirmation.BATTERY_LOW_ON_DEVICE -> ConfirmationType.BATTERY_LOW_ON_DEVICE
            UpgradeConfirmation.COMMIT -> ConfirmationType.COMMIT
            UpgradeConfirmation.IN_PROGRESS -> ConfirmationType.IN_PROGRESS
            UpgradeConfirmation.TRANSFER_COMPLETE -> ConfirmationType.TRANSFER_COMPLETE
            UpgradeConfirmation.WARNING_FILE_IS_DIFFERENT -> ConfirmationType.WARNING_FILE_IS_DIFFERENT
            else -> null
        }
    }

    companion object {
        private const val TAG = "UpgradeHelper"
        private const val LOG_METHODS = DEBUG.Upgrade.PLUGIN_HELPER
        private const val LOG_BYTES = DEBUG.Upgrade.PLUGIN_HELPER_DETAILS
        private fun getState(type: EndType?): UpgradeState {
            return if (type == null) {
                UpgradeState.END
            } else when (type) {
                EndType.COMPLETE -> UpgradeState.COMPLETE
                EndType.ABORTED -> UpgradeState.ABORTED
                EndType.SILENT_COMMIT, EndType.UPGRADE_IN_PROGRESS_WITH_DIFFERENT_ID -> UpgradeState.END
                else -> UpgradeState.END
            }
        }
    }
}
