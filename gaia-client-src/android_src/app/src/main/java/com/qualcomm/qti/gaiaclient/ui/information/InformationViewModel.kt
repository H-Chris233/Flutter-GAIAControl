/*
 * ************************************************************************************************
 * * © 2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.information

import android.app.Application
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo
import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.CrossUpdateVersion
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformation
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformation.ApplicationBuildId
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.BatteryLevel
import com.qualcomm.qti.gaiaclient.repository.Resource
import com.qualcomm.qti.gaiaclient.repository.battery.BatteryRepository
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository
import com.qualcomm.qti.gaiaclient.repository.connection.Device
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformation
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.Versions
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository
import com.qualcomm.qti.gaiaclient.ui.common.ImageViewData
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.gaiaclient.utils.ConnectionUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class InformationViewModel @Inject constructor(
    application: Application,
    private val connectionRepository: ConnectionRepository,
    private val featuresRepository: FeaturesRepository,
    private val deviceInfoRepository: DeviceInformationRepository,
    private val batteryRepository: BatteryRepository
) : AndroidViewModel(application) {

    val informationViewData = StickyLiveData<InformationViewData>()
    val crossUpdateVersion = StickyLiveData<CrossUpdateVersion?>()

    private val _viewData = InformationViewData()

    private val observers: DataObservers = DataObservers()

    init {
        observers.start()
        informationViewData.postValue(InformationViewData())
    }

    override fun onCleared() {
        super.onCleared()
        observers.stop()
    }

    private fun fetchData() {
        val context = getApplication<Application>().applicationContext
        deviceInfoRepository.fetchDeviceInfo(context, DeviceInfo.GAIA_VERSION)
        deviceInfoRepository.fetchDeviceInfo(context, DeviceInfo.APPLICATION_VERSION)
        deviceInfoRepository.fetchDeviceInfo(context, DeviceInfo.SERIAL_NUMBER)
        deviceInfoRepository.fetchDeviceInfo(context, DeviceInfo.VARIANT_NAME)
        deviceInfoRepository.fetchDeviceInfo(context, DeviceInfo.SYSTEM_INFORMATION)
        deviceInfoRepository.fetchEarbudInfo(context, EarbudInfo.EARBUD_POSITION)
        deviceInfoRepository.fetchEarbudInfo(context, EarbudInfo.SECONDARY_SERIAL_NUMBER)
        batteryRepository.fetchSupportedBatteries(context)
    }

    private fun updateFeaturesInformation(supportedFeatures: ArrayMap<QTILFeature, Int>?) {
        if (supportedFeatures == null) {
            return
        }

        val features: Set<QTILFeature> = supportedFeatures.keys
        val isEarBud = features.contains(QTILFeature.EARBUD)

        val drawableId = if (isEarBud) R.drawable.ic_device_earbuds else R.drawable.ic_device_headphones
        val descriptionId =
            if (isEarBud) R.string.cont_desc_information_image_earbuds else R.string.cont_desc_information_image_headphones
        val description = getApplication<Application>().getString(descriptionId)
        _viewData.deviceImage = ImageViewData(drawableId, description)

        // notify data observer
        notifyDataObserver()
        fetchData()
    }

    private fun updateConnectedDevice(resource: Resource<Device, BluetoothStatus>?) {
        // get the data from the result
        val device = resource?.data
        val state = device?.state

        // build data object
        val name = device?.name ?: getApplication<Application>().getString(R.string.unknown_device)
        val address =
            device?.bluetoothAddress ?: getApplication<Application>().getString(R.string.unknown_bluetooth_address)
        val stateLabel = ConnectionUtils.getLabel(getApplication(), state)
        _viewData.name = name
        _viewData.bluetoothAddress = address
        _viewData.state = stateLabel

        // notify data observer
        notifyDataObserver()

        if (state == ConnectionState.CONNECTED) {
            fetchData()
        }
    }

    private fun updateDeviceInformation(information: DeviceInformation?) {
        if (information == null) {
            return
        }

        // update the view data
        _viewData.variantName = information.variantName
        _viewData.serialNumber = information.serialNumber
        _viewData.serialNumberLeft = information.serialNumberLeft
        _viewData.serialNumberRight = information.serialNumberRight
        _viewData.isCharging = information.chargerStatus?.isCharging ?: false

        // notify data observer
        notifyDataObserver()
    }

    private fun updateVersions(versions: Versions?) {
        if (versions == null) {
            return
        }

        // update the view data
        _viewData.applicationVersion = versions.application
        _viewData.gaiaVersion = versions.gaia

        // notify data observer
        notifyDataObserver()
    }

    private fun fetchBatteryLevels(supported: Set<Battery>?) {
        if (supported == null || supported.isEmpty()) {
            return
        }
        val context = getApplication<Application>().applicationContext
        batteryRepository.fetchBatteryLevels(context, supported)
    }

    private fun updateBatteryLevels(levels: Map<Battery, Int>?) {
        if (levels == null || levels.isEmpty()) {
            return
        }

        levels.forEach { _viewData.batteryLevels[it.key] = formatBatteryLevel(it.key, it.value) }

        // notify data observer
        notifyDataObserver()
    }

    private fun updateSystemInformation(infos: List<SystemInformation>?) {
        if (infos == null || infos.isEmpty()) {
            return
        }

        infos.forEach {
            when (it) {
                is ApplicationBuildId -> _viewData.applicationBuildId = it.value.text
                else -> {}
            }
        }

        // notify data observer
        notifyDataObserver()
    }

    private fun updateCrossUpdateVersion(version: CrossUpdateVersion?) {
        version?.let {
            if(it.required) {
                crossUpdateVersion.value = version
            }
        }

    }

    private fun formatBatteryLevel(battery: Battery, level: Int): String {
        val batteryLabel = getBatteryLabel(battery)
        val levelLabel =
            if (level == BatteryLevel.LEVEL_UNKNOWN) getString(R.string.battery_level_unknown) else level.toString()
        return getApplication<Application>().applicationContext
            .getString(R.string.battery_level_label, batteryLabel, levelLabel)
    }

    private fun getBatteryLabel(battery: Battery): String {
        return when (battery) {
            Battery.LEFT_DEVICE -> getString(R.string.battery_level_percent_left)
            Battery.RIGHT_DEVICE -> getString(R.string.battery_level_percent_right)
            Battery.CHARGER_CASE -> getString(R.string.battery_level_percent_case)
            Battery.SINGLE_DEVICE -> getString(R.string.battery_level_percent_device)
            else -> getString(R.string.battery_level_percent_device)
        }
    }

    private fun getString(resourceId: Int): String {
        return getApplication<Application>().applicationContext.getString(resourceId)
    }

    private fun notifyDataObserver() {
        informationViewData.postValue(_viewData)
    }

    private inner class DataObservers : Observers() {
        private val featuresObserver =
            Observer { supportedFeatures: ArrayMap<QTILFeature, Int>? -> updateFeaturesInformation(supportedFeatures) }
        private val connectedDeviceObserver =
            Observer { resource: Resource<Device, BluetoothStatus>? -> updateConnectedDevice(resource) }
        private val supportedBatteriesObserver = Observer { supported: Set<Battery>? -> fetchBatteryLevels(supported) }
        private val batteryLevelsObserver = Observer { levels: Map<Battery, Int>? -> updateBatteryLevels(levels) }
        private val versionsObserver = Observer { versions: Versions? -> updateVersions(versions) }
        private val deviceInformationObserver =
            Observer { information: DeviceInformation? -> updateDeviceInformation(information) }
        private val systemInformationObserver =
            Observer { infos: List<SystemInformation>? -> updateSystemInformation(infos) }
        private val crossUpdateVersionObserver =
            Observer { version: CrossUpdateVersion? -> updateCrossUpdateVersion(version) }

        override fun registerObservers() {
            featuresRepository.featuresLiveData.observeForever(featuresObserver)
            connectionRepository.connectedDeviceLiveData.observeForever(connectedDeviceObserver)
            batteryRepository.supportedBatteriesLiveData.observeForever(supportedBatteriesObserver)
            batteryRepository.batteryLevelsLiveData.observeForever(batteryLevelsObserver)
            deviceInfoRepository.versions.observeForever(versionsObserver)
            deviceInfoRepository.deviceInformation.observeForever(deviceInformationObserver)
            deviceInfoRepository.systemInformation.observeForever(systemInformationObserver)
            deviceInfoRepository.crossUpdateVersion.observeForever(crossUpdateVersionObserver)
        }

        override fun unregisterObservers() {
            featuresRepository.featuresLiveData.removeObserver(featuresObserver)
            connectionRepository.connectedDeviceLiveData.removeObserver(connectedDeviceObserver)
            batteryRepository.supportedBatteriesLiveData.removeObserver(supportedBatteriesObserver)
            batteryRepository.batteryLevelsLiveData.removeObserver(batteryLevelsObserver)
            deviceInfoRepository.versions.removeObserver(versionsObserver)
            deviceInfoRepository.deviceInformation.removeObserver(deviceInformationObserver)
            deviceInfoRepository.systemInformation.removeObserver(systemInformationObserver)
            deviceInfoRepository.crossUpdateVersion.removeObserver(crossUpdateVersionObserver)
        }
    }
}
