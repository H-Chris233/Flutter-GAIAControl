/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.google.android.material.slider.LabelFormatter;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;

public class DiscreteSliderSettingData extends SettingData {

    private final StickyLiveData<Integer> minValue = new StickyLiveData<>();

    private final StickyLiveData<Integer> maxValue = new StickyLiveData<>();

    private final StickyLiveData<Integer> value = new StickyLiveData<>();

    private final StickyLiveData<Integer> stepSize = new StickyLiveData<>();

    private final StickyLiveData<String> label = new StickyLiveData<>();

    private final StickyLiveData<LabelFormatter> formatter = new StickyLiveData<>();

    public void observeMinValue(LifecycleOwner owner, Observer<Integer> observer) {
        minValue.observe(owner, observer);
    }

    public void observeMaxValue(LifecycleOwner owner, Observer<Integer> observer) {
        maxValue.observe(owner, observer);
    }

    public void observeValue(LifecycleOwner owner, Observer<Integer> observer) {
        value.observe(owner, observer);
    }

    public void observeStepSize(LifecycleOwner owner, Observer<Integer> observer) {
        stepSize.observe(owner, observer);
    }

    public void observeLabel(LifecycleOwner owner, Observer<String> observer) {
        label.observe(owner, observer);
    }

    public void observeLabelFormatter(LifecycleOwner owner, Observer<LabelFormatter> observer) {
        formatter.observe(owner, observer);
    }

    public void setMinValue(int minValue) {
        this.minValue.postValue(minValue);
    }

    public void setMaxValue(int maxValue) {
        this.maxValue.postValue(maxValue);
    }

    public void setValue(int value) {
        this.value.postValue(value);
    }

    public void setStepSize(int stepSize) {
        this.stepSize.postValue(stepSize);
    }

    public void setLabel(String label) {
        this.label.postValue(label);
    }

    public void setLabelFormatter(LabelFormatter formatter) {
        this.formatter.postValue(formatter);
    }
}
