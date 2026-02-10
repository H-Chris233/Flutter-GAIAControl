/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration;

import android.app.Application;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AdaptationState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.AutoTransparencyReleaseTime;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.DemoState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FeatureState;
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
import com.qualcomm.qti.gaiaclient.repository.audiocuration.AudioCurationRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

import java.util.ArrayList;
import java.util.List;

public abstract class AudioCurationViewModel<T extends Settings, VD extends SettingsViewData<T>>
        extends SettingsViewModel<T, VD> {

    private final AudioCurationRepository audioCurationRepository;

    private final FeaturesRepository featuresRepository;

    private final StickyLiveData<List<ModeViewData>> supportedModes = new StickyLiveData<>();

    private final DataObservers observers = new DataObservers();

    public AudioCurationViewModel(@NonNull Application application, AudioCurationRepository audioCurationRepository,
                                  ConnectionRepository connectionRepository, FeaturesRepository featuresRepository) {
        super(application, connectionRepository);
        this.audioCurationRepository = audioCurationRepository;
        this.featuresRepository = featuresRepository;
        new Handler().post(observers::start); // slight delay for child classes to be initialised
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    public void updateData() {
        for (ACInfo info : getInfoToSupport()) {
            if (info == ACInfo.SCENARIO_CONFIGURATION) {
                int version = featuresRepository.getFeatureVersion(QTILFeature.AUDIO_CURATION);
                for (Scenario scenario : Scenario.getValues()) {
                    if (scenario.getFeatureVersion() <= version) {
                        audioCurationRepository
                                .fetchScenarioConfiguration(getApplication().getApplicationContext(), scenario);
                    }
                }
            }
            else if (info == ACInfo.AC_FEATURE_STATE && !audioCurationRepository.hasData(ACInfo.AC_FEATURE_STATE)) {
                for (Feature feature : Feature.getValues()) {
                    audioCurationRepository.fetchFeatureState(getApplication().getApplicationContext(), feature);
                }
            }
            else if (info == ACInfo.ADAPTATION_STATE
                    && audioCurationRepository.getDemoStateLiveData().getValue() == DemoState.IN) {
                fetchData(info);
            }
            else if (info != ACInfo.TOGGLE_CONFIGURATION /* fetched automatically here on receiving toggles count */
                    && info != ACInfo.ADVERSE_ACOUSTIC_GAIN_REDUCTION /* not fetchable */
                    && info != ACInfo.HOWLING_CONTROL_GAIN_REDUCTION /* not fetchable */) {
                fetchData(info);
            }
        }
    }

    public void fetchData(ACInfo info) {
        if (!audioCurationRepository.hasData(info)) {
            audioCurationRepository.fetchData(getApplication().getApplicationContext(), info);
        }
    }

    public void setState(Feature feature, boolean state) {
        audioCurationRepository.setState(getApplication(), new FeatureState(feature, State.valueOf(state)));
    }

    public void setMode(int mode) {
        audioCurationRepository.setMode(getApplication(), mode);
    }

    public void setLeakthroughGain(long gain) {
        if (gain < Gain.MIN || Gain.MAX < gain) {
            // Gain must be between GAIN_MIN and GAIN_MAX
            String title = getApplication().getString(R.string.audio_curation_gain_alert_out_of_range_title);
            String message =
                    getApplication().getString(R.string.audio_curation_gain_alert_out_of_range_message,
                                               Gain.MIN, Gain.MAX, gain);
            int value = gain < Gain.MIN ? Gain.MIN : Gain.MAX;
            String positiveLabel =
                    getApplication().getString(R.string.audio_curation_gain_alert_out_of_range_positive_label,
                                               value);
            setAlert(title, message, positiveLabel, (dialogInterface, i) ->
                    audioCurationRepository
                            .setLeakthroughGain(AudioCurationViewModel.this.getApplication(), value, value));
            return;
        }

        // gain value is within the bounds, it can be set on the device
        audioCurationRepository.setLeakthroughGain(getApplication(), (int) gain, (int) gain);
    }

    public void setToggleConfiguration(int toggle, int value) {
        audioCurationRepository.setToggleConfiguration(getApplication(), toggle, value);
    }

    public void setScenarioConfiguration(int scenario, int value) {
        audioCurationRepository.setScenarioConfiguration(getApplication(), scenario, value);
    }

    public void setDemoState(DemoState state) {
        audioCurationRepository.setDemoState(getApplication(), state);
    }

    public void setAdaptationState(boolean enable) {
        AdaptationState state = enable ? AdaptationState.RESUMED : AdaptationState.PAUSED;
        audioCurationRepository.setAdaptationState(getApplication(), state);
    }

    public void setLeakthroughGainStep(int step) {
        audioCurationRepository.setLeakthroughGainStep(getApplication(), step);
    }

    public void setLeftRightBalance(LeftRightBalance balance) {
        audioCurationRepository.setLeftRightBalance(getApplication(), balance);
    }

    public void setWindNoiseDetectionState(State state) {
        audioCurationRepository.setWindNoiseDetectionState(getApplication(), state);
    }

    public void setAutoTransparencyState(boolean enable) {
        State state = State.valueOf(enable);
        audioCurationRepository.setAutoTransparencyState(getApplication(), state);
    }

    public void setAutoTransparencyReleaseTime(AutoTransparencyReleaseTime time) {
        audioCurationRepository.setAutoTransparencyReleaseTime(getApplication(), time);
    }

    public void setHowlingDetectionState(boolean enable) {
        State state = State.valueOf(enable);
        audioCurationRepository.setHowlingDetectionState(getApplication(), state);
    }

    public void setAdverseAcousticState(boolean enable) {
        State state = enable ? State.Enabled.INSTANCE : State.Disabled.INSTANCE;
        audioCurationRepository.setAdverseAcousticState(getApplication(), state);
    }

    public void setNoiseIdState(boolean enable) {
        State state = State.valueOf(enable);
        audioCurationRepository.setNoiseIdState(getApplication(), state);
    }

    @Override
    protected final void onConnectionStateUpdated(ConnectionState state) {
        // override to avoid the connection state to change the enable state of the settings
    }

    protected void onState(FeatureState state) {
    }

    private void onModeCount(Integer count) {
        List<ModeViewData> modes = new ArrayList<>();
        count = count == null ? 0 : count;
        for (int i = 1; i <= count; i++) {
            modes.add(new ModeViewData(getApplication(), i));
        }
        supportedModes.postValue(modes);
        onSupportedModes(modes);
    }

    protected void onSupportedModes(List<ModeViewData> modes) {
    }

    protected void onMode(Mode mode) {
    }

    protected void onFeedForwardGain(Gain gain) {
    }

    private void onTogglesCount(Integer count) {
        if (count == null) {
            return;
        }

        observers.startForTogglesConfiguration(count);

        for (int toggle = 1; toggle <= count; toggle++) {
            audioCurationRepository.fetchToggleConfiguration(getApplication().getApplicationContext(), toggle);
        }
    }

    protected void onToggleConfiguration(ToggleConfiguration configuration) {
    }

    protected void onScenarioConfiguration(ScenarioConfiguration configuration) {
    }

    protected void onDemoSupport(Support support) {
    }

    protected void onDemoState(DemoState state) {
    }

    protected void onAdaptationState(AdaptationState state) {
    }

    protected void onLeakthroughConfiguration(LeakthroughGainConfiguration configuration) {

    }

    protected void onLeakthroughGainStep(LeakthroughGainStep step) {

    }

    protected void onLeftRightBalance(LeftRightBalance balance) {

    }

    protected void onWindNoiseDetectionSupport(Support support) {

    }

    protected void onWindNoiseDetectionState(State state) {

    }

    protected void onWindNoiseReduction(WindNoiseReduction reduction) {

    }

    protected void onAutoTransparencySupport(Support support) {

    }

    protected void onAutoTransparencyState(State state) {

    }

    protected void onAutoTransparencyReleaseTime(AutoTransparencyReleaseTime time) {

    }

    protected void onHowlingDetectionSupport(Support support) {

    }

    protected void onHowlingDetectionState(State state) {

    }

    protected void onFeedbackGain(Gain gain) {

    }

    protected void onNoiseIdSupport(Support support) {

    }

    protected void onNoiseIdState(State state) {

    }

    protected void onNoiseIdCategory(NoiseIdCategory category) {

    }

    protected void onAdverseAcousticSupport(Support support) {

    }

    protected void onAdverseAcousticState(State state) {

    }

    protected void onAdverseAcousticGainReduction(GainReduction reduction) {

    }

    protected void onHowlingControlGainReduction(GainReduction reduction) {

    }

    protected int getVersion() {
        return featuresRepository.getFeatureVersion(QTILFeature.AUDIO_CURATION);
    }

    protected boolean isANCEnabled() {
        FeatureState state = audioCurationRepository.getState();
        return state != null && Feature.ANC.equals(state.getFeature()) && State.Enabled.INSTANCE == state.getState();
    }

    protected Mode getMode() {
        return audioCurationRepository.getMode();
    }

    protected State getHowlingDetectionState() {
        return audioCurationRepository.getHowlingDetectionState();
    }

    protected WindNoiseReduction getWindNoiseReduction() {
        return audioCurationRepository.getWindNoiseReduction();
    }

    protected GainReduction getAdverseAcousticGainReduction() {
        return audioCurationRepository.getAdverseAcousticGainReduction();
    }

    protected GainReduction getHowlingControlGainReduction() {
        return audioCurationRepository.getHowlingControlGainReduction();
    }

    protected Integer getConfigurationSelection(Configuration configuration) {
        if (configuration == null) {
            return null;
        }

        switch (configuration.getInfo()) {
            case TOGGLE_CONFIGURATION:
                ToggleConfiguration toggleConfig =
                        audioCurationRepository.getToggleConfiguration(configuration.getToggle());
                return toggleConfig != null ? toggleConfig.getOptionValue() : null;

            case SCENARIO_CONFIGURATION:
                Scenario scenario = configuration.getScenario();
                ScenarioConfiguration scenarioConfig =
                        audioCurationRepository.getScenarioConfiguration(scenario);
                return scenarioConfig != null ? scenarioConfig.getOptionValue() : null;
        }

        return null;
    }

    protected Pair<String[], String[]> getConfigurationEntries(@NonNull Configuration configuration,
                                                               @Nullable List<ModeViewData> modes) {
        @NonNull
        List<ConfigurationOption> options = configuration.getOptions();

        int optionsCount = options.size();
        int modesCount = modes == null ? 0 : modes.size();
        int length = optionsCount + modesCount;

        String[] entries = new String[length];
        String[] values = new String[length];

        setConfigurationEntries(options, entries, values);
        int modeLabelResId = configuration.isToggle() ? R.string.audio_curation_mode_label
                                                      : R.string.settings_audio_curation_option_change_to_mode;
        setModesEntries(modes, entries, values, optionsCount, modeLabelResId);

        return new Pair<>(entries, values);
    }

    protected ModeViewData getMode(int value) {
        List<ModeViewData> modes = supportedModes.getValue();

        if (modes == null) {
            return null;
        }

        for (ModeViewData mode : modes) {
            if (mode.getValue() == value) {
                return mode;
            }
        }

        return null;
    }

    protected abstract ACInfo[] getInfoToSupport();

    protected abstract void onAudioCurationSupported(boolean supported, Integer version);

    private void onFeatures(ArrayMap<QTILFeature, Integer> features) {
        Integer version = features != null ? features.get(QTILFeature.AUDIO_CURATION) : null;
        boolean supported = version != null;
        if (supported) {
            updateData();
        }
        onAudioCurationSupported(supported, version);
    }

    private void setConfigurationEntries(List<ConfigurationOption> options, String[] entries, String[] values) {
        if (options == null || entries == null || values == null) {
            return;
        }

        int count = options.size();
        if (entries.length < count || values.length < count) {
            return;
        }

        for (int i = 0; i < count; i++) {
            ConfigurationOption option = options.get(i);
            entries[i] = option.getLabel(getApplication());
            values[i] = Integer.toString(option.getValue());
        }
    }

    private void setModesEntries(@Nullable List<ModeViewData> modes, String[] entries, String[] values, int offset,
                                 int labelResId) {
        if (modes == null || entries == null || values == null) {
            return;
        }

        int modesCount = modes.size();
        int minLength = offset + modesCount;
        if (entries.length < minLength || values.length < minLength) {
            return;
        }

        for (int i = 0; i < modesCount; i++) {
            ModeViewData mode = modes.get(i);
            int index = offset + i;
            entries[index] = getApplication().getString(labelResId, mode.getValue());
            values[index] = Integer.toString(mode.getValue());
        }
    }

    private class DataObservers extends Observers {
        private final Observer<ArrayMap<QTILFeature, Integer>> featuresObserver =
                AudioCurationViewModel.this::onFeatures;
        private final Observer<FeatureState> stateObserver = AudioCurationViewModel.this::onState;
        private final Observer<Integer> modeCountObserver = AudioCurationViewModel.this::onModeCount;
        private final Observer<Mode> modeObserver = AudioCurationViewModel.this::onMode;
        private final Observer<Gain> feedForwardGainObserver = AudioCurationViewModel.this::onFeedForwardGain;
        private final Observer<Integer> toggleCountObserver = AudioCurationViewModel.this::onTogglesCount;
        private int toggleObserversCount = 0;
        private final Observer<ToggleConfiguration> toggleConfigurationObserver =
                AudioCurationViewModel.this::onToggleConfiguration;
        private final Observer<ScenarioConfiguration> scenarioConfigurationObservers =
                AudioCurationViewModel.this::onScenarioConfiguration;
        private final Observer<Support> demoSupportObserver = AudioCurationViewModel.this::onDemoSupport;
        private final Observer<DemoState> demoStateObserver = AudioCurationViewModel.this::onDemoState;
        private final Observer<AdaptationState> adaptationStateObserver =
                AudioCurationViewModel.this::onAdaptationState;
        private final Observer<LeakthroughGainConfiguration> leakthroughGainConfigurationObserver =
                AudioCurationViewModel.this::onLeakthroughConfiguration;
        private final Observer<LeakthroughGainStep> leakthroughGainStepObserver =
                AudioCurationViewModel.this::onLeakthroughGainStep;
        private final Observer<LeftRightBalance> leftRightBalanceObserver =
                AudioCurationViewModel.this::onLeftRightBalance;
        private final Observer<Support> windNoiseDetectionSupportObserver =
                AudioCurationViewModel.this::onWindNoiseDetectionSupport;
        private final Observer<State> windNoiseDetectionStateObserver =
                AudioCurationViewModel.this::onWindNoiseDetectionState;
        private final Observer<WindNoiseReduction> windNoiseReductionObserver =
                AudioCurationViewModel.this::onWindNoiseReduction;
        private final Observer<Support> autoTransparencySupportObserver =
                AudioCurationViewModel.this::onAutoTransparencySupport;
        private final Observer<State> autoTransparencyStateObserver =
                AudioCurationViewModel.this::onAutoTransparencyState;
        private final Observer<AutoTransparencyReleaseTime> autoTransparencyReleaseTimeObserver =
                AudioCurationViewModel.this::onAutoTransparencyReleaseTime;
        private final Observer<Support> howlingDetectionSupportObserver =
                AudioCurationViewModel.this::onHowlingDetectionSupport;
        private final Observer<State> howlingDetectionStateObserver =
                AudioCurationViewModel.this::onHowlingDetectionState;
        private final Observer<Gain> feedbackGainObserver = AudioCurationViewModel.this::onFeedbackGain;
        private final Observer<Support> noiseIdSupportObserver = AudioCurationViewModel.this::onNoiseIdSupport;
        private final Observer<State> noiseIdStateObserver = AudioCurationViewModel.this::onNoiseIdState;
        private final Observer<NoiseIdCategory> noiseIdCategoryObserver =
                AudioCurationViewModel.this::onNoiseIdCategory;
        private final Observer<Support> adverseAcousticSupportObserver =
                AudioCurationViewModel.this::onAdverseAcousticSupport;
        private final Observer<State> adverseAcousticStateObserver =
                AudioCurationViewModel.this::onAdverseAcousticState;
        private final Observer<GainReduction> adverseAcousticGainReductionObserver =
                AudioCurationViewModel.this::onAdverseAcousticGainReduction;
        private final Observer<GainReduction> howlingControlGainReductionObserver =
                AudioCurationViewModel.this::onHowlingControlGainReduction;

        @Override
        protected void registerObservers() {
            featuresRepository.getFeaturesLiveData().observeForever(featuresObserver);
            audioCurationRepository.getStateLiveData().observeForever(stateObserver);
            audioCurationRepository.getModeCountLiveData().observeForever(modeCountObserver);
            audioCurationRepository.getModeLiveData().observeForever(modeObserver);
            audioCurationRepository.getFeedForwardGainLiveData().observeForever(feedForwardGainObserver);
            audioCurationRepository.getTogglesCountLiveData().observeForever(toggleCountObserver);
            for (Scenario scenario : Scenario.getValues()) {
                audioCurationRepository.getScenarioConfigurationLiveData(scenario)
                        .observeForever(scenarioConfigurationObservers);
            }
            audioCurationRepository.getDemoSupportLiveData().observeForever(demoSupportObserver);
            audioCurationRepository.getDemoStateLiveData().observeForever(demoStateObserver);
            audioCurationRepository.getAdaptationStateLiveData().observeForever(adaptationStateObserver);
            audioCurationRepository.getLeakthroughGainConfigurationLiveData()
                    .observeForever(leakthroughGainConfigurationObserver);
            audioCurationRepository.getLeakthroughGainStepLiveData().observeForever(leakthroughGainStepObserver);
            audioCurationRepository.getLeftRightBalanceLiveData().observeForever(leftRightBalanceObserver);
            audioCurationRepository.getWindNoiseDetectionSupportLiveData()
                    .observeForever(windNoiseDetectionSupportObserver);
            audioCurationRepository.getWindNoiseDetectionStateLiveData()
                    .observeForever(windNoiseDetectionStateObserver);
            audioCurationRepository.getWindNoiseReductionLiveData().observeForever(windNoiseReductionObserver);
            audioCurationRepository.getAutoTransparencySupportLiveData()
                    .observeForever(autoTransparencySupportObserver);
            audioCurationRepository.getAutoTransparencyStateLiveData().observeForever(autoTransparencyStateObserver);
            audioCurationRepository.getAutoTransparencyReleaseTimeLiveData()
                    .observeForever(autoTransparencyReleaseTimeObserver);
            audioCurationRepository.getHowlingDetectionSupportLiveData()
                    .observeForever(howlingDetectionSupportObserver);
            audioCurationRepository.getHowlingDetectionStateLiveData().observeForever(howlingDetectionStateObserver);
            audioCurationRepository.getFeedbackGainLiveData().observeForever(feedbackGainObserver);
            audioCurationRepository.getNoiseIdSupportLiveData().observeForever(noiseIdSupportObserver);
            audioCurationRepository.getNoiseIdStateLiveData().observeForever(noiseIdStateObserver);
            audioCurationRepository.getNoiseIdCategoryLiveData().observeForever(noiseIdCategoryObserver);
            audioCurationRepository.getAdverseAcousticSupportLiveData().observeForever(adverseAcousticSupportObserver);
            audioCurationRepository.getAdverseAcousticStateLiveData().observeForever(adverseAcousticStateObserver);
            audioCurationRepository.getAdverseAcousticGainReductionLiveData()
                    .observeForever(adverseAcousticGainReductionObserver);
            audioCurationRepository.getHowlingControlGainReductionLiveData()
                    .observeForever(observers.howlingControlGainReductionObserver);
        }

        @Override
        protected void unregisterObservers() {
            featuresRepository.getFeaturesLiveData().removeObserver(featuresObserver);
            audioCurationRepository.getStateLiveData().removeObserver(stateObserver);
            audioCurationRepository.getModeCountLiveData().removeObserver(modeCountObserver);
            audioCurationRepository.getModeLiveData().removeObserver(modeObserver);
            audioCurationRepository.getFeedForwardGainLiveData().removeObserver(feedForwardGainObserver);
            audioCurationRepository.getTogglesCountLiveData().removeObserver(toggleCountObserver);
            clearTogglesConfigurationObservers();
            for (Scenario scenario : Scenario.getValues()) {
                audioCurationRepository.getScenarioConfigurationLiveData(scenario)
                        .removeObserver(scenarioConfigurationObservers);
            }
            audioCurationRepository.getDemoSupportLiveData().removeObserver(demoSupportObserver);
            audioCurationRepository.getDemoStateLiveData().removeObserver(demoStateObserver);
            audioCurationRepository.getAdaptationStateLiveData().removeObserver(adaptationStateObserver);
            audioCurationRepository.getLeakthroughGainConfigurationLiveData()
                    .removeObserver(leakthroughGainConfigurationObserver);
            audioCurationRepository.getLeakthroughGainStepLiveData().removeObserver(leakthroughGainStepObserver);
            audioCurationRepository.getLeftRightBalanceLiveData().removeObserver(leftRightBalanceObserver);
            audioCurationRepository.getWindNoiseDetectionSupportLiveData()
                    .removeObserver(windNoiseDetectionSupportObserver);
            audioCurationRepository.getWindNoiseDetectionStateLiveData()
                    .removeObserver(windNoiseDetectionStateObserver);
            audioCurationRepository.getWindNoiseReductionLiveData().removeObserver(windNoiseReductionObserver);
            audioCurationRepository.getAutoTransparencySupportLiveData()
                    .removeObserver(autoTransparencySupportObserver);
            audioCurationRepository.getAutoTransparencyStateLiveData().removeObserver(autoTransparencyStateObserver);
            audioCurationRepository.getAutoTransparencyReleaseTimeLiveData()
                    .removeObserver(autoTransparencyReleaseTimeObserver);
            audioCurationRepository.getHowlingDetectionSupportLiveData()
                    .removeObserver(howlingDetectionSupportObserver);
            audioCurationRepository.getHowlingDetectionStateLiveData().removeObserver(howlingDetectionStateObserver);
            audioCurationRepository.getFeedbackGainLiveData().removeObserver(feedbackGainObserver);
            audioCurationRepository.getNoiseIdSupportLiveData().removeObserver(noiseIdSupportObserver);
            audioCurationRepository.getNoiseIdStateLiveData().removeObserver(noiseIdStateObserver);
            audioCurationRepository.getNoiseIdCategoryLiveData().removeObserver(noiseIdCategoryObserver);
            audioCurationRepository.getAdverseAcousticSupportLiveData().removeObserver(adverseAcousticSupportObserver);
            audioCurationRepository.getAdverseAcousticStateLiveData().removeObserver(adverseAcousticStateObserver);
            audioCurationRepository.getAdverseAcousticGainReductionLiveData()
                    .removeObserver(adverseAcousticGainReductionObserver);
            audioCurationRepository.getHowlingControlGainReductionLiveData()
                    .removeObserver(observers.howlingControlGainReductionObserver);
        }

        private void startForTogglesConfiguration(int count) {
            clearTogglesConfigurationObservers();

            for (int toggle = 1; toggle <= count; toggle++) {
                audioCurationRepository.getToggleConfigurationLiveData(toggle)
                        .observeForever(toggleConfigurationObserver);
            }
            toggleObserversCount = count;
        }

        private void clearTogglesConfigurationObservers() {
            for (int toggle = 1; toggle <= toggleObserversCount; toggle++) {
                audioCurationRepository.getToggleConfigurationLiveData(toggle)
                        .removeObserver(toggleConfigurationObserver);
            }
            toggleObserversCount = 0;
        }
    }

}
