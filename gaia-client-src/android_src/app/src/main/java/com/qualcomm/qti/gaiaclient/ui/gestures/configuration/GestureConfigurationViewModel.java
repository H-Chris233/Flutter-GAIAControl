/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.gestureconfiguration.GestureConfigurationRepository;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.gestures.GestureLabelProvider;
import com.qualcomm.qti.gaiaclient.ui.gestures.GestureViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GestureConfigurationViewModel extends GestureViewModel {

    private final StickyLiveData<Gesture> gesture = new StickyLiveData<>();

    private final StickyLiveData<String> title = new StickyLiveData<>();

    @Inject
    public GestureConfigurationViewModel(@NonNull Application application,
                                         FeaturesRepository featuresRepository,
                                         GestureConfigurationRepository gestureConfigurationRepository) {
        super(application, featuresRepository, gestureConfigurationRepository);
    }

    @Override
    protected GestureConfigurationInfo[] getInfoToSupport() {
        return new GestureConfigurationInfo[]{GestureConfigurationInfo.SUPPORTED_GESTURES,
                                              GestureConfigurationInfo.SUPPORTED_CONTEXTS,
                                              GestureConfigurationInfo.SUPPORTED_CONTEXTS,
                                              GestureConfigurationInfo.GET_GESTURE_CONFIGURATION,
                                              GestureConfigurationInfo.RESET,
                                              GestureConfigurationInfo.TOUCHPAD_CONFIGURATION,
                                              GestureConfigurationInfo.SET_GESTURE_CONFIGURATION};
    }

    void observeTitle(LifecycleOwner owner, @NonNull Observer<String> observer) {
        title.observe(owner, observer);
    }

    void setGesture(Gesture update) {
        this.gesture.postValue(update);
        Context context = getContext();
        setTitle(update == null ? context.getString(R.string.gesture_configuration_title_default)
                                : context.getString(R.string.gesture_configuration_title,
                                                    GestureLabelProvider.getGestureLabel(context, update)));
    }

    void setTitle(String update) {
        this.title.postValue(update);
    }

    public Gesture getGesture() {
        return gesture.getValue();
    }
}