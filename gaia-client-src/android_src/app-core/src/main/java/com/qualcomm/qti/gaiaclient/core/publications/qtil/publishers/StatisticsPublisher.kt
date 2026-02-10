/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers

import com.qualcomm.qti.gaiaclient.core.data.Reason
import com.qualcomm.qti.gaiaclient.core.data.StatisticsInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticValue
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.StatisticsSubscriber

class StatisticsPublisher : Publisher<StatisticsSubscriber>() {
    override fun getSubscription(): Subscription {
        return CoreSubscription.STATISTICS
    }

    fun publishCategories(categories: List<CategoryID>) {
        forEachSubscriber { subscriber -> subscriber.onCategories(categories) }
    }

    fun publishStatistics(statistics: Map<CategoryID, List<StatisticValue>>) {
        forEachSubscriber { subscriber -> subscriber.onStatistics(statistics) }
    }

    fun publishError(info: StatisticsInfo?, reason: Reason?) {
        forEachSubscriber { subscriber -> subscriber.onError(info, reason) }
    }
}
