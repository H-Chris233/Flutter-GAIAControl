/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.core;

import androidx.annotation.NonNull;

/**
 * <p>Subscribers are used to observe {@link Publisher}. It gets notified when a corresponding
 * publisher has an event to send.</p>
 * <p>By default a subscriber gets notified in the {@link ExecutionType#UI_THREAD}. Override
 * {@link #getExecutionType()} to change this.</p>
 */
public interface Subscriber {

    /**
     * The key that identifies to what Publication the subscriber corresponds. This is used to
     * match the subscriber to publishers when subscribing it with
     * {@link com.qualcomm.qti.gaiaclient.core.publications.PublicationManager#subscribe(Subscriber)}.
     *
     * @return the subscription key, this must be unique within the application.
     */
    @NonNull
    Subscription getSubscription();

    /**
     * Defines on what thread the subscriber must be called when it receives an event. By default
     * this returns {@link ExecutionType#UI_THREAD} to indicate that the subscriber should be
     * notified in the UI thread.
     */
    @NonNull
    default ExecutionType getExecutionType() {
        return ExecutionType.UI_THREAD;
    }

}
