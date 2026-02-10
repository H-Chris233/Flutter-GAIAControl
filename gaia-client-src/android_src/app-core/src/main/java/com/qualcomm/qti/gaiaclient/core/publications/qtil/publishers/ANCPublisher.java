/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.ANCState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptedGain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveStateInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ANCSubscriber;

public class ANCPublisher extends Publisher<ANCSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.ANC;
    }

    public void publishState(ANCState state) {
        forEachSubscriber(subscriber -> subscriber.onANCInfo(ANCInfo.ANC_STATE, state));
    }

    public void publishAdaptiveState(AdaptiveStateInfo state) {
        forEachSubscriber(subscriber -> subscriber.onANCInfo(ANCInfo.ADAPTIVE_STATE, state));
    }

    public void publishModeCount(int count) {
        forEachSubscriber(subscriber -> subscriber.onANCInfo(ANCInfo.ANC_MODE_COUNT, count));
    }

    public void publishMode(int mode) {
        forEachSubscriber(subscriber -> subscriber.onANCInfo(ANCInfo.ANC_MODE, mode));
    }

    public void publishAdaptedGain(AdaptedGain gain) {
        forEachSubscriber(subscriber -> subscriber.onANCInfo(ANCInfo.ADAPTED_GAIN, gain));
    }

    public void publishLeakthroughGain(Gain gain) {
        forEachSubscriber(subscriber -> subscriber.onANCInfo(ANCInfo.LEAKTHROUGH_GAIN, gain));
    }

    public void publishError(ANCInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onANCError(info, reason));
    }
}
