/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins

import com.qualcomm.qti.gaiaclient.core.data.StatisticsInfo
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.StatisticsPublisher


interface StatisticsPlugin {
    // to force the StatisticsPlugin to implement StatisticsPublisher
    // this is unused
    fun getStatisticsPublisher(): StatisticsPublisher

    fun fetchInfo(info: StatisticsInfo, parameter: Any?): Boolean
}
