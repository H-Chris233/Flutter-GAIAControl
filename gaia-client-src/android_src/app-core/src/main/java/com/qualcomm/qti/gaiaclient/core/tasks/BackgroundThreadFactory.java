/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;


import android.os.Process;

import java.util.concurrent.ThreadFactory;

import androidx.annotation.NonNull;

public class BackgroundThreadFactory implements ThreadFactory {

    private int count = 0;

    @Override
    public Thread newThread(@NonNull Runnable action) {
        Runnable runnable = () -> {
            try {
                Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);
            }
            catch (Throwable ignored) {

            }
            action.run();
        };

        count++;
        return new Thread(runnable, "GaiaClient-" + count);
    }
}
