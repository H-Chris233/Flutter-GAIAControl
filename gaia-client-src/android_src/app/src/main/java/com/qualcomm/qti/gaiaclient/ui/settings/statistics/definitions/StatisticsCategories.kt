/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions

import android.content.Context
import com.qualcomm.qti.gaiaclient.R

interface CategoryStatisticSettings {
    val identifier: Int
    fun getTitle(context: Context?): String
    fun getValue(context: Context?, data: ByteArray): String?
}

enum class StatisticsCategories(
    val identifier: Int,
    val allStatistics: List<CategoryStatisticSettings>,
    private val labelId: Int
) {
    STREAMING(1, StreamingStatistics.values().toList(), R.string.statistics_streaming_category_title),
    SPATIAL_AUDIO(2, SpatialAudioStatistics.values().toList(), R.string.statistics_spatial_audio_category_title);

    fun getTitle(context: Context?): String = context?.getString(labelId) ?: ""

    companion object {
        fun fromIdentifier(identifier: Int): StatisticsCategories? {
            return values().firstOrNull { it.identifier == identifier }
        }
    }
}
