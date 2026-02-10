/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.main;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant;
import com.qualcomm.qti.gaiaclient.ui.Navigator;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainSettingsFragment extends SettingsFragment<MainSettings, MainSettingsViewData, MainSettingsViewModel> {

    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // this prevents the navigation to come back to another screen while it is upgrading
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, mBackPressedCallback);
    }

    @Override
    protected int getXmlSettingsId() {
        return R.xml.main_settings;
    }

    @Override
    protected MainSettingsViewModel initViewModel() {
        return new ViewModelProvider(this).get(MainSettingsViewModel.class);
    }

    @Override
    protected MainSettings[] getSettings() {
        return MainSettings.values();
    }

    @Override
    protected boolean onPreferenceChange(MainSettings key, Preference preference, Object update) {
        if (MainSettings.VOICE_UI.equals(key) && update instanceof String) {
            try {
                VoiceAssistant assistant = VoiceAssistant.valueOf(Integer.parseInt((String) update));
                getViewModel().setAssistant(assistant);
            }
            catch (Exception ignored) {
            }
            return false;
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().onResumed();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();

        if (isKey(key, MainSettings.ANC)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toAncLegacy());
            return true;
        }
        else if (isKey(key, MainSettings.AUDIO_CURATION)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toAudioCurationSettings());
            return true;
        }
        else if (isKey(key, MainSettings.MUSIC_PROCESSING)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toMusicProcessing());
            return true;
        }
        else if (isKey(key, MainSettings.UPGRADE)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toUpgradeSettings());
            return true;
        }
        else if (isKey(key, MainSettings.LOGS)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toLogsSettings());
            return true;
        }
        else if (isKey(key, MainSettings.EARBUD_FIT)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toEarbudFit());
            return true;
        }
        else if (isKey(key, MainSettings.HANDSET_SERVICE)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toHandsetService());
            return true;
        }
        else if (isKey(key, MainSettings.VOICE_PROCESSING)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toVoiceProcessing());
            return true;
        }
        else if (isKey(key, MainSettings.GESTURES)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toGestures());
            return true;
        }
        else if (isKey(key, MainSettings.STATISTICS)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toStatistics());
            return true;
        }
        else if (isKey(key, MainSettings.FEEDBACK)) {
            Navigator.navigate(getRoot(), MainSettingsFragmentDirections.toFeedbackForm());
            return true;
        }
        else if (isKey(key, MainSettings.CHANGE_DEVICE)) {
            disconnect();
            return true;
        }
        return false;
    }

    private void disconnect() {
        getViewModel().disconnect();
        Navigator.navigate(getRoot(), MainSettingsFragmentDirections.back());
    }

}
