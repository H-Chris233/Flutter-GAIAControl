/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import java.util.Set;

public interface GestureConfigurationSubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.GESTURE_CONFIGURATION;
    }

    void onNumberOfTouchpads(TouchpadConfiguration configuration);

    void onGestures(Set<Gesture> gestures);

    void onGestureContexts(Set<GestureContext> contexts);

    void onActions(Set<Action> actions);

    void onConfiguration(Gesture gesture, Set<Configuration> configurations);

    void onConfigurationChanged(Gesture gesture);

    void onConfigurationsReset();

    void onError(GestureConfigurationInfo info, Reason reason);
}
