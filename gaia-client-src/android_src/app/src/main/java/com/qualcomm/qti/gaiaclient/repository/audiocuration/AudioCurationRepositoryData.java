/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.audiocuration;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AdaptationState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.DemoState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FilterTopology;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Gain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.GainReduction;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGainConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGainStep;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeftRightBalance;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Mode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.NoiseIdCategory;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Scenario;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ScenarioConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.State;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Support;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ToggleConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.WindNoiseReduction;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import javax.inject.Singleton;

@Singleton
public abstract class AudioCurationRepositoryData implements AudioCurationRepository {

    private final StickyLiveData<FeatureState> state = new StickyLiveData<>();

    private final StickyLiveData<Integer> modesCount = new StickyLiveData<>();

    private final StickyLiveData<Mode> currentMode = new StickyLiveData<>();

    private final StickyLiveData<Gain> feedForwardGain = new StickyLiveData<>();

    private final StickyLiveData<Integer> toggleCount = new StickyLiveData<>();

    private final ArrayMap<Integer, StickyLiveData<ToggleConfiguration>> toggleConfigurations = new ArrayMap<>();

    private final ArrayMap<Scenario, StickyLiveData<ScenarioConfiguration>> scenarioConfiguration = new ArrayMap<>();

    private final StickyLiveData<Support> demoSupport = new StickyLiveData<>();

    private final StickyLiveData<DemoState> demoState = new StickyLiveData<>();

    private final StickyLiveData<AdaptationState> adaptationState = new StickyLiveData<>();

    private final StickyLiveData<LeakthroughGainConfiguration> leakthroughGainConfiguration = new StickyLiveData<>();

    private final StickyLiveData<LeakthroughGainStep> leakthroughGainStep = new StickyLiveData<>();

    private final StickyLiveData<LeftRightBalance> leftRightBalance = new StickyLiveData<>();

    private final StickyLiveData<Support> windNoiseDetectionSupport = new StickyLiveData<>();

    private final StickyLiveData<State> windNoiseDetectionState = new StickyLiveData<>();

    private final StickyLiveData<WindNoiseReduction> windNoiseReduction = new StickyLiveData<>();

    private final StickyLiveData<Support> autoTransparencySupport = new StickyLiveData<>();

    private final StickyLiveData<State> autoTransparencyState = new StickyLiveData<>();

    private final StickyLiveData<AutoTransparencyReleaseTime> autoTransparencyReleaseTime = new StickyLiveData<>();

    private final StickyLiveData<Support> howlingDetectionSupport = new StickyLiveData<>(null);

    private final StickyLiveData<State> howlingDetectionState = new StickyLiveData<>(null);

    private final StickyLiveData<Support> noiseIdSupport = new StickyLiveData<>(null);

    private final StickyLiveData<State> noiseIdState = new StickyLiveData<>(null);

    private final StickyLiveData<NoiseIdCategory> noiseIdCategory = new StickyLiveData<>(null);

    private final StickyLiveData<Gain> feedbackGain = new StickyLiveData<>(null);

    private final StickyLiveData<Support> adverseAcousticSupport = new StickyLiveData<>(null);

    private final StickyLiveData<State> adverseAcousticState = new StickyLiveData<>(null);

    private final StickyLiveData<GainReduction> adverseAcousticGainReduction = new StickyLiveData<>(null);

    private final StickyLiveData<GainReduction> howlingControlGainReduction = new StickyLiveData<>(null);

    private final StickyLiveData<FilterTopology> filterTopology = new StickyLiveData<>(null);

    @NonNull
    @Override
    public LiveData<FeatureState> getStateLiveData() {
        return state;
    }

    @NonNull
    @Override
    public LiveData<Integer> getModeCountLiveData() {
        return modesCount;
    }

    @NonNull
    @Override
    public LiveData<Mode> getModeLiveData() {
        return currentMode;
    }

    @NonNull
    @Override
    public LiveData<Gain> getFeedForwardGainLiveData() {
        return feedForwardGain;
    }

    @NonNull
    @Override
    public LiveData<Integer> getTogglesCountLiveData() {
        return toggleCount;
    }

