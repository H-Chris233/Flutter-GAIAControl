/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import androidx.annotation.NonNull;

public interface QTILFeaturesSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.FEATURES;
    }

    void onFeatureSupported(QTILFeature feature, int version);

    void onFeatureNotSupported(QTILFeature feature, Reason reason);

    void onError(Reason reason);

}
