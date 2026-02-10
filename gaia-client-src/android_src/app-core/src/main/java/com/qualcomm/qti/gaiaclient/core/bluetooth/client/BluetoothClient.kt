/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client

import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.bluetooth.GaiaTransport
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device
import com.qualcomm.qti.gaiaclient.core.tasks.TaskManager
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils.findBluetoothDevice
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils.getBluetoothAdapter
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger

private const val TAG = "BluetoothClient"
private const val LOG_METHODS = DEBUG.Bluetooth.CLIENT

interface StreamReader {
    fun reset()
    fun read(taskManager: TaskManager, stream: ByteArray)
}

abstract class BluetoothClient(
    val bluetoothType: BluetoothType,
    val device: Device,
    val listener: Listener,
    val streamReader: StreamReader
) {
    abstract val gaiaTransport: GaiaTransport

    @set:Synchronized
    open var connectionState: ConnectionState = ConnectionState.DISCONNECTED
        protected set(state) {
            Logger.log(LOG_METHODS, TAG, "ConnectionState", Pair("previous", field), Pair("new", state))
            field = state
            listener.onConnectionState(state)
        }

    val isConnected: Boolean
        get() = connectionState == ConnectionState.CONNECTED

    fun connect(context: Context): BluetoothStatus {
        Logger.log(LOG_METHODS, TAG, "connect", Pair("address", device.bluetoothAddress))

        when (connectionState) {
            ConnectionState.CONNECTED -> return BluetoothStatus.ALREADY_CONNECTED
            ConnectionState.CONNECTING -> return BluetoothStatus.IN_PROGRESS
            else -> {}
        }

        val adapter = getBluetoothAdapter(context) ?: return BluetoothStatus.NO_BLUETOOTH
        val device = findBluetoothDevice(adapter, device.bluetoothAddress) ?: return BluetoothStatus.DEVICE_NOT_FOUND

        return connect(context, device)
    }

    abstract fun connect(context: Context, device: BluetoothDevice) : BluetoothStatus

    abstract fun reconnect(): BluetoothStatus
    abstract fun disconnect()
    abstract fun logBytes(log: Boolean)

    override fun toString(): String {
        return "${javaClass.simpleName}(device='$device', type='$bluetoothType', state=$connectionState)"
    }

    interface Listener {
        fun onConnectionState(state: ConnectionState)
        fun onConnectionError(status: BluetoothStatus)
    }
}
