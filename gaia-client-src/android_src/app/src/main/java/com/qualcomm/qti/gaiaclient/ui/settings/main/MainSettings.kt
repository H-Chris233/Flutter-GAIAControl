/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.main

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings

enum class MainSettings(override val resourceId: Int, val requireConnected: Boolean) : Settings {
    ANC(R.string.settings_id_anc_legacy, true),
    AUDIO_CURATION(R.string.settings_id_audio_curation, true),
    VOICE_UI(R.string.settings_id_voice_ui, true),
    UPGRADE(R.string.settings_id_upgrade, true),
    MUSIC_PROCESSING(R.string.settings_id_music_processing, true),
    LOGS(R.string.settings_id_logs, true),
    EARBUD_FIT(R.string.settings_id_earbud_fit, true),
    HANDSET_SERVICE(R.string.settings_id_handset_service, true),
    VOICE_PROCESSING(R.string.settings_id_voice_processing, true),
    GESTURES(R.string.settings_id_gestures, true),
    STATISTICS(R.string.settings_id_statistics, true),
    CHANGE_DEVICE(R.string.settings_id_change_device, false),
    GAIA_CLIENT_VERSION(R.string.settings_id_gaia_client_version, false),
    DEVICE_FEATURES(R.string.settings_id_device_features, false),
    FEEDBACK(R.string.settings_id_feedback, false);
}
