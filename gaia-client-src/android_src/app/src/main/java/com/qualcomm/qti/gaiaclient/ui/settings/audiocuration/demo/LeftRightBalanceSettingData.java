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

public class LeftRightBalanceSettingData extends SettingData {

    private final StickyLiveData<Integer> value = new StickyLiveData<>();

    private final StickyLiveData<String> label = new StickyLiveData<>();

    private final StickyLiveData<LabelFormatter> formatter = new StickyLiveData<>();

    public void observeValue(LifecycleOwner owner, Observer<Integer> observer) {
        value.observe(owner, observer);
    }

    public void observeLabel(LifecycleOwner owner, Observer<String> observer) {
        label.observe(owner, observer);
    }

    public void observeLabelFormatter(LifecycleOwner owner, Observer<LabelFormatter> observer) {
        formatter.observe(owner, observer);
    }

    public void setValue(int value) {
        this.value.postValue(value);
    }

    public void setLabel(String label) {
        this.label.postValue(label);
    }

    public void setLabelFormatter(LabelFormatter formatter) {
        this.formatter.postValue(formatter);
    }
}
