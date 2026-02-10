/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings

enum class AudioCurationDemoSettings(override val resourceId: Int) : Settings {
    ANC_STATE(R.string.settings_id_audio_curation_demo_anc_state),
    MODE(R.string.settings_id_audio_curation_demo_mode),
    MEDIA(R.string.settings_id_audio_curation_demo_media),
    LEAKTHROUGH_GAIN(R.string.settings_id_audio_curation_demo_leakthrough_gain),
    ADAPTATION(R.string.settings_id_audio_curation_demo_adaptation),
    FEED_FORWARD_GAIN(R.string.settings_id_audio_curation_demo_feed_forward_gain),
    LEAKTHROUGH_GAIN_STEPPER(R.string.settings_id_audio_curation_demo_leakthrough_gain_stepper),
    LEFT_RIGHT_BALANCE(R.string.settings_id_audio_curation_demo_left_right_balance),
    WIND_NOISE_DETECTION_STATE(R.string.settings_id_audio_curation_demo_wind_noise_detection_state),
    WIND_NOISE_REDUCTION(R.string.settings_id_audio_curation_demo_wind_noise_reduction),
    HOWLING_DETECTION_STATE(R.string.settings_id_audio_curation_demo_howling_detection_state),
    FEEDBACK_GAIN(R.string.settings_id_audio_curation_demo_feedback_gain),
    NOISE_ID_CATEGORY(R.string.settings_id_audio_curation_demo_noise_id_category),
    ADVERSE_ACOUSTIC_STATE(R.string.settings_id_audio_curation_demo_adverse_acoustic_state),
    ADVERSE_ACOUSTIC_GAIN_REDUCTION(R.string.settings_id_audio_curation_demo_adverse_acoustic_gain_reduction),
    HOWLING_CONTROL_GAIN_REDUCTION(R.string.settings_id_audio_curation_demo_howling_control_gain_reduction);
}
