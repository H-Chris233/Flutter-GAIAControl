/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.transport

import android.util.Log
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.StreamReader
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.TransportProtocol.Rfcomm.Frame
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.TransportProtocol.Rfcomm.Packet
import com.qualcomm.qti.gaiaclient.core.tasks.TaskManager
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger

private const val TAG = "GaiaReader"
private const val LOG_METHODS = DEBUG.Gaia.GAIA_STREAM_ANALYSER

sealed class GaiaReader(val listener: Listener) : StreamReader {
    val tag = "GaiaReader.${javaClass.simpleName}"

    class LeGatt(listener: Listener) : GaiaReader(listener) {
        override fun reset() {}
        override fun read(taskManager: TaskManager, stream: ByteArray) {
            taskManager.runInBackground { listener.onDataFound(stream) }
        }
    }

    class Rfcomm(listener: Listener) : GaiaReader(listener) {
        private var frameOffset = 0
        private var packet: Packet = Packet()

        init {
            reset() // unnecessary but done for consistency
        }

        override fun reset() {
            packet = Packet()
            frameOffset = 0
        }

        override fun read(taskManager: TaskManager, stream: ByteArray) {
            Logger.log(LOG_METHODS, tag, "read", Pair("stream", stream))

            // go through the received bytes
            for (byte in stream) {
                // not reading a frame and has not identified a new frame
                if (frameOffset == 0 && byte != Frame.Header.SOF.value) continue // no bytes to read

                read(frameOffset, byte)
                frameOffset++ // number of received bytes can be incremented

                // once the frame is completed, the content can be dispatched
                if (frameOffset == packet.expectedLength) {
                    val pdu = packet.pdu
                    // dispatch the packet on another thread to allow the reading of the next stream
                    taskManager.runInBackground { listener.onDataFound(pdu) }
                    reset()
                }
            }
        }

        fun read(offset: Int, byte: Byte) {
            // get sof
            if (offset == Frame.Header.SOF.offset) {
                // start of a new frame
                packet._sof = byte
            } else if (offset == Frame.Header.Version.offset) { // = 1
                packet._version = byte // uint8
            } else if (offset == Frame.Header.Flags.offset) { // = 2
                packet.flags = byte
            } else if (offset == Frame.Header.Length.offset) { // = 3
                if (!packet.hasLengthExtension) {
                    packet.length = byte.toInt() and 0xFF
                } else {
                    packet.lengthFields[0] = byte
                }
            } else if (packet.hasLengthExtension && offset == Frame.Header.Length.offset + 1) { // = 4
                packet.lengthFields[1] = byte
                packet.length = BytesUtils.getUINT16(packet.lengthFields, 0)
            } else if (packet.hasChecksum && offset == packet.checksumOffset) {
                packet._checksum = byte
            } else {
                val contentOffset = offset - Frame.Header.length(packet.hasLengthExtension)
                if (contentOffset < 0 || contentOffset >= packet.pduBuffer.size) {
                    Log.w(TAG, "[read] offset ($offset) is out of range (0, ${packet.pduBuffer.size})")
                    return
                }
                packet.pduBuffer[contentOffset] = byte
            }
        }
    }

    interface Listener {
        fun onDataFound(data: ByteArray)
    }

    companion object {
        @JvmStatic
        fun getReader(type: BluetoothType, listener: Listener): GaiaReader {
            return when (type) {
                BluetoothType.LOW_ENERGY -> LeGatt(listener)
                BluetoothType.CLASSIC -> Rfcomm(listener)
            }
        }
    }
}
