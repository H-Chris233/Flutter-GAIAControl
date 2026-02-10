/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.core;

/**
 * <p>Defines the different execution types that can be used when a {@link Subscriber} gets notified
 * of an event by a {@link Publisher}. It indicates on what category of threads the subscriber
 * should be notified.</p>
 */
public enum ExecutionType {
    /**
     * For a subscriber to be notified onto the UI/Main thread of the application. Must be used
     * for any event that triggers UI changes.
     */
    UI_THREAD,
    /**
     * For a subscriber to be notified onto a background thread to avoid loading the UI/Main
     * thread with heavy processes. If UI tasks were run onto those threads, the application
     * would most certainly crash.
     */
    BACKGROUND
}
