/*
 * ************************************************************************************************
 *  Copyright 2016-2018 Qualcomm Technologies International, Ltd.                                 *
 *  © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.         *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade;

import android.annotation.SuppressLint;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationType;
import com.qualcomm.qti.libraries.upgrade.data.DataChunk;
import com.qualcomm.qti.libraries.upgrade.data.DataReader;
import com.qualcomm.qti.libraries.upgrade.data.DeviceState;
import com.qualcomm.qti.libraries.upgrade.data.EndType;
import com.qualcomm.qti.libraries.upgrade.data.ResumePoint;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeError;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeException;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeState;
import com.qualcomm.qti.libraries.upgrade.messages.CommitStatus;
import com.qualcomm.qti.libraries.upgrade.messages.ErrorCodes;
import com.qualcomm.qti.libraries.upgrade.messages.OpCodes;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessageListener;
import com.qualcomm.qti.libraries.upgrade.utils.Logger;
import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import java.util.Arrays;

/**
 * <p>This class manages the process of the Upgrade Protocol. It defines and builds
 * the upgrade messages and implements the protocol.</p>
 * <p>This class implements the {@link UpgradeManager} interface to provide an API for users of
 * this library.</p>
 */
final class UpgradeManagerImpl {

    // ====== CONSTANTS ============================================================================

    /**
     * <p>The tag to display for logs.</p>
     */
    private static final String TAG = "UpgradeManager";

    /**
     * The values that represents the upgrade protocol versions.
     */
    private static final class PROTOCOL_VERSIONS {

        private static final byte V4 = 0x04;
        private static final byte V5 = 0x05;
        private static final byte V6 = 0x06;
    }


    // ====== FIELDS ===============================================================================

    /**
     * The listener that receives events and requests from this manager.
     */
    private UpgradeListener mListener;
    /**
     * <p>An object that manages synchronously the states of the upgrade.</p>
     */
    protected final UpgradeState mState = new UpgradeState();
    /**
     * <p>An object that manages synchronously the states of the device to upgrade.</p>
     */
    @VisibleForTesting
    protected final DeviceState mDeviceState = new DeviceState();
    /**
     * An object to manage synchronously the upload offset.
     */
    private final DataReader mDataReader = new DataReader();
    /**
     * An executor to run scheduled tasks.
     */
    private UpgradeTaskScheduler mScheduler;


    // ====== CONSTRUCTORS =========================================================================

    /**
     * <p>Main constructor of this class that initialises a listener to send messages
     * to a device or dispatch any received Upgrade messages.</p>
     *
     * @param listener
     *         An object that implements the {@link UpgradeListener} interface.
     * @param scheduler
     *         An object that implements the {@link UpgradeTaskScheduler} interface for this
     *         manager to schedule upgrade tasks to ve run later on.
     */
    UpgradeManagerImpl(@NonNull UpgradeListener listener,
                       @NonNull UpgradeTaskScheduler scheduler) {
        this.mListener = listener;
        this.mScheduler = scheduler;
    }


    // ====== UPGRADE MANAGER METHODS ============================================================

    /**
     * See {@link UpgradeManager#showDebugLogs(boolean)}.
     */
    void showDebugLogs(boolean show) {
        mState.setShowLogs(show);
        Log.i(TAG, "Debug logs are now " + (show ? "activated" : "deactivated") + ".");
    }

    /**
     * See {@link UpgradeManager#setChunkSize(int)}.
     */
    int setChunkSize(int size) {
        // setting the maximum number of data bytes that can be sent at once
        return mDataReader.setMaxChunkSize(size);
    }

    /**
     * See {@link UpgradeManager#startUpgrade(byte[], byte[])}.
     */
    void startUpgrade(byte[] data, byte[] dataID) {
        if (data == null || data.length == 0) {
            // no data
            mListener.onUpgradeError(new UpgradeError(UpgradeError.ErrorTypes.NO_DATA));
            return;
        }

        // initialising the state
        if (!mState.setIsUpgrading(false, true)) {
            mListener.onUpgradeError(new UpgradeError(UpgradeError.ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING));
            return;
        }

        // setting the data information
        dataID = dataID == null ? new byte[0] : Arrays.copyOf(dataID, dataID.length);
        mDataReader.setData(Arrays.copyOf(data, data.length), dataID);

        // start the process
        mListener.enableUpgradeMode();
    }

