/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.XpanTransport

interface XpanPlugin {
    fun connectAP()
    fun disconnectAP()
    fun toggleTransport()
    fun setTransport(transport: XpanTransport)
}
