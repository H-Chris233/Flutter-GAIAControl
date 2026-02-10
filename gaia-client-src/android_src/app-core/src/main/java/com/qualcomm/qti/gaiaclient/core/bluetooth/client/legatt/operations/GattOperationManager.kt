/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.AttStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.ConnectionStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.GattStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger
import java.util.LinkedList
import java.util.Queue
import java.util.UUID

private const val TAG = "GattOperationManager"
private const val LOG_METHODS = DEBUG.Bluetooth.Gatt.OPERATIONS_MANAGER

class GattOperationManager(val device: BluetoothDevice, listener: Listener) : GattHandler.Listener {
    private val gatt: GattHandler = GattHandler(device, this)
    private val operationHandler: Handler
    private val operations: Queue<GattOperation> = LinkedList()
    private var current: GattOperation? = null

    private var connected: Boolean = false
    private var running: Boolean = true
    private var processing: Boolean = false
    private val canProcess: Boolean
        get() = connected && running && !processing

    private val idCreator = IdCreator()

    var logBytes = DEBUG.Bluetooth.DATA

    var listener: Listener? = listener
        private set

    init {
        logState("init")
        val handlerThread = HandlerThread(javaClass.simpleName)
        handlerThread.start()
        operationHandler = Handler(handlerThread.looper)
    }

    fun connect(context: Context, autoConnect: Boolean) {
        logState("connect")
        operationHandler.post { if (running && !connected) gatt.connect(context, autoConnect) }
    }

    fun disconnect() {
        logState("disconnect")
        operationHandler.post {
            if (!running) return@post
            running = false
            if (connected) {
                gatt.disconnect()
            } else {
                clean()
            }
        }
    }

    fun operate(operation: GattOperation): Long {
        logState("operate", Pair("operation", operation))
        if (!running) return IdCreator.NULL_ID
        operationHandler.post {
            logState("offer", Pair("operation", operation))
            if (!running) return@post
            operations.offer(operation)
            if (!processing) processNextOperation()
        }
        return idCreator.nextId()
    }

    override fun onConnectionStateChange(status: Int, newState: Int) {
        logState("onConnectionStateChange")
        val listener = listener
        when (newState) {
            BluetoothGatt.STATE_CONNECTED -> connected = true
            BluetoothGatt.STATE_DISCONNECTED -> clean()
        }
        listener?.onConnectionState(connectionState(newState), ConnectionStatus.valueOf(status))
    }

    override fun onServicesDiscovered(services: MutableList<BluetoothGattService>, status: Int) {
        onCallback(GattResult.Services(AttStatus.valueOf(status), services(services)))
    }

    override fun onCharacteristicRead(characteristic: BluetoothGattCharacteristic, value: ByteArray, status: Int) {
        onCallback(GattResult.Read(AttStatus.valueOf(status), characteristic, value))
    }

    override fun onCharacteristicWrite(characteristic: BluetoothGattCharacteristic, status: Int) {
        onCallback(GattResult.Write(AttStatus.valueOf(status), characteristic))
    }

    override fun onCharacteristicChanged(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        Logger.log(logBytes, "GATT", "read", Pair("data", value))
        listener?.onNotification(characteristic, value)
    }

    override fun onDescriptorWrite(descriptor: BluetoothGattDescriptor, status: Int) {
        onCallback(GattResult.WriteDescriptor(AttStatus.valueOf(status)))
    }

    override fun onMtuChanged(mtu: Int, status: Int) {
        logState("onMtuChanged")
        if (!onCallback(GattResult.Mtu(AttStatus.valueOf(status), mtu))) {
            // can be triggered in response to a connection event
            listener?.onMtu(mtu)
        }
    }

    override fun onServiceChanged() {
        logState("onServiceChanged")
        listener?.onServiceChanged()
    }

    @Synchronized
    private fun processNextOperation() {
        logState("processNextOperation")
        if (!canProcess) return
        processing = true

        val operation = operations.poll()
        if (operation == null) {
            processing = false
            return
        }

        processOperation(operation)
    }

    private fun processOperation(operation: GattOperation) {
        logState("processOperation", Pair("operation", operation))
        if (operation.hasCallback) {
            current = operation
            operation.timeOutRunnable = TimeOutRunnable(operation)
            operationHandler.postDelayed(operation.timeOutRunnable, operation.timeout)
        }
        operationHandler.post {
            logState("execute", Pair("operation", operation))
            val result = operation.execute(gatt)
            logState("execute", Pair("result", result))

            if (!result) current = null
            processing = result && operation.hasCallback

            if (!result && operation.hasCallback) {
                operationHandler.removeCallbacks(operation.timeOutRunnable)
                operation.callback(GattResult.Error.NotExecuted)
            }

            processNextOperation()
        }
    }

    private fun onCallback(result: GattResult): Boolean {
        logState("onCallback", Pair("result", result))
        if (!running) return false
        return operationHandler.post {
            current?.let {
                if (it.hasCallback && it.isExpected(result)) {
                    current = null
                    processing = false
                    operationHandler.removeCallbacks(it.timeOutRunnable)
                    it.callback(result)
                    processNextOperation()
                }
            }
        }
    }

    @Synchronized
    private fun clean() {
        logState("clean")
        running = false
        operations.clear()
        operationHandler.removeCallbacksAndMessages(null)
        operationHandler.looper.quit()
        current = null
        listener = null
    }

    private inner class TimeOutRunnable(val operation: GattOperation) : Runnable {
        override fun run() {
            logState("timeout->run")

            if (current?.isExpected(operation) == false) return

            if (operation.attempts < operation.maxAttempts) {
                processOperation(operation)
            } else {
                current = null
                processing = false
                operation.timeout()
                processNextOperation()
            }
        }
    }

    private fun logState(method: String) {
        Logger.log(
            LOG_METHODS, TAG, method,
            Pair("thread", Thread.currentThread().name),
            Pair("count", operations.size),
            Pair("connected", connected), Pair("running", running), Pair("processing", processing),
            Pair("canProcess", canProcess), Pair("current", current),
        )
    }

    private fun logState(method: String, data: Pair<String, Any>) {
        Logger.log(
            LOG_METHODS, TAG, method,
            Pair("thread", Thread.currentThread().name),
            Pair("count", operations.size),
            Pair("connected", connected), Pair("running", running), Pair("processing", processing),
            Pair("canProcess", canProcess), Pair("current", current),
            data
        )
    }

    interface Listener {
        fun onConnectionState(state: ConnectionState, status: GattStatus) {}
        fun onNotification(characteristic: BluetoothGattCharacteristic, value: ByteArray) {}
        fun onServiceChanged() {}
        fun onMtu(mtu: Int) {}
    }
}

private fun connectionState(value: Int): ConnectionState {
    return when (value) {
        BluetoothGatt.STATE_CONNECTED -> ConnectionState.CONNECTED
        BluetoothGatt.STATE_CONNECTING -> ConnectionState.CONNECTING
        BluetoothGatt.STATE_DISCONNECTED -> ConnectionState.DISCONNECTED
        BluetoothGatt.STATE_DISCONNECTING -> ConnectionState.DISCONNECTING
        else -> ConnectionState.DISCONNECTED
    }
}

private fun services(services: MutableList<BluetoothGattService>): Map<UUID, List<BluetoothGattService>> {
    val result: MutableMap<UUID, MutableList<BluetoothGattService>> = mutableMapOf()

    services.forEach {
        var list = result[it.uuid]
        if (list == null) {
            list = mutableListOf()
            result[it.uuid] = list
        }
        list.add(it)
    }

    return result
}
