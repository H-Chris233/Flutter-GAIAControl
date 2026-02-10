/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.configuration

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Scenario
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3.V3AudioCurationPlugin.VERSIONS
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.Configuration
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings

enum class AudioCurationSettings(override val resourceId: Int, val featureVersion: Int) : Settings {
    ANC_STATE(R.string.settings_id_audio_curation_anc_state, VERSIONS.V1),
    TOGGLE_CONFIGURATION_1(R.string.settings_id_audio_curation_toggle_1, VERSIONS.V1),
    TOGGLE_CONFIGURATION_2(R.string.settings_id_audio_curation_toggle_2, VERSIONS.V1),
    TOGGLE_CONFIGURATION_3(R.string.settings_id_audio_curation_toggle_3, VERSIONS.V1),
    IDLE_CONFIGURATION(R.string.settings_id_audio_curation_idle_configuration, VERSIONS.V1),
    PLAYBACK_CONFIGURATION(R.string.settings_id_audio_curation_playback_configuration, VERSIONS.V1),
    CALL_CONFIGURATION(R.string.settings_id_audio_curation_calls_configuration, VERSIONS.V1),
    ASSISTANT_CONFIGURATION(R.string.settings_id_audio_curation_assistant_configuration, VERSIONS.V1),
    VOICE_RECORDING_CONFIGURATION(R.string.settings_id_audio_curation_voice_recording_configuration, VERSIONS.V3),
    ENTER_DEMO(R.string.settings_id_audio_curation_enter_demo, VERSIONS.V1),
    AUTO_TRANSPARENCY_CATEGORY(R.string.settings_id_audio_curation_auto_transparency_category, VERSIONS.V4),
    AUTO_TRANSPARENCY_STATE(R.string.settings_id_audio_curation_auto_transparency_state, VERSIONS.V4),
    AUTO_TRANSPARENCY_RELEASE_TIME(R.string.settings_id_audio_curation_auto_transparency_release_time, VERSIONS.V4),
    NOISE_ID_PREF_CATEGORY(R.string.settings_id_audio_curation_noise_id_pref_category, VERSIONS.V6),
    NOISE_ID_STATE(R.string.settings_id_audio_curation_noise_id_state, VERSIONS.V6);

    val configuration: Configuration?
        get() = when (this) {
            TOGGLE_CONFIGURATION_1 -> Configuration.TOGGLE_1
            TOGGLE_CONFIGURATION_2 -> Configuration.TOGGLE_2
            TOGGLE_CONFIGURATION_3 -> Configuration.TOGGLE_3
            IDLE_CONFIGURATION -> Configuration.IDLE
            PLAYBACK_CONFIGURATION -> Configuration.PLAYBACK
            CALL_CONFIGURATION -> Configuration.CALL
            ASSISTANT_CONFIGURATION -> Configuration.ASSISTANT
            VOICE_RECORDING_CONFIGURATION -> Configuration.LE_STEREO_RECORDING
            else -> null
        }

    val toggle: Int
        get() = when (this) {
            TOGGLE_CONFIGURATION_1 -> 1
            TOGGLE_CONFIGURATION_2 -> 2
            TOGGLE_CONFIGURATION_3 -> 3
            else -> 0
        }

    val scenario: Scenario?
        get() = when (this) {
            IDLE_CONFIGURATION -> Scenario.IDLE
            PLAYBACK_CONFIGURATION -> Scenario.PLAYBACK_MUSIC
            CALL_CONFIGURATION -> Scenario.VOICE_CALL
            ASSISTANT_CONFIGURATION -> Scenario.DIGITAL_ASSISTANT
            VOICE_RECORDING_CONFIGURATION -> Scenario.LE_STEREO_RECORDING
            else -> null
        }

    fun isConfiguration(): Boolean {
        return configuration != null
    }

    companion object {
        @JvmStatic
        fun valueOf(configuration: Configuration?): AudioCurationSettings? {
            return when (configuration) {
                Configuration.TOGGLE_1 -> TOGGLE_CONFIGURATION_1
                Configuration.TOGGLE_2 -> TOGGLE_CONFIGURATION_2
                Configuration.TOGGLE_3 -> TOGGLE_CONFIGURATION_3
                Configuration.IDLE -> IDLE_CONFIGURATION
                Configuration.PLAYBACK -> PLAYBACK_CONFIGURATION
                Configuration.CALL -> CALL_CONFIGURATION
                Configuration.ASSISTANT -> ASSISTANT_CONFIGURATION
                Configuration.LE_STEREO_RECORDING -> VOICE_RECORDING_CONFIGURATION
                else -> null
            }
        }
    }
}
