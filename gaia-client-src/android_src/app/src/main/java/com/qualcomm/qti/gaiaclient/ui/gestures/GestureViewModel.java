/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepository;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;

import java.util.Arrays;
import java.util.Set;

public abstract class GestureViewModel extends AndroidViewModel {

    private final FeaturesRepository featuresRepository;

    private final GestureConfigurationRepository gestureConfigurationRepository;

    private final StickyLiveData<Boolean> isFeatureSupportedLiveData = new StickyLiveData<>(false);

    private final DataObservers observers = new DataObservers();

    public GestureViewModel(@NonNull Application application, FeaturesRepository featuresRepository,
                            GestureConfigurationRepository gestureConfigurationRepository) {
        super(application);
        this.featuresRepository = featuresRepository;
        this.gestureConfigurationRepository = gestureConfigurationRepository;

        // simulate a late init to allow child classes to be initialised
        new Handler().post(observers::start);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    public void updateData() {
        for (GestureConfigurationInfo info : getInfoToSupport()) {
            if (!gestureConfigurationRepository.hasData(info)) {
                gestureConfigurationRepository.fetchData(getContext(), info);
            }
        }
    }

    public void resetToDefault() {
        gestureConfigurationRepository.resetToDefaults(getContext());
    }

    public void setConfigurations(Gesture gesture, Set<Configuration> configurations) {
        gestureConfigurationRepository.setGestureConfigurations(getApplication(), gesture, configurations);
    }

    protected void onGestures(Set<Gesture> update) {
        if (update != null && Arrays.stream(getInfoToSupport())
                .anyMatch(info -> info == GestureConfigurationInfo.GET_GESTURE_CONFIGURATION)) {
            for (Gesture gesture : update) {
                observers.observeConfiguration(gesture);
                if (!gestureConfigurationRepository.hasConfiguration(gesture)) {
                    gestureConfigurationRepository.fetchConfiguration(getContext(), gesture);
                }
            }
        }
    }

    protected Context getContext() {
        return getApplication().getApplicationContext();
    }

    protected void onContexts(Set<GestureContext> update) {
    }

    protected void onActions(Set<Action> update) {
    }

    protected void onTouchpadConfiguration(TouchpadConfiguration touchpadConfiguration) {
    }

    protected void onConfiguration(Gesture gesture, Set<Configuration> update) {
    }

    protected void onFeatureSupport(boolean supported) {
        Boolean currentValue = isFeatureSupportedLiveData.getValue();
        if (currentValue == null || currentValue != supported) {
            this.isFeatureSupportedLiveData.setValue(supported);
        }
    }

    protected abstract GestureConfigurationInfo[] getInfoToSupport();

    private void onFeatures(ArrayMap<QTILFeature, Integer> features) {
        boolean supported = features != null && features.containsKey(QTILFeature.GESTURE_CONFIGURATION);
        if (supported) {
            updateData();
        }
        onFeatureSupport(supported);
    }

    public void observeFeatureSupported(LifecycleOwner owner, @NonNull Observer<Boolean> observer) {
        isFeatureSupportedLiveData.observe(owner, observer);
    }

    private class DataObservers extends Observers {
        private final Observer<ArrayMap<QTILFeature, Integer>> featuresObserver = GestureViewModel.this::onFeatures;
        private final Observer<Set<Gesture>> gesturesObserver = GestureViewModel.this::onGestures;
        private final Observer<Set<GestureContext>> contextsObserver = GestureViewModel.this::onContexts;
        private final Observer<Set<Action>> actionsObserver = GestureViewModel.this::onActions;
        private final Observer<TouchpadConfiguration> touchpadConfigurationObserver =
                GestureViewModel.this::onTouchpadConfiguration;
        private final ArrayMap<Gesture, Observer<Set<Configuration>>> configurationsObserver = new ArrayMap<>();

        @Override
        protected void registerObservers() {
            featuresRepository.getFeaturesLiveData().observeForever(featuresObserver);
            gestureConfigurationRepository.getGesturesLiveData().observeForever(gesturesObserver);
            gestureConfigurationRepository.getContextsLiveData().observeForever(contextsObserver);
            gestureConfigurationRepository.getActionsLiveData().observeForever(actionsObserver);
            gestureConfigurationRepository.getTouchpadTypeLiveData().observeForever(touchpadConfigurationObserver);

        }

        @Override
        protected void unregisterObservers() {
            featuresRepository.getFeaturesLiveData().removeObserver(featuresObserver);
            gestureConfigurationRepository.getGesturesLiveData().removeObserver(gesturesObserver);
            gestureConfigurationRepository.getContextsLiveData().removeObserver(contextsObserver);
            gestureConfigurationRepository.getActionsLiveData().removeObserver(actionsObserver);
            gestureConfigurationRepository.getTouchpadTypeLiveData().removeObserver(touchpadConfigurationObserver);
            configurationsObserver.forEach(
                    (gesture, observer) -> gestureConfigurationRepository.getConfigurationLiveData(gesture)
                            .removeObserver(observer));
            configurationsObserver.clear();
        }

        private void observeConfiguration(Gesture gesture) {
            if (!configurationsObserver.containsKey(gesture)) {
                Observer<Set<Configuration>> observer = (set) -> GestureViewModel.this.onConfiguration(gesture, set);
                configurationsObserver.put(gesture, observer);
                gestureConfigurationRepository.getConfigurationLiveData(gesture).observeForever(observer);
            }
        }
    }
}
