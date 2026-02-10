/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.FitInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

public interface EarbudFitSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.EARBUD_FIT;
    }

    void onFitResults(EarbudsFitResults results);

    void onFitError(FitInfo info, Reason reason);

}
