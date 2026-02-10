/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;

import java.util.List;

public class ModesSettingData extends SettingData {

    private final StickyLiveData<List<ModeViewData>> modes = new StickyLiveData<>();

    private final StickyLiveData<ModeViewData> mode = new StickyLiveData<>();

    public void observeModesList(LifecycleOwner owner, Observer<List<ModeViewData>> observer) {
        modes.observe(owner, observer);
    }

    public void observeMode(LifecycleOwner owner, Observer<ModeViewData> observer) {
        mode.observe(owner, observer);
    }

    public void setModes(List<ModeViewData> modes) {
        this.modes.postValue(modes);
    }

    public void setMode(ModeViewData mode) {
        this.mode.postValue(mode);
    }
}
