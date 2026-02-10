/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BluetoothSubscriber;

public class BluetoothPublisher extends Publisher<BluetoothSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.BLUETOOTH;
    }

    public void publishBluetoothEnabled() {
        forEachSubscriber(BluetoothSubscriber::onEnabled);
    }

    public void publishBluetoothDisabled() {
        forEachSubscriber(BluetoothSubscriber::onDisabled);
    }

}
