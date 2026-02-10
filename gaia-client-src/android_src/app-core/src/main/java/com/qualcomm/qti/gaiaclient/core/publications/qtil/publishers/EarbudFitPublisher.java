/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.FitInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.EarbudFitSubscriber;

public class EarbudFitPublisher extends Publisher<EarbudFitSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.EARBUD_FIT;
    }

    public void publishFitResults(EarbudsFitResults results) {
        forEachSubscriber(subscriber -> subscriber.onFitResults(results));
    }

    public void publishError(FitInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onFitError(info, reason));
    }
}
