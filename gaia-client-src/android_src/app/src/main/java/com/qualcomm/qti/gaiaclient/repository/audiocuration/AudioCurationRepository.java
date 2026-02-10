/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.audiocuration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AdaptationState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.DemoState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
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

public interface AudioCurationRepository {

    void init();

    void fetchData(Context context, ACInfo info);

    void fetchFeatureState(Context context, Feature feature);

    void fetchToggleConfiguration(Context context, int toggle);

    void fetchScenarioConfiguration(Context context, Scenario scenario);

    @NonNull
    LiveData<FeatureState> getStateLiveData();

    @NonNull
    LiveData<Integer> getModeCountLiveData();

    @NonNull
    LiveData<Mode> getModeLiveData();

    @NonNull
    LiveData<Gain> getFeedForwardGainLiveData();

    @NonNull
    LiveData<Integer> getTogglesCountLiveData();

    @NonNull
    LiveData<ToggleConfiguration> getToggleConfigurationLiveData(int toggle);

    @NonNull
    LiveData<ScenarioConfiguration> getScenarioConfigurationLiveData(Scenario scenario);

    @NonNull
    LiveData<Support> getDemoSupportLiveData();

    @NonNull
    LiveData<DemoState> getDemoStateLiveData();

    @NonNull
    LiveData<AdaptationState> getAdaptationStateLiveData();

    @NonNull
    LiveData<LeakthroughGainConfiguration> getLeakthroughGainConfigurationLiveData();

    @NonNull
    LiveData<LeakthroughGainStep> getLeakthroughGainStepLiveData();

    @NonNull
    LiveData<LeftRightBalance> getLeftRightBalanceLiveData();

    @NonNull
    LiveData<Support> getWindNoiseDetectionSupportLiveData();

    @NonNull
    LiveData<State> getWindNoiseDetectionStateLiveData();

    @NonNull
    LiveData<WindNoiseReduction> getWindNoiseReductionLiveData();

    @NonNull
    LiveData<Support> getAutoTransparencySupportLiveData();

    @NonNull
    LiveData<State> getAutoTransparencyStateLiveData();

    @NonNull
    LiveData<AutoTransparencyReleaseTime> getAutoTransparencyReleaseTimeLiveData();

    @NonNull
    LiveData<Support> getHowlingDetectionSupportLiveData();

    @NonNull
    LiveData<State> getHowlingDetectionStateLiveData();

    @NonNull
    LiveData<Gain> getFeedbackGainLiveData();

    @NonNull
    LiveData<Support> getNoiseIdSupportLiveData();

    @NonNull
    LiveData<State> getNoiseIdStateLiveData();

    @NonNull
    LiveData<NoiseIdCategory> getNoiseIdCategoryLiveData();

    @NonNull
    LiveData<Support> getAdverseAcousticSupportLiveData();

    @NonNull
    LiveData<State> getAdverseAcousticStateLiveData();

    @NonNull
    LiveData<GainReduction> getAdverseAcousticGainReductionLiveData();

    @NonNull
    LiveData<GainReduction> getHowlingControlGainReductionLiveData();

    @NonNull
    LiveData<FilterTopology> getFilterTopologyLiveData();

    default FeatureState getState() {
        return getStateLiveData().getValue();
    }

    default Mode getMode() {
        return getModeLiveData().getValue();
    }

    default ToggleConfiguration getToggleConfiguration(int toggle) {
        return getToggleConfigurationLiveData(toggle).getValue();
    }

    default ScenarioConfiguration getScenarioConfiguration(Scenario scenario) {
        return scenario != null ? getScenarioConfigurationLiveData(scenario).getValue() : null;
    }

    default State getHowlingDetectionState() {
        return getHowlingDetectionStateLiveData().getValue();
    }

    default WindNoiseReduction getWindNoiseReduction() {
        return getWindNoiseReductionLiveData().getValue();
    }

    default GainReduction getAdverseAcousticGainReduction() {
        return getAdverseAcousticGainReductionLiveData().getValue();
    }

    default GainReduction getHowlingControlGainReduction() {
        return getHowlingControlGainReductionLiveData().getValue();
    }

    default FilterTopology getFilterTopology() {
        return getFilterTopologyLiveData().getValue();
    }

    void setState(Context context, FeatureState state);

    void setMode(Context context, int mode);

    void setLeakthroughGain(Context context, int leftValue, int rightValue);

    void setToggleConfiguration(Context context, int toggle, int value);

    void setScenarioConfiguration(Context context, int scenario, int value);

    void setDemoState(Context context, DemoState state);

    void setAdaptationState(Context context, AdaptationState state);

    void setLeakthroughGainStep(Context context, int step);

    void setLeftRightBalance(Context context, LeftRightBalance balance);

    void setWindNoiseDetectionState(Context context, State state);

    void setAutoTransparencyState(Context context, State state);

    void setAutoTransparencyReleaseTime(Context context, AutoTransparencyReleaseTime time);

    void setHowlingDetectionState(Context context, State state);

    void setNoiseIdState(Context context, State state);

    void setAdverseAcousticState(Context context, State state);

    boolean hasData(ACInfo info);

    void reset();
}
