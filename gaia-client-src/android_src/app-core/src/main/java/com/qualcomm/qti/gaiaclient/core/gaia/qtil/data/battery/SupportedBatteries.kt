/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8

class SupportedBatteries(data: ByteArray) {
    val supported: Set<Battery> = buildSet {
        for (i in data.indices) {
            val type = Battery.valueOf(getUINT8(data[i]))
            type?.let { battery -> add(battery) }
        }
    }

    override fun toString(): String {
        return "SupportedBatteries(supported=$supported)"
    }

}
