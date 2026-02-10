/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber;

import androidx.annotation.NonNull;

public class ConnectionPublisher extends Publisher<ConnectionSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.CONNECTION;
    }

    public void publishConnectionState(@NonNull Device device, ConnectionState state) {
        forEachSubscriber(subscriber -> subscriber.onConnectionStateChanged(device, state));
    }

    public void publishConnectionError(Device device, BluetoothStatus error) {
        forEachSubscriber(subscriber -> subscriber.onConnectionError(device, error));
    }

}
