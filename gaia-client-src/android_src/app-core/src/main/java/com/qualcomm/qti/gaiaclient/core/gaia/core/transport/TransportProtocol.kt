/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.transport

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.ProtocolVersion
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils
import kotlin.math.min

/**
 * Defines "GAIA transport protocol" that is used to transport GAIA PDUs over different Bluetooth transports.
 *
 * Note: over LE GATT the PDU is sent as is.
 */
object TransportProtocol {

    /**
     * The RFCOMM transport protocol for GAIA PDUs, defines a packet with the following default frame:
     * ```
     *      0 bytes   1         2         3         4  5  6  7  8          len+8          len+9
     *      +---------+---------+---------+---------+--+--+--+--+-----------+ +------------+
     *      |         |         |         | LENGTH  |          PDU          | |            |
     *      |   SOF   | VERSION |  FLAGS  | OF      |-----------------------| |  CHECKSUM  |
     *      |         |         |         | PAYLOAD |   HEADER  |  PAYLOAD  | | (optional) |
     *      +---------+---------+---------+---------+--+--+--+--+-----------+ +------------+
     * ```
     *
     * Frame with Length Extension:
     * ```
     *      bytes  3         4         5  6  7  8  9          len+9         len+10
     *      ...    +---------+---------+--+--+--+--+-----------+ +------------+
     *      ...    |       LENGTH      |          PDU          | |  CHECKSUM  |
     *      ...    |     OF PAYLOAD    |   HEADER  |  PAYLOAD  | | (optional) |
     *      ...    +---------+---------+--+--+--+--+-----------+ +------------+
     * ```
     */
    object Rfcomm {
        class Packet {
            // SOF expected as
            var _sof: Byte = 0
            val sof: Int
                get() = _sof.toInt() and 0xFF

            var _version: Byte = 0
            val version: Long
                get() = (_version.toInt() and 0xFFFF).toLong()

            var flags: Byte = 0

            var length: Int = 0xFFFF
            val lengthFields = ByteArray(Frame.Header.Length.lengthWithExtension)
            val expectedLength: Int
                get() = Frame.length(hasLengthExtension, length, hasChecksum)
            val hasLengthExtension: Boolean
                get() = version >= ProtocolVersion.V4 && Flag.LENGTH_EXTENSION.isRaised(flags)

            val pduBuffer = ByteArray(GaiaPdu.Header.length + Frame.Pdu.Payload.v4maxLength)    /* 65539 */
            val pdu: ByteArray
                get() {
                    val pduLength = GaiaPdu.Header.length + length
                    return pduBuffer.copyOfRange(0, pduLength)
                }

            var _checksum: Byte = 0
            val checksum: Int
                get() = _checksum.toInt() and 0xFF
            val checksumOffset: Int
                get() = Frame.Footer.Checksum.offset(hasLengthExtension, length)
            val hasChecksum: Boolean
                get() = /* version >= 0x01 && */Flag.CHECKSUM.isRaised(flags)
        }

        object Frame {
            object Header {
                fun length(hasLengthExtension: Boolean): Int =                                       /* 4 or 5 */
                    SOF.length + Version.length + Flags.length + Length.length(hasLengthExtension)

                /* SOF = start of frame */
                object SOF {
                    const val value = 0xFF.toByte() // All GAIA frames over RFCOMM use 0xFF as their first byte
                    const val offset = 0
                    const val length = 1
                }

                object Version {
                    const val offset = 1
                    const val length = 1
                }

                object Flags {
                    const val offset = 2
                    const val length = 1
                }

                object Length {
                    const val offset = 3
                    const val length = 1
                    const val lengthWithExtension = 2

                    fun length(hasLengthExtension: Boolean) = if (hasLengthExtension) lengthWithExtension else length
                }
            }

            object Pdu {
                fun offset(hasLengthExtension: Boolean): Int = Header.length(hasLengthExtension)

                object Payload {
                    /**
                     * maximum value is 0xFF, requirement before V4 was to have an even number of bytes => 0xFE (254)
                     */
                    const val maxLength = 0xFE                             /* 254 */
                    const val v4maxLengthNoExtension = 0xFF                /* 255 */
                    const val v4maxLength = 0xFFFF                         /* 65535 */

                    fun available(transportSize: Int, protocolVersion: Long, hasChecksum: Boolean): Int {
                        val maxNoExtension =
                            v4maxLengthNoExtension + Header.length(false) + GaiaPdu.Header.length + Footer.length(hasChecksum)
                        val useLengthExtension: Boolean =
                            protocolVersion >= ProtocolVersion.V4 && transportSize > maxNoExtension
                        val surrounds = Header.length(useLengthExtension) + GaiaPdu.Header.length + Footer.length(hasChecksum)
                        val available = if (transportSize <= surrounds) 0 else (transportSize - surrounds)
                        val max = when {
                            protocolVersion < ProtocolVersion.V4 -> maxLength        /* 0xFE (254) */
                            useLengthExtension -> v4maxLength                        /* 0xFFFF (65535) */
                            else -> v4maxLengthNoExtension                           /* 0xFF (255) */
                        }

                        return min(available, max)
                    }
                }
            }

