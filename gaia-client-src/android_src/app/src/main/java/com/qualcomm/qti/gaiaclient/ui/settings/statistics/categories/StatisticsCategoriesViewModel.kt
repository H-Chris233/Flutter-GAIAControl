/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.categories

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsCategoriesCategoryInfo
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject

@HiltViewModel
class StatisticsCategoriesViewModel @Inject constructor(
    application: Application,
    private val statisticsRepository: StatisticsRepository
) : AndroidViewModel(application) {

    private val _viewData: StickyLiveData<StatisticsCategoriesViewData> = StickyLiveData()
    val viewData: LiveData<StatisticsCategoriesViewData> = _viewData

    var isFetched: Boolean = false
        private set

    private val observers: DataObservers = DataObservers()

    init {
        observers.start()
    }

    override fun onCleared() {
        super.onCleared()
        observers.stop()
    }

    fun fetchData() {
        statisticsRepository.fetchCategories()
    }

    fun stopAllRecording(): List<File> {
        val files = statisticsRepository.stopAllRecording()
        statisticsRepository.stopAllRefreshing()
        return files
    }

    private fun onCategories(categoryInfo: List<StatisticsCategoriesCategoryInfo>?) {
        isFetched = true
        val newData = StatisticsCategoriesViewData(
            statisticsRepository.isRecording(),
            categoryInfo ?: emptyList()
        )
        _viewData.postValue(newData)
    }

    private inner class DataObservers : Observers() {
        private val categoriesObserver = this@StatisticsCategoriesViewModel::onCategories

        override fun registerObservers() {
            statisticsRepository.categoriesLiveData.observeForever(categoriesObserver)
        }

        override fun unregisterObservers() {
            statisticsRepository.categoriesLiveData.removeObserver(categoriesObserver)
        }
    }
}
