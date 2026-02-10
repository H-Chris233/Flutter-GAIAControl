/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.GestureConfigurationSubscriber;

import java.util.Set;

public class GestureConfigurationPublisher extends Publisher<GestureConfigurationSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.GESTURE_CONFIGURATION;
    }

    public void publishNumberOfTouchpads(TouchpadConfiguration configuration) {
        forEachSubscriber(subscriber -> subscriber.onNumberOfTouchpads(configuration));
    }

    public void publishGestures(Set<Gesture> gestures) {
        forEachSubscriber(subscriber -> subscriber.onGestures(gestures));
    }

    public void publishGestureContexts(Set<GestureContext> contexts) {
        forEachSubscriber(subscriber -> subscriber.onGestureContexts(contexts));
    }

    public void publishActions(Set<Action> actions) {
        forEachSubscriber(subscriber -> subscriber.onActions(actions));
    }

    public void publishConfiguration(Gesture gesture, Set<Configuration> configurations) {
        forEachSubscriber(subscriber -> subscriber.onConfiguration(gesture, configurations));
    }

    public void publishConfigurationChanged(Gesture gesture) {
        forEachSubscriber(subscriber -> subscriber.onConfigurationChanged(gesture));
    }

    public void publishConfigurationsReset() {
        forEachSubscriber(GestureConfigurationSubscriber::onConfigurationsReset);
    }

    public void publishError(GestureConfigurationInfo info, Reason reason) {
        forEachSubscriber(subscriber -> subscriber.onError(info, reason));
    }
}
