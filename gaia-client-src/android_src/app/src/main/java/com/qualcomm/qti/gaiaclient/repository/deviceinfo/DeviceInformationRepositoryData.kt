/*
 * ************************************************************************************************
 * * © 2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.deviceinfo

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ChargerStatus
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.CrossUpdateVersion
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformation
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures.UserFeatures
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData

abstract class DeviceInformationRepositoryData : DeviceInformationRepository {

    override val versions = StickyLiveData<Versions>()
    private val _versions: Versions
        get() {
            val versions = versions.value
            return versions ?: Versions()
        }

    override val deviceInformation = StickyLiveData<DeviceInformation>()
    private val _deviceInformation: DeviceInformation
        get() {
            val information = deviceInformation.value
            return information ?: DeviceInformation()
        }

    override val userFeatures = StickyLiveData<UserFeatures>()

    override val systemInformation = StickyLiveData<List<SystemInformation>>()

    override val crossUpdateVersion = StickyLiveData<CrossUpdateVersion>()

    override fun reset() {
        versions.value = Versions()
        deviceInformation.value = DeviceInformation()
        systemInformation.value = emptyList()
        crossUpdateVersion.value = CrossUpdateVersion(false, -1, -1, -1)
    }

    protected fun updateGaiaVersion(version: Int) {
        val versions = _versions
        versions.gaia = version
        this.versions.postValue(versions)
    }

    protected fun updateProtocolVersion(version: Long) {
        val versions = _versions
        versions.protocol = version
        this.versions.postValue(versions)
    }

    protected fun updateApplicationVersion(version: String?) {
        val versions = _versions
        versions.application = version
        this.versions.postValue(versions)
    }

    protected fun updateVariantName(variantName: String?) {
        val information = _deviceInformation
        information.variantName = variantName
        deviceInformation.postValue(information)
    }

    protected fun updateSerialNumber(serialNumber: String?) {
        val information = _deviceInformation
        information.serialNumber = serialNumber
        deviceInformation.postValue(information)
    }

    protected fun updateUserFeatures(features: UserFeatures) {
        userFeatures.postValue(features)
    }

    protected fun updateChargerStatus(status: ChargerStatus?) {
        val information = _deviceInformation
        information.chargerStatus = status
        deviceInformation.postValue(information)
    }

    protected fun updateEarbudPosition(position: EarbudPosition?) {
        val information = _deviceInformation
        information.position = position
        deviceInformation.postValue(information)
    }

    protected fun updateSecondarySerialNumber(serialNumber: String?) {
        val information = _deviceInformation
        information.secondarySerialNumber = serialNumber
        deviceInformation.postValue(information)
    }

    protected fun updateSystemInformation(list: List<SystemInformation>) {
        systemInformation.value = list
    }

    protected fun updateCrossUpdateRequired(updateVersion: CrossUpdateVersion) {
        crossUpdateVersion.value = updateVersion
    }
}