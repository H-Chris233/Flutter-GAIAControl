/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import androidx.annotation.NonNull;

public interface EarbudSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.EARBUD;
    }

    void onInfo(EarbudInfo info, Object update, boolean mustRefreshData);

    void onError(EarbudInfo info, Reason reason);

}
