/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothStatusCodes
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.Priority
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.WriteType
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger

private const val TAG = "GattHandler"
private const val LOG_METHODS = DEBUG.Bluetooth.Gatt.HANDLER

@Suppress("OVERRIDE_DEPRECATION")
@SuppressLint("MissingPermission")
class GattHandler(val device: BluetoothDevice, private var listener: Listener?) {

    private var gatt: BluetoothGatt? = null
    private val handler: Handler
    private val bleHandler: Handler

    init {
        Logger.log(LOG_METHODS, TAG, "init", Pair("address", device.address))
        val bleHandlerThread = HandlerThread("${javaClass.simpleName}-ble")
        // TODO: Possibly not needed.
        val handlerThread = HandlerThread(javaClass.simpleName)
        handlerThread.start()
        bleHandlerThread.start()
        handler = Handler(handlerThread.looper)
        bleHandler = Handler(bleHandlerThread.looper)
    }

    fun connect(context: Context?, autoConnect: Boolean) {
        Logger.log(LOG_METHODS, TAG, "connect", Pair("autoConnect", autoConnect))
        if (gatt != null) return
        gatt = device.connectGatt(context, autoConnect, callback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice.PHY_OPTION_NO_PREFERRED, bleHandler)
    }

    fun disconnect() {
        Logger.log(LOG_METHODS, TAG, "disconnect")
        gatt?.disconnect()
    }

    fun discoverServices(): Boolean {
        Logger.log(LOG_METHODS, TAG, "discoverServices")
        return gatt?.discoverServices() ?: false
    }

    fun readCharacteristic(characteristic: BluetoothGattCharacteristic): Boolean {
        Logger.log(LOG_METHODS, TAG, "readCharacteristic", Pair("uuid", characteristic.uuid))
        return gatt?.readCharacteristic(characteristic) ?: false
    }