    /**
     * See {@link UpgradeManager#start()}.
     */
    boolean start() {
        boolean isUpgrading = mState.isUpgrading();

        if (!mDeviceState.setIsTransportConnected(false, true)) {
            // already started (?)
            return false;
        }

        if (isUpgrading) {
            mListener.enableUpgradeMode();
        }

        return isUpgrading;
    }

    /**
     * See {@link UpgradeManager#pause()}.
     */
    void pause() {
        mDeviceState.setIsTransportConnected(true, false);
        if (mState.isAborting()) {
            // if pause while aborting the process is considered as aborted.
            mState.setIsAborting(false);
            onEnded(EndType.ABORTED);
        }
    }

    /**
     * See {@link UpgradeManager#isUpgrading()}.
     */
    boolean isUpgrading() {
        return mState.isUpgrading();
    }

    /**
     * See {@link UpgradeManager#onUpgradeMessage(byte[])}.
     */
    void onUpgradeMessage(byte[] bytes) {
        try {
            UpgradeMessage message = new UpgradeMessage(bytes);
            if (mState.isUpgrading() || message.getOpCode() == OpCodes.Enum.UPGRADE_ABORT_CFM) {
                Logger.log(mState.showLogs(), TAG, "received", new Pair<>("message", message));
                handleUpgradeMessage(message);
            }
            else {
                Log.w(TAG, "Received upgrade message while application is not upgrading anymore, " +
                        "opcode received: " +
                        OpCodes.getString(message.getOpCode()));
            }
        }
        catch (UpgradeException exception) {
            UpgradeError error = new UpgradeError(exception);
            startAbortion(error);
        }
    }

    /**
     * See {@link UpgradeManager#abort()}.
     */
    void abort() {
        if (mState.setIsUpgrading(true, false)) {
            mState.setIsAborting(true);
            mListener.isAborting();
            if (mDeviceState.isTransportConnected()) {
                sendAbortReq();
            }
            else {
                onEnded(EndType.ABORTED);
                mState.setIsAborting(false);
            }
        }
    }

    /**
     * See {@link UpgradeManager#forceStop()}.
     */
    void forceStop() {
        mState.reset();
    }

    /**
     * See {@link UpgradeManager#onConfirmation(ConfirmationType, ConfirmationOptions)}.
     */
    void onConfirmation(@NonNull ConfirmationType type, @NonNull ConfirmationOptions option) {
        if (!mDeviceState.isTransportConnected()) {
            // sync required when transport reconnected
            return;
        }

        if (!Arrays.asList(getConfirmationOptions(type, mDeviceState.isSilentCommitSupported())).contains(option)) {
            // unexpected option
            mListener.onUpgradeError(new UpgradeError(UpgradeError.ErrorTypes.UNSUPPORTED_CONFIRMATION_OPTION));
            abort();
            return;
        }

        switch (type) {
            case TRANSFER_COMPLETE:
                if (option == ConfirmationOptions.ABORT) {
                    mListener.isAborting();
                    mState.setMustAbort(true);
                }
                sendTransferCompleteRes(option);
                break;

            case COMMIT:
                if (option == ConfirmationOptions.ABORT) {
                    mListener.isAborting();
                    mState.setMustAbort(true);
                }
                sendCommitCfm(option);
                break;

            case IN_PROGRESS:
                if (option == ConfirmationOptions.ABORT) {
                    mListener.isAborting();
                    mState.setMustAbort(true);
                }
                sendProceedToCommit(option);
                break;

            case BATTERY_LOW_ON_DEVICE:
                if (option == ConfirmationOptions.CONFIRM) {
                    // soft syncing to count the number of attempts
                    sendSyncReq();
                }
                else {
                    abort();
                }
                break;

            case WARNING_FILE_IS_DIFFERENT:
                boolean upgradeConfirmed = option == ConfirmationOptions.CONFIRM;
                mState.setMustRestartUpgrade(upgradeConfirmed);
                if (upgradeConfirmed) {
                    // aborting the previous upgrade => soft abort
                    sendAbortReq();
                    // ONce the CFm is received this manager must sync again with the new file ID
                }
                else {
                    // soft ending: no abort for the device to keep its current process
                    onEnded(EndType.UPGRADE_IN_PROGRESS_WITH_DIFFERENT_ID);
                }
                break;
        }
    }

