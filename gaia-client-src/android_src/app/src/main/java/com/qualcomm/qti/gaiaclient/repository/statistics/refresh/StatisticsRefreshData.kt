/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics.refresh

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import java.util.concurrent.ConcurrentHashMap

abstract class StatisticsRefreshData: StatisticsRefresh {
    protected class RefreshInfo(val rate: Long, val timerRef: Long?, val paused: Boolean)

    private var categoryInfo = ConcurrentHashMap<CategoryID, StickyLiveData<RefreshInfo>>()

    private val DEFAULT_RATE_IN_MS: Long = 5000

    override fun isRefreshing(category: CategoryID): Boolean {
        val entry = categoryInfo[category]?.value
        if (entry != null) {
            return entry.timerRef != null
        }
        return false
    }

    protected fun isPaused(category: CategoryID): Boolean {
        val entry = categoryInfo[category]?.value
        return entry?.paused ?: false
    }

    protected fun getEntry(category: CategoryID): RefreshInfo {
        return categoryInfo[category]?.value ?: RefreshInfo(DEFAULT_RATE_IN_MS, null, false)
    }

    protected fun updateEntry(category: CategoryID, entry: RefreshInfo) {
        categoryInfo[category] = StickyLiveData<RefreshInfo>(entry)
    }

    override fun stopAllRefreshing() {
        val keys = categoryInfo.keys
        keys.forEach {
            stopRefreshing(it)
        }
    }

    abstract fun pause(category: CategoryID)

    override fun pauseAllRefreshing() {
        val keys = categoryInfo.keys
        keys.forEach {
            if (isRefreshing(it) && !isPaused(it)) {
                pause(it)
            }
        }
    }

    override fun resumePaused(repository: StatisticsRepository) {
        // Nothing to do
        val keys = categoryInfo.keys
        keys.forEach {
            if (isPaused(it) && !isRefreshing(it)) {
                startRefreshing(it, repository)
            }
        }
    }
}