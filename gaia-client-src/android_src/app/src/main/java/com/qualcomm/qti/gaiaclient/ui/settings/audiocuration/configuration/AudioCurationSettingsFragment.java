/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.configuration;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Scenario;
import com.qualcomm.qti.gaiaclient.ui.Navigator;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.Configuration;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AudioCurationSettingsFragment
        extends SettingsFragment<AudioCurationSettings, AudioCurationSettingsViewData, AudioCurationSettingsViewModel> {

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().updateData();
    }

    @Override
    protected int getXmlSettingsId() {
        return R.xml.audio_curation_settings;
    }

    @Override
    protected AudioCurationSettingsViewModel initViewModel() {
        return new ViewModelProvider(this).get(AudioCurationSettingsViewModel.class);
    }

    @Override
    protected AudioCurationSettings[] getSettings() {
        return AudioCurationSettings.values();
    }

    @Override
    protected boolean onPreferenceChange(AudioCurationSettings key, Preference preference, Object update) {
        if (AudioCurationSettings.ANC_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setState(Feature.ANC, (boolean) update);
            return false; // view model will update the state depending on request result
        }
        else if (AudioCurationSettings.AUTO_TRANSPARENCY_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setAutoTransparencyState((boolean) update);
            return false; // view model will update the state depending on request result
        }
        else if (AudioCurationSettings.NOISE_ID_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setNoiseIdState((boolean) update);
            return false; // view model will update the state depending on request result
        }
        else if (key != null && key.isConfiguration() && update instanceof String) {
            Configuration configuration = key.getConfiguration();
            int selection = Integer.parseInt((String) update);
            ACInfo info = configuration != null ? configuration.getInfo() : null;
            if (info == ACInfo.TOGGLE_CONFIGURATION) {
                getViewModel().setToggleConfiguration(key.getToggle(), selection);
            }
            else if (info == ACInfo.SCENARIO_CONFIGURATION) {
                Scenario scenario = key.getScenario();
                if (scenario != null) {
                    getViewModel().setScenarioConfiguration(scenario.getValue(), selection);
                }

            }
            return false; // view model will update the state depending on request result
        }
        else if (AudioCurationSettings.AUTO_TRANSPARENCY_RELEASE_TIME.equals(key) && update instanceof String) {
            ReleaseTime releaseTime = null;
            try {
                releaseTime = ReleaseTime.valueOf((String) update);
            }
            catch (Exception ignored) {
            }

            if (releaseTime != null) {
                getViewModel().setAutoTransparencyReleaseTime(releaseTime.getTime());
            }
            return false; // view model will update the state depending on request result
        }

        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (isKey(key, AudioCurationSettings.ENTER_DEMO)) {
            Navigator.navigate(getRoot(), AudioCurationSettingsFragmentDirections.toAudioCurationDemo());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }
}
