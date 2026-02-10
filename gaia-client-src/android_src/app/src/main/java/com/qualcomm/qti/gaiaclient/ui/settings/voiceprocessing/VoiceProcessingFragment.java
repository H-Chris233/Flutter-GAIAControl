/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.voiceprocessing;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCBypassMode;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VoiceProcessingFragment
        extends SettingsFragment<VoiceProcessingSettings, VoiceProcessingViewData, VoiceProcessingViewModel> {

    @Override
    protected int getXmlSettingsId() {
        return R.xml.voice_processing_settings;
    }

    @Override
    protected VoiceProcessingViewModel initViewModel() {
        return new ViewModelProvider(this).get(VoiceProcessingViewModel.class);
    }

    @Override
    protected VoiceProcessingSettings[] getSettings() {
        return VoiceProcessingSettings.values();
    }

    @Override
    protected boolean onPreferenceChange(VoiceProcessingSettings key, Preference preference, Object update) {
        if (VoiceProcessingSettings.MICROPHONE_MODE.equals(key) && update instanceof String) {
            // MicConfig config = MicConfig.valueOf((String) update);
            int mode = Integer.parseInt((String) update);
            getViewModel().setMicrophoneMode(mode);
            return false; // view model will update the state
        }
        else if (VoiceProcessingSettings.BYPASS_MODE.equals(key) && update instanceof String) {
            CVCBypassMode mode = CVCBypassMode.valueOf((String) update);
            getViewModel().setBypassMode(mode);
            return false; // view model will update the state
        }

        return true;
    }

}
