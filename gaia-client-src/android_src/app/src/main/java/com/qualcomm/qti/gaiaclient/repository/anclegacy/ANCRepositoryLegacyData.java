/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.anclegacy;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import javax.inject.Singleton;

@Singleton
public abstract class ANCRepositoryLegacyData implements ANCRepositoryLegacy {

    private final StickyLiveData<Boolean> state = new StickyLiveData<>();

    private final StickyLiveData<AdaptiveStatesLegacy> adaptiveStates = new StickyLiveData<>();

    private final StickyLiveData<ANCModeLegacy> currentMode = new StickyLiveData<>();

    private final StickyLiveData<ANCModeLegacy[]> supportedModes = new StickyLiveData<>();

    private final StickyLiveData<Gain> leakthroughGain = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<Boolean> getStateLiveData() {
        return state;
    }

    @NonNull
    @Override
    public LiveData<AdaptiveStatesLegacy> getAdaptiveStatesLiveData() {
        return adaptiveStates;
    }

    @NonNull
    @Override
    public LiveData<ANCModeLegacy> getModeLiveData() {
        return currentMode;
    }

    @NonNull
    @Override
    public LiveData<ANCModeLegacy[]> getSupportedModesLiveData() {
        return supportedModes;
    }

    @NonNull
    @Override
    public LiveData<Gain> getLeakthroughGainLiveData() {
        return leakthroughGain;
    }

    @Override
    public void reset() {
        adaptiveStates.postValue(null);
        state.postValue(null);
        supportedModes.postValue(null);
        leakthroughGain.postValue(null);
        currentMode.postValue(null);
    }

    @Override
    public final boolean hasData(ANCInfo info) {
        switch (info) {
            case ANC_STATE:
                return state.getValue() != null;

            case ANC_MODE:
                return currentMode.getValue() != null;

            case ANC_MODE_COUNT:
                return supportedModes.getValue() != null;

            case LEAKTHROUGH_GAIN:
                return leakthroughGain.getValue() != null;

            case ADAPTIVE_STATE:
                AdaptiveStatesLegacy states = adaptiveStates.getValue();
                return states != null && states.getState(EarbudPosition.LEFT) != null
                        && states.getState(EarbudPosition.RIGHT) != null;

            case ADAPTED_GAIN:
                AdaptiveStatesLegacy statesForGain = adaptiveStates.getValue();
                return statesForGain != null && statesForGain.getGain(EarbudPosition.LEFT) != null
                        && statesForGain.getGain(EarbudPosition.RIGHT) != null;

            default:
                return false;
        }
    }

    @NonNull
    protected final AdaptiveStatesLegacy getAdaptiveStates() {
        AdaptiveStatesLegacy states = adaptiveStates.getValue();
        return states != null ? states : new AdaptiveStatesLegacy();
    }

    protected void updateState(boolean enabled) {
        state.postValue(enabled);
    }

    protected void updateAdaptiveState(AdaptiveStatesLegacy states) {
        adaptiveStates.postValue(states);
    }

    protected void updateCurrentMode(ANCModeLegacy mode) {
        currentMode.postValue(mode);
    }

    protected void updateSupportedModes(int count) {
        ANCModeLegacy[] modes = ANCModeLegacy.getModes(count);
        supportedModes.postValue(modes);
    }

    protected void updateLeakthroughGain(Gain gain) {
        leakthroughGain.postValue(gain);
    }

}
