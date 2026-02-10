/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3

import com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.SupportedAssistants
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceUIPlugin
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.VoiceUIPublisher
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

class V3VoiceUIPlugin(sender: GaiaSender) : V3QTILPlugin(QTILFeature.VOICE_UI, sender), VoiceUIPlugin {

    override val voiceUIPublisher = VoiceUIPublisher()

    public override fun onStarted() {
        getPublicationManager().register(voiceUIPublisher)
    }

    override fun onStopped() {
        getPublicationManager().unregister(voiceUIPublisher)
    }

    override fun fetchSupportedAssistants() {
        sendPacket(COMMANDS.V1_GET_SUPPORTED_ASSISTANTS)
    }

    override fun fetchSelectedAssistant() {
        sendPacket(COMMANDS.V1_GET_SELECTED_ASSISTANT)
    }

    override fun setSelectedAssistant(assistant: VoiceAssistant) {
        sendPacket(COMMANDS.V1_SET_SELECTED_ASSISTANT, assistant.value)
    }

    override fun onNotification(packet: NotificationPacket) {
        if (packet.command == NOTIFICATIONS.V1_ASSISTANT_CHANGED) {
            onSelectedAssistant(packet.data)
        }
    }

    override fun onResponse(response: ResponsePacket, sent: CommandPacket?) {
        when (response.command) {
            COMMANDS.V1_GET_SELECTED_ASSISTANT -> onSelectedAssistant(response.data)
            COMMANDS.V1_SET_SELECTED_ASSISTANT -> {}
            COMMANDS.V1_GET_SUPPORTED_ASSISTANTS -> {
                val assistants = SupportedAssistants(response.data)
                voiceUIPublisher.publishAssistants(assistants.assistants)
            }
        }
    }

    override fun onError(errorPacket: ErrorPacket, sent: CommandPacket?) {}

    override fun onFailed(packet: GaiaPacket, reason: Reason) {}

    private fun onSelectedAssistant(data: ByteArray) {
        val assistantOffset = 0
        val assistant = VoiceAssistant.valueOf(BytesUtils.getUINT8(data, assistantOffset))
        voiceUIPublisher.publishSelectedAssistant(assistant)
    }

    private object COMMANDS {
        const val V1_GET_SELECTED_ASSISTANT = 0x00
        const val V1_SET_SELECTED_ASSISTANT = 0x01
        const val V1_GET_SUPPORTED_ASSISTANTS = 0x02
    }

    private object NOTIFICATIONS {
        const val V1_ASSISTANT_CHANGED = 0x00
    }
}
