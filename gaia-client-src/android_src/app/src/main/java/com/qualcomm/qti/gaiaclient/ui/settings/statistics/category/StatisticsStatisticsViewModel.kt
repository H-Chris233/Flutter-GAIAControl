/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.category

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.GaiaClientApplication
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.XpanTransport
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository
import com.qualcomm.qti.gaiaclient.repository.statistics.StatisticsRepository
import com.qualcomm.qti.gaiaclient.repository.xpan.XpanRepository
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StatisticsCategories
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StreamingStatistics
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

@HiltViewModel
class StatisticsStatisticsViewModel @Inject constructor(
    application: Application,
    private val statisticsRepository: StatisticsRepository,
    private val featuresRepository: FeaturesRepository,
    private val xpanRepository: XpanRepository
) : AndroidViewModel(application) {

    var categoryID: CategoryID? = null
        private set

    private val _viewData = StickyLiveData<StatisticsStatisticsViewData>()
    val viewData: LiveData<StatisticsStatisticsViewData> = _viewData

    private val _title = StickyLiveData<String>()
    val title: LiveData<String> = _title

    var isFetched: Boolean = false
        private set

    private val observers: DataObservers = DataObservers()

    init {
        observers.start()
        statisticsRepository.registerConnectionCallbacks()
    }

    override fun onCleared() {
        super.onCleared()
        observers.stop()
        statisticsRepository.unregisterConnectionCallbacks()
    }

    fun setCategoryID(update: CategoryID) {
        val context = getApplication<GaiaClientApplication>().applicationContext
        categoryID = update
        val categoryInfo = StatisticsCategories.fromIdentifier(update.value) ?: return

        _title.postValue(categoryInfo.getTitle(context))

        observers.registerCategoryObserver(update)
    }

    fun resume() {
        val categoryID = categoryID ?: return
        if (!statisticsRepository.isRefreshing(categoryID)) {
            statisticsRepository.startRefreshing(categoryID)
        }
    }

    fun pause() {
        val categoryID = categoryID ?: return
        if (!statisticsRepository.isRecording(categoryID)) {
            statisticsRepository.stopRefreshing(categoryID)
        }
    }

    fun refreshRateMsecs(): Long {
        val categoryID = categoryID ?: return 0
        return statisticsRepository.refreshRate(categoryID)
    }

    fun setRefreshRate(mSecs: Long) {
        val categoryID = categoryID ?: return
        statisticsRepository.adjustRefreshRate(mSecs, categoryID)
    }

    fun isRecording(): Boolean {
        val categoryID = categoryID ?: return false
        return statisticsRepository.isRecording(categoryID)
    }

    fun startRecording() {
        val context = getApplication<GaiaClientApplication>().applicationContext
        val categoryID = categoryID

        if (context != null && categoryID != null) {
            statisticsRepository.startRecording(context, categoryID)
        }
    }

    fun stopRecording(): File? {
        val categoryID = categoryID ?: return null
        return statisticsRepository.stopRecording(categoryID)
    }

    private fun onStatistics(category: CategoryID, statistics: ConcurrentHashMap<StatisticID, ByteArray>?) {
        if (category != categoryID) return

        isFetched = true

        val context = getApplication<GaiaClientApplication>().applicationContext
        val categoryInfo = StatisticsCategories.fromIdentifier(category.value) ?: return
        val possibleStatistics = categoryInfo.allStatistics

        val transformed = statistics?.mapNotNull {
            val statisticID = it.key
            val value = it.value
            if (value.isEmpty()) {
                return@mapNotNull null
            }

            val statisticInfo =
                possibleStatistics.firstOrNull { info -> statisticID.value == info.identifier }
                    ?: return@mapNotNull null
            val title = statisticInfo.getTitle(context)
            val subtitle = statisticInfo.getValue(context, value) ?: return@mapNotNull null

            return@mapNotNull StatisticViewData(title, subtitle, statisticID.value)
        } ?: emptyList()

        val update = (_viewData.value?.statistics?.associateBy { it.id }?.toMutableMap() ?:
        mutableMapOf())

        update.putAll(transformed.associateBy { it.id }.toMutableMap())

        val sorted = update.values.toMutableList()

        // Workaround to always display transport buttons
        if(sorted.none { it.id == StreamingStatistics.TRANSPORT.identifier}) {
            sorted.add(StatisticViewData("", "", StreamingStatistics.TRANSPORT.identifier))
        }

        val newData = StatisticsStatisticsViewData(
            isRecording(),
            sorted.sortedBy { it.title }
        )
        _viewData.postValue(newData)
    }

    fun isSupported(feature: QTILFeature): Boolean {
        return featuresRepository.isFeatureAvailable(feature)
    }

    fun toggleXpanTransport() {
        xpanRepository.toggleTransport()
    }

    fun setXpanTransport(transport: XpanTransport) {
        xpanRepository.setTransport(transport)
    }

    fun setAPConnectionState(connect: Boolean) {
        if(connect) xpanRepository.connectAP() else xpanRepository.disconnectAP()
    }

    private inner class DataObservers : Observers() {
        private val categoriesObservers: MutableMap<CategoryID, Observer<ConcurrentHashMap<StatisticID, ByteArray>>> =
            mutableMapOf()

        override fun registerObservers() {
        }

        fun registerCategoryObserver(category: CategoryID) {
            val observer: Observer<ConcurrentHashMap<StatisticID, ByteArray>> = Observer { statistics ->
                this@StatisticsStatisticsViewModel.onStatistics(category, statistics)
            }
            if (categoriesObservers.putIfAbsent(category, observer) == null) {
                statisticsRepository.getCategoryLiveData(category).observeForever(observer)
            }
        }

        override fun unregisterObservers() {
            categoriesObservers.forEach { (category, observer) ->
                statisticsRepository.getCategoryLiveData(category).removeObserver(observer)
            }
        }
    }
}
