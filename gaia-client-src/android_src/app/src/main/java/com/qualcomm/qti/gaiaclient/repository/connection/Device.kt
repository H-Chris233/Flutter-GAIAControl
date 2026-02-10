/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.connection

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice

/**
 * This represents the data model of a Bluetooth device.
 */
class Device(val name: String, val bluetoothAddress: String, val bluetoothType: BluetoothType) : Parcelable {
    var state = ConnectionState.DISCONNECTED

    constructor(device: DiscoveredDevice) : this(device.name, device.bluetoothAddress, device.bluetoothType)
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        BluetoothType.valueOf(parcel.readString() ?: "")
    )

    // Parcelable
    override fun describeContents(): Int {
        return 0
    }

    // Parcelable
    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(name)
        parcel.writeString(bluetoothAddress)
        parcel.writeString(bluetoothType.name)
    }

    override fun toString(): String {
        return "Device(name='$name', address='$bluetoothAddress', type=$bluetoothType, state=$state)"
    }


    companion object CREATOR : Creator<Device> {
        override fun createFromParcel(parcel: Parcel): Device {
            return Device(parcel)
        }

        override fun newArray(size: Int): Array<Device?> {
            return arrayOfNulls(size)
        }
    }
}
