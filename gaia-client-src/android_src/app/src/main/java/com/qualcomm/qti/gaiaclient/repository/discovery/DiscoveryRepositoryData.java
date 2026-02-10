/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.discovery;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.repository.connection.BluetoothReason;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import java.util.ArrayList;
import java.util.List;

public abstract class DiscoveryRepositoryData implements DiscoveryRepository {

    private final ArrayMap<DiscoveryType, StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>>> devicesMap =
            new ArrayMap<>();

    @NonNull
    @Override
    public LiveData<Result<List<DiscoveredDevice>, BluetoothReason>> getDevices(@NonNull DiscoveryType type) {
        return getMutableDevicesLiveData(type);
    }

    public final void refreshDevices(Context context, DiscoveryType type) {
        getMutableDevicesLiveData(type).postValue(Result.inProgress(new ArrayList<>()));
        fetchDevices(context, type);
    }

    protected void onSuccess(DiscoveryType type, @NonNull List<DiscoveredDevice> devices) {
        StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData = getMutableDevicesLiveData(type);
        liveData.postValue(Result.success(devices));
    }

    protected void onSuccess(DiscoveryType type) {
        StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData = getMutableDevicesLiveData(type);
        liveData.postValue(Result.success(getDevicesList(liveData)));
    }

    protected void onProgress(DiscoveryType type) {
        StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData = getMutableDevicesLiveData(type);
        liveData.postValue(Result.inProgress(getDevicesList(liveData)));
    }

    protected void onProgress(DiscoveryType type, DiscoveredDevice device) {
        StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData = getMutableDevicesLiveData(type);
        List<DiscoveredDevice> devices = getDevicesList(liveData);
        if (!devices.contains(device)) {
            devices.add(device);
        }
        liveData.postValue(Result.inProgress(devices));
    }

    protected void onError(DiscoveryType type, BluetoothReason reason) {
        StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData = getMutableDevicesLiveData(type);
        liveData.postValue(Result.error(getDevicesList(liveData), reason));
    }

    @NonNull
    private StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> getMutableDevicesLiveData(
            @NonNull DiscoveryType type) {
        StickyLiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData = devicesMap.get(type);
        if (liveData == null) {
            liveData = new StickyLiveData<>();
            devicesMap.put(type, liveData);
        }
        return liveData;
    }

    @NonNull
    private List<DiscoveredDevice> getDevicesList(LiveData<Result<List<DiscoveredDevice>, BluetoothReason>> liveData) {
        Result<List<DiscoveredDevice>, BluetoothReason> result = liveData.getValue();
        List<DiscoveredDevice> devices = result != null ? result.getData() : new ArrayList<>();
        return devices != null ? devices : new ArrayList<>();
    }

    protected abstract void fetchDevices(Context context, DiscoveryType type);

}
