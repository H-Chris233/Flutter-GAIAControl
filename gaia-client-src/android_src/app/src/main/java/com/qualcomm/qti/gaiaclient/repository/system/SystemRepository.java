/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.system;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

public interface SystemRepository {

    void init(Context context);

    @NonNull
    LiveData<Boolean> getAndroidDeveloperModeLiveData();

    @NonNull
    LiveData<Boolean> getBluetoothStateLiveData();

    void checkAndroidDeveloperModeState(Context context);

    boolean isAndroidDeveloperModeOn();

    void updateBluetoothState(Context context);

    Boolean isBluetoothEnabled();
}
