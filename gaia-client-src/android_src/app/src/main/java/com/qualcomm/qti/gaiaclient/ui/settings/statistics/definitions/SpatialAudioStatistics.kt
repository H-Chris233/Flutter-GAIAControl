/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

enum class SpatialAudioStatistics(override val identifier: Int, private val titleStringId: Int) :
    CategoryStatisticSettings {
    QUATERNION_W(1, R.string.statistics_spatial_audio_title_quaternion_w),
    QUATERNION_X(2, R.string.statistics_spatial_audio_title_quaternion_x),
    QUATERNION_Y(3, R.string.statistics_spatial_audio_title_quaternion_y),
    QUATERNION_Z(4, R.string.statistics_spatial_audio_title_quaternion_z);

    override fun getTitle(context: Context?): String = context?.getString(titleStringId) ?: ""

    override fun getValue(context: Context?, data: ByteArray): String? =
        if (data.size != BytesUtils.INT16_BYTES_LENGTH) null else BytesUtils.getSINT16(data, 0).toString()

    companion object {
        fun fromIdentifier(identifier: Int): SpatialAudioStatistics? {
            return SpatialAudioStatistics.values().firstOrNull { it.identifier == identifier }
        }
    }
}
