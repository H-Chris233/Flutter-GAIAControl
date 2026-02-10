/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.system;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public abstract class SystemRepositoryData implements SystemRepository {

    private final StickyLiveData<Boolean> isAndroidDeveloperModeOnLiveData = new StickyLiveData<>();

    private final StickyLiveData<Boolean> isBluetoothEnabledLiveData = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<Boolean> getAndroidDeveloperModeLiveData() {
        return isAndroidDeveloperModeOnLiveData;
    }

    @NonNull
    @Override
    public LiveData<Boolean> getBluetoothStateLiveData() {
        return isBluetoothEnabledLiveData;
    }

    @Override
    public boolean isAndroidDeveloperModeOn() {
        return isAndroidDeveloperModeOnLiveData.getValue() != null && isAndroidDeveloperModeOnLiveData.getValue();
    }

    @Override
    public Boolean isBluetoothEnabled() {
        return isBluetoothEnabledLiveData.getValue();
    }

    protected void updateAndroidDeveloperModeState(boolean state) {
        isAndroidDeveloperModeOnLiveData.postValue(state);
    }

    protected void updateBluetoothState(boolean state) {
        isBluetoothEnabledLiveData.postValue(state);
    }
}
