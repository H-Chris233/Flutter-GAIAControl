/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.gestureconfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Singleton;

@Singleton
public abstract class GestureConfigurationRepositoryData implements GestureConfigurationRepository {

    private final StickyLiveData<Set<Gesture>> gestures = new StickyLiveData<>();

    private final StickyLiveData<Set<GestureContext>> contexts = new StickyLiveData<>();

    private final StickyLiveData<Set<Action>> actions = new StickyLiveData<>();

    private final StickyLiveData<TouchpadConfiguration> touchpadConfiguration = new StickyLiveData<>();

    private final ConcurrentHashMap<Gesture, StickyLiveData<Set<Configuration>>> configurations =
            new ConcurrentHashMap<>();

    @Override
    public void reset() {
        gestures.setValue(null);
        contexts.setValue(null);
        actions.setValue(null);
        touchpadConfiguration.setValue(null);
        configurations.clear();
    }

    @Override
    public boolean hasData(GestureConfigurationInfo info) {
        switch (info) {
            case SUPPORTED_GESTURES:
                return gestures.getValue() != null;
            case SUPPORTED_CONTEXTS:
                return contexts.getValue() != null;
            case SUPPORTED_ACTIONS:
                return actions.getValue() != null;
            case TOUCHPAD_CONFIGURATION:
                return touchpadConfiguration.getValue() != null;

            case GET_GESTURE_CONFIGURATION: // use fetchConfiguration instead
            case SET_GESTURE_CONFIGURATION:
            case RESET:
                return true; // no data for those
        }
        return false;
    }

    @Override
    public boolean hasConfiguration(Gesture gesture) {
        StickyLiveData<Set<Configuration>> liveData = getConfigurationsLiveData(gesture);
        return liveData.getValue() != null;
    }

    @NonNull
    @Override
    public LiveData<Set<Gesture>> getGesturesLiveData() {
        return gestures;
    }

    @NonNull
    @Override
    public LiveData<Set<GestureContext>> getContextsLiveData() {
        return contexts;
    }

    @NonNull
    @Override
    public LiveData<Set<Action>> getActionsLiveData() {
        return actions;
    }

    @NonNull
    @Override
    public LiveData<TouchpadConfiguration> getTouchpadTypeLiveData() {
        return touchpadConfiguration;
    }

    @NonNull
    @Override
    public LiveData<Set<Configuration>> getConfigurationLiveData(Gesture gesture) {
        return getConfigurationsLiveData(gesture);
    }

    protected void updateGestures(Set<Gesture> update) {
        gestures.postValue(update);
    }

    protected void updateContexts(Set<GestureContext> update) {
        contexts.postValue(update);
    }

    protected void updateActions(Set<Action> update) {
        actions.postValue(update);
    }

    protected void updateConfigurations(Gesture gesture, Set<Configuration> update) {
        StickyLiveData<Set<Configuration>> liveData = getConfigurationsLiveData(gesture);
        liveData.postValue(update);
    }

    protected void resetConfigurations() {
        Set<Gesture> fetched = clearConfigurations();
        for (Gesture gesture : fetched) {
            fetchConfiguration(null, gesture);
        }
    }

    protected void updateTouchpadConfiguration(TouchpadConfiguration update) {
        touchpadConfiguration.postValue(update);
    }

    protected Set<Configuration> getConfigurations(Gesture gesture) {
        StickyLiveData<Set<Configuration>> liveData = getConfigurationsLiveData(gesture);
        return copySet(liveData.getValue());
    }

    @NonNull
    private StickyLiveData<Set<Configuration>> getConfigurationsLiveData(Gesture gesture) {
        StickyLiveData<Set<Configuration>> liveData = configurations.get(gesture);

        if (liveData == null) {
            liveData = new StickyLiveData<>();
            configurations.put(gesture, liveData);
        }

        return liveData;
    }

    @NonNull
    private Set<Gesture> clearConfigurations() {
        Set<Gesture> fetched = new LinkedHashSet<>();
        for (Map.Entry<Gesture, StickyLiveData<Set<Configuration>>> entry : configurations.entrySet()) {
            fetched.add(entry.getKey());
            entry.getValue().postValue(new LinkedHashSet<>());
        }
        return fetched;
    }

    private static <T> Set<T> copySet(@Nullable Set<T> origin) {
        return origin == null ? new LinkedHashSet<>() : new LinkedHashSet<>(origin);
    }
}
