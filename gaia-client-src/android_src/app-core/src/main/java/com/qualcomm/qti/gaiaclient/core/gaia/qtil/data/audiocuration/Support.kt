/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

sealed class Support(open val value: Int) {
    object NotSupported : Support(0x00)
    object Supported : Support(0x01)
    class Undefined(value: Int) : Support(value)

    companion object {
        @JvmStatic
        fun valueOf(value: Int): Support {
            return when(value) {
                NotSupported.value -> NotSupported
                Supported.value -> Supported
                else -> Undefined(value)
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Support

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value
    }

    override fun toString(): String {
        return when (this) {
            is Undefined -> "Undefined($value)"
            else -> this.javaClass.simpleName
        }
    }
}
