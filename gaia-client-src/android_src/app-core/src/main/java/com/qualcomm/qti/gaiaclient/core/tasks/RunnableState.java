/*
 * ************************************************************************************************
 * * © 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;

import java.util.concurrent.atomic.AtomicBoolean;

public class RunnableState {

    private final AtomicBoolean running = new AtomicBoolean(false);

    private long id = IdCreator.NULL_ID;

    public boolean isRunning() {
        return running.get();
    }

    public void setIsRunning(boolean running) {
        this.running.set(running);
    }

    public boolean compareSetIsRunning(boolean expected, boolean newValue) {
        return this.running.compareAndSet(expected, newValue);
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
