/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.core.sending

import android.util.Log
import com.qualcomm.qti.gaiaclient.core.bluetooth.GaiaTransport
import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.Gatt
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.GaiaFormatter
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.ProtocolVersion
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ProtocolSubscriber
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "GaiaSenderWrapper"

class GaiaSenderWrapper(publicationManager: PublicationManager) : GaiaSender {
    private var transport: GaiaTransport? = null
    private val sizes = ConcurrentHashMap<SizeInfo, Int>()
    private var protocolVersion = DEFAULT_PROTOCOL_VERSION
    private lateinit var formatter: GaiaFormatter

    private val protocolSubscriber: ProtocolSubscriber = object : ProtocolSubscriber {
        override fun getExecutionType(): ExecutionType {
            return ExecutionType.BACKGROUND
        }

        override fun onProtocolVersion(version: Long) {
            if (version > ProtocolVersion.V4) {
                Log.w(TAG, "[ProtocolSubscriber->onProtocolVersion] unsupported version: $version")
                // keep the previous version for formatting packets
                return
            }
            protocolVersion = version
        }

        override fun onSizeInfo(info: SizeInfo, size: Int) {
            sizes[info] = size
        }

        override fun onError(info: Any?, reason: Reason) {
            if (info == null) {
                protocolVersion = DEFAULT_PROTOCOL_VERSION
            }
        }
    }
    private val mConnectionSubscriber: ConnectionSubscriber = object : ConnectionSubscriber {
        override fun getExecutionType(): ExecutionType {
            return ExecutionType.BACKGROUND
        }

        override fun onConnectionStateChanged(device: Device, state: ConnectionState) {
            if (state != ConnectionState.CONNECTED) {
                // reset protocol version
                protocolVersion = DEFAULT_PROTOCOL_VERSION
            }
        }

        override fun onConnectionError(device: Device, reason: BluetoothStatus) {
            // nothing to do
        }
    }

    init {
        publicationManager.subscribe(protocolSubscriber)
        publicationManager.subscribe(mConnectionSubscriber)
        // no unsubscription required: GaiaSenderWrapper is destroyed when PublicationManager is
    }

    fun setTransport(sender: GaiaTransport?) {
        this.formatter = GaiaFormatter.getFormatter(sender?.bluetoothType)
        this.transport = sender
    }

    override fun sendData(content: ByteArray, isFlushed: Boolean, useRwcp: Boolean, listener: SendListener): Long {
        val transport = transport ?: return IdCreator.NULL_ID
        return if (!transport.isConnected) IdCreator.NULL_ID else
            transport.sendData(formatPacket(protocolVersion, content), isFlushed, useRwcp, listener)
    }

    override fun isConnected(): Boolean = transport?.isConnected == true
    override fun canHoldData(): Boolean {
        return transport?.canHoldData() ?: false
    }

    override fun holdData(ids: Collection<Long>) {
        transport?.holdData(ids)
    }

    override fun resumeData(ids: Collection<Long>) {
        transport?.resumeData(ids)
    }

    override fun cancelData(ids: Collection<Long>) {
        transport?.cancelData(ids)
    }

    override fun getMaxPayloadSize(info: SizeInfo): Int {
        val value = sizes[info]
        return value ?: 0
    }

    override fun getPayloadSize(transportSize: Long): Int =
        formatter.getPayloadSize(transportSize.toInt(), protocolVersion)

    private fun formatPacket(version: Long, content: ByteArray): ByteArray {
        return formatter.format(content, version)
    }

    val maxPayloadSize: Int
        get() {
            val available = transport?.maxSize ?: Gatt.Mtu.DEFAULT
            return formatter.getPayloadSize(available, protocolVersion)
        }

    companion object {
        /**
         * This is set to V1 as it is the minimum version supported
         */
        private const val DEFAULT_PROTOCOL_VERSION = ProtocolVersion.V1
    }
}
