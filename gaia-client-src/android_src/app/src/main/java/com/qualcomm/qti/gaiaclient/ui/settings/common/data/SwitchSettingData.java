/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common.data;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public class SwitchSettingData extends SettingData {

    private final StickyLiveData<Boolean> checked = new StickyLiveData<>();

    public void observeChecked(LifecycleOwner owner, Observer<Boolean> observer) {
        checked.observe(owner, observer);
    }

    public void setChecked(boolean checked) {
        this.checked.postValue(checked);
    }
}