    @NonNull
    @Override
    public StickyLiveData<ToggleConfiguration> getToggleConfigurationLiveData(int toggle) {
        StickyLiveData<ToggleConfiguration> data = toggleConfigurations.get(toggle);
        if (data == null) {
            data = new StickyLiveData<>(null);
            toggleConfigurations.put(toggle, data);
        }
        return data;
    }

    @NonNull
    @Override
    public StickyLiveData<ScenarioConfiguration> getScenarioConfigurationLiveData(Scenario scenario) {
        StickyLiveData<ScenarioConfiguration> data = scenarioConfiguration.get(scenario);
        if (data == null) {
            data = new StickyLiveData<>(null);
            scenarioConfiguration.put(scenario, data);
        }
        return data;
    }

    @NonNull
    @Override
    public LiveData<Support> getDemoSupportLiveData() {
        return demoSupport;
    }

    @NonNull
    @Override
    public LiveData<DemoState> getDemoStateLiveData() {
        return demoState;
    }

    @NonNull
    @Override
    public LiveData<AdaptationState> getAdaptationStateLiveData() {
        return adaptationState;
    }

    @NonNull
    @Override
    public LiveData<LeakthroughGainConfiguration> getLeakthroughGainConfigurationLiveData() {
        return leakthroughGainConfiguration;
    }

    @NonNull
    @Override
    public LiveData<LeakthroughGainStep> getLeakthroughGainStepLiveData() {
        return leakthroughGainStep;
    }

    @NonNull
    @Override
    public LiveData<LeftRightBalance> getLeftRightBalanceLiveData() {
        return leftRightBalance;
    }

    @NonNull
    @Override
    public LiveData<Support> getWindNoiseDetectionSupportLiveData() {
        return windNoiseDetectionSupport;
    }

    @NonNull
    @Override
    public LiveData<State> getWindNoiseDetectionStateLiveData() {
        return windNoiseDetectionState;
    }

    @NonNull
    @Override
    public LiveData<WindNoiseReduction> getWindNoiseReductionLiveData() {
        return windNoiseReduction;
    }

    @NonNull
    @Override
    public LiveData<Support> getAutoTransparencySupportLiveData() {
        return autoTransparencySupport;
    }

    @NonNull
    @Override
    public LiveData<State> getAutoTransparencyStateLiveData() {
        return autoTransparencyState;
    }

    @NonNull
    @Override
    public LiveData<AutoTransparencyReleaseTime> getAutoTransparencyReleaseTimeLiveData() {
        return autoTransparencyReleaseTime;
    }

    @NonNull
    @Override
    public LiveData<Support> getHowlingDetectionSupportLiveData() {
        return howlingDetectionSupport;
    }

    @NonNull
    @Override
    public LiveData<State> getHowlingDetectionStateLiveData() {
        return howlingDetectionState;
    }

    @NonNull
    @Override
    public LiveData<Gain> getFeedbackGainLiveData() {
        return feedbackGain;
    }

    @NonNull
    @Override
    public LiveData<Support> getNoiseIdSupportLiveData() {
        return noiseIdSupport;
    }

    @NonNull
    @Override
    public LiveData<State> getNoiseIdStateLiveData() {
        return noiseIdState;
    }

    @NonNull
    @Override
    public LiveData<NoiseIdCategory> getNoiseIdCategoryLiveData() {
        return noiseIdCategory;
    }

    @NonNull
    @Override
    public LiveData<Support> getAdverseAcousticSupportLiveData() {
        return adverseAcousticSupport;
    }

    @NonNull
    @Override
    public LiveData<State> getAdverseAcousticStateLiveData() {
        return adverseAcousticState;
    }

    @NonNull
    @Override
    public LiveData<GainReduction> getAdverseAcousticGainReductionLiveData() {
        return adverseAcousticGainReduction;
    }

    @NonNull
    @Override
    public LiveData<GainReduction> getHowlingControlGainReductionLiveData() {
        return howlingControlGainReduction;
    }

    @NonNull
    @Override
    public LiveData<FilterTopology> getFilterTopologyLiveData() {
        return filterTopology;
    }