    /**
     * See {@link UpgradeManager#onUpgradeModeEnabled()}.
     */
    void onUpgradeModeEnabled() {
        mDeviceState.setIsTransportConnected(false, true);
        syncUpgrade();
    }

    /**
     * See {@link UpgradeManager#onUpgradeModeDisabled()}.
     */
    void onUpgradeModeDisabled() {
        mDeviceState.setIsTransportConnected(true, false);
    }

    /**
     * See {@link UpgradeManager#release()}.
     */
    void release() {
        mDeviceState.setIsTransportConnected(true, false);
        mListener = null;
        mScheduler = null;
    }

    /**
     * See {@link UpgradeManager#onMessageTransferred()}.
     */
    void onMessageTransferred() {
        if (mState.mustAbort()) {
            mState.setMustAbort(false);
            abort();
        }
    }


    // ====== PRIVATE METHODS ======================================================================

    /**
     * <p>To send an {@link UpgradeMessage UpgradeMessage} through the listener.</p>
     *
     * @param message
     *         The Upgrade Message to send.
     */
    private void sendUpgradeMessage(UpgradeMessage message) {
        sendUpgradeMessage(message, null);
    }

    /**
     * <p>To send an {@link UpgradeMessage UpgradeMessage} through the listener.</p>
     *
     * @param message
     *         The Upgrade Message to send.
     * @param listener
     *         The listener to notify when the data is sent.
     */
    private void sendUpgradeMessage(UpgradeMessage message,
                                    @Nullable UpgradeMessageListener listener) {
        if (!mDeviceState.isTransportConnected()) {
            return;
        }

        byte[] bytes = message.getBytes();
        if (mState.isUpgrading() || mState.isAborting()) {
            Logger.log(mState.showLogs(), TAG, "send", new Pair<>("message", message));
            mListener.sendUpgradeMessage(bytes, listener);
        }
        else {
            Log.w(TAG,
                  "Sending failed: not upgrading anymore, operation code="
                          + OpCodes.getString(message.getOpCode()));
        }
    }

    /**
     * <p>When an error occurs during the process on the mobile application side: this method is
     * called to initiate the abort.</p>
     *
     * @param error
     *         The error which occurs.
     */
    private void startAbortion(UpgradeError error) {
        String strBuilder = "Error occurs during upgrade process: " + error.getString() +
                "\nAborting...";
        Log.e(TAG, strBuilder);
        mListener.onUpgradeError(error);
        abort();
    }

    /**
     * To define the actual resume point and to display the dialog which provides the information to
     * the user.
     *
     * @param point
     *         The resume point ot define as the actual one.
     */
    private void setResumePoint(ResumePoint point) {
        mDeviceState.setResumePoint(point);
        mListener.onResumePointChanged(point);
    }

    /**
     * <p>To continue the process this manager needs the listener to confirm it.</p>
     *
     * @param type
     *         <p>The type of confirmation to request to the listener.</p>
     */
    private void askForConfirmation(ConfirmationType type) {
        boolean isSilentCommitSupported = mDeviceState.isSilentCommitSupported()
                && mDeviceState.getProtocolVersion() >= PROTOCOL_VERSIONS.V4;
        mListener.onConfirmationRequired(type, getConfirmationOptions(type, isSilentCommitSupported));
    }

    /**
     * This method allows to synchronise the upgrade process with the device when an interruption has occurred.
     */
    private void syncUpgrade() {
        if (isUpgrading()) {
            reset();
            sendSyncReq();
        }
    }

    /**
     * <p>To reset the data transfer.</p>
     */
    private void reset() {
        mState.resetStartAttempts();
        mDataReader.reset();
    }

    /**
     * To send the next data message depending on the number of bytes requested by the Device
     * through its last {@link OpCodes.Enum#UPGRADE_DATA_BYTES_REQ} request.
     */
    private void sendNextDataMessage() {
        DataChunk dataChunk = mDataReader.readNext();
        UpgradeMessageListener listener = () -> {
            // always send the current resume point: when called the device might have already moved to the next point
            mListener.onUploadProgress(mDeviceState.getResumePoint(), dataChunk.getProgress());

            if (dataChunk.isEndOfData()) {
                setResumePoint(ResumePoint.PRE_VALIDATE);
                sendIsValidationDoneReq();
            }
        };

        sendData(dataChunk, listener);
    }

