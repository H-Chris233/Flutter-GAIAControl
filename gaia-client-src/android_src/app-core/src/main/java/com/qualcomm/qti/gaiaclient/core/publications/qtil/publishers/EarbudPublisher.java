/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.EarbudSubscriber;

import androidx.annotation.NonNull;

public class EarbudPublisher extends Publisher<EarbudSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.EARBUD;
    }

    public void publishInfo(@NonNull EarbudInfo info, Object update, boolean mustRefreshData) {
        forEachSubscriber(subscriber -> subscriber.onInfo(info, update, mustRefreshData));
    }

    public void publishError(@NonNull EarbudInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }

}
