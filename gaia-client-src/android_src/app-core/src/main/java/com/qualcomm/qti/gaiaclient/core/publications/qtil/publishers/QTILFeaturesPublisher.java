/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.QTILFeaturesSubscriber;

import androidx.annotation.NonNull;

public class QTILFeaturesPublisher extends Publisher<QTILFeaturesSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.FEATURES;
    }

    public void publishFeatureSupported(@NonNull QTILFeature feature, int version) {
        forEachSubscriber(subscriber -> subscriber.onFeatureSupported(feature, version));
    }

    public void publishFeatureNotSupported(@NonNull QTILFeature feature, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onFeatureNotSupported(feature, reason));
    }

    public void publishError(Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(reason));
    }

}
