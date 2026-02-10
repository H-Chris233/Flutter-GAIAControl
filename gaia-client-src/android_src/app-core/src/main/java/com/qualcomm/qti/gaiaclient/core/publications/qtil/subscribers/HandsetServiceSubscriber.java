/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.HandsetServiceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

public interface HandsetServiceSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.HANDSET_SERVICE;
    }

    void onInfo(HandsetServiceInfo info, Object update);

    void onError(HandsetServiceInfo info, Reason reason);

}
