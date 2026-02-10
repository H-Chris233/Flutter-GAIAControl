/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.material.slider.LabelFormatter;
import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AdaptationState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.DemoState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Detection;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureType;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Gain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.GainReduction;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGainConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGainStep;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeftRightBalance;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Mode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.NoiseIdCategory;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.State;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Support;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.WindNoiseReduction;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.WindNoiseReductionState;
import com.qualcomm.qti.gaiaclient.repository.audiocuration.AudioCurationRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaPlaybackRepository;
import com.qualcomm.qti.gaiaclient.repository.mediaplayback.MediaEvent;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.AudioCurationViewModel;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AudioCurationDemoViewModel
        extends AudioCurationViewModel<AudioCurationDemoSettings, AudioCurationDemoViewData> {

    private final MediaPlaybackRepository mediaPlaybackRepository;
    private final TouchpadContentDescription TOUCHPAD_CONTENT_DESCRIPTIONS = new TouchpadContentDescription();
    private LeakthroughGainConfiguration gainConfiguration = null;

    @Inject
    public AudioCurationDemoViewModel(@NonNull Application application, AudioCurationRepository audioCurationRepository,
                                      ConnectionRepository connectionRepository, FeaturesRepository featuresRepository,
                                      MediaPlaybackRepository mediaPlaybackRepository) {
        super(application, audioCurationRepository, connectionRepository, featuresRepository);
        this.mediaPlaybackRepository = mediaPlaybackRepository;
    }

    @Override
    protected AudioCurationDemoViewData initViewData(Context context) {
        return new AudioCurationDemoViewData(context);
    }

    @Override
    protected ACInfo[] getInfoToSupport() {
        return new ACInfo[]{ACInfo.AC_FEATURE_STATE, ACInfo.MODES_COUNT, ACInfo.MODE, ACInfo.FEED_FORWARD_GAIN,
                            ACInfo.DEMO_SUPPORT, ACInfo.DEMO_STATE, ACInfo.ADAPTATION_STATE,
                            ACInfo.LEAKTHROUGH_GAIN_CONFIGURATION, ACInfo.LEAKTHROUGH_GAIN_STEP,
                            ACInfo.LEFT_RIGHT_BALANCE, ACInfo.WIND_NOISE_DETECTION_SUPPORT,
                            ACInfo.WIND_NOISE_DETECTION_STATE, ACInfo.WIND_NOISE_REDUCTION,
                            ACInfo.HOWLING_DETECTION_SUPPORT, ACInfo.HOWLING_DETECTION_STATE, ACInfo.FEEDBACK_GAIN,
                            ACInfo.NOISE_ID_SUPPORT, ACInfo.NOISE_ID_STATE, ACInfo.NOISE_ID_CATEGORY,
                            ACInfo.ADVERSE_ACOUSTIC_SUPPORT, ACInfo.ADVERSE_ACOUSTIC_STATE,
                            ACInfo.ADVERSE_ACOUSTIC_GAIN_REDUCTION, ACInfo.HOWLING_CONTROL_GAIN_REDUCTION,
                            ACInfo.FILTER_TOPOLOGY};
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        setDemoState(DemoState.OUT);
    }

    @Override
    protected void onAudioCurationSupported(boolean supported, Integer version) {
        setSettingEnable(AudioCurationDemoSettings.ANC_STATE, supported);

        if (!supported) {
            setSettingEnable(AudioCurationDemoSettings.MODE, false);
            setSettingEnable(AudioCurationDemoSettings.FEED_FORWARD_GAIN, false);
            setSettingEnable(AudioCurationDemoSettings.ADAPTATION, false);
            setSettingEnable(AudioCurationDemoSettings.LEAKTHROUGH_GAIN, false);
            setSettingEnable(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER, false);
            setSettingEnable(AudioCurationDemoSettings.LEFT_RIGHT_BALANCE, false);
            setSettingEnable(AudioCurationDemoSettings.WIND_NOISE_DETECTION_STATE, false);
            setSettingEnable(AudioCurationDemoSettings.WIND_NOISE_REDUCTION, false);
            setSettingEnable(AudioCurationDemoSettings.HOWLING_DETECTION_STATE, false);
            setSettingEnable(AudioCurationDemoSettings.FEEDBACK_GAIN, false);
            setSettingEnable(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_STATE, false);
            setSettingEnable(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION, false);
            setSettingEnable(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION, false);
        }
        else {
            boolean isBeforeV2 = version != null && version < 2;
            setSettingVisibility(AudioCurationDemoSettings.LEAKTHROUGH_GAIN, isBeforeV2);
            setSettingVisibility(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER, !isBeforeV2);
            setSettingVisibility(AudioCurationDemoSettings.LEFT_RIGHT_BALANCE, !isBeforeV2);
            if (isBeforeV2) {
                setSettingVisibility(AudioCurationDemoSettings.WIND_NOISE_DETECTION_STATE, false);
                setSettingVisibility(AudioCurationDemoSettings.WIND_NOISE_REDUCTION, false);
            }
            boolean isBeforeV5 = version != null && version < 5;
            if (isBeforeV5) {
                setSettingVisibility(AudioCurationDemoSettings.HOWLING_DETECTION_STATE, false);
            }
            setSettingVisibility(AudioCurationDemoSettings.FEEDBACK_GAIN, !isBeforeV5);
            boolean isBeforeV6 = version != null && version < 6;
            if (isBeforeV6) {
                setSettingVisibility(AudioCurationDemoSettings.NOISE_ID_CATEGORY, false);
            }
            boolean isBeforeV7 = version != null && version < 7;
            if (isBeforeV7) {
                setSettingVisibility(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_STATE, false);
                setSettingVisibility(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION, false);
                setSettingVisibility(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION, false);
            }
        }
    }

    public void onMediaEvent(MediaEvent event) {
        mediaPlaybackRepository.onEvent(event);
    }

    public void setLeftRightBalance(int value) {
        int gain = Math.abs(value);
        gain = gain <= 2 ? 0 : gain; // ignoring values of 1 and 2 to help setting the value to 0
        LeftRightBalance balance = new LeftRightBalance(value < 0 ? EarbudPosition.LEFT : EarbudPosition.RIGHT, gain);
        setLeftRightBalance(balance);
    }

    public void setWindNoiseDetectionState(boolean enabled) {
        State state = State.valueOf(enabled);
        setWindNoiseDetectionState(state);
    }

    @Override
    protected void onState(FeatureState state) {
        if (state != null && state.getFeature() == Feature.ANC) {
            boolean enabled = state.getState() == State.Enabled.INSTANCE;
            setSettingSwitch(AudioCurationDemoSettings.ANC_STATE, enabled);

            setSettingEnable(AudioCurationDemoSettings.MODE, enabled);
            setSettingEnable(AudioCurationDemoSettings.FEED_FORWARD_GAIN, enabled);
            setSettingEnable(AudioCurationDemoSettings.WIND_NOISE_REDUCTION, enabled);
            setSettingEnable(AudioCurationDemoSettings.WIND_NOISE_DETECTION_STATE, enabled);
            setSettingEnable(AudioCurationDemoSettings.FEEDBACK_GAIN, enabled);
            enableSettingsOnSelectedMode(enabled ? getMode() : null);
        }
    }

    @Override
    protected void onSupportedModes(List<ModeViewData> modes) {
        if (modes != null) {
            updateSettingData((viewData) -> viewData.setModesList(AudioCurationDemoSettings.MODE, modes));

            // update the UI selected mode as it could be missing its label
            Mode mode = getMode();
            if (mode != null) {
                updateSelectedMode(mode.getValue());
            }
        }
    }

    @Override
    protected void onMode(Mode mode) {
        if (mode != null) {
            updateSelectedMode(mode.getValue());
            enableSettingsOnSelectedMode(mode);
        }
    }

    @Override
    protected void onFeedForwardGain(Gain gain) {
        if (gain != null) {
            updateEarbudGain(AudioCurationDemoSettings.FEED_FORWARD_GAIN, gain);
            if (FeatureType.Leakthrough.INSTANCE.equals(gain.getFeatureType())) {
                int leftGain = gain.getGain(EarbudPosition.LEFT, 0);
                int leakthroughGain = leftGain != 0 ? leftGain : gain.getGain(EarbudPosition.RIGHT, 0);
                updateLeakthroughGain(leakthroughGain);
            }
        }
    }

    @Override
    protected void onAdaptationState(AdaptationState state) {
        if (state != null) {
            setSettingSwitch(AudioCurationDemoSettings.ADAPTATION, state == AdaptationState.RESUMED);
        }
    }

    @Override
    protected void onDemoState(DemoState state) {
        if (state != DemoState.IN) {
            setDemoState(DemoState.IN);
        }
        else /* state == ACDemoState.IN */ {
            // can only be fetched if demo state is IN
            fetchData(ACInfo.ADAPTATION_STATE);
        }
    }

    @Override
    protected void onLeakthroughConfiguration(LeakthroughGainConfiguration configuration) {
        if (configuration == null) {
            return;
        }

        this.gainConfiguration = configuration;

        int initialValue = configuration.getInitialStep();

        updateLeakthroughGainStepperMaxValue(configuration.getStepsNumber());
        updateLeakthroughGainStepperValue(initialValue);
        updateLeakthroughGainStepperLabelFormatter(value -> {
            return LabelProvider.getForLeakthroughGainStepper(getApplication(), configuration.getMinimumGainInDB(),
                                                              configuration.getStepSizeInDB(), (int) value);
            // cast value to int as the values are integers only
        });
        updateLeakthroughGainStepperLabel(
                LabelProvider.getForLeakthroughGainStepper(getApplication(), configuration.getMinimumGainInDB(),
                                                           configuration.getStepSizeInDB(), initialValue));
    }

    @Override
    protected void onLeakthroughGainStep(LeakthroughGainStep step) {
        if (step == null) {
            return;
        }

        int minimumGainInDB = gainConfiguration != null ? gainConfiguration.getMinimumGainInDB() : 0;
        int stepSizeInDB = gainConfiguration != null ? gainConfiguration.getStepSizeInDB() : 1;
        int currentValue = step.getCurrentStep();
        updateLeakthroughGainStepperValue(currentValue);
        updateLeakthroughGainStepperLabel(
                LabelProvider
                        .getForLeakthroughGainStepper(getApplication(), minimumGainInDB, stepSizeInDB, currentValue));
    }

    @Override
    protected void onLeftRightBalance(LeftRightBalance balance) {
        if (balance == null) {
            return;
        }

        int value = balance.getGain() * (balance.getPosition() == EarbudPosition.LEFT ? -1 : 1);
        updateLeftRightBalanceValue(value);
        updateLeftRightBalanceLabel(LabelProvider.getForLeftRightBalance(getApplication(), balance));
    }

    @Override
    protected void onWindNoiseDetectionSupport(Support support) {
        boolean supported = support == Support.Supported.INSTANCE;
        setSettingVisibility(AudioCurationDemoSettings.WIND_NOISE_DETECTION_STATE, supported);
        setSettingVisibility(AudioCurationDemoSettings.WIND_NOISE_REDUCTION, supported);
    }

    @Override
    protected void onWindNoiseDetectionState(State state) {
        boolean isEnabled = state == State.Enabled.INSTANCE;
        setSettingSwitch(AudioCurationDemoSettings.WIND_NOISE_DETECTION_STATE, isEnabled);
        setSettingEnable(AudioCurationDemoSettings.WIND_NOISE_REDUCTION, isEnabled && isANCEnabled());
        if (isEnabled) {
            onWindNoiseReduction(getWindNoiseReduction());
        }
        else {
            updateLeftTouchpad(AudioCurationDemoSettings.WIND_NOISE_REDUCTION,
                               new TouchpadViewData(false, TOUCHPAD_CONTENT_DESCRIPTIONS.LEFT_NOT_DETECTED));
            updateRightTouchpad(AudioCurationDemoSettings.WIND_NOISE_REDUCTION,
                                new TouchpadViewData(false, TOUCHPAD_CONTENT_DESCRIPTIONS.RIGHT_NOT_DETECTED));
        }
    }

    @Override
    protected void onWindNoiseReduction(WindNoiseReduction reduction) {
        if (reduction == null) {
            return;
        }

        updateLeftTouchpad(AudioCurationDemoSettings.WIND_NOISE_REDUCTION,
                           getLeftTouchpadViewData(
                                   reduction.getWindDetectionOnLeft() == WindNoiseReductionState.WIND_DETECTED));
        updateRightTouchpad(AudioCurationDemoSettings.WIND_NOISE_REDUCTION,
                            getRightTouchpadViewData(
                                    reduction.getWindDetectionOnRight() == WindNoiseReductionState.WIND_DETECTED));
    }

    @Override
    protected void onHowlingDetectionSupport(Support support) {
        boolean supported = support == Support.Supported.INSTANCE;
        setSettingVisibility(AudioCurationDemoSettings.HOWLING_DETECTION_STATE, supported);
        setSettingVisibility(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION, supported && getVersion() >= 7);
    }

    @Override
    protected void onHowlingDetectionState(State state) {
        boolean isEnabled = state == State.Enabled.INSTANCE;
        setSettingSwitch(AudioCurationDemoSettings.HOWLING_DETECTION_STATE, isEnabled);
        Mode mode = getMode();
        setSettingEnable(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION,
                         isEnabled && isANCEnabled() && mode != null &&
                                 mode.getHowlingDetectionControlSupport() == Support.Supported.INSTANCE);
        if (isEnabled) {
            onHowlingControlGainReduction(getHowlingControlGainReduction());
        }
        else {
            updateLeftTouchpad(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION,
                               new TouchpadViewData(false, TOUCHPAD_CONTENT_DESCRIPTIONS.LEFT_NOT_DETECTED));
            updateRightTouchpad(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION,
                                new TouchpadViewData(false, TOUCHPAD_CONTENT_DESCRIPTIONS.RIGHT_NOT_DETECTED));
        }
    }

    @Override
    protected void onFeedbackGain(Gain gain) {
        if (gain != null) {
            updateEarbudGain(AudioCurationDemoSettings.FEEDBACK_GAIN, gain);
        }
    }

    @Override
    protected void onNoiseIdSupport(Support support) {
        boolean isSupported = support == Support.Supported.INSTANCE;
        setSettingVisibility(AudioCurationDemoSettings.NOISE_ID_CATEGORY, isSupported);
    }

    @Override
    protected void onNoiseIdState(State state) {
        boolean enabled = state != null && state == State.Enabled.INSTANCE;
        setSettingEnable(AudioCurationDemoSettings.NOISE_ID_CATEGORY, enabled);
    }

    @Override
    protected void onNoiseIdCategory(NoiseIdCategory category) {
        String label = NoiseIdCategoryMapper.INSTANCE.getLabel(getApplication(), category);
        setSettingSummary(AudioCurationDemoSettings.NOISE_ID_CATEGORY, label);
    }

    @Override
    protected void onAdverseAcousticSupport(Support support) {
        boolean supported = support == Support.Supported.INSTANCE;
        setSettingVisibility(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_STATE, supported);
        setSettingVisibility(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION, supported);
    }

    @Override
    protected void onAdverseAcousticState(State state) {
        boolean isEnabled = state == State.Enabled.INSTANCE;
        setSettingSwitch(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_STATE, isEnabled);
        setSettingEnable(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION, isEnabled && isANCEnabled());
        if (isEnabled) {
            onAdverseAcousticGainReduction(getAdverseAcousticGainReduction());
        }
        else {
            updateLeftTouchpad(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION,
                               new TouchpadViewData(false, TOUCHPAD_CONTENT_DESCRIPTIONS.LEFT_NOT_DETECTED));
            updateRightTouchpad(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION,
                                new TouchpadViewData(false, TOUCHPAD_CONTENT_DESCRIPTIONS.RIGHT_NOT_DETECTED));
        }
    }

    @Override
    protected void onAdverseAcousticGainReduction(GainReduction reduction) {
        if (reduction == null) {
            return;
        }

        updateLeftTouchpad(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION,
                           getLeftTouchpadViewData(
                                   reduction.getDetectionOnLeft() == Detection.Detected.INSTANCE));
        updateRightTouchpad(AudioCurationDemoSettings.ADVERSE_ACOUSTIC_GAIN_REDUCTION,
                            getRightTouchpadViewData(
                                    reduction.getDetectionOnRight() == Detection.Detected.INSTANCE));
    }

    @Override
    protected void onHowlingControlGainReduction(GainReduction reduction) {
        if (reduction == null) {
            return;
        }

        updateLeftTouchpad(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION,
                           getLeftTouchpadViewData(
                                   reduction.getDetectionOnLeft() == Detection.Detected.INSTANCE));
        updateRightTouchpad(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION,
                            getRightTouchpadViewData(
                                    reduction.getDetectionOnRight() == Detection.Detected.INSTANCE));
    }

    private void updateSelectedMode(int value) {
        ModeViewData data = getMode(value);
        if (data != null) {
            updateSettingData((viewData) -> viewData.setMode(AudioCurationDemoSettings.MODE, data));
        }
    }

    private void enableSettingsOnSelectedMode(Mode mode) {
        boolean isANCEnabled = isANCEnabled();
        setSettingEnable(AudioCurationDemoSettings.ADAPTATION,
                         isANCEnabled && mode != null &&
                                 mode.getAdaptationControlSupport() == Support.Supported.INSTANCE);
        setSettingEnable(AudioCurationDemoSettings.LEAKTHROUGH_GAIN,
                         isANCEnabled && mode != null && mode.getGainControlSupport() == Support.Supported.INSTANCE);
        setSettingEnable(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER,
                         isANCEnabled && mode != null && mode.getGainControlSupport() == Support.Supported.INSTANCE);
        setSettingEnable(AudioCurationDemoSettings.LEFT_RIGHT_BALANCE,
                         isANCEnabled && mode != null && mode.getGainControlSupport() == Support.Supported.INSTANCE);
        boolean howlingEnabled =
                isANCEnabled && mode != null && mode.getHowlingDetectionControlSupport() == Support.Supported.INSTANCE;
        setSettingEnable(AudioCurationDemoSettings.HOWLING_DETECTION_STATE, howlingEnabled);
        setSettingEnable(AudioCurationDemoSettings.HOWLING_CONTROL_GAIN_REDUCTION,
                         howlingEnabled && getHowlingDetectionState() == State.Enabled.INSTANCE);
    }

    private void updateEarbudGain(@NonNull AudioCurationDemoSettings setting, @NonNull Gain gain) {
        GainPreferenceViewData data = new GainPreferenceViewData(getApplication(), getVersion(), gain);
        updateSettingData((viewData -> viewData.setGain(setting, data)));
    }

    private void updateLeakthroughGain(int gain) {
        String label = String.valueOf(gain);
        SliderViewData data = new SliderViewData(gain, label);
        updateSettingData((viewData -> viewData.setProgress(AudioCurationDemoSettings.LEAKTHROUGH_GAIN, data)));
    }

    private void updateLeakthroughGainStepperMaxValue(int value) {
        updateSettingData((viewData) -> viewData
                .setDiscreteSliderMaxValue(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER, value));
    }

    private void updateLeakthroughGainStepperValue(int value) {
        updateSettingData((viewData) -> viewData
                .setDiscreteSliderValue(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER, value));
    }

    private void updateLeakthroughGainStepperLabel(String value) {
        updateSettingData((viewData) -> viewData
                .setDiscreteSliderLabel(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER, value));
    }

    private void updateLeakthroughGainStepperLabelFormatter(LabelFormatter formatter) {
        updateSettingData((viewData) -> viewData
                .setDiscreteSliderLabelFormatter(AudioCurationDemoSettings.LEAKTHROUGH_GAIN_STEPPER, formatter));
    }

    private void updateLeftRightBalanceValue(int value) {
        updateSettingData((viewData) -> viewData
                .setLeftRightBalanceValue(AudioCurationDemoSettings.LEFT_RIGHT_BALANCE, value));
    }

    private void updateLeftRightBalanceLabel(String label) {
        updateSettingData((viewData) -> viewData
                .setLeftRightBalanceLabel(AudioCurationDemoSettings.LEFT_RIGHT_BALANCE, label));
    }

    private void updateLeftTouchpad(AudioCurationDemoSettings setting, TouchpadViewData data) {
        updateSettingData((viewData) -> viewData.setLeftTouchpadViewData(setting, data));
    }

    private void updateRightTouchpad(AudioCurationDemoSettings setting, TouchpadViewData data) {
        updateSettingData((viewData) -> viewData.setRightTouchpadViewData(setting, data));
    }

    private TouchpadViewData getLeftTouchpadViewData(boolean detected) {
        String contentDescription =
                detected ? TOUCHPAD_CONTENT_DESCRIPTIONS.LEFT_DETECTED
                         : TOUCHPAD_CONTENT_DESCRIPTIONS.LEFT_NOT_DETECTED;
        return new TouchpadViewData(detected, contentDescription);
    }

    private TouchpadViewData getRightTouchpadViewData(boolean detected) {
        String contentDescription =
                detected ? TOUCHPAD_CONTENT_DESCRIPTIONS.RIGHT_DETECTED
                         : TOUCHPAD_CONTENT_DESCRIPTIONS.RIGHT_NOT_DETECTED;
        return new TouchpadViewData(detected, contentDescription);
    }

    private class TouchpadContentDescription {
        String LEFT_NOT_DETECTED =
                getApplication().getString(R.string.cont_desc_audio_curation_not_detected_on_left);
        String LEFT_DETECTED =
                getApplication().getString(R.string.cont_desc_audio_curation_detected_on_left);
        String RIGHT_NOT_DETECTED =
                getApplication().getString(R.string.cont_desc_audio_curation_not_detected_on_right);
        String RIGHT_DETECTED =
                getApplication().getString(R.string.cont_desc_audio_curation_detected_on_right);
    }

}
