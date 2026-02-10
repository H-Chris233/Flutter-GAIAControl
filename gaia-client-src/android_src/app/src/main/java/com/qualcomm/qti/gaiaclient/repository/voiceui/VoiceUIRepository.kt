/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.voiceui

import android.content.Context
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant

interface VoiceUIRepository {
    val assistants: LiveData<List<VoiceAssistant>>
    val selectedAssistant: LiveData<VoiceAssistant?>

    fun init()
    fun reset()

    fun setAssistant(context: Context, assistant: VoiceAssistant)
    fun fetchSupportedAssistants(context: Context)
    fun fetchSelectedAssistant(context: Context)
}
