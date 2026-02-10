/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;

public class TouchpadIndicatorsSettingData extends SettingData {

    private final StickyLiveData<TouchpadViewData> leftTouchpadData = new StickyLiveData<>();

    private final StickyLiveData<TouchpadViewData> rightTouchpadData = new StickyLiveData<>();

    public void observeLeftTouchpadData(LifecycleOwner owner, Observer<TouchpadViewData> observer) {
        leftTouchpadData.observe(owner, observer);
    }

    public void observeRightTouchpadData(LifecycleOwner owner, Observer<TouchpadViewData> observer) {
        rightTouchpadData.observe(owner, observer);
    }

    public void setLeftTouchpadData(TouchpadViewData data) {
        this.leftTouchpadData.postValue(data);
    }

    public void setRightTouchpadData(TouchpadViewData data) {
        this.rightTouchpadData.postValue(data);
    }
}
