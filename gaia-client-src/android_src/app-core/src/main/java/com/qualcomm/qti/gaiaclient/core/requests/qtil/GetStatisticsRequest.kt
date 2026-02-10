/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil

import android.content.Context
import com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager
import com.qualcomm.qti.gaiaclient.core.data.StatisticsInfo
import com.qualcomm.qti.gaiaclient.core.requests.core.Request

class GetStatisticsRequest : Request<Void?, Void?, Void?> {
    private val info: StatisticsInfo

    private val parameters: Any?

    constructor(info: StatisticsInfo) : super(null) {
        this.info = info
        parameters = null
    }

    constructor(info: StatisticsInfo, parameters: Any?) : super(null) {
        this.info = info
        this.parameters = parameters
    }

    override fun run(context: Context?) {
        val plugin = getQtilManager().statisticsPlugin
        if (plugin != null) {
            plugin.fetchInfo(info, parameters)
            onComplete(null)
        } else {
            onError(null)
        }
    }

    override fun onEnd() {}
}
