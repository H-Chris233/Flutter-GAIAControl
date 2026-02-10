/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.anclegacy;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;
import com.qualcomm.qti.gaiaclient.repository.anclegacy.ANCModeLegacy;
import com.qualcomm.qti.gaiaclient.repository.anclegacy.ANCRepositoryLegacy;
import com.qualcomm.qti.gaiaclient.repository.anclegacy.AdaptiveStatesLegacy;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ANCLegacyViewModel extends SettingsViewModel<ANCLegacySettings, ANCLegacyViewData> {

    private static final ANCInfo[] SUPPORTED_INFO =
            new ANCInfo[]{ANCInfo.ANC_STATE, ANCInfo.ADAPTIVE_STATE, ANCInfo.LEAKTHROUGH_GAIN,
                          ANCInfo.ADAPTED_GAIN, ANCInfo.ANC_MODE_COUNT, ANCInfo.ANC_MODE};

    private final ANCRepositoryLegacy ancRepositoryLegacy;

    private final FeaturesRepository featuresRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public ANCLegacyViewModel(@NonNull Application application, ANCRepositoryLegacy ancRepositoryLegacy,
                              ConnectionRepository connectionRepository, FeaturesRepository featuresRepository) {
        super(application, connectionRepository);
        this.ancRepositoryLegacy = ancRepositoryLegacy;
        this.featuresRepository = featuresRepository;

        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    @Override
    protected ANCLegacyViewData initViewData(Context context) {
        return new ANCLegacyViewData(context);
    }

    @Override
    protected final void onConnectionStateUpdated(ConnectionState state) {
        // override to avoid the connection state to change the enable state of the settings
    }

    public void updateData() {
        for (ANCInfo info : SUPPORTED_INFO) {
            if (!ancRepositoryLegacy.hasData(info)) {
                ancRepositoryLegacy.fetchData(getApplication().getApplicationContext(), info);
            }
        }
    }

    public void setState(boolean state) {
        ancRepositoryLegacy.setState(getApplication(), state);
    }

    public void setMode(ANCModeLegacy mode) {
        ancRepositoryLegacy.setMode(getApplication(), mode);
    }

    public void setLeakthroughGain(long gain) {
        if (gain < Gain.GAIN_MIN || Gain.GAIN_MAX < gain) {
            // Gain must be between GAIN_MIN and GAIN_MAX
            String title = getApplication().getString(R.string.legacy_anc_gain_alert_out_of_range_title);
            String message =
                    getApplication().getString(R.string.legacy_anc_gain_alert_out_of_range_message,
                                               Gain.GAIN_MIN, Gain.GAIN_MAX, gain);
            int alternativeValue = gain < Gain.GAIN_MIN ? Gain.GAIN_MIN : Gain.GAIN_MAX;
            String positiveLabel =
                    getApplication().getString(R.string.anc_gain_alert_out_of_range_positive_label, alternativeValue);
            setAlert(title, message, positiveLabel, (dialogInterface, i) ->
                    ancRepositoryLegacy.setLeakthroughGain(ANCLegacyViewModel.this.getApplication(), alternativeValue));
            return;
        }

        // gain value is within the bounds, it can be set on the device
        ancRepositoryLegacy.setLeakthroughGain(getApplication(), (int) gain);
    }

    private void onFeatures(ArrayMap<QTILFeature, Integer> features) {
        boolean supported = features != null && features.containsKey(QTILFeature.ANC);
        if (supported) {
            updateData();
        }

        setEnable(supported);
    }

    private void onState(Boolean data) {
        if (data != null) {
            setSettingSwitch(ANCLegacySettings.STATE, data);
            setSettingVisibility(ANCLegacySettings.CATEGORY_STATE_DEPENDANT, data);
        }
    }

    private void onAdaptiveStates(AdaptiveStatesLegacy states) {
        if (states == null) {
            return;
        }

        // adaptive is supported, showing related data
        setSettingVisibility(ANCLegacySettings.ADAPTIVE_STATE, true);

        // leakthrough gain and adapted gain are mutually exclusive
        // in the case of full static adapted gain is hidden and leakthrough gain is shown
        boolean isDisabled = states.isAdaptiveDisabled();
        setSettingVisibility(ANCLegacySettings.ADAPTED_GAIN, !isDisabled);
        setSettingVisibility(ANCLegacySettings.LEAKTHROUGH_GAIN, isDisabled);

        String stateLabel = getStateLabel(states);
        setSettingSummary(ANCLegacySettings.ADAPTIVE_STATE, stateLabel);

        String gainLabel = getGainLabel(states);
        setSettingSummary(ANCLegacySettings.ADAPTED_GAIN, gainLabel);
    }

    private void onSupportedModes(ANCModeLegacy[] modes) {
        if (modes != null) {
            Pair<String[], String[]> entries = ANCModeLegacy.getANCModesEntries(getApplication(), modes);
            setSettingList(ANCLegacySettings.MODE, entries);
        }
    }

    protected void onMode(ANCModeLegacy mode) {
        if (mode != null) {
            String label = mode.getLabel(getApplication());
            setSettingSummary(ANCLegacySettings.MODE, label);
            String value = mode.toString();
            setSettingValue(ANCLegacySettings.MODE, value);
            // leakthrough can only be set if mode is != 0
            setSettingEnable(ANCLegacySettings.LEAKTHROUGH_GAIN, mode.getValue() != 0);
        }
    }

    protected void onLeakthroughGain(Gain gain) {
        if (gain != null) {
            String valueToString = String.valueOf(gain.getValue());
            setSettingSummary(ANCLegacySettings.LEAKTHROUGH_GAIN, valueToString);
            setSettingValue(ANCLegacySettings.LEAKTHROUGH_GAIN, valueToString);
        }
    }

    private String getGainLabel(AdaptiveStatesLegacy states) {
        return getApplication().getString(R.string.settings_legacy_anc_adaptive_label,
                                          getGainLabel(states, EarbudPosition.LEFT),
                                          getGainLabel(states, EarbudPosition.RIGHT));
    }

    private String getGainLabel(AdaptiveStatesLegacy states, EarbudPosition position) {
        return states != null && states.isAdaptive(position) ? String.valueOf(states.getGainValue(position))
                                                             : getApplication()
                       .getString(R.string.legacy_anc_state_disabled);
    }

    private String getStateLabel(AdaptiveStatesLegacy states) {
        return getApplication().getString(R.string.settings_legacy_anc_adaptive_label,
                                          getStateLabel(states.getState(EarbudPosition.LEFT)),
                                          getStateLabel(states.getState(EarbudPosition.RIGHT)));
    }

    private String getStateLabel(AdaptiveState state) {
        int resourceId = state == AdaptiveState.ENABLED ? R.string.legacy_anc_state_enabled :
                         state == AdaptiveState.DISABLED ? R.string.legacy_anc_state_disabled :
                         R.string.legacy_anc_state_unknown;
        return getApplication().getString(resourceId);
    }

    private class DataObservers extends Observers {
        private final Observer<Boolean> stateObserver = ANCLegacyViewModel.this::onState;
        private final Observer<AdaptiveStatesLegacy> adaptiveStatesObserver = ANCLegacyViewModel.this::onAdaptiveStates;
        private final Observer<ANCModeLegacy> modeObserver = ANCLegacyViewModel.this::onMode;
        private final Observer<ANCModeLegacy[]> supportedModesObserver = ANCLegacyViewModel.this::onSupportedModes;
        private final Observer<Gain> leakthroughGainObserver = ANCLegacyViewModel.this::onLeakthroughGain;
        private final Observer<ArrayMap<QTILFeature, Integer>> featuresObserver = ANCLegacyViewModel.this::onFeatures;

        @Override
        protected void registerObservers() {
            featuresRepository.getFeaturesLiveData().observeForever(featuresObserver);
            ancRepositoryLegacy.getStateLiveData().observeForever(stateObserver);
            ancRepositoryLegacy.getAdaptiveStatesLiveData().observeForever(adaptiveStatesObserver);
            ancRepositoryLegacy.getModeLiveData().observeForever(modeObserver);
            ancRepositoryLegacy.getSupportedModesLiveData().observeForever(supportedModesObserver);
            ancRepositoryLegacy.getLeakthroughGainLiveData().observeForever(leakthroughGainObserver);
        }

        @Override
        protected void unregisterObservers() {
            featuresRepository.getFeaturesLiveData().removeObserver(featuresObserver);
            ancRepositoryLegacy.getStateLiveData().removeObserver(stateObserver);
            ancRepositoryLegacy.getAdaptiveStatesLiveData().removeObserver(adaptiveStatesObserver);
            ancRepositoryLegacy.getModeLiveData().removeObserver(modeObserver);
            ancRepositoryLegacy.getSupportedModesLiveData().removeObserver(supportedModesObserver);
            ancRepositoryLegacy.getLeakthroughGainLiveData().removeObserver(leakthroughGainObserver);
        }
    }
}
