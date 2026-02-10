/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers

import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.data.StatisticsInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticValue
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription

interface StatisticsSubscriber : Subscriber {

    override fun getSubscription() = CoreSubscription.STATISTICS

    fun onCategories(categories: List<CategoryID>)

    fun onStatistics(statistics: Map<CategoryID, List<StatisticValue>>)

    fun onError(info: StatisticsInfo?, reason: Reason?)
}
