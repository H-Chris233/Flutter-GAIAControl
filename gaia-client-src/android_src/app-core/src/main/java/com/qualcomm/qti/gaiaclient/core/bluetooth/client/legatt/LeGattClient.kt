/*
 * ************************************************************************************************
 * * © 2023-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.util.Log
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.bluetooth.GaiaTransport
import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.BluetoothClient
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.StreamReader
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.data.Characteristic
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.data.GattDescription
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.data.Notification
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations.GattOperation
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.rwcp.RWCPClient
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.rwcp.RWCPListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import java.util.UUID

class LeGattClient(device: Device, listener: Listener, streamReader: StreamReader) :
    BluetoothClient(BluetoothType.LOW_ENERGY, device, listener, streamReader), RWCPListener {
    override var connectionState: ConnectionState
        get() = super.connectionState
        set(value) {
            super.connectionState = value
            gaia.init(gatt?.services)
        }

    var gatt: GattConnection? = null
        set(value) {
            field = value
            field?.logBytes = logBytes
        }

    private val rwcpClient: RWCPClient = RWCPClient(this)

    private var logBytes = DEBUG.Bluetooth.DATA
        set(value) {
            field = value || DEBUG.Bluetooth.DATA
            gatt?.logBytes = field
        }

    val description = GattDescription(
        Characteristic(ServiceUuid.Gaia.uuid, CharacteristicUuid.GaiaData.uuid),
        setOf(
            Notification(ServiceUuid.Gaia.uuid, CharacteristicUuid.GaiaResponse.uuid) { onGaiaResponse(it) },
            Notification(ServiceUuid.Gaia.uuid, CharacteristicUuid.GaiaData.uuid, false) { onGaiaData(it) })
    )

    override fun connect(context: Context, device: BluetoothDevice): BluetoothStatus {
        val gatt = GattConnection(
            context,
            device,
            description,
            object : GattConnection.Listener {
                override val onConnectionState: (ConnectionState) -> Unit = { connectionState = it }
                override val onConnectionError: (BluetoothStatus) -> Unit = { listener.onConnectionError(it) }
            }
        )
        this.gatt = gatt
        return gatt.connect()
    }

    override fun reconnect(): BluetoothStatus {
        return gatt?.reconnect() ?: BluetoothStatus.DEVICE_NOT_FOUND
    }

    override fun disconnect() {
        gatt?.disconnect()
    }

    override fun logBytes(log: Boolean) {
        logBytes = log
    }

    private val gaia = object {
        var gaiaCommand: BluetoothGattCharacteristic? = null
        var gaiaData: BluetoothGattCharacteristic? = null

        fun init(allServices: Map<UUID, List<BluetoothGattService>>?) {
            val services = allServices?.get(ServiceUuid.Gaia.uuid)
            val service = services?.firstOrNull()
            gaiaCommand = service?.getCharacteristic(CharacteristicUuid.GaiaCommand.uuid)
            gaiaData = service?.getCharacteristic(CharacteristicUuid.GaiaData.uuid)
        }
    }

    override val gaiaTransport = object : GaiaTransport {
        override val maxSize: Int
            get() = gatt?.mtu ?: Gatt.Mtu.DEFAULT

        override val isConnected: Boolean
            get() = this@LeGattClient.isConnected

        override val bluetoothType: BluetoothType
            get() = this@LeGattClient.bluetoothType

        override fun sendData(bytes: ByteArray, isFlushed: Boolean, useRwcp: Boolean, listener: SendListener?): Long {
            val gaiaCommand = gaia.gaiaCommand ?: return IdCreator.NULL_ID
            val gatt = gatt ?: return IdCreator.NULL_ID
            if (useRwcp) {
                listener?.onSending()
                rwcpClient.sendData(bytes) {
                    listener?.onSent()
                }
                // Return valid but meaningless ID - packet not acknowledged
                return 0
            }
            return gatt.operate(GattOperation.Write(gaiaCommand, bytes, sendListener = listener, logBytes = logBytes))
        }

        override fun canHoldData(): Boolean {
            return false
        }

        override fun holdData(ids: Collection<Long>?) {
            // Not applicable for LE GATT
        }

        override fun resumeData(ids: Collection<Long>?) {
            // Not applicable for LE GATT
        }

        override fun cancelData(ids: Collection<Long>?) {
            rwcpClient.cancelTransfer()
        }
    }

    private fun onGaiaResponse(value: ByteArray) {
        streamReader.read(GaiaClientService.getTaskManager(), value)
    }

    private fun onGaiaData(value: ByteArray) {
        rwcpClient.onReceiveRWCPSegment(value)
    }

    override fun sendRWCPSegment(bytes: ByteArray?): Boolean {
        val gaiaData = gaia.gaiaData ?: return false
        val gatt = gatt ?: return false
        val payload = bytes ?: return false
        val id = gatt.operate(
            GattOperation.Write(
                gaiaData, payload, sendListener = null, writeType = WriteType.NO_RESPONSE, logBytes =
                logBytes
            )
        )
        return id != IdCreator.NULL_ID
    }

    override fun onTransferFailed() {
//        TODO("Not yet implemented")
    }

    override fun onTransferFinished() {
//        TODO("Not yet implemented")
    }

    override fun onTransferProgress(acknowledged: Int) {
//        TODO("Not yet implemented")
    }

}
