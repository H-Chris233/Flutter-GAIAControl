/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.audiocuration;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AdaptationState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.DemoState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FilterTopology;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Gain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.GainReduction;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeakthroughGain;
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
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.AudioCurationSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetACInfoRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetAudioCurationRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class AudioCurationRepositoryImpl extends AudioCurationRepositoryData {

    private final AudioCurationSubscriber mSubscriber = new AudioCurationSubscriber() {
        @Override
        public void onInfo(ACInfo info, Object update) {
            AudioCurationRepositoryImpl.this.onAudioCurationInfo(info, update);
        }

        @Override
        public void onError(ACInfo info, Reason reason) {
        }
    };

    @Inject
    public AudioCurationRepositoryImpl() {
    }

    @Override
    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mSubscriber);
    }

    @Override
    public void fetchData(Context context, ACInfo info) {
        fetchACInfo(context, info);
    }

    @Override
    public void fetchFeatureState(Context context, Feature feature) {
        FeatureState state = new FeatureState(feature);
        fetchACInfo(context, ACInfo.AC_FEATURE_STATE, state);
    }

    @Override
    public void fetchToggleConfiguration(Context context, int toggle) {
        ToggleConfiguration configuration = new ToggleConfiguration(toggle);
        fetchACInfo(context, ACInfo.TOGGLE_CONFIGURATION, configuration);
    }

    @Override
    public void fetchScenarioConfiguration(Context context, Scenario scenario) {
        ScenarioConfiguration configuration = new ScenarioConfiguration(scenario);
        fetchACInfo(context, ACInfo.SCENARIO_CONFIGURATION, configuration);
    }

    @Override
    public void setState(Context context, FeatureState state) {
        setACInfo(context, ACInfo.AC_FEATURE_STATE, state);
    }

    @Override
    public void setMode(Context context, int mode) {
        setACInfo(context, ACInfo.MODE, new Mode(mode));
    }

    @Override
    public void setLeakthroughGain(Context context, int leftValue, int rightValue) {
        LeakthroughGain gain = new LeakthroughGain(leftValue, rightValue);
        setACInfo(context, ACInfo.LEAKTHROUGH_GAIN, gain);
    }

    @Override
    public void setToggleConfiguration(Context context, int toggle, int value) {
        ToggleConfiguration configuration = new ToggleConfiguration(toggle, value);
        setACInfo(context, ACInfo.TOGGLE_CONFIGURATION, configuration);
    }

    @Override
    public void setScenarioConfiguration(Context context, int scenario, int value) {
        ScenarioConfiguration configuration = new ScenarioConfiguration(scenario, value);
        setACInfo(context, ACInfo.SCENARIO_CONFIGURATION, configuration);
    }

    @Override
    public void setDemoState(Context context, DemoState state) {
        setACInfo(context, ACInfo.DEMO_STATE, state);
    }

    @Override
    public void setAdaptationState(Context context, AdaptationState state) {
        setACInfo(context, ACInfo.ADAPTATION_STATE, state);
    }

    @Override
    public void setLeakthroughGainStep(Context context, int step) {
        setACInfo(context, ACInfo.LEAKTHROUGH_GAIN_STEP, step);
    }

    @Override
    public void setLeftRightBalance(Context context, LeftRightBalance balance) {
        setACInfo(context, ACInfo.LEFT_RIGHT_BALANCE, balance);
    }

    @Override
    public void setWindNoiseDetectionState(Context context, State state) {
        setACInfo(context, ACInfo.WIND_NOISE_DETECTION_STATE, state);
    }

    @Override
    public void setAutoTransparencyState(Context context, State state) {
        setACInfo(context, ACInfo.AUTO_TRANSPARENCY_STATE, state);
    }

    @Override
    public void setAutoTransparencyReleaseTime(Context context, AutoTransparencyReleaseTime time) {
        setACInfo(context, ACInfo.AUTO_TRANSPARENCY_RELEASE_TIME, time);
    }

    @Override
    public void setHowlingDetectionState(Context context, State state) {
        setACInfo(context, ACInfo.HOWLING_DETECTION_STATE, state);
    }

    @Override
    public void setNoiseIdState(Context context, State state) {
        setACInfo(context, ACInfo.NOISE_ID_STATE, state);
    }

    @Override
    public void setAdverseAcousticState(Context context, State state) {
        setACInfo(context, ACInfo.ADVERSE_ACOUSTIC_STATE, state);
    }

    private void setACInfo(Context context, ACInfo info, Object data) {
        SetAudioCurationRequest request = new SetAudioCurationRequest(null, info, data);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    private void fetchACInfo(Context context, ACInfo info) {
        GetACInfoRequest request = new GetACInfoRequest(info);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    private void fetchACInfo(Context context, ACInfo info, Object parameters) {
        GetACInfoRequest request = new GetACInfoRequest(info, parameters);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    private void onAudioCurationInfo(ACInfo info, Object update) {
        switch (info) {
            case AC_FEATURE_STATE:
                if (update instanceof FeatureState) {
                    FeatureState state = (FeatureState) update;
                    updateState(state);
                }
                break;

            case MODES_COUNT:
                if (update instanceof Integer) {
                    int count = (int) update;
                    updateModeCount(count);
                }
                break;

            case MODE:
                if (update instanceof Mode) {
                    Mode mode = (Mode) update;
                    updateCurrentMode(mode);
                }
                break;

            case FEED_FORWARD_GAIN:
                if (update instanceof Gain) {
                    Gain gain = (Gain) update;
                    updateFeedForwardGain(gain);
                }
                break;

            case TOGGLES_COUNT:
                if (update instanceof Integer) {
                    int count = (int) update;
                    updateTogglesCount(count);
                }
                break;

            case TOGGLE_CONFIGURATION:
                if (update instanceof ToggleConfiguration) {
                    ToggleConfiguration configuration = (ToggleConfiguration) update;
                    updateToggleConfiguration(configuration);
                }
                break;

            case SCENARIO_CONFIGURATION:
                if (update instanceof ScenarioConfiguration) {
                    ScenarioConfiguration configuration = (ScenarioConfiguration) update;
                    updateScenarioConfiguration(configuration);
                }
                break;

            case DEMO_SUPPORT:
                if (update instanceof Support) {
                    Support support = (Support) update;
                    updateDemoSupport(support);
                }
                break;

            case DEMO_STATE:
                if (update instanceof DemoState) {
                    DemoState state = (DemoState) update;
                    updateDemoState(state);
                }
                break;

            case ADAPTATION_STATE:
                if (update instanceof AdaptationState) {
                    AdaptationState state = (AdaptationState) update;
                    updateAdaptationState(state);
                }
                break;

            case LEAKTHROUGH_GAIN_CONFIGURATION:
                if (update instanceof LeakthroughGainConfiguration) {
                    LeakthroughGainConfiguration configuration = (LeakthroughGainConfiguration) update;
                    updateLeakthroughGainConfiguration(configuration);
                }
                break;

            case LEAKTHROUGH_GAIN_STEP:
                if (update instanceof LeakthroughGainStep) {
                    LeakthroughGainStep step = (LeakthroughGainStep) update;
                    updateLeakthroughGainStep(step);
                }
                break;

            case LEFT_RIGHT_BALANCE:
                if (update instanceof LeftRightBalance) {
                    LeftRightBalance balance = (LeftRightBalance) update;
                    updateLeftRightBalance(balance);
                }
                break;

            case WIND_NOISE_DETECTION_SUPPORT:
                if (update instanceof Support) {
                    Support support = (Support) update;
                    updateWindNoiseDetectionSupport(support);
                }
                break;

            case WIND_NOISE_DETECTION_STATE:
                if (update instanceof State) {
                    State state = (State) update;
                    updateWindNoiseDetectionState(state);
                }
                break;

            case WIND_NOISE_REDUCTION:
                if (update instanceof WindNoiseReduction) {
                    WindNoiseReduction reduction = (WindNoiseReduction) update;
                    updateWindNoiseReduction(reduction);
                }
                break;

            case AUTO_TRANSPARENCY_SUPPORT:
                if (update instanceof Support) {
                    Support support = (Support) update;
                    updateAutoTransparencySupport(support);
                }
                break;

            case AUTO_TRANSPARENCY_STATE:
                if (update instanceof State) {
                    State state = (State) update;
                    updateAutoTransparencyState(state);
                }
                break;

            case AUTO_TRANSPARENCY_RELEASE_TIME:
                if (update instanceof AutoTransparencyReleaseTime) {
                    AutoTransparencyReleaseTime time = (AutoTransparencyReleaseTime) update;
                    updateAutoTransparencyReleaseTime(time);
                }
                break;

            case HOWLING_DETECTION_SUPPORT:
                if (update instanceof Support) {
                    Support support = (Support) update;
                    updateHowlingDetectionSupport(support);
                }
                break;

            case HOWLING_DETECTION_STATE:
                if (update instanceof State) {
                    State state = (State) update;
                    updateHowlingDetectionState(state);
                }
                break;

            case FEEDBACK_GAIN:
                if (update instanceof Gain) {
                    Gain gain = (Gain) update;
                    updateFeedbackGain(gain);
                }
                break;

            case NOISE_ID_SUPPORT:
                if (update instanceof Support) {
                    Support support = (Support) update;
                    updateNoiseIdSupport(support);
                }
                break;

            case NOISE_ID_STATE:
                if (update instanceof State) {
                    State state = (State) update;
                    updateNoiseIdState(state);
                }
                break;

            case NOISE_ID_CATEGORY:
                if (update instanceof NoiseIdCategory) {
                    NoiseIdCategory category = (NoiseIdCategory) update;
                    updateNoiseIdCategory(category);
                }
                break;

            case ADVERSE_ACOUSTIC_SUPPORT:
                if (update instanceof Support) {
                    Support support = (Support) update;
                    updateAdverseAcousticSupport(support);
                }
                break;

            case ADVERSE_ACOUSTIC_STATE:
                if (update instanceof State) {
                    State state = (State) update;
                    updateAdverseAcousticState(state);
                }
                break;

            case ADVERSE_ACOUSTIC_GAIN_REDUCTION:
                if (update instanceof GainReduction) {
                    GainReduction reduction = (GainReduction) update;
                    updateAdverseAcousticGainReduction(reduction);
                }
                break;

            case HOWLING_CONTROL_GAIN_REDUCTION:
                if (update instanceof GainReduction) {
                    GainReduction reduction = (GainReduction) update;
                    updateHowlingControlGainReduction(reduction);
                }
                break;

            case FILTER_TOPOLOGY:
                if (update instanceof FilterTopology) {
                    FilterTopology topology = (FilterTopology) update;
                    updateFilterTopology(topology);
                }
                break;
        }
    }
}
