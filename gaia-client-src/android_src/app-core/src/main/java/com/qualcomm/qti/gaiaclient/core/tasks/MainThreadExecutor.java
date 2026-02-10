/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

public class MainThreadExecutor {

    private final Handler handler = new Handler(Looper.getMainLooper());

    public void execute(@NonNull Runnable runnable) {
        handler.post(runnable);
    }

}
