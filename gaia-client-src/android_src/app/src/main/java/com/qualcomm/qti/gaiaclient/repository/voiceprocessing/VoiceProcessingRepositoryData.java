/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.voiceprocessing;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCBypassMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCOperationMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Capability;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import java.util.ArrayList;

import javax.inject.Singleton;

@Singleton
public abstract class VoiceProcessingRepositoryData implements VoiceProcessingRepository {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final StickyLiveData<ArrayList<VoiceEnhancement>> enhancements = new StickyLiveData<>();

    private final StickyLiveData<CVCOperationMode> operationMode = new StickyLiveData<>();

    private final StickyLiveData<CVCBypassMode> bypassMode = new StickyLiveData<>();

    private final StickyLiveData<Integer> microphoneMode = new StickyLiveData<>();

    private final StickyLiveData<Pair<VoiceProcessingInfo, Reason>> error = new StickyLiveData<>();

    @Override
    public void reset() {
        enhancements.setValue(null);
        operationMode.setValue(null);
        bypassMode.setValue(null);
        microphoneMode.setValue(null);
        error.setValue(null);
    }

    @NonNull
    @Override
    public LiveData<ArrayList<VoiceEnhancement>> getEnhancementsLiveData() {
        return enhancements;
    }

    @NonNull
    @Override
    public LiveData<CVCOperationMode> getOperationModeLiveData() {
        return operationMode;
    }

    @NonNull
    @Override
    public LiveData<CVCBypassMode> getBypassModeLiveData() {
        return bypassMode;
    }

    @NonNull
    @Override
    public LiveData<Integer> getMicrophoneModeLiveData() {
        return microphoneMode;
    }

    @NonNull
    @Override
    public LiveData<Pair<VoiceProcessingInfo, Reason>> getErrorLiveData() {
        return error;
    }

    @Override
    public boolean hasData(VoiceProcessingInfo info) {
        return enhancements.getValue() != null;
    }

    @Override
    public boolean hasCapability(Capability capability) {
        ArrayList<VoiceEnhancement> list = enhancements.getValue();
        return list != null && list.stream().anyMatch(enhancement -> enhancement.getCapability() == capability);
    }

    @Override
    public boolean hasData(Capability capability) {
        return capability == Capability.CVC_3MIC
                && getMicrophoneMode() >= 0 && getOperationMode() != null && getBypassMode() != null;
    }

    protected void updateEnhancements(ArrayList<VoiceEnhancement> enhancements) {
        this.enhancements.postValue(enhancements);
    }

    protected void updateOperationMode(CVCOperationMode mode) {
        operationMode.postValue(mode);
    }

    protected void updateBypassMode(CVCBypassMode mode) {
        bypassMode.postValue(mode);
    }

    protected void updateMicrophoneMode(int mode) {
        microphoneMode.postValue(mode);
    }

    protected void updateAndClearError(VoiceProcessingInfo info, Reason reason) {
        error.setValue(new Pair<>(info, reason)); // must be consumed
        handler.post(() -> error.setValue(null)); // slightly delay the clearance for observers
    }
}
