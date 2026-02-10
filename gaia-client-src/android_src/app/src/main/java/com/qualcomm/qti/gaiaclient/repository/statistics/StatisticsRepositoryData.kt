/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.collection.ArrayMap
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticDescriptor
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticValue
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository
import com.qualcomm.qti.gaiaclient.repository.statistics.recording.StatisticsRecording
import com.qualcomm.qti.gaiaclient.repository.statistics.recording.StatisticsRecordingImpl
import com.qualcomm.qti.gaiaclient.repository.statistics.refresh.StatisticsRefresh
import com.qualcomm.qti.gaiaclient.repository.statistics.refresh.StatisticsRefreshImpl
import com.qualcomm.qti.gaiaclient.ui.common.Observers
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.category.StatisticsStatisticsViewData
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StatisticsCategories
import java.io.File
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

abstract class StatisticsRepositoryData(private val context: Context?) : StatisticsRepository {
    private var recorder: StatisticsRecording = StatisticsRecordingImpl()
    private var refresher: StatisticsRefresh = StatisticsRefreshImpl()

    private val _categoriesLiveData =
        StickyLiveData<List<StatisticsCategoriesCategoryInfo>>()
    private val _statisticsValuesLiveData =
        ConcurrentHashMap<CategoryID, StickyLiveData<ConcurrentHashMap<StatisticID, ByteArray>>>()

    override val categoriesLiveData: LiveData<List<StatisticsCategoriesCategoryInfo>> = _categoriesLiveData

    private var featuresRepository: FeaturesRepository? = null
    private val observer = DataObservers()

    private val connectivityManager: ConnectivityManager? =
        context?.getSystemService(ConnectivityManager::class.java)

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            networkCapabilities
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                updateStatistics(
                    mapOf(
                        Pair(
                            CategoryID(1), listOf(
                                StatisticValue(
                                    StatisticDescriptor(
                                        CategoryID(1),
                                        StatisticID(999)
                                    ),
                                    ByteBuffer.allocate(Int.SIZE_BYTES).putInt(networkCapabilities.signalStrength)
                                        .array()
                                )
                            )
                        )
                    )
                )
            } else {
                // Nothing
            }
        }
    }

    override fun init(featuresRepository: FeaturesRepository) {
        this.featuresRepository = featuresRepository
        observer.start()
    }

    override fun registerConnectionCallbacks() {
        connectivityManager?.registerDefaultNetworkCallback(networkCallback)
    }

    override fun unregisterConnectionCallbacks() {
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private fun onFeatures(features: ArrayMap<QTILFeature, Int>?) {
        val supported = features != null && features.containsKey(QTILFeature.STATISTICS)
        if (supported) {
            refresher.resumePaused(this)
        }
    }

    override fun reset(hardReset: Boolean) {
        _categoriesLiveData.value = emptyList() // must be consumed
        _statisticsValuesLiveData.clear()

        if (hardReset) {
            recorder.stopAllRecording()
            refresher.stopAllRefreshing()
        } else {
            refresher.pauseAllRefreshing()
        }
    }

    override fun getCategoryLiveData(category: CategoryID): LiveData<ConcurrentHashMap<StatisticID, ByteArray>> =
        getStatsValuesLiveData(category)

    protected fun updateCategories(categories: List<CategoryID>) {
        _categoriesLiveData.value = categories.mapNotNull {
            val category = StatisticsCategories.fromIdentifier(it.value) ?: return@mapNotNull null
            return@mapNotNull StatisticsCategoriesCategoryInfo(category, isRecording(it))
        }
    }

    private fun updateCategoriesWithRecording() {
        val oldData = _categoriesLiveData.value ?: emptyList()
        val ids = oldData.map {
            return@map CategoryID(it.category.identifier)
        }
        updateCategories(ids)
    }

    protected fun updateStatistics(statistics: Map<CategoryID, List<StatisticValue>>) {
        statistics.forEach { group ->
            val categoryID = group.key
            val statisticValues = group.value

            val liveDataForCategory = getStatsValuesLiveData(categoryID)
            val statisticMapForCategory = liveDataForCategory.value ?: ConcurrentHashMap()
            statisticValues.forEach { statisticValue ->
                statisticMapForCategory[statisticValue.descriptor.statistic] = statisticValue.value
            }
            liveDataForCategory.postValue(statisticMapForCategory)

            if (recorder.isRecording(categoryID)) {
                if (context != null) {
                    logRow(context, categoryID)
                }
            }
        }
    }

    override fun getCategoriesInfo(): List<StatisticsCategoriesCategoryInfo> {
        return _categoriesLiveData.value ?: emptyList()
    }

    override fun getStatisticValue(descriptor: StatisticDescriptor): StatisticValue? {
        val liveDataForCategory = getStatsValuesLiveData(descriptor.category)
        val statisticMapForCategory = liveDataForCategory.value ?: ConcurrentHashMap()
        val value = statisticMapForCategory[descriptor.statistic]
        if (null != value) {
            return StatisticValue(descriptor, value)
        }
        return null
    }

    private fun getStatsValuesLiveData(category: CategoryID): StickyLiveData<ConcurrentHashMap<StatisticID,
            ByteArray>> {
        var liveData = _statisticsValuesLiveData[category]
        if (null == liveData) {
            liveData =
                StickyLiveData<ConcurrentHashMap<StatisticID,
                        ByteArray>>()
            _statisticsValuesLiveData[category] = liveData
        }
        return liveData
    }

    // Recording

    override fun isRecording(): Boolean {
        return recorder.isRecording()
    }

    override fun isRecording(category: CategoryID): Boolean {
        return recorder.isRecording(category)
    }

    override fun startRecording(context: Context, category: CategoryID): Boolean {
        val success = recorder.startRecording(context, category)
        updateCategoriesWithRecording()
        return success
    }

    override fun stopRecording(category: CategoryID): File? {
        val file = recorder.stopRecording(category)
        updateCategoriesWithRecording()
        return file
    }

    override fun stopAllRecording(): List<File> {
        val files = recorder.stopAllRecording()
        updateCategoriesWithRecording()
        return files
    }

    override fun eraseAllExpired(context: Context) {
        recorder.eraseAllExpired(context)
    }

    private fun logRow(
        context: Context,
        category: CategoryID
    ): Boolean {
        val value = getStatsValuesLiveData(category).value ?: ConcurrentHashMap()
        val hashMap = HashMap<StatisticID, ByteArray>()
        hashMap.putAll(value)
        return recorder.logRow(context, category, hashMap)
    }

    // Refresh

    override fun startRefreshing(category: CategoryID) {
        refresher.startRefreshing(category, this)
    }

    override fun stopRefreshing(category: CategoryID) {
        refresher.stopRefreshing(category)
    }

    override fun stopAllRefreshing() {
        refresher.stopAllRefreshing()
    }

    override fun isRefreshing(category: CategoryID): Boolean {
        return refresher.isRefreshing(category)
    }

    override fun refreshRate(category: CategoryID): Long {
        return refresher.refreshRate(category)
    }

    override fun adjustRefreshRate(refreshRateMS: Long, category: CategoryID) {
        return refresher.adjustRefreshRate(refreshRateMS, category, this)
    }

    private inner class DataObservers : Observers() {
        private val featuresObserver = this@StatisticsRepositoryData::onFeatures

        override fun registerObservers() {
            featuresRepository?.featuresLiveData?.observeForever(featuresObserver)
        }

        override fun unregisterObservers() {
            featuresRepository?.featuresLiveData?.removeObserver(featuresObserver)
        }
    }
}
