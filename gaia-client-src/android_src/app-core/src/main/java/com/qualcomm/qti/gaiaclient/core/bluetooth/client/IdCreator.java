/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client;

import java.util.concurrent.atomic.AtomicLong;

public final class IdCreator {

    public static final long NULL_ID = -1;

    private final AtomicLong COUNTER = new AtomicLong(NULL_ID);

    public long nextId() {
        // it is not expected to generate more IDs than 2^63-1
        return COUNTER.incrementAndGet();
    }

}
