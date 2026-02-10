/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data

sealed class VoiceAssistant(val value: Int) {
    object None : VoiceAssistant(0x00)
    object Reserved : VoiceAssistant(0x01)
    object GAA : VoiceAssistant(0x02)
    object AMA : VoiceAssistant(0x03)
    class Unknown(value: Int) : VoiceAssistant(value)

    companion object {
        @JvmStatic
        fun valueOf(value: Int): VoiceAssistant = when (value) {
            None.value -> None
            Reserved.value -> Reserved
            GAA.value -> GAA
            AMA.value -> AMA
            else -> Unknown(value)
        }
    }
}
