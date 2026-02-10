/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.discovery.ConnectedDevicesFetcher;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

import java.lang.ref.WeakReference;
import java.util.List;

import static com.qualcomm.qti.gaiaclient.core.bluetooth.discovery.ConnectedDevicesFetcher.Profile;

public class GetConnectedDevicesRequest extends Request<List<DiscoveredDevice>, Void, BluetoothStatus> {

    private ConnectedDevicesFetcher fetcher;

    private WeakReference<Context> appContextRef = null;

    @Profile
    private final int profile;

    public GetConnectedDevicesRequest(@Profile int profile,
                                      @NonNull RequestListener<List<DiscoveredDevice>, Void, BluetoothStatus> listener) {
        super(listener);
        this.profile = profile;
    }

    @Override
    public void run(@Nullable Context context) {
        appContextRef = context != null ? new WeakReference<>(context.getApplicationContext()) : null;

        fetcher = new ConnectedDevicesFetcher(profile, this::onComplete);
        BluetoothStatus status = fetcher.get(context);

        // check returned status
        if (status != BluetoothStatus.IN_PROGRESS) {
            onError(status);
        }
        else {
            onProgress(null);
        }
    }

    @Override
    protected void onEnd() {
        if (fetcher != null) {
            Context context = appContextRef != null ? appContextRef.get() : null;
            fetcher.release(context);
            fetcher = null;
        }
    }

}
