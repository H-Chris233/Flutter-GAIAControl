/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.os.Bundle;
import android.view.View;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Gain;
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaEvent;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsBinding;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;

@AndroidEntryPoint
public class AudioCurationDemoFragment
        extends SettingsFragment<AudioCurationDemoSettings, AudioCurationDemoViewData, AudioCurationDemoViewModel> {

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().updateData();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        SliderPreference sliderPreference = findPreference(AudioCurationDemoSettings.LEAKTHROUGH_GAIN);
        if (sliderPreference != null) {
            sliderPreference.setMax(Gain.MAX);
            sliderPreference.setOnProgressChanged(progress -> {
                getViewModel().setLeakthroughGain(progress);
                return Unit.INSTANCE;
            });
        }

        DiscreteSliderPreference discreteSliderPreference =
                findPreference(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER);
        if (discreteSliderPreference != null) {
            discreteSliderPreference.setListener((progress) -> getViewModel().setLeakthroughGainStep((int) progress));
            // For LEAKTHROUGH_GAIN_STEPPER progress are int values
        }

        LeftRightBalancePreference leftRightBalancePreference =
                findPreference(AudioCurationDemoSettings.LEFT_RIGHT_BALANCE);
        if (leftRightBalancePreference != null) {
            leftRightBalancePreference.setListener((progress) -> getViewModel().setLeftRightBalance((int) progress));
            // For LEFT_RIGHT_BALANCE progress are int values
        }

        ModesPreference modesPreference = findPreference(AudioCurationDemoSettings.MODE);
        if (modesPreference != null) {
            modesPreference.setListener(mode -> getViewModel().setMode(mode.getValue()));
        }

        MediaPreference mediaPreference = findPreference(AudioCurationDemoSettings.MEDIA);
        if (mediaPreference != null) {
            mediaPreference.setOnEvent(this::onEvent);
        }
    }

    private Unit onEvent(MediaEvent event) {
        getViewModel().onMediaEvent(event);
        return Unit.INSTANCE;
    }

    @Override
    protected SettingsBinding<AudioCurationDemoSettings, AudioCurationDemoViewData> initSettingsBinding(
            PreferenceManager preferenceManager, View view) {
        return new AudioCurationDemoBinding(preferenceManager, view);
    }

    @Override
    protected int getXmlSettingsId() {
        return R.xml.audio_curation_demo_settings;
    }

    @Override
    protected AudioCurationDemoViewModel initViewModel() {
        return new ViewModelProvider(this).get(AudioCurationDemoViewModel.class);
    }

    @Override
    protected AudioCurationDemoSettings[] getSettings() {
        return AudioCurationDemoSettings.values();
    }

    @Override
    protected boolean onPreferenceChange(AudioCurationDemoSettings key, Preference preference, Object update) {
        if (AudioCurationDemoSettings.ANC_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setState(Feature.ANC, (boolean) update);
            return false; // view model will update the state depending on request result
        }
        else if (AudioCurationDemoSettings.ADAPTATION.equals(key) && update instanceof Boolean) {
            getViewModel().setAdaptationState((boolean) update);
            return false; // view model will update the state depending on request result
        }
        else if (AudioCurationDemoSettings.WIND_NOISE_DETECTION_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setWindNoiseDetectionState((boolean) update);
            return true; // view model will update the state depending on request result
        }
        else if (AudioCurationDemoSettings.HOWLING_DETECTION_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setHowlingDetectionState((boolean) update);
            return true; // view model will update the state depending on request result
        }
        else if (AudioCurationDemoSettings.ADVERSE_ACOUSTIC_STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setAdverseAcousticState((boolean) update);
            return true; // view model will update the state depending on request result
        }

        return true;
    }
}
