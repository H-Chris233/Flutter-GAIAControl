/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import java.util.List;

import androidx.annotation.NonNull;

public interface VoiceUISubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.VOICE_UI;
    }

    void onSelectedAssistant(VoiceAssistant assistant);

    void onSupportedAssistants(List<VoiceAssistant> assistants);

}
