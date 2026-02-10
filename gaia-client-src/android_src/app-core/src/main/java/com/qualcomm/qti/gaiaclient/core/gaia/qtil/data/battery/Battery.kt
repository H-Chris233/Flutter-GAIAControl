/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery

enum class Battery(val value: Int) {
    SINGLE_DEVICE(0x00),
    LEFT_DEVICE(0x01),
    RIGHT_DEVICE(0x02),
    CHARGER_CASE(0x03);

    companion object {
        @JvmStatic
        fun valueOf(value: Int): Battery? = values().find { battery -> battery.value == value.toInt() }
    }
}
