/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.uuids;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

/**
 * <p>This class allows reception of information from the system about discovery of UUID for a BluetoothDevice.</p>
 * <p>This receiver should be used with the following intent filter:
 * {@link BluetoothDevice#ACTION_UUID ACTION_UUID}.</p>
 */
public class UUIDReceiver extends BroadcastReceiver {

    private static final String TAG = "UUIDReceiver";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.Rfcomm.UUID_RECEIVER;

    /**
     * The listener to dispatch events from this receiver.
     */
    private final UUIDListener mListener;

    private final BluetoothDevice mDevice;

    /**
     * <p>The constructor of this class.</p>
     *
     * @param listener
     *         The listener to inform of broadcast events from this receiver.
     * @param device
     *         the device this receiver should listen UUIDs for.
     */
    public UUIDReceiver(UUIDListener listener, BluetoothDevice device) {
        this.mListener = listener;
        this.mDevice = device;
    }

    @Override // BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        Logger.log(LOG_METHODS, TAG, "onReceive");

        String action = intent.getAction();
        if (action != null && action.equals(BluetoothDevice.ACTION_UUID)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Parcelable[] parcels = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
            // note: EXTRA_UUID key provides an array of ParcelUuid as values
            onReceiveUuids(context, device, parcels);
        }
    }

    private void onReceiveUuids(Context context, @Nullable BluetoothDevice device, @Nullable Parcelable[] parcels) {
        Logger.log(LOG_METHODS, TAG, "onReceiveUuids", new Pair<>("count", parcels == null ? "null" : parcels.length));

        if (mDevice != null && mDevice.getAddress().equalsIgnoreCase(device.getAddress())) {
            ParcelUuid[] uuids = prepareUuids(parcels);
            mListener.onUUIDsFound(context, device, uuids);
        }
    }

    @NonNull
    private ParcelUuid[] prepareUuids(Parcelable[] parcels) {
        Logger.log(LOG_METHODS, TAG, "prepareUuids");

        if (parcels == null) {
            return new ParcelUuid[0];
        }

        ParcelUuid[] uuids = new ParcelUuid[parcels.length];

        for (int i = 0; i < parcels.length; i++) {
            uuids[i] = (ParcelUuid) parcels[i];
        }
        return uuids;
    }

    /**
     * <p>The listener for the {@link UUIDReceiver UUIDReceiver} receiver.</p>
     */
    public interface UUIDListener {

        /**
         * <p>The method to dispatch a found UUID to a listener of this receiver.</p>
         *
         * @param parcels
         *         The uuid which had been found for the device set up when creating the
         *         UUIDReceiver.
         * @param device
         *         The device for which the UUID has been found.
         */
        void onUUIDsFound(Context context, @NonNull BluetoothDevice device, @NonNull ParcelUuid[] parcels);
    }

}
