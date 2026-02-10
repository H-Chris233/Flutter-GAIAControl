/*
 * ************************************************************************************************
 * * © 2021-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.upgrade

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.annotation.VisibleForTesting
import androidx.core.util.Pair
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.BuildConfig
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.GaiaVersion
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpdateOptions
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeInfoType
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeState
import com.qualcomm.qti.gaiaclient.repository.Result
import com.qualcomm.qti.gaiaclient.repository.ResultStatus
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert
import java.util.concurrent.ConcurrentHashMap

abstract class UpgradeRepositoryData protected constructor(private val deviceInformationRepository: DeviceInformationRepository) :
    UpgradeRepository {
    private val upgradeLiveData = StickyLiveData<Result<UpgradeProgress, UpgradeFail>?>()
    private val alertLiveData = StickyLiveData<Pair<UpgradeAlert, Boolean>>()
    private val sizesLiveData = ConcurrentHashMap<SizeType, StickyLiveData<Int>>()
    private val handler = Handler(Looper.getMainLooper())

    init {
        for (type in SizeType.values()) {
            sizesLiveData[type] = StickyLiveData(type.default)
        }
    }

    override fun getSize(type: SizeType): Int {
        val data = getSizeData(type)
        val value = data.value
        return value ?: SizeType.NULL_VALUE
    }

    override fun getUpgradeLiveData(): LiveData<Result<UpgradeProgress, UpgradeFail>?> {
        return upgradeLiveData
    }

    override fun getSizeLiveData(type: SizeType): LiveData<Int> {
        return getSizeData(type)
    }

    override fun getAlertsLiveData(): LiveData<Pair<UpgradeAlert, Boolean>> {
        return alertLiveData
    }

    override fun startUpgrade(context: Context, fileLocation: Uri, userSelection: UpgradeOptions) {
        resetUpgradeProgress()

        // delays execution to make sure the caller process ends first
        handler.post {

            //  Preparing the building options from user's selection
            val versions =
                deviceInformationRepository.versions.value
            val gaiaVersionValue = versions?.gaia
            val gaiaVersion = gaiaVersionValue ?: GaiaVersion.V1
            val options = buildUpdateOptions(
                fileLocation, userSelection, gaiaVersion,
                BuildConfig.UPGRADE_DEVELOPER_OPTIONS
            )

            // keep the size information
            val expectedSize = options.expectedChunkSize
            setSize(SizeType.EXPECTED, if (expectedSize <= 0) SizeType.NULL_VALUE else expectedSize)
            executeUpgrade(context, options)
        }
    }

    override fun abortUpgrade(context: Context) {
        // delays execution to make sure the caller process ends first
        handler.post { executeAbort(context) }
    }

    override fun confirmUpgrade(
        context: Context, confirmation: UpgradeConfirmation,
        option: ConfirmationOptions
    ) {
        // delays execution to make sure the caller process ends first
        handler.post { executeConfirmation(context, confirmation, option) }
    }

    override fun reset() {
        val result = upgradeLiveData.value
        if (result != null && result.status !== ResultStatus.IN_PROGRESS) {
            upgradeLiveData.value = null // must be consumed
        }
    }

    protected abstract fun executeUpgrade(context: Context?, options: UpdateOptions?)
    protected abstract fun executeAbort(context: Context?)
    protected abstract fun executeConfirmation(
        context: Context?, confirmation: UpgradeConfirmation?,
        option: ConfirmationOptions
    )

    protected fun onUpgradeProgress(upgradeProgress: UpgradeProgress?) {
        if (upgradeProgress == null) {
            // no progress to display
            return
        }
        if (upgradeProgress.type == UpgradeInfoType.UPLOAD_PROGRESS) {
            // soft data update
            upgradeLiveData.setValue(Result.inProgress(upgradeProgress))
        } else {
            // hard data update
            upgradeLiveData.setValue(Result.inProgress(upgradeProgress))
        }
        val state = upgradeProgress.state
        if (state == UpgradeState.RECONNECTING || state == UpgradeState.REBOOT) {
            clearAlert() // alert is not running anymore
        }
    }

    protected fun onUpgradeEnd(info: UpgradeProgress) {
        clearAlert()
        upgradeLiveData.value = Result.success(info) // must be consumed
    }

    protected fun onUpgradeError(upgradeFail: UpgradeFail) {
        clearAlert()
        val progress = upgradeProgress
        val result = if (progress == null) Result.error(upgradeFail) else Result.error(progress, upgradeFail)
        upgradeLiveData.value = result // must be consumed
    }

    private val upgradeProgress: UpgradeProgress?
        private get() {
            val previous = upgradeLiveData.value
            return previous?.data
        }

    protected fun setSize(type: SizeType, value: Int) {
        val data = getSizeData(type)
        data.value = value
    }

    protected fun updateAlert(alert: UpgradeAlert, raised: Boolean) {
        alertLiveData.value = Pair(alert, raised)
    }

    protected fun clearAlert() {
        alertLiveData.value = Pair(null, null) // must be consumed
    }

    @VisibleForTesting
    protected fun buildUpdateOptions(
        fileLocation: Uri, userSelection: UpgradeOptions, gaiaVersion: Int,
        upgradeOptionsAvailable: Boolean
    ): UpdateOptions {
        return if (!upgradeOptionsAvailable) {
            // options cannot be used -> default options
            UpdateOptions(fileLocation = fileLocation, gaiaVersion = gaiaVersion)
        } else if (userSelection.useDefault) {
            // options can be used and default ones must be used -> default + logging
            val useRwcp = userSelection.useRwcp && GaiaClientService.getTransportManager().gaiaTransport
                ?.bluetoothType == BluetoothType.LOW_ENERGY
            UpdateOptions(
                fileLocation = fileLocation, gaiaVersion = gaiaVersion, isLogged = userSelection.isLogged,
                useRwcp = useRwcp)
        } else {
            // using all the options as provided
            UpdateOptions(
                fileLocation = fileLocation,
                isLogged = userSelection.isLogged,
                isUploadFlushed = userSelection.isUploadFlushed,
                isUploadAcknowledged = userSelection.isUploadAcknowledged,
                expectedChunkSize = userSelection.expectedChunkSize,
                useRwcp = userSelection.useRwcp && GaiaClientService.getTransportManager().gaiaTransport
                    ?.bluetoothType == BluetoothType.LOW_ENERGY,
            )
        }
    }

    private fun resetUpgradeProgress() {
        upgradeLiveData.value =
            Result.inProgress(UpgradeProgress.state(UpgradeState.INITIALISATION)) // must be consumed
    }

    private fun getSizeData(type: SizeType): StickyLiveData<Int> {
        val data = sizesLiveData[type]
        return data ?: /* unexpected */StickyLiveData(type.default)
    }
}