            object Footer {
                object Checksum {
                    fun offset(hasLengthExtension: Boolean, payloadLength: Int): Int =
                        Header.length(hasLengthExtension) + GaiaPdu.Header.length + payloadLength
                    fun length(hasChecksum: Boolean): Int = if (hasChecksum) 1 else 0
                }
                fun length(hasChecksum: Boolean): Int = Checksum.length(hasChecksum)                 /* 0 or 1 */
            }

            @JvmField
            val v4maxLength =                                                                    /* 65545 */
                Header.length(true) + GaiaPdu.Header.length + Pdu.Payload.v4maxLength + Footer.length(true)

            @JvmStatic
            fun length(hasLengthExtension: Boolean, payloadLength: Int, hasChecksum: Boolean): Int =
                Header.length(hasLengthExtension) + GaiaPdu.Header.length + payloadLength + Footer.length(hasChecksum)

            fun format(protocolVersion: Long, content: ByteArray, hasChecksum: Boolean): ByteArray {
                val payloadLength = content.size - GaiaPdu.Header.length
                val length = when {
                    payloadLength < 0 -> 0
                    payloadLength > Pdu.Payload.maxLength && protocolVersion < ProtocolVersion.V4 -> Pdu.Payload.maxLength
                    payloadLength > Pdu.Payload.v4maxLength -> Pdu.Payload.v4maxLength
                    else -> payloadLength
                }
                val hasLengthExtension = payloadLength > Pdu.Payload.v4maxLengthNoExtension
                val frameLength = length(hasLengthExtension, length, hasChecksum)
                val result = ByteArray(frameLength)

                // add sof
                result[Header.SOF.offset] = Header.SOF.value

                // add version
                result[Header.Version.offset] = protocolVersion.toByte()

                // add flags
                result[Header.Flags.offset] = Flag.getValue(hasChecksum, hasLengthExtension)

                // add payload length field
                if (hasLengthExtension) BytesUtils.setUINT16(length, result, Header.Length.offset) else
                    result[Header.Length.offset] = length.toByte()

                // add content
                val contentOffset = Pdu.offset(hasLengthExtension)
                content.copyInto(result, contentOffset, 0, GaiaPdu.Header.length + length)

                // add checksum
                if (hasChecksum) {
                    result[frameLength - 1] = getChecksum(result, frameLength - 1)
                }
                return result
            }

            /**
             * Calculates the checksum of a byte array as an unsigned byte - from 0x00 to 0xFF.
             */
            private fun getChecksum(source: ByteArray, length: Int): Byte {
                var check: Byte = 0
                for (i in 0 until length) {
                    check = (check.toInt() xor source[i].toInt()).toByte()
                }
                return check
            }
        }

        /**
         * An enumeration to represent all the flags that can be contained in the flags filed of a
         * [com.qualcomm.qti.gaiaclient.core.gaia.core.transport.TransportProtocol.Rfcomm.Packet] as follows:
         * ```
         *      7 bits  6       5       4       3       2                  1           0
         *      +-------+-------+-------+-------+-------+------------------+-----------+
         *      |               RESERVED                | LENGTH EXTENSION | CHECKSUM  |
         *      +-------+-------+-------+-------+-------+------------------+-----------+
         * ```
         *
         * @property mask The mask of the flag to know if the flag is raised.
         */
        enum class Flag(val mask: Int) {
            /**
             * The flag is raised when the frame contains a checksum.
             */
            CHECKSUM(0b1),

            /**
             * The flag is raised when the frame uses the length extension aka the length is over 2 bytes instead of 1.
             */
            LENGTH_EXTENSION(0b10);

            /**
             * @return True if the flag is raised in the given value, false otherwise.
             */
            fun isRaised(value: Byte): Boolean {
                return value.toInt() and mask != 0
            }

            companion object {
                /**
                 * To get a value that contains all the given flags.
                 * ```kotlin
                 *     buildValue(CHECKSUM, LENGTH_EXTENSION) -> 0b11
                 *     buildValue(CHECKSUM) -> 0b01
                 * ```
                 */
                @JvmStatic
                fun getValue(vararg flags: Flag): Byte {
                    var result = 0
                    for (flag in flags) {
                        result += flag.mask
                    }
                    return result.toByte()
                }

                /**
                 * To build the value depending on the knowledge of the flags being raised.
                 */
                @JvmStatic
                fun getValue(hasChecksum: Boolean, hasLengthExtension: Boolean): Byte {
                    return when {
                        hasChecksum && hasLengthExtension -> getValue(CHECKSUM, LENGTH_EXTENSION)
                        hasChecksum -> getValue(CHECKSUM)
                        hasLengthExtension -> getValue(LENGTH_EXTENSION)
                        else -> 0
                    }
                }
            }
        }
    }
}
