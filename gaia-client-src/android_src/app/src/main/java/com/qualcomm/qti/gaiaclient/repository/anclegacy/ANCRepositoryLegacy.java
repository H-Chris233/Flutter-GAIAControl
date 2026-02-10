/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.anclegacy;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;

public interface ANCRepositoryLegacy {

    void init();

    void fetchData(Context context, ANCInfo info);

    boolean hasData(ANCInfo info);

    @NonNull
    LiveData<Boolean> getStateLiveData();

    @NonNull
    LiveData<AdaptiveStatesLegacy> getAdaptiveStatesLiveData();

    @NonNull
    LiveData<ANCModeLegacy> getModeLiveData();

    @NonNull
    LiveData<ANCModeLegacy[]> getSupportedModesLiveData();

    @NonNull
    LiveData<Gain> getLeakthroughGainLiveData();

    void setState(Context context, boolean state);

    void setMode(Context context, ANCModeLegacy mode);

    void setLeakthroughGain(Context context, int value);

    void reset();
}
