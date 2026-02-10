/*
 * ************************************************************************************************
 * * © 2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.rwcp

typealias  OnSent = ()->Unit
data class RWCPData(
    val bytes: ByteArray,
    val onSent: OnSent? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RWCPData

        if (!bytes.contentEquals(other.bytes)) return false
        if (onSent != other.onSent) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + onSent.hashCode()
        return result
    }
}
