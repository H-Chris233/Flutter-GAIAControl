/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;

import androidx.annotation.NonNull;

import java.util.concurrent.ThreadPoolExecutor;

public class TaskManagerWrapper implements TaskManager {

    private final TaskManagerImpl mTaskManager;

    public TaskManagerWrapper() {
        mTaskManager = new TaskManagerImpl();
    }

    @Override
    public ThreadPoolExecutor getBackgroundExecutor() {
        return mTaskManager.getBackgroundExecutor();
    }

    @Override
    public void runOnMain(@NonNull Runnable runnable) {
        mTaskManager.runOnMain(runnable);
    }

    @Override
    public void runInBackground(@NonNull Runnable runnable) {
        mTaskManager.runInBackground(runnable);
    }

    @Override
    public long schedule(@NonNull Runnable runnable, long delayMs) {
        return mTaskManager.schedule(runnable, delayMs);
    }

    @Override
    public void cancelScheduled(long id) {
        mTaskManager.cancelScheduled(id);
    }

    public void release() {
        mTaskManager.release();
    }
}