    /**
     * To send a {@link OpCodes.Enum#UPGRADE_SYNC_REQ UPGRADE_SYNC_REQ} message.
     */
    private void sendSyncReq() {
        // send the MD5 information here
        int identifierLength = OpCodes.UpgradeSyncREQ.IDENTIFIER_LENGTH;
        byte[] data = new byte[OpCodes.UpgradeSyncREQ.CONTENT_LENGTH];

        @NonNull byte[] dataID = mDataReader.getDataID();

        if (dataID.length >= identifierLength) {
            // the id contains more bytes than needed: stripped to its last bytes
            System.arraycopy(dataID, dataID.length - identifierLength, data,
                             OpCodes.UpgradeSyncREQ
                                     .IDENTIFIER_OFFSET, identifierLength);
        }
        else if (dataID.length > 0) {
            // all bytes of the id should be sent
            System.arraycopy(dataID, 0, data, OpCodes.UpgradeSyncREQ.IDENTIFIER_OFFSET,
                             dataID.length);
        }
        // otherwise the id is empty, and 0 is sent as the id
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_SYNC_REQ, data);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_START_REQ message.
     */
    private void sendStartReq() {
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_START_REQ);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_START_DATA_REQ message.
     */
    private void sendStartDataReq() {
        setResumePoint(ResumePoint.START);
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_START_DATA_REQ);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_DATA message.
     *
     * @param dataChunk
     *         The object that contains the information to build the content of the DATA message.
     * @param listener
     *         The listener to notify when the data is sent.
     */
    private void sendData(DataChunk dataChunk, UpgradeMessageListener listener) {
        // build message content
        byte[] data = dataChunk.getData();
        byte[] dataToSend = new byte[data.length + 1];
        dataToSend[OpCodes.UpgradeData.IS_LAST_DATA_MESSAGE_OFFSET] = dataChunk.isEndOfData() ?
                                                                      OpCodes.UpgradeData.LastDataMessage.IS_LAST_DATA_MESSAGE
                                                                                              :
                                                                      OpCodes.UpgradeData.LastDataMessage.IS_NOT_LAST_DATA_MESSAGE;
        System.arraycopy(data, 0, dataToSend, OpCodes.UpgradeData.DATA_OFFSET, data.length);

        // send message
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_DATA, dataToSend);
        sendUpgradeMessage(message, listener);
    }

    /**
     * To send an UPGRADE_IS_VALIDATION_DONE_REQ message.
     */
    private void sendIsValidationDoneReq() {
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_IS_VALIDATION_DONE_REQ);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_TRANSFER_COMPLETE_RES message.
     * This method checks that the given option is compatible with the protocol supported by the device.
     *
     * @param option
     *         The option to send to the device.
     */
    private void sendTransferCompleteRes(ConfirmationOptions option) {
        byte[] data = new byte[OpCodes.UpgradeTransferCompleteRES.CONTENT_LENGTH];
        if (option == ConfirmationOptions.SILENT_COMMIT && mDeviceState.getProtocolVersion() < PROTOCOL_VERSIONS.V4) {
            Log.w(TAG, String.format(
                    "[sendTransferCompleteRes] Unsupported action(%1$s) for protocol v%2$d. Using interactive commit instead.",
                    option, mDeviceState.getProtocolVersion()));
            option = ConfirmationOptions.INTERACTIVE_COMMIT;
        }
        data[OpCodes.UpgradeTransferCompleteRES.ACTION_OFFSET] = (byte) option.getValue();
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_TRANSFER_COMPLETE_RES,
                                                    data);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_PROCEED_TO_COMMIT message.
     */
    private void sendProceedToCommit(@NonNull ConfirmationOptions option) {
        byte[] data = new byte[OpCodes.UpgradeProceedToCommitRES.CONTENT_LENGTH];
        data[OpCodes.UpgradeProceedToCommitRES.ACTION_OFFSET] = (byte) option.getValue();
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_PROCEED_TO_COMMIT, data);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_COMMIT_CFM message.
     *
     * @param option
     *         The option to send to the device, expected : ABORT, CONFIRM.
     */
    private void sendCommitCfm(@NonNull ConfirmationOptions option) {
        byte[] data = new byte[OpCodes.UpgradeCommitCFM.CONTENT_LENGTH];
        data[OpCodes.UpgradeCommitCFM.ACTION_OFFSET] = (byte) option.getValue();
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_COMMIT_CFM, data);
        sendUpgradeMessage(message);
        setResumePoint(ResumePoint.POST_COMMIT);
    }

    /**
     * To send a message to abort the upgrade.
     */
    private void sendAbortReq() {
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_ABORT_REQ);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_ERROR_RES message.
     *
     * @param data
     *         contains the received error code that is acknowledged through sending an
     *         UPGRADE_ERROR_RES message.
     */
    private void sendErrorRes(byte[] data) {
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_ERROR_RES, data);
        sendUpgradeMessage(message);
    }

    /**
     * To send an UPGRADE_SILENT_COMMIT_SUPPORTED_REQ message to know if the device supports silent commit.
     */
    private void sendSilentCommitSupportedReq() {
        UpgradeMessage message = new UpgradeMessage(OpCodes.Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_REQ);
        sendUpgradeMessage(message);
    }

    /**
     * To manage the reception of a message about the upgrade.
     *
     * @param message
     *         the received message.
     */
    @SuppressLint("SwitchIntDef") // not all operation codes are received from the Device
    private void handleUpgradeMessage(UpgradeMessage message) {
        switch (message.getOpCode()) {
            case OpCodes.Enum.UPGRADE_SYNC_CFM:
                receiveSyncCfm(message);
                break;

            case OpCodes.Enum.UPGRADE_START_CFM:
                receiveStartCfm(message);
                break;

            case OpCodes.Enum.UPGRADE_DATA_BYTES_REQ:
                receiveDataBytesReq(message);
                break;

            case OpCodes.Enum.UPGRADE_ABORT_CFM:
                receiveAbortCfm();
                break;

            case OpCodes.Enum.UPGRADE_ERROR_IND:
                receiveErrorInd(message);
                break;

            case OpCodes.Enum.UPGRADE_IS_VALIDATION_DONE_CFM:
                receiveIsValidationDoneCfm(message);
                break;

            case OpCodes.Enum.UPGRADE_TRANSFER_COMPLETE_IND:
                receiveTransferCompleteInd();
                break;

            case OpCodes.Enum.UPGRADE_COMMIT_REQ:
                receiveCommitRes();
                break;

            case OpCodes.Enum.UPGRADE_COMPLETE_IND:
                receiveCompleteInd();
                break;

            case OpCodes.Enum.UPGRADE_SILENT_COMMIT_SUPPORTED_CFM:
                receiveSilentCommitSupportedCfm(message);
                break;

            case OpCodes.Enum.UPGRADE_SILENT_COMMIT_CFM:
                receiveSilentCommitCfm();
                break;

            case OpCodes.Enum.UPGRADE_PUT_EARBUDS_IN_CASE_REQ:
                receivePutEarbudsInCaseReq();
                break;

            case OpCodes.Enum.UPGRADE_EARBUDS_IN_CASE_CFM:
                receiveEarbudsInCaseCfm();
                break;

            case OpCodes.Enum.UPGRADE_COMPLETE_IND_WITH_STATUS:
                receiveCompleteIndWithStatus(message);
                break;

        }
    }

    /**
     * To manage errors received during an upgrade.
     *
     * @param message
     *         The received message.
     */
    @SuppressLint("SwitchIntDef")
    private void receiveErrorInd(UpgradeMessage message) {
        byte[] content = message.getContent();
        sendErrorRes(content); // immediate answer, data is the same as the received one.

        short code = Utils.extractShortFromByteArray(content,
                                                     OpCodes.UpgradeErrorWarnIND.RETURN_CODE_OFFSET,
                                                     OpCodes.UpgradeErrorWarnIND.RETURN_CODE_LENGTH, false);
        @ErrorCodes.Enum int returnCode = ErrorCodes.getReturnCode(code);

        switch (returnCode) {
            case ErrorCodes.Enum.WARN_SYNC_ID_IS_DIFFERENT:
                askForConfirmation(ConfirmationType.WARNING_FILE_IS_DIFFERENT);
                break;
            case ErrorCodes.Enum.ERROR_BATTERY_LOW:
                askForConfirmation(ConfirmationType.BATTERY_LOW_ON_DEVICE);
                break;
            default:
                UpgradeError error =
                        new UpgradeError(UpgradeError.ErrorTypes.RECEIVED_ERROR_FROM_DEVICE,
                                         returnCode);
                startAbortion(error);
                break;
        }

    }

    /**
     * This method is called when an UPGRADE_SYNC_CFM message is received. This method starts the
     * next step which is sending an UPGRADE_START_REQ message.
     */
    private void receiveSyncCfm(UpgradeMessage message) {
        byte[] content = message.getContent();
        if (content.length >= OpCodes.UpgradeSyncCFM.CONTENT_LENGTH) {
            ResumePoint step =
                    ResumePoint.valueOf(content[OpCodes.UpgradeSyncCFM.RESUME_POINT_OFFSET]);
            int identifier = Utils.extractIntFromByteArray(content,
                                                           OpCodes.UpgradeSyncCFM.IDENTIFIER_OFFSET,
                                                           OpCodes.UpgradeSyncCFM.IDENTIFIER_LENGTH,
                                                           false);
            byte protocolVersion = content[OpCodes.UpgradeSyncCFM.PROTOCOL_VERSION_OFFSET];
            mDeviceState.setProtocolVersion(protocolVersion);
            if (protocolVersion > PROTOCOL_VERSIONS.V6) {
                Log.w(TAG, String.format(
                        "[receiveSyncCfm] Aborting the upgrade due to unsupported protocol version: implemented=%d$1, device=%d$2",
                        PROTOCOL_VERSIONS.V6, protocolVersion));
                startAbortion(new UpgradeError(UpgradeError.ErrorTypes.UNSUPPORTED_PROTOCOL_VERSION));
                return;
            }

            if (step == ResumePoint.POST_REBOOT) {
                setResumePoint(step);
            }
            else {
                mDeviceState.setResumePoint(step);
            }
        }
        else {
            mDeviceState.setResumePoint(ResumePoint.START);
        }
        sendStartReq();
    }

    /**
     * This method is called when an UPGRADE_START_CFM message is received. This method reads the
     * message and starts the next step which is sending an UPGRADE_START_DATA_REQ message, or
     * aborts the upgrade depending on the received message.
     *
     * @param message
     *         The received message.
     */
    private void receiveStartCfm(UpgradeMessage message) {
        byte[] content = message.getContent();

        // the message requires a content.
        if (content.length >= OpCodes.UpgradeStartCFM.CONTENT_LENGTH) {
            // to get the battery level
            short batteryLevel = Utils.extractShortFromByteArray(content, OpCodes.UpgradeStartCFM
                    .BATTERY_LEVEL_OFFSET, OpCodes.UpgradeStartCFM.BATTERY_LEVEL_LENGTH, false);

            if (content[OpCodes.UpgradeStartCFM.STATUS_OFFSET] == OpCodes.UpgradeStartCFM.Status.SUCCESS) {
                mState.resetStartAttempts();
                // the device is ready for the process, the manager moves to its resume point
                switch (mDeviceState.getResumePoint()) {
                    case COMMIT:
                        askForConfirmation(ConfirmationType.COMMIT);
                        break;
                    case PRE_REBOOT:
                        if (mDeviceState.getProtocolVersion() < PROTOCOL_VERSIONS.V4) {
                            askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
                        }
                        else {
                            sendSilentCommitSupportedReq();
                        }
                        break;
                    case POST_REBOOT:
                        askForConfirmation(ConfirmationType.IN_PROGRESS);
                        break;
                    case PRE_VALIDATE:
                        sendIsValidationDoneReq();
                        break;
                    case POST_COMMIT:
                        // nothing to do: the device has to send UPGRADE_COMPLETE_IND.
                        break;
                    case START:
                    default:
                        sendStartDataReq();
                        break;
                }
            }
            else if (content[OpCodes.UpgradeStartCFM.STATUS_OFFSET] ==
                    OpCodes.UpgradeStartCFM.Status.ERROR_APP_NOT_READY) {
                int START_ATTEMPTS_MAX = 5;
                int START_ATTEMPTS_TIME = 2000;

                if (mState.getStartAttempts() < START_ATTEMPTS_MAX) {
                    // the device is not ready: trying again to start the upgrade
                    mState.incrementStartAttempts();
                    mScheduler.scheduleTask(() -> {
                        if (mState.isUpgrading()) {
                            sendStartReq();
                        }
                    }, START_ATTEMPTS_TIME);
                }
                else {
                    mState.resetStartAttempts();
                    UpgradeError error =
                            new UpgradeError(UpgradeError.ErrorTypes.ERROR_BOARD_NOT_READY);
                    startAbortion(error);
                }
            }
            else {
                UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER);
                startAbortion(error);
            }
        }
        else {
            UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER);
            startAbortion(error);
        }
    }

    /**
     * This method is called when an UPGRADE_DATA_BYTES_REQ message is received. This method
     * manages the message and use it for the next step which is to upload the data on the device
     * using UPGRADE_DATA messages.
     *
     * @param message
     *         The received message.
     */
    private void receiveDataBytesReq(UpgradeMessage message) {
        byte[] content = message.getContent();

        // Checking the data has the good length
        if (content.length == OpCodes.UpgradeDataBytesREQ.CONTENT_LENGTH) {

            // get information from the message content
            int requestedBytes = Utils.extractIntFromByteArray(content,
                                                               OpCodes.UpgradeDataBytesREQ.NB_BYTES_OFFSET,
                                                               OpCodes.UpgradeDataBytesREQ.NB_BYTES_LENGTH, false);
            int offsetMove = Utils.extractIntFromByteArray(content,
                                                           OpCodes.UpgradeDataBytesREQ.OFFSET_MOVE_OFFSET,
                                                           OpCodes.UpgradeDataBytesREQ.OFFSET_MOVE_LENGTH, false);

            // updating the upload information
            mDataReader.set(offsetMove, requestedBytes);

            if (mState.isUpgrading() && mDeviceState.isTransportConnected()) {
                // ensure a response even if the data reader does not have a next data
                sendNextDataMessage();
            }

            // fill up the size request by preparing all other upload packets
            while (mState.isUpgrading() && mDeviceState.isTransportConnected() && mDataReader.hasNext()) {
                sendNextDataMessage();
            }
        }
        else {
            UpgradeError error = new UpgradeError(UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER);
            startAbortion(error);
        }
    }

    /**
     * This method is called when an UPGRADE_IS_VALIDATION_DONE_CFM message is received. This
     * manager uses the content of that message it to trigger UPGRADE_IS_VALIDATION_DONE_REQ.
     */
    private void receiveIsValidationDoneCfm(UpgradeMessage message) {
        byte[] data = message.getContent();
        if (data.length >= OpCodes.UpgradeIsValidationDoneCFM.CONTENT_LENGTH) {
            long time = Utils.extractLongFromByteArray(data, OpCodes.UpgradeIsValidationDoneCFM
                                                               .WAITING_TIME_OFFSET,
                                                       OpCodes.UpgradeIsValidationDoneCFM.WAITING_TIME_LENGTH,
                                                       false);
            mScheduler.scheduleTask(() -> {
                if (mState.isUpgrading()) {
                    sendIsValidationDoneReq();
                }
            }, time);
        }
        else {
            sendIsValidationDoneReq();
        }
    }

    /**
     * This method is called when an UPGRADE_TRANSFER_COMPLETE_IND message is received. This manager
     * uses this message for the next step which is to send a validation to continue the
     * process or to abort it temporally - it will be done later.
     */
    private void receiveTransferCompleteInd() {
        setResumePoint(ResumePoint.PRE_REBOOT);
        if (mDeviceState.getProtocolVersion() < PROTOCOL_VERSIONS.V4) {
            askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
        }
        else {
            sendSilentCommitSupportedReq();
        }
    }

    /**
     * This method is called when an UPGRADE_COMMIT_RES message is received. The manager
     * processes the message and uses it for the next step which is to send a validation to
     * continue the process or to abort it temporally - it will be done later.
     */
    private void receiveCommitRes() {
        setResumePoint(ResumePoint.COMMIT);
        askForConfirmation(ConfirmationType.COMMIT);
    }

    /**
     * This method is called when an UPGRADE_COMPLETE_IND message is received.
     */
    private void receiveCompleteInd() {
        onEnded(EndType.COMPLETE);
    }

    /**
     * This method is called when an UPGRADE_ABORT_CFM message is received after the device was
     * asked to abort its process to the upgrade process.
     */
    private void receiveAbortCfm() {
        if (mState.mustRestartUpgrade()) {
            // soft abort for re-syncing of the upgrade with the device
            mState.setMustRestartUpgrade(false);
            syncUpgrade();
        }
        else {
            mState.setIsAborting(false);
            onEnded(EndType.ABORTED);
        }
    }

    /**
     * This method is called when an UPGRADE_SILENT_COMMIT_SUPPORTED_CFM message is received after this host has sent
     * UPGRADE_SILENT_COMMIT_SUPPORTED_REQ.
     * This message is expected to be received prior to ask confirmation to the listener if the device should move to
     * the next step: commit of the upgrade.
     */
    @VisibleForTesting
    void receiveSilentCommitSupportedCfm(UpgradeMessage message) {
        byte[] data = message.getContent();

        short support = data.length >= OpCodes.UpgradeSilentCommitSupportedCFM.CONTENT_LENGTH ?
                        Utils.extractShortFromByteArray(data, OpCodes.UpgradeSilentCommitSupportedCFM.SUPPORT_OFFSET,
                                                        OpCodes.UpgradeSilentCommitSupportedCFM.SUPPORT_LENGTH, false)
                                                                                              : OpCodes.UpgradeSilentCommitSupportedCFM.Support.NOT_SUPPORTED;

        mDeviceState.setSilentCommitSupport(support == OpCodes.UpgradeSilentCommitSupportedCFM.Support.SUPPORTED);

        askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
    }

    /**
     * This method is called when an UPGRADE_COMPLETE_IND message is received.
     */
    private void receiveSilentCommitCfm() {
        onEnded(EndType.SILENT_COMMIT);
    }

    /**
     * This method is called when an UPGRADE_PUT_EARBUDS_IN_CASE_REQ message is received.
     */
    private void receivePutEarbudsInCaseReq() {
        mListener.onAlert(UpgradeAlert.PUT_EARBUD_IN_CASE, true);
    }

    /**
     * This method is called when an UPGRADE_EARBUDS_IN_CASE_CFM message is received.
     */
    private void receiveEarbudsInCaseCfm() {
        mListener.onAlert(UpgradeAlert.PUT_EARBUD_IN_CASE, false);
    }

    /**
     * This method is called when an UPGRADE_COMPLETE_IND_WITH_STATUS message is received at the ned of the upgrade.
     */
    void receiveCompleteIndWithStatus(UpgradeMessage message) {
        byte[] data = message.getContent();

        CommitStatus status = null;
        if (data.length >= OpCodes.UpgradeCompleteINDWithStatus.CONTENT_LENGTH) {
            short value = Utils.extractShortFromByteArray(data, OpCodes.UpgradeCompleteINDWithStatus.STATUS_OFFSET,
                                                          OpCodes.UpgradeCompleteINDWithStatus.STATUS_LENGTH, false);
            status = CommitStatus.valueOf(value);
        }

        if (status == CommitStatus.UPGRADE_COMMIT_SUCCESS_SECURITY_UPDATE_FAILED) {
            onEnded(EndType.COMPLETE_WITH_SECURITY_UPDATE_FAILED);
        }
        else {
            onEnded(EndType.COMPLETE);
        }
    }

    /**
     * When an operation result in this manager having completed its upgrade.
     */
    private void onEnded(EndType type) {
        mState.setIsUpgrading(true, false);
        mListener.onUpgradeEnd(type);
        mListener.disableUpgradeMode();
    }

    /**
     * Builds the list of {@link ConfirmationOptions ConfirmationOptions} that corresponds to the given confirmation
     * type.
     */
    private ConfirmationOptions[] getConfirmationOptions(ConfirmationType type, boolean isSilentCommitSupported) {
        switch (type) {
            case TRANSFER_COMPLETE:
                if (isSilentCommitSupported) {
                    return new ConfirmationOptions[]{ConfirmationOptions.ABORT, ConfirmationOptions.INTERACTIVE_COMMIT,
                                                     ConfirmationOptions.SILENT_COMMIT};
                }
                else {
                    return new ConfirmationOptions[]{ConfirmationOptions.ABORT, ConfirmationOptions.CONFIRM};
                }
            case COMMIT:
            case IN_PROGRESS:
            case BATTERY_LOW_ON_DEVICE:
                return new ConfirmationOptions[]{ConfirmationOptions.ABORT, ConfirmationOptions.CONFIRM};
            case WARNING_FILE_IS_DIFFERENT:
                return new ConfirmationOptions[]{ConfirmationOptions.CANCEL, ConfirmationOptions.CONFIRM};
            default:
                return new ConfirmationOptions[0];
        }
    }

}

