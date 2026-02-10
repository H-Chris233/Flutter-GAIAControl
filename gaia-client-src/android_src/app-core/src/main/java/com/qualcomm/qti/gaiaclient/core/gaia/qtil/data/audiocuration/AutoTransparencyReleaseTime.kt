/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

enum class AutoTransparencyReleaseTime(val value: Int) {
    NO_ACTION_ON_RELEASE(0x00),
    SHORT_RELEASE(0x01),
    NORMAL_RELEASE(0x02),
    LONG_RELEASE(0x03);

    companion object {
        @JvmStatic
        fun valueOf(value: Int): AutoTransparencyReleaseTime? {
            return values().firstOrNull { time -> time.value == value }
        }
    }
}
