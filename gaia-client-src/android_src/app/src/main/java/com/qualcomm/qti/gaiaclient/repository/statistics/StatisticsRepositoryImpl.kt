/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics

import android.content.Context
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.data.StatisticsInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticDescriptor
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticValue
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.StatisticsSubscriber
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetStatisticsRequest
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepositoryImpl
@Inject constructor(@ApplicationContext private val context: Context) : StatisticsRepositoryData(context) {

    private val mSubscriber: StatisticsSubscriber = object : StatisticsSubscriber {
        override fun onCategories(categories: List<CategoryID>) {
            updateCategories(categories)
        }

        override fun onStatistics(statistics: Map<CategoryID, List<StatisticValue>>) {
            updateStatistics(statistics)
        }

        override fun onError(info: StatisticsInfo?, reason: Reason?) {
        }
    }

    override fun init(featuresRepository: FeaturesRepository){
        super.init(featuresRepository)

        val publicationManager = GaiaClientService.getPublicationManager()
        publicationManager.subscribe(mSubscriber)
    }

    override fun fetchCategories() {
        val request = GetStatisticsRequest(StatisticsInfo.CATEGORIES)
        GaiaClientService.getRequestManager().execute(null, request)
    }

    override fun fetchStatistics(category: CategoryID) {
        val request = GetStatisticsRequest(StatisticsInfo.ALL_STATISTICS_IN_CATEGORY, category)
        GaiaClientService.getRequestManager().execute(null, request)
    }

    override fun fetchStatistics(descriptors: List<StatisticDescriptor>) {
        val request = GetStatisticsRequest(StatisticsInfo.STATISTIC_VALUES, descriptors)
        GaiaClientService.getRequestManager().execute(null, request)
    }
}
