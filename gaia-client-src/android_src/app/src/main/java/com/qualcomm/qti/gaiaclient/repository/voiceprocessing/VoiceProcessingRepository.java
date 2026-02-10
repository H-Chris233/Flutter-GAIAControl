/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.voiceprocessing;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCBypassMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCOperationMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Capability;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;

import java.util.ArrayList;

import javax.annotation.Nullable;

public interface VoiceProcessingRepository {

    void init();

    void reset();

    void fetchEnhancements(Context context);

    void fetchConfiguration(Context context, Capability capability);

    @NonNull
    LiveData<ArrayList<VoiceEnhancement>> getEnhancementsLiveData();

    @NonNull
    LiveData<CVCOperationMode> getOperationModeLiveData();

    @NonNull
    LiveData<CVCBypassMode> getBypassModeLiveData();

    @NonNull
    LiveData<Integer> getMicrophoneModeLiveData();

    @NonNull
    LiveData<Pair<VoiceProcessingInfo, Reason>> getErrorLiveData();

    @Nullable
    default ArrayList<VoiceEnhancement> getEnhancements() {
        return getEnhancementsLiveData().getValue();
    }

    @Nullable
    default CVCOperationMode getOperationMode() {
        return getOperationModeLiveData().getValue();
    }

    @Nullable
    default CVCBypassMode getBypassMode() {
        return getBypassModeLiveData().getValue();
    }

    default int getMicrophoneMode() {
        Integer mode = getMicrophoneModeLiveData().getValue();
        return mode == null ? -1 : mode;
    }

    void setBypassMode(Context context, @NonNull CVCBypassMode mode);

    void setMicrophoneMode(Context context, int mode);

    boolean hasData(VoiceProcessingInfo info);

    boolean hasData(Capability capability);

    boolean hasCapability(Capability capability);
}