    @Override
    public void reset() {
        state.setValue(null);
        modesCount.setValue(null);
        currentMode.setValue(null);
        feedForwardGain.setValue(null);
        toggleCount.setValue(null);

        for (StickyLiveData<ToggleConfiguration> data : toggleConfigurations.values()) {
            data.setValue(null);
        }

        for (StickyLiveData<ScenarioConfiguration> data : scenarioConfiguration.values()) {
            data.setValue(null);
        }

        adaptationState.setValue(null);
        demoSupport.setValue(null);
        demoState.setValue(null);
        leakthroughGainConfiguration.setValue(null);
        leakthroughGainStep.setValue(null);
        leftRightBalance.setValue(null);
        windNoiseDetectionSupport.setValue(null);
        windNoiseDetectionState.setValue(null);
        windNoiseReduction.setValue(null);
        autoTransparencySupport.setValue(null);
        autoTransparencyState.setValue(null);
        autoTransparencyReleaseTime.setValue(null);
        howlingDetectionSupport.setValue(null);
        howlingDetectionState.setValue(null);
        feedbackGain.setValue(null);
        noiseIdSupport.setValue(null);
        noiseIdState.setValue(null);
        noiseIdCategory.setValue(null);
        adverseAcousticSupport.setValue(null);
        adverseAcousticState.setValue(null);
        adverseAcousticGainReduction.setValue(null);
        howlingControlGainReduction.setValue(null);
        filterTopology.setValue(null);
    }

    @Override
    public final boolean hasData(ACInfo info) {
        switch (info) {
            case AC_FEATURE_STATE:
                return state.getValue() != null;

            case MODES_COUNT:
                return modesCount.getValue() != null;

            case MODE:
                return currentMode.getValue() != null;

            case FEED_FORWARD_GAIN:
                return feedForwardGain.getValue() != null;

            case TOGGLES_COUNT:
                return toggleCount.getValue() != null;

            case TOGGLE_CONFIGURATION:
                return !toggleConfigurations.isEmpty();

            case SCENARIO_CONFIGURATION:
                return !scenarioConfiguration.isEmpty();

            case DEMO_SUPPORT:
                return demoSupport.getValue() != null;

            case DEMO_STATE:
                return demoState.getValue() != null;

            case ADAPTATION_STATE:
                return adaptationState.getValue() != null;

            case LEAKTHROUGH_GAIN_CONFIGURATION:
                return leakthroughGainConfiguration.getValue() != null;

            case LEAKTHROUGH_GAIN_STEP:
                return leakthroughGainStep.getValue() != null;

            case LEFT_RIGHT_BALANCE:
                return leftRightBalance.getValue() != null;

            case WIND_NOISE_DETECTION_SUPPORT:
                return windNoiseDetectionSupport.getValue() != null;

            case WIND_NOISE_DETECTION_STATE:
                return windNoiseDetectionState.getValue() != null;

            case WIND_NOISE_REDUCTION:
                return windNoiseReduction.getValue() != null;

            case AUTO_TRANSPARENCY_SUPPORT:
                return autoTransparencySupport.getValue() != null;

            case AUTO_TRANSPARENCY_STATE:
                return autoTransparencyState.getValue() != null;

            case AUTO_TRANSPARENCY_RELEASE_TIME:
                return autoTransparencyReleaseTime.getValue() != null;

            case HOWLING_DETECTION_SUPPORT:
                return howlingDetectionSupport.getValue() != null;

            case HOWLING_DETECTION_STATE:
                return howlingDetectionState.getValue() != null;

            case FEEDBACK_GAIN:
                return feedbackGain.getValue() != null;

            case NOISE_ID_SUPPORT:
                return noiseIdSupport.getValue() != null;

            case NOISE_ID_STATE:
                return noiseIdState.getValue() != null;

            case NOISE_ID_CATEGORY:
                return noiseIdCategory.getValue() != null;

            case ADVERSE_ACOUSTIC_SUPPORT:
                return adverseAcousticSupport.getValue() != null;

            case ADVERSE_ACOUSTIC_STATE:
                return adverseAcousticState.getValue() != null;

            case ADVERSE_ACOUSTIC_GAIN_REDUCTION:
                return adverseAcousticGainReduction.getValue() != null;

            case HOWLING_CONTROL_GAIN_REDUCTION:
                return howlingControlGainReduction.getValue() != null;

            case FILTER_TOPOLOGY:
                return filterTopology.getValue() != null;

            default:
                return false;
        }
    }

