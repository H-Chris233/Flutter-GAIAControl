/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3

import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.XpanTransport
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.XpanPlugin
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger

private const val TAG = "V3XpanPlugin"
private const val LOG_METHODS = DEBUG.QTIL.V3_XPAN_PLUGIN

class V3XpanPlugin(sender: GaiaSender) : V3QTILPlugin(QTILFeature.XPAN, sender), XpanPlugin {
    public override fun onStarted() {}

    override fun onStopped() {}
    override fun connectAP() {
        Logger.log(LOG_METHODS, TAG, "connectAP")
        sendPacket(COMMANDS.V1_CONNECT_AP)
    }

    override fun disconnectAP() {
        Logger.log(LOG_METHODS, TAG, "disconnectAP")
        sendPacket(COMMANDS.V1_DISCONNECT_AP)
    }

    override fun toggleTransport() {
        Logger.log(LOG_METHODS, TAG, "toggleTransport")
        sendPacket(COMMANDS.V1_TOGGLE_TRANSPORT)
    }

    override fun setTransport(transport: XpanTransport) {
        Logger.log(LOG_METHODS, TAG, "setTransport", Pair("transport", transport))
        sendPacket(COMMANDS.V1_SET_TRANSPORT, transport.id)
    }

    override fun onNotification(packet: NotificationPacket) {}
    override fun onResponse(response: ResponsePacket, sent: CommandPacket?) {
        Logger.log(LOG_METHODS, TAG, "onResponse", Pair("response", response), Pair("sent", sent))
    }

    override fun onError(errorPacket: ErrorPacket, sent: CommandPacket?) {
        Logger.log(LOG_METHODS, TAG, "onError", Pair("packet", errorPacket), Pair("sent", sent))
    }

    override fun onFailed(source: GaiaPacket, reason: Reason) {
        Logger.log(LOG_METHODS, TAG, "onFailed", Pair("reason", reason), Pair("packet", source))
    }

    private object COMMANDS {
        const val V1_TOGGLE_TRANSPORT = 0x00
        const val V1_SET_TRANSPORT = 0x01
        const val V1_CONNECT_AP = 0x02
        const val V1_DISCONNECT_AP = 0x03
    }
}
