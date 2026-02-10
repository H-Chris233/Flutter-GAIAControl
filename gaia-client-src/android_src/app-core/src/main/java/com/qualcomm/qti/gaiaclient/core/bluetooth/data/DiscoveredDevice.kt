/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.data

import android.bluetooth.BluetoothDevice
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils

/**
 * This represents the data model of a Bluetooth device that could be connectable.
 */
class DiscoveredDevice(
    val name: String,
    val bluetoothAddress: String,
    val discoveryType: DiscoveryType,
    val bluetoothType: BluetoothType
) {
    constructor(device: BluetoothDevice, discoveryType: DiscoveryType, bluetoothType: BluetoothType) : this(
        BluetoothUtils.getDeviceName(device),
        device.address ?: "",
        discoveryType,
        bluetoothType
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DiscoveredDevice
        return discoveryType == other.discoveryType
                && bluetoothType == other.bluetoothType
                && name == other.name
                && bluetoothAddress == other.bluetoothAddress
    }

    override fun hashCode(): Int {
        var result = discoveryType.hashCode()
        result = 31 * result + bluetoothType.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + bluetoothAddress.hashCode()
        return result
    }
}
