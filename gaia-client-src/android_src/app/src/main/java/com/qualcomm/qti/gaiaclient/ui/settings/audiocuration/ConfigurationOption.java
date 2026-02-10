/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.R;

public enum ConfigurationOption {
    TOGGLE_OFF(0x00),
    CHANGE_TO_OFF(0x00),
    DISABLED(0xFF),
    NO_CHANGE(0xFF);

    private final int value;

    ConfigurationOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public String getLabel(Context context) {
        switch (this) {
            case TOGGLE_OFF:
                return context.getString(R.string.settings_audio_curation_option_toggle_off);
            case CHANGE_TO_OFF:
                return context.getString(R.string.settings_audio_curation_option_change_to_off);
            case DISABLED:
                return context.getString(R.string.settings_audio_curation_option_disabled);
            case NO_CHANGE:
                return context.getString(R.string.settings_audio_curation_option_no_change);
            default: // unexpected
                return context.getString(R.string.settings_audio_curation_option_unknown);

        }
    }
}
