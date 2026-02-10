/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.categories

import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterItemData
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StatisticsCategories
import java.util.Objects

class CategoryViewData : ListAdapterItemData {
    val label: String
    val isRecording: Boolean
    val categoryInfo: StatisticsCategories

    constructor(title: String, isRecording: Boolean, categoryInfo: StatisticsCategories) {
        this.label = title
        this.isRecording = isRecording
        this.categoryInfo = categoryInfo
    }

    constructor(data: CategoryViewData) {
        this.label = data.label
        this.isRecording = data.isRecording
        this.categoryInfo = data.categoryInfo
    }

    override fun isSameContent(itemData: ListAdapterItemData): Boolean {
        if (this === itemData) {
            return true
        }
        if (javaClass != itemData.javaClass) {
            return false
        }
        val that = itemData as CategoryViewData
        return that.categoryInfo == categoryInfo && that.isRecording == isRecording
    }

    override fun isSameItem(itemData: ListAdapterItemData): Boolean {
        return this == itemData
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val that = other as CategoryViewData
        return that.categoryInfo == categoryInfo
    }

    override fun hashCode(): Int {
        return Objects.hash(categoryInfo)
    }
}
