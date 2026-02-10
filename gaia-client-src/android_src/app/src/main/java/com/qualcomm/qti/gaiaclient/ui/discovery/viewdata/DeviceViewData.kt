/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.discovery.viewdata

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType
import com.qualcomm.qti.gaiaclient.ui.common.ImageViewData
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData

class DeviceViewData(val device: DiscoveredDevice, context: Context) : ItemViewData(ItemType.DEVICE) {
    val name: String = device.name
    val bluetoothAddress: String = device.bluetoothAddress
    val bluetoothType: String = getType(context, device.bluetoothType)
    val image: ImageViewData

    init {
        val isPaired = device.discoveryType === DiscoveryType.PAIRED
        val drawableId = getImage(device.discoveryType)
        val descriptionId = if (isPaired) R.string.cont_desc_paired else R.string.cont_desc_not_paired
        image = ImageViewData(drawableId, context.getString(descriptionId))
    }

    // ListAdapterItemData
    override fun isSameContent(item: ListAdapterItemData?): Boolean {
        return equals(item)
    }

    // ListAdapterItemData
    override fun isSameItem(item: ListAdapterItemData?): Boolean {
        return item is DeviceViewData
                && device.bluetoothAddress == item.device.bluetoothAddress
                && device.bluetoothType == item.device.bluetoothType
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is DeviceViewData
                && name == other.name
                && bluetoothAddress == other.bluetoothAddress
                && bluetoothType == other.bluetoothType
                && image == other.image
                && device == other.device
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + bluetoothAddress.hashCode()
        result = 31 * result + bluetoothType.hashCode()
        result = 31 * result + image.hashCode()
        result = 31 * result + (device.hashCode())
        return result
    }

    private fun getImage(type: DiscoveryType): Int {
        return when (type) {
            DiscoveryType.CONNECTED -> R.drawable.ic_list_device_connected
            DiscoveryType.PAIRED -> R.drawable.ic_list_device_paired
            DiscoveryType.DISCOVERED -> R.drawable.ic_list_device_other
            else -> R.drawable.ic_list_device_other
        }
    }

    private fun getType(context: Context, type: BluetoothType?): String {
        return context.getString(
            when (type) {
                BluetoothType.LOW_ENERGY -> R.string.discovery_list_device_type_low_energy
                BluetoothType.CLASSIC -> R.string.discovery_list_device_type_classic
                else -> R.string.discovery_list_device_type_classic
            }
        )
    }
}
