/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.xpan

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.XpanTransport

interface XpanRepository {

    fun connectAP()
    fun disconnectAP()
    fun toggleTransport()
    fun setTransport(transport: XpanTransport)
}

abstract class XpanRepositoryData : XpanRepository
