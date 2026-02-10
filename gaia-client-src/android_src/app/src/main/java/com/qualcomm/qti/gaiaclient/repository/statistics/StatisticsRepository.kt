/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics

import android.content.Context
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticDescriptor
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticValue
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StatisticsCategories
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class StatisticsCategoriesCategoryInfo(
    val category: StatisticsCategories,
    val recordingStatus: Boolean)

interface StatisticsRepository {

    val categoriesLiveData: LiveData<List<StatisticsCategoriesCategoryInfo>>
    fun getCategoryLiveData(category: CategoryID): LiveData<ConcurrentHashMap<StatisticID, ByteArray>>

    fun init(featuresRepository: FeaturesRepository)
    fun registerConnectionCallbacks()
    fun unregisterConnectionCallbacks()
    fun reset(hardReset: Boolean)

    fun fetchCategories()
    fun fetchStatistics(category: CategoryID)
    fun fetchStatistics(descriptors: List<StatisticDescriptor>)

    fun getCategoriesInfo(): List<StatisticsCategoriesCategoryInfo>
    fun getStatisticValue(descriptor: StatisticDescriptor): StatisticValue?

    // Recording

    fun isRecording(): Boolean
    fun isRecording(category: CategoryID): Boolean
    fun startRecording(context: Context, category: CategoryID): Boolean
    fun stopRecording(category: CategoryID): File?
    fun stopAllRecording(): List<File>
    fun eraseAllExpired(context: Context)

    // Refreshing

    fun startRefreshing(category: CategoryID)
    fun stopRefreshing(category: CategoryID)
    fun stopAllRefreshing()

    fun isRefreshing(category: CategoryID) : Boolean

    fun refreshRate(category: CategoryID) : Long
    fun adjustRefreshRate(refreshRateMS: Long, category: CategoryID)
}
