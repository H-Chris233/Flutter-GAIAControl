/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations

import android.annotation.SuppressLint
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import androidx.annotation.WorkerThread
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.AttStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.Priority
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.UuidDescriptor
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.WriteType
import com.qualcomm.qti.gaiaclient.core.utils.Logger
import java.util.UUID

@SuppressLint("MissingPermission")
sealed class GattOperation(val hasCallback: Boolean = true) {
    open val timeout: Long = 35000
    lateinit var timeOutRunnable: Runnable
    var attempts = 0
        private set
    open val maxAttempts = 2

    open fun isExpected(result: GattResult): Boolean = false
    open fun isExpected(operation: GattOperation): Boolean = false

    @WorkerThread
    fun execute(gatt: GattHandler): Boolean {
        attempts++
        return execution(gatt)
    }

    fun timeout() = callback(GattResult.Error.Timeout)

    abstract fun execution(gatt: GattHandler): Boolean
    open val callback: (result: GattResult) -> Unit = {}

    override fun toString(): String {
        return this.javaClass.simpleName
    }


    class DiscoverServices(override val callback: (GattResult) -> Unit) : GattOperation() {
        override fun isExpected(result: GattResult): Boolean = result is GattResult.Services
        override fun isExpected(operation: GattOperation): Boolean = operation is DiscoverServices
        override fun execution(gatt: GattHandler): Boolean = gatt.discoverServices()
    }

    class Read(
        private val characteristic: BluetoothGattCharacteristic,
        override val maxAttempts: Int,
        override val callback: (GattResult) -> Unit
    ) : GattOperation() {
        constructor(characteristic: BluetoothGattCharacteristic, callback: (GattResult) -> Unit) :
                this(characteristic, 2, callback)

        override fun isExpected(result: GattResult): Boolean = result is GattResult.Read
        override fun isExpected(operation: GattOperation): Boolean = operation is Read
        override fun execution(gatt: GattHandler): Boolean = gatt.readCharacteristic(characteristic)
    }

    class Write(
        private val characteristic: BluetoothGattCharacteristic,
        private val value: ByteArray,
        private val writeType: WriteType = WriteType.DEFAULT,
        private val sendListener: SendListener? = null,
        private val logBytes: Boolean = false,
    ) : GattOperation() {
        override fun isExpected(result: GattResult): Boolean = result is GattResult.Write
        override fun isExpected(operation: GattOperation): Boolean = operation is Write
        override fun execution(gatt: GattHandler): Boolean {
            sendListener?.onSending()
            Logger.log(logBytes, "GATT", "write", Pair("data", value))
            val result = gatt.writeCharacteristic(characteristic, value, writeType)
            if (writeType != WriteType.NO_RESPONSE) {
                if (result) sendListener?.onSent() else sendListener?.onFailed()
            }
            return result
        }

        override val callback: (GattResult) -> Unit = {
            if (it is GattResult.Write && it.status == AttStatus.ANDROID_SUCCESS) sendListener?.onSent() else sendListener?.onFailed()
        }
    }

    class Mtu(private val mtu: Int, override val callback: (GattResult) -> Unit) : GattOperation() {
        override fun isExpected(result: GattResult): Boolean = result is GattResult.Mtu
        override fun isExpected(operation: GattOperation): Boolean = operation is Mtu
        override fun execution(gatt: GattHandler): Boolean = gatt.requestMtu(mtu)
    }

    class EnableNotifications(
        private val characteristic: BluetoothGattCharacteristic,
        private val enable: Boolean = true,
        callback: (GattResult) -> Unit
    ) : GattOperation() {
        private val _callback: (GattResult) -> Unit = callback
        override val callback: (GattResult) -> Unit = {
            _callback(if (it is GattResult.WriteDescriptor) GattResult.GetNotifications(it) else it)
        }

        override fun isExpected(result: GattResult): Boolean = result is GattResult.WriteDescriptor
        override fun isExpected(operation: GattOperation): Boolean = operation is EnableNotifications
        override fun execution(gatt: GattHandler): Boolean {
            val value =
                if (enable) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
            val descriptor = characteristic.getDescriptor(UuidDescriptor.ClientCharacteristicConfiguration.uuid)
            return descriptor != null
                    && gatt.registerForNotifications(characteristic, enable)
                    && gatt.writeDescriptor(descriptor, value)
        }

    }

    class ConnectionPriority(private val priority: Priority) : GattOperation(false) {
        override fun execution(gatt: GattHandler): Boolean = gatt.setConnectionPriority(priority)
    }
}

sealed class GattResult {
    class Services(val status: AttStatus, val services: Map<UUID, List<BluetoothGattService>>) : GattResult()
    class Read(val status: AttStatus, val characteristic: BluetoothGattCharacteristic, val value: ByteArray) :
        GattResult()

    class Write(val status: AttStatus, val characteristic: BluetoothGattCharacteristic) : GattResult()
    data class Mtu(val status: AttStatus, val mtu: Int) : GattResult()
    class GetNotifications(val status: AttStatus) : GattResult() {
        constructor(result: WriteDescriptor) : this(result.status)
    }

    class WriteDescriptor(val status: AttStatus) : GattResult()

    sealed class Error : GattResult() {
        object Timeout : Error()
        object NotExecuted : Error()
    }

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}
