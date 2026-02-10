/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.DebugInfo;
import com.qualcomm.qti.gaiaclient.core.data.DownloadLogsState;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.LogSize;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DebugSubscriber;

public class DebugPublisher extends Publisher<DebugSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.DEBUG;
    }

    public void publishLogSize(LogSize size) {
        forEachSubscriber(subscriber -> subscriber.onLogSize(size));
    }

    public void publishDownloadProgress(DownloadLogsState state, double progress) {
        forEachSubscriber(subscriber -> subscriber.onDownloadProgress(state, progress));
    }

    public void publishError(DebugInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }
}
