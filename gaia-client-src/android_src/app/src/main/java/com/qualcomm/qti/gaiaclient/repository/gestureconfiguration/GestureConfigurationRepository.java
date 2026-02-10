/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.gestureconfiguration;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;

import java.util.Set;

public interface GestureConfigurationRepository {

    void init();

    void reset();

    @NonNull
    LiveData<Set<Gesture>> getGesturesLiveData();

    @NonNull
    LiveData<Set<GestureContext>> getContextsLiveData();

    @NonNull
    LiveData<Set<Action>> getActionsLiveData();

    @NonNull
    LiveData<TouchpadConfiguration> getTouchpadTypeLiveData();

    @NonNull
    LiveData<Set<Configuration>> getConfigurationLiveData(Gesture gesture);

    boolean hasData(GestureConfigurationInfo info);

    boolean hasConfiguration(Gesture gesture);

    void fetchData(Context context, GestureConfigurationInfo info);

    void fetchConfiguration(Context context, Gesture gesture);

    void resetToDefaults(Context context);

    void setGestureConfigurations(Context context, Gesture gesture, Set<Configuration> configurations);
}
