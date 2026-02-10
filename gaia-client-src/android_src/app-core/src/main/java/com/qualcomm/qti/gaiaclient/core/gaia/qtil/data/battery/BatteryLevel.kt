/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8

data class BatteryLevel(val battery: Battery?, val level: Int) {

    companion object {
        private const val TYPE_OFFSET = 0
        private const val LEVEL_OFFSET = 1
        const val LEVEL_UNKNOWN = 0xFF

        @JvmStatic
        fun buildSet(data: ByteArray): Set<BatteryLevel> = buildSet {
            for (i in data.indices step 2) {
                add(
                    BatteryLevel(
                        Battery.valueOf(getUINT8(data, i + TYPE_OFFSET, 0)),
                        getUINT8(data, i + LEVEL_OFFSET, 0)
                    )
                )
            }
        }
    }

}
