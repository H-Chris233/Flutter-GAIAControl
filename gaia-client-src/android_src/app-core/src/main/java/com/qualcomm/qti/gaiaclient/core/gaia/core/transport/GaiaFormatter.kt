/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.transport

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.Gatt
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.TransportProtocol.Rfcomm.Frame

sealed class GaiaFormatter {
    abstract fun getPayloadSize(transportSize: Int, protocolVersion: Long): Int
    abstract fun format(content: ByteArray, protocolVersion: Long): ByteArray

    object LeGatt : GaiaFormatter() {
        override fun getPayloadSize(transportSize: Int, protocolVersion: Long): Int =
            transportSize - GaiaPdu.Header.length - Gatt.Att.headerLength

        override fun format(content: ByteArray, protocolVersion: Long): ByteArray = content
    }

    class Rfcomm(private val hasChecksum: Boolean = false) : GaiaFormatter() {
        override fun getPayloadSize(transportSize: Int, protocolVersion: Long): Int =
            Frame.Pdu.Payload.available(transportSize, protocolVersion, hasChecksum)

        override fun format(content: ByteArray, protocolVersion: Long): ByteArray {
            return Frame.format(protocolVersion, content, hasChecksum)
        }
    }

    companion object {
        @JvmStatic
        fun getFormatter(type: BluetoothType?): GaiaFormatter {
            return when (type) {
                BluetoothType.LOW_ENERGY -> LeGatt
                BluetoothType.CLASSIC, null -> Rfcomm()
            }
        }
    }
}
