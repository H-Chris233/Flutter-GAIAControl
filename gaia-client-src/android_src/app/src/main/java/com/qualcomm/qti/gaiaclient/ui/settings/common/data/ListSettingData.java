/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common.data;

import androidx.core.util.Pair;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public class ListSettingData extends SettingData {

    private final StickyLiveData<String[]> entries = new StickyLiveData<>(new String[0]);

    private final StickyLiveData<String[]> entryValues = new StickyLiveData<>(new String[0]);

    private final StickyLiveData<String> value = new StickyLiveData<>();

    public void observeEntries(LifecycleOwner owner, Observer<String[]> observer) {
        entries.observe(owner, observer);
    }

    public void observeEntryValues(LifecycleOwner owner, Observer<String[]> observer) {
        entryValues.observe(owner, observer);
    }

    public void observeValue(LifecycleOwner owner, Observer<String> observer) {
        value.observe(owner, observer);
    }

    public void setEntries(Pair<String[], String[]> entries) {
        this.entries.postValue(entries.first);
        this.entryValues.postValue(entries.second);
    }

    public void setValue(String value) {
        this.value.postValue(value);
    }

    public String getValue() {
        return this.value.getValue();
    }
}
