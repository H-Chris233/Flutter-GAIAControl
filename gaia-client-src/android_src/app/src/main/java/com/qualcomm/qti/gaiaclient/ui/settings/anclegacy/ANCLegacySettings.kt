/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.anclegacy

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings

enum class ANCLegacySettings(override val resourceId: Int) : Settings {
    STATE(R.string.settings_id_anc_legacy_state),
    ADAPTIVE_STATE(R.string.settings_id_anc_legacy_adaptive_state),
    MODE(R.string.settings_id_anc_legacy_mode),
    ADAPTED_GAIN(R.string.settings_id_anc_legacy_adapted_gain),
    LEAKTHROUGH_GAIN(R.string.settings_id_anc_legacy_leakthrough_gain),
    CATEGORY_STATE_DEPENDANT(R.string.settings_id_anc_legacy_state_dependant_settings);
}
