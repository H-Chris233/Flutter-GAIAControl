/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common.data;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public class CustomSettingData extends SettingData {

    @NonNull
    private final StickyLiveData<Object> data = new StickyLiveData<>();

    public void observeData(LifecycleOwner owner, Observer<Object> observer) {
        data.observe(owner, observer);
    }

    public void setData(Object data) {
        this.data.postValue(data);
    }
}
