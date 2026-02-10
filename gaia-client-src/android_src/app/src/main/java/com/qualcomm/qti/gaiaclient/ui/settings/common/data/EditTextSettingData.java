/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public class EditTextSettingData extends SettingData {

    @NonNull
    private final StickyLiveData<String> value = new StickyLiveData<>();

    public void observeValue(LifecycleOwner owner, Observer<String> observer) {
        value.observe(owner, observer);
    }

    public void setValue(String value) {
        this.value.postValue(value);
    }

    public String getValue() {
        return this.value.getValue();
    }
}
