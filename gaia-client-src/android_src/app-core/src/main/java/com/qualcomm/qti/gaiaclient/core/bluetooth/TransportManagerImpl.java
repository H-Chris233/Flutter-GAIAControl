/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.BluetoothClient;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.BluetoothClientFactory;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.GaiaReader;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ConnectionPublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

final class TransportManagerImpl {

    private static final String TAG = "TransportManager";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.TRANSPORT_MANAGER;

    private BluetoothClient mClient = null;

    private final ConnectionPublisher mConnectionPublisher = new ConnectionPublisher();

    TransportManagerImpl(@NonNull PublicationManager publicationManager) {
        publicationManager.register(mConnectionPublisher);
    }

    void release() {
        if (mClient != null) {
            mClient.disconnect();
            mClient = null;
        }
    }

    void logBytes(boolean log) {
        if (mClient != null) {
            mClient.logBytes(log);
        }
    }

    Device getDevice() {
        return mClient != null ? mClient.getDevice() : null;
    }

    GaiaTransport getGaiaTransport() {
        if(mClient!= null) {
            return mClient.getGaiaTransport();
        } else {
            return null;
        }
    }

    BluetoothStatus connect(@NonNull Context context, @NonNull String address, @NonNull BluetoothType type) {
        Logger.log(LOG_METHODS, TAG, "connect", new Pair<>("device", address), new Pair<>("type", type));
        Device device = new Device(address, type);

        boolean sameConnection = mClient != null && device.equals(mClient.getDevice());
        boolean isConnected = mClient != null && mClient.isConnected();

        if (isConnected && !sameConnection) {
            Log.w(TAG, "[connect] already connected to a different device or through a different transport:" +
                    " call disconnect() first.");
            return BluetoothStatus.INCORRECT_STATE;
        }

        if (mClient == null || !sameConnection) {
            mClient = BluetoothClientFactory.INSTANCE
                    .createBluetoothClient(type, device,
                                           new BluetoothClient.Listener() {
                                               @Override
                                               public void onConnectionState(@NonNull ConnectionState state) {
                                                   onConnectionStateChanged(device, state);
                                               }

                                               @Override
                                               public void onConnectionError(@NonNull BluetoothStatus status) {
                                                   Logger.log(LOG_METHODS, TAG,
                                                              "RfcommClientListener->error",
                                                              new Pair<>("error", status));
                                                   mConnectionPublisher.publishConnectionError(device, status);
                                               }
                                           },
                                           GaiaReader.getReader(type, GaiaClientService.getGaiaManager().getReaderListener()));
        }

        GaiaClientService.getReconnectionObserver().enable();
        return mClient.connect(context);
    }

    BluetoothStatus reconnect() {
        Logger.log(LOG_METHODS, TAG, "reconnect");
        // reconnect to device
        if (mClient != null) {
            return mClient.reconnect();
        }

        return BluetoothStatus.DEVICE_NOT_FOUND;
    }

    void disconnect(boolean hard) {
        Logger.log(LOG_METHODS, TAG, "disconnect");

        if (mClient != null) {
            if (hard) {
                GaiaClientService.getReconnectionObserver().disable();
            }
            mClient.disconnect();
        }
    }

    private void onConnectionStateChanged(@NonNull Device device, ConnectionState state) {
        Logger.log(LOG_METHODS, TAG, "onConnectionStateChanged", new Pair<>("state", state));
        // publishing connection state
        mConnectionPublisher.publishConnectionState(device, state);
    }
}
