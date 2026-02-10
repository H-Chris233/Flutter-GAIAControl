/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils
import java.util.Objects

class StatisticDescriptor(val category: CategoryID, val statistic: StatisticID) {
    private val BYTE_ARRAY_SIZE = 3
    private val BYTE_ARRAY_CATEGORY_OFFSET = 0
    private val BYTE_ARRAY_STATISTIC_OFFSET = 2

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || javaClass != other.javaClass) {
            return false
        }
        val o = other as StatisticDescriptor
        return category == o.category &&
                statistic == o.statistic
    }

    override fun hashCode(): Int {
        return Objects.hash(category, statistic)
    }

    fun toByteArray(): ByteArray {
        val ret = ByteArray(BYTE_ARRAY_SIZE)
        BytesUtils.setUINT16(category.value, ret, BYTE_ARRAY_CATEGORY_OFFSET)
        BytesUtils.setUINT8(statistic.value, ret, BYTE_ARRAY_STATISTIC_OFFSET)
        return ret
    }
}
