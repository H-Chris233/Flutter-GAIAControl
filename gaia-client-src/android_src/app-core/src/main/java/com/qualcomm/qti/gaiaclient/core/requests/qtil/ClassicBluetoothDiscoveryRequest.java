/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import static com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils.areScanningPermissionsGranted;
import static com.qualcomm.qti.gaiaclient.core.utils.LocationUtils.isLocationEnabled;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.discovery.BluetoothDiscoveryReceiver;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils;

@SuppressLint("MissingPermission") // permissions are managed by the UI layer
public class ClassicBluetoothDiscoveryRequest extends Request<Void, DiscoveredDevice, BluetoothStatus> {

    private static final String TAG = "DiscoverClassicDevicesRequest";

    private boolean mIsScanning = false;

    private final BluetoothDiscoveryReceiver mDiscoveryReceiver =
            new BluetoothDiscoveryReceiver(new BluetoothDiscoveryReceiver.BluetoothDiscoveryListener() {
                @Override
                public void onDeviceFound(DiscoveredDevice device) {
                    ClassicBluetoothDiscoveryRequest.this.onProgress(device);
                }

                @Override
                public void onStartDiscovery() {
                    mIsScanning = true;
                }

                @Override
                public void onStopDiscovery(Context context) {
                    mIsScanning = false;
                    ClassicBluetoothDiscoveryRequest.this.onDiscoveryStopped(context);
                    ClassicBluetoothDiscoveryRequest.this.onComplete(null);
                    context.unregisterReceiver(mDiscoveryReceiver);
                }
            });

    public ClassicBluetoothDiscoveryRequest(@NonNull RequestListener<Void, DiscoveredDevice, BluetoothStatus> listener) {
        super(listener);
    }

    @Override
    public void run(@Nullable Context context) {
        BluetoothAdapter adapter = BluetoothUtils.getBluetoothAdapter(context);

        if (adapter == null || context == null) {
            // if context is null, adapter is also null
            // the condition is to ensure that context is non null for startScan
            Log.w(TAG, "[run] error: Bluetooth is unavailable.");
            onError(BluetoothStatus.NO_BLUETOOTH);
            return;
        }

        if (!areScanningPermissionsGranted(context)) {
            // scanning for devices requires the location permissions to be granted
            Log.w(TAG, "[run] error: missing permissions to scan for devices.");
            onError(BluetoothStatus.NO_PERMISSIONS);
            return;
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R && !isLocationEnabled(context)) {
            // scanning for devices requires the location services
            Log.w(TAG, "[run] error: location is disabled.");
            onError(BluetoothStatus.NO_LOCATION);
            return;
        }

        // cancelling any ongoing discovery
        adapter.cancelDiscovery();

        // starting or re-starting the discovery
        BluetoothStatus status = startScan(context, adapter);

        // check returned status
        if (status != BluetoothStatus.IN_PROGRESS) {
            onError(status);
        }
    }

    @Override
    protected void onEnd() {
    }

    private BluetoothStatus startScan(@NonNull Context context, @NonNull BluetoothAdapter adapter) {
        if (mIsScanning) {
            return BluetoothStatus.IN_PROGRESS;
        }

        // register a receiver to get discovered devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mDiscoveryReceiver, filter);

        // start discovery
        boolean isDiscovering = adapter.startDiscovery();

        // analyse result
        if (isDiscovering) {
            return BluetoothStatus.IN_PROGRESS;
        }
        else {
            onDiscoveryStopped(context);
            return BluetoothStatus.DISCOVERY_FAILED;
        }
    }

    private void onDiscoveryStopped(Context context) {
        if (mIsScanning) {
            mIsScanning = false;
            context.unregisterReceiver(mDiscoveryReceiver);
        }
    }
}
