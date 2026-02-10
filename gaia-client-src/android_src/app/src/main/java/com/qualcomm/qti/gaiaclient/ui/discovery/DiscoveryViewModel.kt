/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.discovery

import android.app.Application
import android.content.Context
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType
import com.qualcomm.qti.gaiaclient.repository.Result
import com.qualcomm.qti.gaiaclient.repository.ResultStatus
import com.qualcomm.qti.gaiaclient.repository.connection.BluetoothReason
import com.qualcomm.qti.gaiaclient.repository.discovery.DiscoveryRepository
import com.qualcomm.qti.gaiaclient.ui.common.MessageData
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.DeviceViewData
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.ItemViewData
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.TitleViewData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    application: Application,
    private val discoveryRepository: DiscoveryRepository
) : AndroidViewModel(application) {
    private val devices = ArrayMap<DiscoveryType, MutableList<DeviceViewData>>()

    private val _itemsList = StickyLiveData<MutableList<ItemViewData>>()
    val itemsList: LiveData<MutableList<ItemViewData>> = _itemsList

    private val _message = StickyLiveData<MessageData>()
    val message: LiveData<MessageData> = _message

    private val _isRefreshing = StickyLiveData(true)
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private val observers: DataObservers = DataObservers()

    init {
        observers.start()
    }

    override fun onCleared() {
        super.onCleared()
        observers.stop()
    }

    fun refreshDevices(context: Context, type: DiscoveryType) {
        if (type == DiscoveryType.DISCOVERED) {
            // other types are simple fetch
            _isRefreshing.postValue(true)
        }
        discoveryRepository.refreshDevices(context, type)
    }

    private fun updateData(type: DiscoveryType, result: Result<List<DiscoveredDevice>, BluetoothReason>?) {
        // get the data from the result
        val status = result?.status
        val found = result?.data

        // get view data
        val items = getDeviceItems(type, found)
        val isListEmpty = items.isEmpty()
        val message = getEmptyListMessage(isListEmpty, status)

        // notify data observers
        _itemsList.postValue(items)
        _message.postValue(message)

        // update the visual helper on the refreshing state
        if (type == DiscoveryType.DISCOVERED) {
            // other types are simple fetch
            _isRefreshing.postValue(status == ResultStatus.IN_PROGRESS)
        }
    }

    private fun getDeviceItems(type: DiscoveryType, found: List<DiscoveredDevice>?): MutableList<ItemViewData> {
        // get the list of origin
        val origin = getDeviceDataList(type)

        // update the devices data
        devices[type] = updateDevices(origin, found)

        // generate flat list
        return generateList(devices)
    }

    private fun updateDevices(
        source: MutableList<DeviceViewData>,
        found: List<DiscoveredDevice>?
    ): MutableList<DeviceViewData> {
        if (found == null || found.isEmpty()) {
            // no devices
            return ArrayList()
        }
        for (device in found) {
            val data = DeviceViewData(device, getApplication())
            if (!source.contains(data)) {
                source.add(data)
            }
        }
        return source
    }

    private fun generateList(devices: ArrayMap<DiscoveryType, MutableList<DeviceViewData>>?): MutableList<ItemViewData> {
        if (devices == null || devices.isEmpty) {
            return ArrayList()
        }
        val result: MutableList<ItemViewData> = ArrayList()
        for (type in ORDERED_DEVICE_TYPES) {
            val list = devices[type]
            if (list != null && list.isNotEmpty()) {
                val count = result.size

                list.forEach {
                    if (result.none { item ->
                            item is DeviceViewData
                                    && item.bluetoothAddress == it.bluetoothAddress
                                    && item.bluetoothType == it.bluetoothType
                        }) {
                        result.add(it)
                    }
                }

                val added = result.size - count
                if (added > 0) result.add(count, TitleViewData(type, getListTitle(type, added)))
            }
        }
        return result
    }

    private fun getDeviceDataList(type: DiscoveryType): MutableList<DeviceViewData> {
        return devices[type] ?: mutableListOf()
    }

    private fun getListTitle(type: DiscoveryType, count: Int): String {
        return when (type) {
            DiscoveryType.CONNECTED -> getQuantityString(R.plurals.discovery_connected_devices, count)
            DiscoveryType.PAIRED -> getQuantityString(R.plurals.discovery_paired_devices, count)
            DiscoveryType.DISCOVERED -> getQuantityString(R.plurals.discovery_other_devices, count)
            else -> getQuantityString(R.plurals.discovery_other_devices, count)
        }
    }

    private fun getEmptyListMessage(isListEmpty: Boolean, status: ResultStatus?): MessageData {
        if (status == null) {
            return MessageData(isListEmpty, "", "")
        }
        val success = status == ResultStatus.SUCCESS
        val title =
            if (success) R.string.discovery_list_empty_title
            else R.string.discovery_list_empty_in_progress_title
        val message =
            if (success) R.string.discovery_list_empty_message
            else R.string.discovery_list_empty_in_progress_message
        return MessageData(isListEmpty, getString(title), getString(message))
    }

    private fun getQuantityString(resourceId: Int, count: Int): String {
        return getApplication<Application>().resources.getQuantityString(resourceId, count)
    }

    private fun getString(resourceId: Int): String {
        return getApplication<Application>().getString(resourceId)
    }

    private inner class DataObservers : Observers() {
        private val devicesObservers: MutableMap<DiscoveryType, Observer<Result<List<DiscoveredDevice>, BluetoothReason>>> =
            ArrayMap()

        override fun registerObservers() {
            for (type in DiscoveryType.values()) {
                val observer = Observer<Result<List<DiscoveredDevice>, BluetoothReason>> { updateData(type, it) }
                devicesObservers[type] = observer
                discoveryRepository.getDevices(type).observeForever(observer)
            }
        }

        override fun unregisterObservers() {
            devicesObservers.forEach { (type, observer) ->
                discoveryRepository.getDevices(type).removeObserver(observer)
            }
        }
    }

    companion object {
        private val ORDERED_DEVICE_TYPES =
            arrayOf(DiscoveryType.CONNECTED, DiscoveryType.PAIRED, DiscoveryType.DISCOVERED)
    }
}
