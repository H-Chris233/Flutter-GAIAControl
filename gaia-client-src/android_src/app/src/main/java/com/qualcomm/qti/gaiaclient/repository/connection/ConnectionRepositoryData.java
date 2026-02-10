/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.connection;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.GaiaClientApplication;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

public abstract class ConnectionRepositoryData implements ConnectionRepository {

    private final StickyLiveData<Resource<Device, BluetoothStatus>> connectedDevice = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<Resource<Device, BluetoothStatus>> getConnectedDeviceLiveData() {
        return connectedDevice;
    }

    @Override
    public void disconnect(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext instanceof GaiaClientApplication) {
            ((GaiaClientApplication) appContext).resetRepositories(true);
        }
    }

    protected void updateDevice(Device device) {
        connectedDevice.postValue(Resource.data(device));
    }

    protected void onError(BluetoothStatus status) {
        Resource.error(connectedDevice.getValue(), status);
    }
}
