/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.HandsetServiceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.MultipointType;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.HandsetServiceSubscriber;

public class HandsetServicePublisher extends Publisher<HandsetServiceSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.HANDSET_SERVICE;
    }

    public void publishMultiPointType(MultipointType type) {
        forEachSubscriber(subscriber -> subscriber.onInfo(HandsetServiceInfo.MULTIPOINT_TYPE, type));
    }

    public void publishError(HandsetServiceInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }
}
