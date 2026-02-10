/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

sealed class Detection(open val value: Int) {
    object Detected : Detection(0x00)
    object NotDetected : Detection(0x01)
    class Undefined(value: Int) : Detection(value)

    companion object {
        fun valueOf(value: Int): Detection {
            return when (value) {
                Detected.value -> Detected
                NotDetected.value -> NotDetected
                else -> Undefined(value)
            }
        }
    }
}
