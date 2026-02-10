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

public class SettingData {

    @NonNull
    private final StickyLiveData<Boolean> isVisible = new StickyLiveData<>(true);

    @NonNull
    private final StickyLiveData<Boolean> isEnabled = new StickyLiveData<>(true);

    @NonNull
    private final StickyLiveData<String> summary = new StickyLiveData<>();

    public void observeVisibility(LifecycleOwner owner, Observer<Boolean> observer) {
        isVisible.observe(owner, observer);
    }

    public void observeEnable(LifecycleOwner owner, Observer<Boolean> observer) {
        isEnabled.observe(owner, observer);
    }

    public void observeSummary(LifecycleOwner owner, Observer<String> observer) {
        summary.observe(owner, observer);
    }

    public void setVisible(boolean visible) {
        isVisible.postValue(visible);
    }

    public void setEnabled(boolean enabled) {
        isEnabled.postValue(enabled);
    }

    public void setSummary(String summary) {
        this.summary.postValue(summary == null ? "" : summary);
    }
}
