/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancementConfiguration;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.VoiceProcessingSubscriber;

import java.util.ArrayList;

public class VoiceProcessingPublisher extends Publisher<VoiceProcessingSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.VOICE_PROCESSING;
    }

    public void publishEnhancements(@NonNull ArrayList<VoiceEnhancement> enhancements) {
        forEachSubscriber(subscriber -> subscriber.onEnhancements(enhancements));
    }

    public void publishConfiguration(@NonNull VoiceEnhancementConfiguration configuration) {
        forEachSubscriber(subscriber -> subscriber.onConfiguration(configuration));
    }

    public void publishError(VoiceProcessingInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }

}
