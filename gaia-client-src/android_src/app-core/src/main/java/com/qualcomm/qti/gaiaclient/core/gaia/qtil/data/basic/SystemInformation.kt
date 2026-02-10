/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.TextData
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

sealed class SystemInformation(val id: Int, val bytes: ByteArray) {

    private object IDs {
        const val APPLICATION_BUILD_ID: Int = 0
    }

    class ApplicationBuildId(bytes: ByteArray) : SystemInformation(IDs.APPLICATION_BUILD_ID, bytes) {
        val value = TextData(bytes)
    }

    class Undefined(id: Int, data: ByteArray) : SystemInformation(id, data)

    companion object {
        private const val ID_OFFSET = 0
        private const val LENGTH_OFFSET = 1
        private const val DATA_OFFSET = 2
        private const val HEADER_LENGTH = 2

        fun valueOf(id: Int, data: ByteArray): SystemInformation {
            return when (id) {
                IDs.APPLICATION_BUILD_ID -> ApplicationBuildId(data)
                else -> Undefined(id, data)
            }
        }

        @JvmStatic
        fun valueOf(source: ByteArray): MutableList<SystemInformation> {
            val infoSet: MutableList<SystemInformation> = ArrayList()
            var offset = 0
            while (offset < source.size) {
                val id = BytesUtils.getUINT8(source, offset + ID_OFFSET)
                val length = BytesUtils.getUINT8(source, offset + LENGTH_OFFSET)
                val data = BytesUtils.getSubArray(source, offset + DATA_OFFSET, length)
                infoSet.add(valueOf(id, data))
                offset += length + HEADER_LENGTH
            }
            return infoSet
        }
    }
}
