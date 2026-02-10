/*
 * ************************************************************************************************
 * * © 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

sealed class State(open val value: Int) {
    object Disabled : State(0x00)
    object Enabled : State(0x01)

    class Undefined(value: Int) : State(value) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Undefined) return false
            return value == other.value
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }
    }

    override fun toString(): String {
        return when (this) {
            is Undefined -> "Undefined($value)"
            else -> this.javaClass.simpleName
        }
    }

    companion object {
        @JvmStatic
        fun valueOf(value: Int): State = when (value) {
            Disabled.value -> Disabled
            Enabled.value -> Enabled
            else -> Undefined(value)
        }

        @JvmStatic
        fun valueOf(enabled: Boolean): State = if (enabled) Enabled else Disabled
    }
}
