/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.discovery;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

/**
 * <p>This class allows reception of information from the system about Bluetooth devices that have
 * been found during a device discovery.</p>
 * <p>This receiver should be used with the following intent filter:
 * {@link BluetoothDevice#ACTION_FOUND ACTION_FOUND}.</p>
 */
public class BluetoothDiscoveryReceiver extends BroadcastReceiver {

    private static final String TAG = "BluetoothDiscoveryReceiver";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.DISCOVERY_RECEIVER;
    /**
     * The listener to dispatch discovered devices from this receiver.
     */
    private final BluetoothDiscoveryListener mListener;

    /**
     * <p>Constructor.</p>
     *
     * @param listener
     *         The listener to inform of discovered devices.
     */
    public BluetoothDiscoveryReceiver(BluetoothDiscoveryListener listener) {
        this.mListener = listener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        Logger.log(LOG_METHODS, TAG, "onReceive", new Pair<>("action", action));

        if (action == null || context == null) {
            Log.w(TAG, "[onReceive] action or context is null.");
            return;
        }

        switch (action) {
            case BluetoothAdapter.ACTION_DISCOVERY_STARTED:
                mListener.onStartDiscovery();
                break;

            case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                mListener.onStopDiscovery(context);
                break;

            case BluetoothDevice.ACTION_FOUND:
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                onDeviceFound(device);
                break;
        }
    }

    @SuppressLint("MissingPermission")
    private void onDeviceFound(BluetoothDevice device) {
        Logger.log(LOG_METHODS, TAG, "onDeviceFound", new Pair<>("device", device));

        if (device == null) {
            return;
        }

        switch (device.getType()) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                mListener.onDeviceFound(new DiscoveredDevice(device, DiscoveryType.DISCOVERED, BluetoothType.CLASSIC));
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                mListener.onDeviceFound(new DiscoveredDevice(device, DiscoveryType.DISCOVERED, BluetoothType.LOW_ENERGY));
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                mListener.onDeviceFound(new DiscoveredDevice(device, DiscoveryType.DISCOVERED, BluetoothType.LOW_ENERGY));
                mListener.onDeviceFound(new DiscoveredDevice(device, DiscoveryType.DISCOVERED, BluetoothType.CLASSIC));
                break;
        }
    }

    /**
     * <p>The listener for the
     * {@link BluetoothDiscoveryReceiver BluetoothDiscoveryReceiver} receiver.</p>
     */
    public interface BluetoothDiscoveryListener {

        /**
         * <p>The method to dispatch a found device to a listener of this receiver.</p>
         *
         * @param device
         *         The device that has been found.
         */
        void onDeviceFound(DiscoveredDevice device);

        void onStartDiscovery();

        void onStopDiscovery(Context context);
    }

}