    protected void updateState(FeatureState state) {
        this.state.postValue(state);
    }

    protected void updateModeCount(int count) {
        modesCount.postValue(count);
    }

    protected void updateCurrentMode(Mode mode) {
        currentMode.postValue(mode);
    }

    protected void updateFeedForwardGain(Gain gain) {
        feedForwardGain.postValue(gain);
    }

    protected void updateTogglesCount(int count) {
        toggleCount.postValue(count);
    }

    protected void updateToggleConfiguration(ToggleConfiguration configuration) {
        if (configuration != null) {
            StickyLiveData<ToggleConfiguration> data = getToggleConfigurationLiveData(configuration.getToggle());
            data.postValue(configuration);
        }
    }

    protected void updateScenarioConfiguration(ScenarioConfiguration configuration) {
        Scenario scenario = configuration != null ? configuration.getScenario() : null;
        if (scenario != null) {
            StickyLiveData<ScenarioConfiguration> data = getScenarioConfigurationLiveData(scenario);
            data.postValue(configuration);
        }
    }

    protected void updateDemoSupport(Support support) {
        demoSupport.postValue(support);
    }

    protected void updateDemoState(DemoState state) {
        demoState.postValue(state);
    }

    protected void updateAdaptationState(AdaptationState state) {
        adaptationState.postValue(state);
    }

    protected void updateLeakthroughGainConfiguration(LeakthroughGainConfiguration configuration) {
        leakthroughGainConfiguration.postValue(configuration);
    }

    protected void updateLeakthroughGainStep(LeakthroughGainStep step) {
        leakthroughGainStep.postValue(step);
    }

    protected void updateLeftRightBalance(LeftRightBalance balance) {
        leftRightBalance.postValue(balance);
    }

    protected void updateWindNoiseDetectionSupport(Support support) {
        windNoiseDetectionSupport.postValue(support);
    }

    protected void updateWindNoiseDetectionState(State state) {
        windNoiseDetectionState.postValue(state);
    }

    protected void updateWindNoiseReduction(WindNoiseReduction reduction) {
        windNoiseReduction.postValue(reduction);
    }

    protected void updateAutoTransparencySupport(Support support) {
        autoTransparencySupport.postValue(support);
    }

    protected void updateAutoTransparencyState(State state) {
        autoTransparencyState.postValue(state);
    }

    protected void updateAutoTransparencyReleaseTime(AutoTransparencyReleaseTime time) {
        autoTransparencyReleaseTime.postValue(time);
    }

    protected void updateHowlingDetectionSupport(Support support) {
        howlingDetectionSupport.postValue(support);
    }

    protected void updateHowlingDetectionState(State state) {
        howlingDetectionState.postValue(state);
    }

    protected void updateNoiseIdSupport(Support support) {
        noiseIdSupport.postValue(support);
    }

    protected void updateNoiseIdState(State state) {
        noiseIdState.postValue(state);
    }

    protected void updateNoiseIdCategory(NoiseIdCategory category) {
        noiseIdCategory.postValue(category);
    }

    protected void updateFeedbackGain(Gain gain) {
        feedbackGain.postValue(gain);
    }

    protected void updateAdverseAcousticSupport(Support support) {
        adverseAcousticSupport.postValue(support);
    }

    protected void updateAdverseAcousticState(State state) {
        adverseAcousticState.postValue(state);
    }

    protected void updateAdverseAcousticGainReduction(GainReduction reduction) {
        adverseAcousticGainReduction.postValue(reduction);
    }

    protected void updateHowlingControlGainReduction(GainReduction reduction) {
        howlingControlGainReduction.postValue(reduction);
    }

    protected void updateFilterTopology(FilterTopology topology) {
        filterTopology.postValue(topology);
    }
}
