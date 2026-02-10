/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common

import java.util.concurrent.atomic.AtomicBoolean

abstract class Observers {
    private val started:AtomicBoolean = AtomicBoolean(false)

    fun start() {
        if (started.compareAndSet(false, true)) {
            registerObservers()
        }
    }

    fun stop() {
        if (started.compareAndSet(true, false)) {
            unregisterObservers()
        }
    }

    protected abstract fun registerObservers()

    protected abstract fun unregisterObservers()
}
