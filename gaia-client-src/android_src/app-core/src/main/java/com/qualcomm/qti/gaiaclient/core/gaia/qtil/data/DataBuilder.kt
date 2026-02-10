/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

open class DataBuilder<out D>(private val factory: (ByteArray) -> D) {
    private val chunks: MutableList<ByteArray?> = ArrayList()
    private var length = 0

    fun add(data: ByteArray?) {
        data?.let { // chunk resource can have been destroyed
            chunks.add(data)
            length += data.size
        }
    }

    fun build(): D {
        val record = ByteArray(length)
        var i = 0
        for (chunk in chunks) {
            chunk?.let {// chunk resource can have been destroyed
                BytesUtils.copyArray(chunk, record, i)
                i += chunk.size
            }
        }
        return factory(record)
    }
}
