/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import com.qualcomm.qti.gaiaclient.core.data.FlowControlInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import androidx.annotation.NonNull;

public interface ProtocolSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.TRANSPORT_INFORMATION;
    }

    default void onProtocolVersion(long version) {
    }

    default void onSizeInfo(SizeInfo info, int size) {
    }

    default void onFlowControlInfo(FlowControlInfo info, boolean enabled) {
    }

    /**
     * @param info
     *         instance of {@link SizeInfo}, {@link FlowControlInfo} or null if the error
     *         occurred for the protocol version information.
     */
    void onError(Object info, Reason reason);

}
