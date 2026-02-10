/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.voiceui

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData

abstract class VoiceUIRepositoryData : VoiceUIRepository {

    private val _assistants = StickyLiveData<List<VoiceAssistant>>()
    override val assistants = _assistants

    private val _selectedAssistant = StickyLiveData<VoiceAssistant?>()
    override val selectedAssistant = _selectedAssistant

    override fun reset() {
        _assistants.value = emptyList()
        _selectedAssistant.value = null
    }

    protected fun updateAssistants(assistants: List<VoiceAssistant>) {
        _assistants.postValue(assistants)
    }

    protected fun updateSelectedAssistant(assistant: VoiceAssistant?) {
        _selectedAssistant.postValue(assistant)
    }
}
