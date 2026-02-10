/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.BluetoothPublisher;

import androidx.annotation.NonNull;

/**
 * This class allows reception of information from the system. We use it to have information about
 * the Bluetooth state.
 */
public class BluetoothStateReceiver extends BroadcastReceiver {
    /**
     * To publish updates about the Bluetooth state.
     */
    private final BluetoothPublisher mBluetoothPublisher = new BluetoothPublisher();

    /**
     * The constructor of this class.
     */
    public BluetoothStateReceiver(@NonNull PublicationManager publicationManager) {
        publicationManager.register(mBluetoothPublisher);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                                                 BluetoothAdapter.ERROR);
            if (state == BluetoothAdapter.STATE_OFF) {
                mBluetoothPublisher.publishBluetoothDisabled();
            }
            else if (state == BluetoothAdapter.STATE_ON) {
                mBluetoothPublisher.publishBluetoothEnabled();
            }
        }
    }

}
