/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTransportManager;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.bluetooth.TransportManager;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

public class ConnectionRequest extends Request<Void, Void, BluetoothStatus> {

    private static final String TAG = "ConnectionRequest";

    private static final boolean LOG_METHODS = DEBUG.Request.CONNECTION_REQUEST;

    private static final int ATTEMPTS_MAX = 2;

    private final String bluetoothAddress;

    private final BluetoothType bluetoothType;

    private int attempts = 0;

    private Context context;

    private final ConnectionSubscriber subscriber = new ConnectionSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onConnectionStateChanged(Device device, ConnectionState state) {
            ConnectionRequest.this.onConnectionStateChanged(device, state);
        }

        @Override
        public void onConnectionError(Device device, BluetoothStatus reason) {
            ConnectionRequest.this.onConnectionError(device, reason);
        }
    };

    public ConnectionRequest(@NonNull String deviceAddress, @NonNull BluetoothType bluetoothType,
                             @NonNull RequestListener<Void, Void, BluetoothStatus> listener) {
        super(listener);
        this.bluetoothAddress = deviceAddress;
        this.bluetoothType = bluetoothType;
    }

    @Override
    public void run(@Nullable Context context) {
        Logger.log(LOG_METHODS, TAG, "run", new Pair<>("address", bluetoothAddress), new Pair<>("type", bluetoothType));

        if (context == null) {
            // without the context the Android device Bluetooth is not accessible.
            onError(BluetoothStatus.NO_BLUETOOTH);
            return;
        }

        attempts++;
        this.context = context;

        if (bluetoothAddress == null) {
            Log.w(TAG, "[run] device address is null");
            onError(BluetoothStatus.DEVICE_NOT_FOUND);
            return;
        }

        if (bluetoothType == null) {
            Log.w(TAG, "[run] Bluetooth type is null");
            onError(BluetoothStatus.NO_TRANSPORT_FOUND);
            return;
        }

        // subscribe for connection updates
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(subscriber);

        // start connection
        TransportManager transportManager = getTransportManager();
        BluetoothStatus status = transportManager.connect(context, bluetoothAddress, bluetoothType);

        // check returned status
        if (status == BluetoothStatus.ALREADY_CONNECTED) {
            onComplete(null);
        }
        else if (status != BluetoothStatus.IN_PROGRESS) {
            onError(status);
        }
        else {
            onProgress(null);
        }
    }

    @Override
    protected void onEnd() {
        Logger.log(LOG_METHODS, TAG, "onEnd");
        PublicationManager manager = GaiaClientService.getPublicationManager();
        manager.unsubscribe(subscriber);
    }

    private void onConnectionStateChanged(Device device, ConnectionState state) {
        Logger.log(LOG_METHODS, TAG, "onConnectionStateChanged",
                   new Pair<>("device", device), new Pair<>("state", state));

        if (device == null || !device.getBluetoothAddress().equals(bluetoothAddress)
                || device.getBluetoothType() != bluetoothType) {
            // not the expected device
            return;
        }

        switch (state) {
            case CONNECTING:
                onProgress(null);
                break;
            case CONNECTED:
                // get the transport manager
                onComplete(null);
                break;
            case DISCONNECTING:
            case DISCONNECTED:
                // an error occurred, reported using onConnectionError
                break;
        }
    }

    private void onConnectionError(Device device, BluetoothStatus status) {
        Logger.log(LOG_METHODS, TAG, "onConnectionError",
                   new Pair<>("device", device), new Pair<>("status", status));

        if (device == null || !device.getBluetoothAddress().equals(bluetoothAddress)
                || device.getBluetoothType() != bluetoothType) {
            // not the expected device
            return;
        }

        onError(status);
    }

    @Override
    protected void onError(BluetoothStatus reason) {
        if (ATTEMPTS_MAX <= attempts || context == null) {
            super.onError(reason);
        }
        else {
            // we try again
            run(context);
        }
    }
}
