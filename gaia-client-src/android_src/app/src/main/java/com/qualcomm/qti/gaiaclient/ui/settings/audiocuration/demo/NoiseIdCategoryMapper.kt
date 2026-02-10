/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.NoiseIdCategory

object NoiseIdCategoryMapper {
    fun getLabel(context: Context, category: NoiseIdCategory?): String =
        when (category) {
            NoiseIdCategory.CategoryA -> context.getString(R.string.settings_audio_curation_demo_noise_id_category_a)
            NoiseIdCategory.CategoryB -> context.getString(R.string.settings_audio_curation_demo_noise_id_category_b)
            NoiseIdCategory.NotApplicable ->
                context.getString(R.string.settings_audio_curation_demo_noise_id_category_na)
            is NoiseIdCategory.Undefined ->
                context.getString(R.string.settings_audio_curation_demo_noise_id_category_undefined, category.id)
            else -> context.getString(R.string.settings_audio_curation_demo_noise_id_category_empty)
        }
}
