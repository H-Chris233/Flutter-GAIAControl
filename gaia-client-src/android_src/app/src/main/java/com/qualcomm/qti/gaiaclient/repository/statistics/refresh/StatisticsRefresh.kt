/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics.refresh

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository

interface StatisticsRefresh {
    fun startRefreshing(category: CategoryID, repository: StatisticsRepository)
    fun stopRefreshing(category: CategoryID)
    fun stopAllRefreshing()

    fun pauseAllRefreshing()
    fun resumePaused(repository: StatisticsRepository)

    fun isRefreshing(category: CategoryID) : Boolean

    fun refreshRate(category: CategoryID) : Long
    fun adjustRefreshRate(refreshRateMS: Long, category: CategoryID, repository: StatisticsRepository)
}
