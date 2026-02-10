/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

/**
 * All messages from the SendingThread and ReceivingThread are run using a ThreadPoolExecutor to
 * avoid blocking them or the MainThread.
 */
final class TaskManagerImpl {

    private final LinkedBlockingQueue<Runnable> mTasksQueue = new LinkedBlockingQueue<>();

    private final MainThreadExecutor mMainExecutor = new MainThreadExecutor();

    private final ThreadPoolExecutor mBackgroundExecutor = new ThreadPoolExecutor(2, 4, 60L,
                                                                                  TimeUnit.SECONDS,
                                                                                  mTasksQueue,
                                                                                  new BackgroundThreadFactory());

    private final ScheduleExecutor mScheduleExecutor = new ScheduleExecutor();

    TaskManagerImpl() {
        // empty constructor
    }

    ThreadPoolExecutor getBackgroundExecutor() {
        return mBackgroundExecutor;
    }

    void runOnMain(@NonNull Runnable runnable) {
        executeOnMainThread(runnable);
    }

    void runInBackground(@NonNull Runnable runnable) {
        executeOnBackgroundThread(runnable);
    }

    long schedule(@NonNull Runnable runnable, long delayMs) {
        return executeDelayedTask(runnable, delayMs);
    }

    void cancelScheduled(long id) {
        cancelDelayedTask(id);
    }

    void release() {
        resetExecutor();
    }

    private void executeOnMainThread(@NonNull Runnable runnable) {
        mMainExecutor.execute(runnable);
    }

    private void executeOnBackgroundThread(@NonNull Runnable runnable) {
        mBackgroundExecutor.execute(runnable);
    }

    private long executeDelayedTask(@NonNull Runnable runnable, long delayInMs) {
        return mScheduleExecutor.execute(runnable, delayInMs);
    }

    private void cancelDelayedTask(long id) {
        mScheduleExecutor.cancel(id);
    }

    private void resetExecutor() {
        mTasksQueue.clear();
        mBackgroundExecutor.purge();
    }

}
