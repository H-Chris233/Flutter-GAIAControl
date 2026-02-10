/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverInformation;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.HandoverSubscriber;

public class HandoverPublisher extends Publisher<HandoverSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.HANDOVER;
    }

    public void publishHandoverStart(HandoverInformation info) {
        forEachSubscriber(subscriber -> subscriber.onHandoverStart(info));
    }

}
