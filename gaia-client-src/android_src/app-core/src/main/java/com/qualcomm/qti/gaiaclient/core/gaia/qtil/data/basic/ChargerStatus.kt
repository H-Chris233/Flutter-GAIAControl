/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic

sealed class ChargerStatus(open val value: Int) {
    object Unplugged : ChargerStatus(0x00)
    object Plugged : ChargerStatus(0x01)
    class Undefined(value: Int) : ChargerStatus(value)

    val isCharging: Boolean
        get() = this == Plugged

    companion object {
        @JvmStatic
        fun valueOf(value: Int): ChargerStatus {
            return when (value) {
                Unplugged.value -> Unplugged
                Plugged.value -> Plugged
                else -> Undefined(value)
            }
        }
    }
}
