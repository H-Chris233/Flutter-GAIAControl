/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.discovery;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType;
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConnectedDevicesFetcher {

    private static final String TAG = "ConnectedDevicesFetcher";

    @Profile
    private final int profile;

    @IntDef(flag = true, value = {BluetoothProfile.HEADSET, BluetoothProfile.A2DP})
    public @interface Profile {

    }

    @NonNull
    private final ConnectedDevicesFetcherListener mListener;

    private BluetoothProfile profileProxy = null;

    private final BluetoothProfile.ServiceListener profileListener = new BluetoothProfile.ServiceListener() {
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            if (profile == ConnectedDevicesFetcher.this.profile) {
                onProfileConnected((BluetoothProfile) proxy); // force cast is to ensure the type if the proxy is null
            }
        }

        public void onServiceDisconnected(int profile) {
            // nothing to do: this is called when BluetoothAdapter.closeProfileProxy(profile, proxy) is called
        }
    };

    public ConnectedDevicesFetcher(@Profile int profile,
                                   @NonNull ConnectedDevicesFetcherListener listener) {
        this.profile = profile;
        this.mListener = listener;
    }

    public BluetoothStatus get(Context context) {
        // getting BluetoothAdapter to get resources
        BluetoothAdapter adapter = BluetoothUtils.getBluetoothAdapter(context);

        if (adapter == null) {
            // no BluetoothAdapter = Bluetooth is off or not available for the device
            Log.w(TAG, "[get] BluetoothAdapter is null.");
            return BluetoothStatus.NO_BLUETOOTH;
        }

        boolean result = adapter.getProfileProxy(context, profileListener, profile);

        return result ? BluetoothStatus.IN_PROGRESS : BluetoothStatus.DISCOVERY_FAILED;
    }

    public void release(@Nullable Context context) {
        // getting BluetoothAdapter to release resources
        BluetoothAdapter adapter = BluetoothUtils.getBluetoothAdapter(context);

        if (adapter == null) {
            // no BluetoothAdapter = Bluetooth is off or not available for the device
            Log.w(TAG, "[release] BluetoothAdapter is null.");
            return;
        }

        if (profileProxy != null) {
            adapter.closeProfileProxy(profile, profileProxy);
        }
    }

    private void onProfileConnected(BluetoothProfile bluetoothProfile) {
        profileProxy = bluetoothProfile;
        List<BluetoothDevice> devices =
                bluetoothProfile == null ? Collections.emptyList() : bluetoothProfile.getConnectedDevices();
        mListener.onGetConnectedDevices(getClassicConnectedDevices(devices));
    }

    @SuppressLint("MissingPermission")
    private List<DiscoveredDevice> getClassicConnectedDevices(List<BluetoothDevice> found) {
        if (found == null || found.isEmpty()) {
            return new ArrayList<>();
        }

        List<DiscoveredDevice> result = new ArrayList<>();

        found.forEach(device -> {
            switch (device.getType()) {
                case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                    result.add(new DiscoveredDevice(device, DiscoveryType.CONNECTED, BluetoothType.CLASSIC));
                    break;
                case BluetoothDevice.DEVICE_TYPE_LE:
                    result.add(new DiscoveredDevice(device, DiscoveryType.CONNECTED, BluetoothType.LOW_ENERGY));
                    break;
                case BluetoothDevice.DEVICE_TYPE_DUAL:
                    result.add(new DiscoveredDevice(device, DiscoveryType.CONNECTED, BluetoothType.LOW_ENERGY));
                    result.add(new DiscoveredDevice(device, DiscoveryType.CONNECTED, BluetoothType.CLASSIC));
                    break;
            }
        });

        return result;
    }

    public interface ConnectedDevicesFetcherListener {

        void onGetConnectedDevices(List<DiscoveredDevice> devices);

    }
}
