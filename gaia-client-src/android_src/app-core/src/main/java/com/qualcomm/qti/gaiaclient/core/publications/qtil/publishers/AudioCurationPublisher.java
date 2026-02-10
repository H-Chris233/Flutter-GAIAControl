/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
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
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ScenarioConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.State;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Support;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.ToggleConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.WindNoiseReduction;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.AudioCurationSubscriber;

public class AudioCurationPublisher extends Publisher<AudioCurationSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.AUDIO_CURATION;
    }

    public void publishState(FeatureState state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.AC_FEATURE_STATE, state));
    }

    public void publishModesCount(int count) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.MODES_COUNT, count));
    }

    public void publishCurrentMode(Mode mode) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.MODE, mode));
    }

    public void publishFeedForwardGain(Gain gain) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.FEED_FORWARD_GAIN, gain));
    }

    public void publishTogglesCount(int count) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.TOGGLES_COUNT, count));
    }

    public void publishToggleConfiguration(ToggleConfiguration configuration) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.TOGGLE_CONFIGURATION, configuration));
    }

    public void publishScenarioConfiguration(ScenarioConfiguration configuration) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.SCENARIO_CONFIGURATION, configuration));
    }

    public void publishDemoSupport(Support support) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.DEMO_SUPPORT, support));
    }

    public void publishDemoState(DemoState state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.DEMO_STATE, state));
    }

    public void publishAdaptationState(AdaptationState state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.ADAPTATION_STATE, state));
    }

    public void publishLeakthroughGainConfiguration(LeakthroughGainConfiguration configuration) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.LEAKTHROUGH_GAIN_CONFIGURATION, configuration));
    }

    public void publishLeakthroughGainStep(LeakthroughGainStep step) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.LEAKTHROUGH_GAIN_STEP, step));
    }

    public void publishLeftRightBalance(LeftRightBalance balance) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.LEFT_RIGHT_BALANCE, balance));
    }

    public void publishWindNoiseDetectionSupport(Support support) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.WIND_NOISE_DETECTION_SUPPORT, support));
    }

    public void publishWindNoiseDetectionState(State state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.WIND_NOISE_DETECTION_STATE, state));
    }

    public void publishWindNoiseReduction(WindNoiseReduction reduction) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.WIND_NOISE_REDUCTION, reduction));
    }

    public void publishAutoTransparencySupport(Support support) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.AUTO_TRANSPARENCY_SUPPORT, support));
    }

    public void publishAutoTransparencyState(State state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.AUTO_TRANSPARENCY_STATE, state));
    }

    public void publishAutoTransparencyReleaseTime(AutoTransparencyReleaseTime time) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.AUTO_TRANSPARENCY_RELEASE_TIME, time));
    }

    public void publishHowlingDetectionSupport(Support support) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.HOWLING_DETECTION_SUPPORT, support));
    }

    public void publishHowlingDetectionState(State state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.HOWLING_DETECTION_STATE, state));
    }

    public void publishFeedbackGain(Gain gain) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.FEEDBACK_GAIN, gain));
    }

    public void publishNoiseIdSupport(Support support) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.NOISE_ID_SUPPORT, support));
    }

    public void publishNoiseIdState(State state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.NOISE_ID_STATE, state));
    }

    public void publishNoiseIdCategory(NoiseIdCategory category) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.NOISE_ID_CATEGORY, category));
    }

    public void publishAdverseAcousticSupport(Support support) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.ADVERSE_ACOUSTIC_SUPPORT, support));
    }

    public void publishAdverseAcousticState(State state) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.ADVERSE_ACOUSTIC_STATE, state));
    }

    public void publishAdverseAcousticGainReduction(GainReduction reduction) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.ADVERSE_ACOUSTIC_GAIN_REDUCTION, reduction));
    }

    public void publishHowlingControlGainReduction(GainReduction reduction) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.HOWLING_CONTROL_GAIN_REDUCTION, reduction));
    }

    public void publishFilterTopology(FilterTopology topology) {
        forEachSubscriber(subscriber -> subscriber.onInfo(ACInfo.FILTER_TOPOLOGY, topology));
    }

    public void publishError(ACInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }
}
