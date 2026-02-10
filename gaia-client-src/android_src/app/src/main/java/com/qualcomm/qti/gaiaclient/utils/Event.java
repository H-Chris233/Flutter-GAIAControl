/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.utils;

/**
 * An object used to trigger one time fragment/activity events (snackbar, navigation, dialog) from the ViewModel.
 * To be used as the data inside LiveData.
 * After the event has been processed it does not trigger again.
 *
 * @param <T>
 *         The type of object that the event has as a payload.
 */
public class Event<T> {

    private final T data;
    private boolean isConsumed = false;

    public Event(T content) {
        this.data = content;
    }

    /**
     * To mark this event as consumed.
     */
    public void consume() {
        this.isConsumed = true;
    }

    /**
     * To know if this one time event has already been handled.
     *
     * @return True if the event was consumed, false otherwise.
     */
    public boolean isConsumed() {
        return isConsumed;
    }

    /**
     * Returns the content.
     */
    public T getData() {
        return data;
    }

}

