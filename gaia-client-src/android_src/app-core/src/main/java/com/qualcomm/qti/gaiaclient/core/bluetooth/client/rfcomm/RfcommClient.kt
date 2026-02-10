/*
 * ************************************************************************************************
 * * © 2023-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.bluetooth.GaiaTransport
import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.BluetoothClient
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.StreamReader
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication.CommunicationError
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication.Communicator
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication.ReceivingListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.connection.ConnectionListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.connection.ConnectionThread
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.uuids.UuidFetcher
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.TransportProtocol.Rfcomm.Frame
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils.getBluetoothAdapter
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger
import java.util.UUID

private const val TAG = "RfcommClient"
private const val LOG_METHODS = DEBUG.Bluetooth.CLIENT

@SuppressLint("MissingPermission")
class RfcommClient(device: Device, listener: Listener, streamReader: StreamReader) :
    BluetoothClient(BluetoothType.CLASSIC, device, listener, streamReader) {

    private var connectionThread: ConnectionThread? = null
    private var communicator: Communicator? = null

    private var bluetoothDevice: BluetoothDevice? = null
    private var uuid: UUID? = null

    override val gaiaTransport: GaiaTransport = object : GaiaTransport {
        override val maxSize: Int = Frame.v4maxLength
        override val isConnected: Boolean
            get() = this@RfcommClient.isConnected
        override val bluetoothType: BluetoothType
            get() = this@RfcommClient.bluetoothType

        override fun sendData(bytes: ByteArray, isFlushed: Boolean, useRwcp: Boolean, listener: SendListener?): Long =
            communicator?.sendData(bytes, isFlushed, listener) ?: IdCreator.NULL_ID

        override fun canHoldData(): Boolean {
            return true
        }

        override fun holdData(ids: Collection<Long>?) {
            communicator?.holdData(ids)
        }

        override fun resumeData(ids: Collection<Long>?) {
            communicator?.resumeData(ids)
        }

        override fun cancelData(ids: Collection<Long>?) {
            communicator?.cancelData(ids)
        }
    }

    override fun connect(context: Context, device: BluetoothDevice): BluetoothStatus {
        val uuidFetcher = UuidFetcher(uuidFetcherListener, device)
        return uuidFetcher.fetch(context)
    }

    override fun reconnect(): BluetoothStatus {
        Logger.log(LOG_METHODS, TAG, "reconnect", Pair("address", device.bluetoothAddress))

        when (connectionState) {
            ConnectionState.CONNECTED -> return BluetoothStatus.ALREADY_CONNECTED
            ConnectionState.CONNECTING -> return BluetoothStatus.IN_PROGRESS
            else -> {}
        }

        val adapter = getBluetoothAdapter(null) ?: return BluetoothStatus.NO_BLUETOOTH
        val device = bluetoothDevice ?: return BluetoothStatus.DEVICE_NOT_FOUND
        val uuid = uuid ?: UUIDS.SPP

        return connect(adapter, device, uuid)
    }

    override fun disconnect() {
        Logger.log(LOG_METHODS, TAG, "disconnect", Pair("address", device.bluetoothAddress))

        when (connectionState) {
            ConnectionState.DISCONNECTED, ConnectionState.DISCONNECTING -> return
            else -> {}
        }

        connectionState = ConnectionState.DISCONNECTING
        cancelConnectionThread()
        cancelCommunicator()
        connectionState = ConnectionState.DISCONNECTED
    }

    override fun logBytes(log: Boolean) {
        communicator?.setLogBytes(log)
    }

    private fun cancelConnectionThread() {
        connectionThread.let { it?.cancel() }
        connectionThread = null
    }

    private fun cancelCommunicator() {
        communicator.let { it?.cancel() }
        communicator = null
    }

    private fun connect(adapter: BluetoothAdapter, device: BluetoothDevice, uuid: UUID): BluetoothStatus {
        Logger.log(LOG_METHODS, TAG, "connect", Pair("device=", device.address))

        if (device.type != BluetoothDevice.DEVICE_TYPE_CLASSIC && device.type != BluetoothDevice.DEVICE_TYPE_DUAL) {
            return BluetoothStatus.DEVICE_NOT_COMPATIBLE
        }

        this.bluetoothDevice = device
        this.uuid = uuid

        connectionState = ConnectionState.CONNECTING
        cancelConnectionThread() // Cancel any thread attempting to make a connection
        cancelCommunicator() // Cancel any thread currently running a connection
        adapter.cancelDiscovery() // Cancel discovery otherwise it slows down the connection.

        // Start the thread to connect with the given device
        val connectionThread =
            ConnectionThread(
                connectionListener,
                device,
                uuid
            )
        this.connectionThread = connectionThread
        connectionThread.start()
        return BluetoothStatus.IN_PROGRESS
    }

    private fun onSocketConnected(socket: BluetoothSocket) {
        Logger.log(LOG_METHODS, TAG, "onSocketConnected")

        cancelConnectionThread() // Cancel the thread that completed the connection
        cancelCommunicator() // Cancel any ongoing thread maintaining a communication

        // the connection is now established

        // initialising the communication pipes
        val communicator = Communicator(socket, receivingListener, streamReader)
        this.communicator = communicator
        communicator.start()
    }

    private val uuidFetcherListener = object : UuidFetcher.UuidFetcherListener {
        override fun onUuidFetched(context: Context, device: BluetoothDevice, uuids: List<UUID>) {
            val adapter =
                getBluetoothAdapter(context) ?: return listener.onConnectionError(BluetoothStatus.NO_BLUETOOTH)
            val uuid: UUID = UUIDS.getUuid(uuids)
            connect(adapter, device, uuid)
        }

        override fun onFailed(reason: BluetoothStatus) {
            listener.onConnectionError(reason)
        }
    }

    private val connectionListener: ConnectionListener = object :
        ConnectionListener {
        override fun onConnectionSuccess(socket: BluetoothSocket) {
            onSocketConnected(socket)
        }

        override fun onConnectionFailed() {
            cancelConnectionThread() // Cancel the connection thread
            connectionState = ConnectionState.DISCONNECTED
            listener.onConnectionError(BluetoothStatus.CONNECTION_FAILED)
        }

    }

    private val receivingListener: ReceivingListener = object :
        ReceivingListener {
        override fun onCommunicationReady() {
            connectionState = ConnectionState.CONNECTED
        }

        override fun onCommunicationFailed(reason: CommunicationError?) {
            when (reason) {
                CommunicationError.CONNECTION_LOST -> listener.onConnectionError(BluetoothStatus.CONNECTION_LOST)
                CommunicationError.INITIALISATION_FAILED, null -> listener.onConnectionError(BluetoothStatus.CONNECTION_FAILED)
            }
        }

        override fun onCommunicationEnded() {
            connectionState = ConnectionState.DISCONNECTED
            cancelCommunicator()
        }
    }

    private object UUIDS {
        val SPP: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        val GAIA_LEGACY: UUID = UUID.fromString("00001107-D102-11E1-9B23-00025B00A5A5")
        val DEFAULT = SPP

        fun getUuid(uuids: List<UUID>): UUID = when {
            uuids.contains(SPP) -> SPP
            uuids.contains(GAIA_LEGACY) -> GAIA_LEGACY
            else -> DEFAULT
        }
    }

}
