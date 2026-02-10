/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics.refresh

import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository

class StatisticsRefreshImpl: StatisticsRefreshData() {
    override fun startRefreshing(category: CategoryID, repository: StatisticsRepository) {
        if (isRefreshing(category)) {
            return
        }
        repository.fetchStatistics(category)
        val entry = getEntry(category)
        setUpTimedTask(category, entry.rate, repository)
    }

    override fun stopRefreshing(category: CategoryID) {
        stopRefreshing(category, false)
    }

    override fun pause(category: CategoryID) {
        stopRefreshing(category, true)
    }

    private fun stopRefreshing(category: CategoryID, leavePaused: Boolean) {
        if (!isRefreshing(category) && !isPaused(category)) {
            return
        }
        val oldEntry = getEntry(category)
        val timerRef = oldEntry.timerRef
        if (timerRef != null) {
            GaiaClientService.getTaskManager().cancelScheduled(timerRef)
        }
        val newEntry = RefreshInfo(oldEntry.rate, null, leavePaused)
        updateEntry(category, newEntry)
    }

    override fun refreshRate(category: CategoryID): Long {
        val entry = getEntry(category)
        return entry.rate
    }

    override fun adjustRefreshRate(refreshRateMS: Long, category: CategoryID, repository: StatisticsRepository) {
        if (isRefreshing(category)) {
            stopRefreshing(category)
            repository.fetchStatistics(category)
            setUpTimedTask(category, refreshRateMS, repository)
        } else {
            val oldEntry = getEntry(category)
            updateEntry(category, RefreshInfo(refreshRateMS, null, oldEntry.paused))
        }
    }

    private fun setUpTimedTask(category: CategoryID, rate: Long, repository: StatisticsRepository) {
        val newTimerRef = GaiaClientService.getTaskManager().schedule(
            object : Runnable {
                override fun run() {
                    repository.fetchStatistics(category)
                    val newRef = GaiaClientService.getTaskManager().schedule(this, rate)
                    val newEntry = RefreshInfo(rate, newRef, false)
                    updateEntry(category, newEntry)
                }
            },
            rate
        )

        val newEntry = RefreshInfo(rate, newTimerRef, false)
        updateEntry(category, newEntry)
    }
}
