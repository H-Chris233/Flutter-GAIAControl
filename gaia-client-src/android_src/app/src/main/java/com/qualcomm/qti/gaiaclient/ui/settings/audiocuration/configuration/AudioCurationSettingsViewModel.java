/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.configuration;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ScenarioConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.State;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Support;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ToggleConfiguration;
import com.qualcomm.qti.gaiaclient.repository.audiocuration.AudioCurationRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.AudioCurationViewModel;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.Configuration;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AudioCurationSettingsViewModel
        extends AudioCurationViewModel<AudioCurationSettings, AudioCurationSettingsViewData> {

    private final ArrayMap<Configuration, Pair<String[], String[]>> mConfigurationSets = new ArrayMap<>();

    @Inject
    public AudioCurationSettingsViewModel(@NonNull Application application,
                                          AudioCurationRepository audioCurationRepository,
                                          ConnectionRepository connectionRepository,
                                          FeaturesRepository featuresRepository) {
        super(application, audioCurationRepository, connectionRepository, featuresRepository);
    }

    @Override
    protected AudioCurationSettingsViewData initViewData(Context context) {
        return new AudioCurationSettingsViewData(context);
    }

    @Override
    protected ACInfo[] getInfoToSupport() {
        return new ACInfo[]{ACInfo.AC_FEATURE_STATE, ACInfo.MODES_COUNT, ACInfo.MODE, ACInfo.TOGGLES_COUNT,
                            ACInfo.TOGGLE_CONFIGURATION, ACInfo.SCENARIO_CONFIGURATION, ACInfo.DEMO_SUPPORT,
                            ACInfo.AUTO_TRANSPARENCY_SUPPORT, ACInfo.AUTO_TRANSPARENCY_STATE,
                            ACInfo.AUTO_TRANSPARENCY_RELEASE_TIME, ACInfo.NOISE_ID_SUPPORT, ACInfo.NOISE_ID_STATE};
    }

    @Override
    protected void onAudioCurationSupported(boolean supported, Integer version) {
        setEnable(supported);

        if (supported) {
            for (AudioCurationSettings setting : AudioCurationSettings.values()) {
                setSettingVisibility(setting, setting.getFeatureVersion() <= version);
            }
        }
    }

    @Override
    protected void onState(FeatureState state) {
        if (state != null && state.getFeature() == Feature.ANC) {
            setSettingSwitch(AudioCurationSettings.ANC_STATE, state.getState() == State.Enabled.INSTANCE);
        }
    }

    @Override
    protected void onSupportedModes(List<ModeViewData> modes) {
        for (Configuration configuration : Configuration.getValues()) {
            AudioCurationSettings setting = AudioCurationSettings.valueOf(configuration);
            if (setting != null) {
                setSettingEnable(setting, true);
                Pair<String[], String[]> entries = getConfigurationEntries(configuration, modes);
                setSettingList(setting, entries);
                mConfigurationSets.put(configuration, entries);

                // prevent "no summary" if the selected value was updated before the modes were
                onConfiguration(configuration, getConfigurationSelection(configuration));
            }
        }
    }

    @Override
    protected void onToggleConfiguration(ToggleConfiguration configuration) {
        if (configuration != null) {
            onConfiguration(Configuration.valueOf(configuration.getToggle()), configuration.getOptionValue());
        }
    }

    @Override
    protected void onScenarioConfiguration(ScenarioConfiguration configuration) {
        if (configuration != null) {
            onConfiguration(Configuration.valueOf(configuration.getScenario()), configuration.getOptionValue());
        }
    }

    @Override
    protected void onDemoSupport(Support support) {
        boolean supported = support == Support.Supported.INSTANCE;
        setSettingVisibility(AudioCurationSettings.ENTER_DEMO, supported);
    }

    @Override
    protected void onAutoTransparencySupport(Support support) {
        setSettingVisibility(AudioCurationSettings.AUTO_TRANSPARENCY_CATEGORY, support == Support.Supported.INSTANCE);
    }

    @Override
    protected void onAutoTransparencyState(State state) {
        setSettingSwitch(AudioCurationSettings.AUTO_TRANSPARENCY_STATE, state == State.Enabled.INSTANCE);
    }

    @Override
    protected void onAutoTransparencyReleaseTime(AutoTransparencyReleaseTime time) {
        ReleaseTime value = ReleaseTime.valueOf(time);
        setSettingSummary(AudioCurationSettings.AUTO_TRANSPARENCY_RELEASE_TIME,
                          value == null ? "" : value.getLabel(getApplication()));
        setSettingValue(AudioCurationSettings.AUTO_TRANSPARENCY_RELEASE_TIME, value == null ? "" : value.name());
    }

    @Override
    protected void onNoiseIdSupport(Support support) {
        boolean visible = support == Support.Supported.INSTANCE;
        setSettingVisibility(AudioCurationSettings.NOISE_ID_PREF_CATEGORY, visible);
    }

    @Override
    protected void onNoiseIdState(State state) {
        boolean enabled = state == State.Enabled.INSTANCE;
        setSettingSwitch(AudioCurationSettings.NOISE_ID_STATE, enabled);
    }

    private Pair<String, String> getEntry(Pair<String[], String[]> entries, Integer value) {
        if (value == null || entries == null) {
            return null;
        }

        String[] labels = entries.first;
        String[] values = entries.second;

        if (labels == null || values == null || labels.length != values.length) {
            return null;
        }

        for (int i = 0; i < values.length; i++) {
            if (Integer.parseInt(values[i]) == value) {
                return new Pair<>(labels[i], values[i]);
            }
        }

        return new Pair<>(getApplication().getString(R.string.settings_audio_curation_option_param, value),
                          Integer.toString(value));
    }

    private void onConfiguration(Configuration configuration, Integer value) {
        if (configuration == null) {
            return;
        }

        Pair<String[], String[]> entries = mConfigurationSets.get(configuration);

        Pair<String, String> selection = getEntry(entries, value);
        AudioCurationSettings setting = AudioCurationSettings.valueOf(configuration);

        if (selection != null && setting != null) {
            setSettingSummary(setting, selection.first);
            setSettingValue(setting, selection.second);
        }
    }
}