    fun writeCharacteristic(
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        writeType: WriteType = WriteType.DEFAULT
    ): Boolean {
        Logger.log(LOG_METHODS, TAG, "readCharacteristic", Pair("uuid", characteristic.uuid), Pair("type", writeType))
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return BluetoothStatusCodes.SUCCESS == gatt?.writeCharacteristic(characteristic, value, writeType.value)
        } else {
            // Unfortunately cannot know if writeCharacteristic was called successfully.
            // If it's not placed inside a Handler Thread it's not thread safe.
            bleHandler.post {
                characteristic.value = value
                characteristic.writeType = writeType.value
                gatt?.writeCharacteristic(characteristic)
            }
        }
    }

    fun requestMtu(mtu: Int): Boolean {
        Logger.log(LOG_METHODS, TAG, "requestMtu", Pair("mtu", mtu))
        return gatt?.requestMtu(mtu) ?: false
    }

    fun writeDescriptor(descriptor: BluetoothGattDescriptor, value: ByteArray): Boolean {
        Logger.log(
            LOG_METHODS, TAG, "writeDescriptor",
            Pair("uuid", descriptor.uuid), Pair("characteristic", descriptor.characteristic.uuid)
        )

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            gatt?.writeDescriptor(descriptor, value) == BluetoothStatusCodes.SUCCESS
        } else {
            descriptor.value = value
            gatt?.writeDescriptor(descriptor) == true
        }
    }

    fun registerForNotifications(characteristic: BluetoothGattCharacteristic, enable: Boolean): Boolean {
        Logger.log(
            LOG_METHODS, TAG, "registerForNotifications",
            Pair("uuid", characteristic.uuid), Pair("enable", enable)
        )
        return gatt?.setCharacteristicNotification(characteristic, enable) == true
    }

    fun setConnectionPriority(priority: Priority): Boolean {
        Logger.log(LOG_METHODS, TAG, "setConnectionPriority", Pair("priority", priority))
        return gatt?.requestConnectionPriority(priority.value) ?: false
    }

    private fun dispose() {
        gatt?.close()
        gatt = null
        listener = null
        handler.removeCallbacksAndMessages(null)
        handler.looper.quit()
        bleHandler.removeCallbacksAndMessages(null)
        bleHandler.looper.quit()
    }

    @Suppress("NAME_SHADOWING")
    val callback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            Logger.log(LOG_METHODS, TAG, "onPhyUpdate")
            handler.post {
                listener?.onPhyUpdate(txPhy, rxPhy, status)
            }
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            Logger.log(LOG_METHODS, TAG, "onPhyRead")
            handler.post {
                listener?.onPhyRead(txPhy, rxPhy, status)
            }
        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            Logger.log(
                LOG_METHODS, TAG, "onConnectionStateChange",
                Pair("newState", newState), Pair("status", status)
            )
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                this@GattHandler.gatt = gatt
            }
            handler.post {
                val listener = listener
                if (newState == BluetoothGatt.STATE_DISCONNECTED) dispose()
                listener?.onConnectionStateChange(status, newState)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            Logger.log(LOG_METHODS, TAG, "onServicesDiscovered")
            val services = gatt.services
            handler.post {
                listener?.onServicesDiscovered(services, status)
            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Logger.log(
                LOG_METHODS, TAG, "onCharacteristicRead",
                Pair("uuid", characteristic.uuid), Pair("status", status)
            )
            val value = characteristic.value
            handler.post { listener?.onCharacteristicRead(characteristic, value, status) }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            Logger.log(
                LOG_METHODS, TAG, "onCharacteristicRead",
                Pair("uuid", characteristic.uuid), Pair("status", status)
            )
            handler.post {
                listener?.onCharacteristicRead(characteristic, value, status)
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            Logger.log(
                LOG_METHODS, TAG, "onCharacteristicWrite",
                Pair("uuid", characteristic.uuid), Pair("status", status)
            )
            handler.post {
                listener?.onCharacteristicWrite(characteristic, status)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            Logger.log(LOG_METHODS, TAG, "onCharacteristicChanged", Pair("uuid", characteristic.uuid))
            val value = characteristic.value
            handler.post {
                listener?.onCharacteristicChanged(characteristic, value)
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray
        ) {
            Logger.log(LOG_METHODS, TAG, "onCharacteristicChanged", Pair("uuid", characteristic.uuid))
            handler.post {
                listener?.onCharacteristicChanged(characteristic, value)
            }
        }

        override fun onDescriptorRead(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Logger.log(
                LOG_METHODS, TAG, "onDescriptorRead",
                Pair("uuid", descriptor.uuid), Pair("characteristic", descriptor.characteristic.uuid),
                Pair("status", status)
            )
            val value = descriptor.value
            handler.post {
                listener?.onDescriptorRead(descriptor, value, status)
            }
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt,
            descriptor: BluetoothGattDescriptor,
            status: Int,
            value: ByteArray
        ) {
            Logger.log(
                LOG_METHODS, TAG, "onDescriptorRead",
                Pair("uuid", descriptor.uuid), Pair("characteristic", descriptor.characteristic.uuid),
                Pair("status", status)
            )
            handler.post {
                listener?.onDescriptorRead(descriptor, value, status)
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            Logger.log(
                LOG_METHODS, TAG, "onDescriptorWrite",
                Pair("uuid", descriptor.uuid), Pair("characteristic", descriptor.characteristic.uuid),
                Pair("status", status)
            )
            handler.post {
                listener?.onDescriptorWrite(descriptor, status)
            }
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            Logger.log(LOG_METHODS, TAG, "onReadRemoteRssi", Pair("rssi", rssi), Pair("status", status))
            handler.post {
                listener?.onReadRemoteRssi(rssi, status)
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            Logger.log(LOG_METHODS, TAG, "onMtuChanged", Pair("mtu", mtu), Pair("status", status))
            handler.post {
                listener?.onMtuChanged(mtu, status)
            }
        }

        override fun onServiceChanged(gatt: BluetoothGatt) {
            Logger.log(LOG_METHODS, TAG, "onServiceChanged")
            handler.post {
                listener?.onServiceChanged()
            }
        }
    }

    interface Listener {

        fun onPhyUpdate(txPhy: Int, rxPhy: Int, status: Int) {}

        fun onPhyRead(txPhy: Int, rxPhy: Int, status: Int) {}

        fun onConnectionStateChange(status: Int, newState: Int) {}

        fun onServicesDiscovered(services: MutableList<BluetoothGattService>, status: Int) {}

        fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {}

        fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic, status: Int) {}

        fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray) {}

        fun onDescriptorRead(descriptor: BluetoothGattDescriptor, value: ByteArray, status: Int) {}

        fun onDescriptorWrite(descriptor: BluetoothGattDescriptor, status: Int) {}

        fun onReadRemoteRssi(rssi: Int, status: Int) {}

        fun onMtuChanged(mtu: Int, status: Int) {}

        fun onServiceChanged() {}
    }

}
