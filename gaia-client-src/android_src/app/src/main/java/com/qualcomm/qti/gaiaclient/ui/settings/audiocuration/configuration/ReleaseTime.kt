/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.configuration

import android.content.Context
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime

enum class ReleaseTime(val time: AutoTransparencyReleaseTime, private val labelId: Int) {
    NO_ACTION(
        AutoTransparencyReleaseTime.NO_ACTION_ON_RELEASE,
        R.string.settings_audio_curation_auto_transparency_release_time_none
    ),
    SHORT(
        AutoTransparencyReleaseTime.SHORT_RELEASE,
        R.string.settings_audio_curation_auto_transparency_release_time_short
    ),
    NORMAL(
        AutoTransparencyReleaseTime.NORMAL_RELEASE,
        R.string.settings_audio_curation_auto_transparency_release_time_normal
    ),
    LONG(
        AutoTransparencyReleaseTime.LONG_RELEASE,
        R.string.settings_audio_curation_auto_transparency_release_time_long
    );

    companion object {
        @JvmStatic
        fun getListEntries(context: Context?): Pair<Array<String>, Array<String>> {
            val enumValues: Array<ReleaseTime> = values()
            val entries: Array<String> = Array(enumValues.size) { i -> enumValues[i].getLabel(context) }
            val values: Array<String> = Array(enumValues.size) { i -> enumValues[i].name }
            return Pair(entries, values)
        }

        @JvmStatic
        fun valueOf(time: AutoTransparencyReleaseTime?): ReleaseTime? =
            values().firstOrNull { value -> value.time == time }
    }

    fun getLabel(context: Context?): String = context?.getString(labelId) ?: ""
}
