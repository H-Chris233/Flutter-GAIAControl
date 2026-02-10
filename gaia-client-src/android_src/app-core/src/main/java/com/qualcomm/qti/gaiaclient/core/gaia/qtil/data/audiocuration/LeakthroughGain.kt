/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

data class LeakthroughGain(val leftGain: Int, val rightGain: Int) {

    val bytes: ByteArray
        get() {
            val parameters = ByteArray(Structure.LENGTH)
            BytesUtils.setUINT8(leftGain, parameters, Structure.Offsets.LEFT_GAIN)
            BytesUtils.setUINT8(rightGain, parameters, Structure.Offsets.RIGHT_GAIN)
            return parameters
        }

    object Structure {
        const val LENGTH = 2
        object Offsets {
            const val LEFT_GAIN = 0
            const val RIGHT_GAIN = 1
        }
    }
}
