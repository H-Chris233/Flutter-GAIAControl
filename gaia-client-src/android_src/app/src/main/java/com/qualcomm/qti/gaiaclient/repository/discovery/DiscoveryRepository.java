/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.discovery;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.repository.connection.BluetoothReason;

import java.util.List;

public interface DiscoveryRepository {

    @NonNull
    LiveData<Result<List<DiscoveredDevice>, BluetoothReason>> getDevices(DiscoveryType type);

    void refreshDevices(Context context, DiscoveryType type);

}
