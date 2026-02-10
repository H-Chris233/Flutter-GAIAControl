/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.anclegacy;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.ANCState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptedGain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveStateInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ANCSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetANCInfoRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetANCRequest;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class ANCRepositoryLegacyImpl extends ANCRepositoryLegacyData {

    private final ANCSubscriber mSubscriber = new ANCSubscriber() {
        @Override
        public void onANCInfo(ANCInfo info, Object update) {
            ANCRepositoryLegacyImpl.this.onANCInfo(info, update);
        }

        @Override
        public void onANCError(ANCInfo info, Reason reason) {
        }
    };

    @Inject
    public ANCRepositoryLegacyImpl() {
    }

    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mSubscriber);
    }

    public void fetchData(Context context, ANCInfo info) {
        if (info.hasGetter()) {
            fetchANCInfo(context, info);
        }
    }

    public void setState(Context context, boolean state) {
        setInfo(context, ANCInfo.ANC_STATE, state ? ANCState.ENABLE : ANCState.DISABLE);
    }

    @Override
    public void setMode(Context context, ANCModeLegacy mode) {
        setInfo(context, ANCInfo.ANC_MODE, mode);
    }

    public void setLeakthroughGain(Context context, int value) {
        Gain gain = new Gain(value);
        setInfo(context, ANCInfo.LEAKTHROUGH_GAIN, gain);
    }

    private void setInfo(Context context, ANCInfo info, Object data) {
        SetANCRequest request = new SetANCRequest(null, info, data);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    private void fetchANCInfo(Context context, ANCInfo info) {
        GetANCInfoRequest request = new GetANCInfoRequest(info);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    private void onANCInfo(ANCInfo info, Object update) {
        switch (info) {
            case ANC_STATE:
                if (update instanceof ANCState) {
                    ANCState state = (ANCState) update;
                    updateState(state == ANCState.ENABLE);
                }
                break;

            case ADAPTIVE_STATE:
                if (update instanceof AdaptiveStateInfo) {
                    AdaptiveStateInfo state = (AdaptiveStateInfo) update;
                    updateAdaptiveState(state);
                }
                break;

            case ANC_MODE:
                if (update instanceof Integer) {
                    ANCModeLegacy mode = ANCModeLegacy.valueOf((int) update);
                    updateCurrentMode(mode);
                }
                break;

            case ANC_MODE_COUNT:
                if (update instanceof Integer) {
                    int count = (int) update;
                    updateSupportedModes(count);
                }
                break;

            case ADAPTED_GAIN:
                if (update instanceof AdaptedGain) {
                    AdaptedGain gain = (AdaptedGain) update;
                    updateAdaptiveGain(gain);
                }
                break;

            case LEAKTHROUGH_GAIN:
                if (update instanceof Gain) {
                    Gain gain = (Gain) update;
                    updateLeakthroughGain(gain);
                }
                break;
        }
    }

    private void updateAdaptiveState(@NonNull AdaptiveStateInfo state) {
        AdaptiveStatesLegacy states = getAdaptiveStates();
        states.updateState(state);
        updateAdaptiveState(states);
    }

    private void updateAdaptiveGain(@NonNull AdaptedGain gain) {
        AdaptiveStatesLegacy states = getAdaptiveStates();
        states.updateGain(gain);
        updateAdaptiveState(states);
    }
}
