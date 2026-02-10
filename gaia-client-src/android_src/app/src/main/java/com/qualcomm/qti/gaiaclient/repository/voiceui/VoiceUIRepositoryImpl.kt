/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.voiceui

import android.content.Context
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.VoiceUISubscriber
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetSelectedAssistantRequest
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetSupportedAssistantsRequest
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetAssistantRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceUIRepositoryImpl @Inject constructor() : VoiceUIRepositoryData() {

    private val subscriber: VoiceUISubscriber = object : VoiceUISubscriber {
        override fun onSelectedAssistant(assistant: VoiceAssistant) {
            updateSelectedAssistant(assistant)
        }

        override fun onSupportedAssistants(assistants: List<VoiceAssistant>) {
            updateAssistants(assistants)
        }
    }

    private val setAssistantListener = object : RequestListener<Void?, Void?, Reason> {
        override fun onProgress(aVoid: Void?) {}
        override fun onComplete(aVoid: Void?) {}
        override fun onError(reason: Reason) {}
    }

    override fun init() {
        val publicationManager = GaiaClientService.getPublicationManager()
        publicationManager.subscribe(subscriber)
    }

    override fun setAssistant(context: Context, assistant: VoiceAssistant) {
        val request = SetAssistantRequest(assistant, setAssistantListener)
        GaiaClientService.getRequestManager().execute(context, request)
    }

    override fun fetchSupportedAssistants(context: Context) {
        val request = GetSupportedAssistantsRequest()
        GaiaClientService.getRequestManager().execute(context, request)
    }

    override fun fetchSelectedAssistant(context: Context) {
        val request = GetSelectedAssistantRequest()
        GaiaClientService.getRequestManager().execute(context, request)
    }
}
