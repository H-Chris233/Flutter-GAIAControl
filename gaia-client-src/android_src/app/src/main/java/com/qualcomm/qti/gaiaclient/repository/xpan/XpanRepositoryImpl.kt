/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.xpan

import android.content.Context
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.XpanTransport
import com.qualcomm.qti.gaiaclient.core.requests.qtil.XpanRequest
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XpanRepositoryImpl @Inject constructor(@ApplicationContext private val context: Context) : XpanRepositoryData() {
    override fun connectAP() {
        val request = XpanRequest.ConnectAP()
        GaiaClientService.getRequestManager().execute(context, request)
    }

    override fun disconnectAP() {
        val request = XpanRequest.DisconnectAP()
        GaiaClientService.getRequestManager().execute(context, request)
    }

    override fun toggleTransport() {
        val request = XpanRequest.ToggleTransport()
        GaiaClientService.getRequestManager().execute(context, request)
    }

    override fun setTransport(transport: XpanTransport) {
        val request = XpanRequest.SetTransport(transport)
        GaiaClientService.getRequestManager().execute(context, request)
    }
}
