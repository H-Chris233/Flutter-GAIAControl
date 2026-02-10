/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.LeGattClient
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.RfcommClient
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device

object BluetoothClientFactory {
    fun createBluetoothClient(
        type: BluetoothType,
        device: Device,
        listener: BluetoothClient.Listener,
        streamReader: StreamReader,
    ): BluetoothClient {
        return when (type) {
            BluetoothType.CLASSIC -> RfcommClient(device, listener, streamReader)
            BluetoothType.LOW_ENERGY -> LeGattClient(device, listener, streamReader)
        }
    }
}
