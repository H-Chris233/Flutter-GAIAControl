/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancementConfiguration;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import java.util.ArrayList;

public interface VoiceProcessingSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.VOICE_PROCESSING;
    }

    void onEnhancements(@NonNull ArrayList<VoiceEnhancement> enhancements);

    void onConfiguration(@NonNull VoiceEnhancementConfiguration configuration);

    void onError(VoiceProcessingInfo info, Reason reason);
}
