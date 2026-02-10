/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DeviceInformationSubscriber;

import androidx.annotation.NonNull;

public class DeviceInformationPublisher extends Publisher<DeviceInformationSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.DEVICE_INFORMATION;
    }

    public void publishInfo(@NonNull DeviceInfo info, @NonNull Object update) {
        forEachSubscriber(subscriber -> subscriber.onInfo(info, update));
    }

    public void publishError(@NonNull DeviceInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }

}
