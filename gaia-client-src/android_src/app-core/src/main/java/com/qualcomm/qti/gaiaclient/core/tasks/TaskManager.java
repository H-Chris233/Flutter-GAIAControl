/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;

import androidx.annotation.NonNull;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>The core library uses threads to run its Bluetooth connection but also for heavy processes
 * - such as the upgrade of a device.</p>
 * <p>To ease the utilisation of the core library it provides a TaskManager to run
 * tasks - implemented as {@link Runnable} - on the UI/Main Thread or a background thread.</p>
 * <p>The task manager also provides a mechanism to run scheduled task. This is to avoid background
 * threads to create UI thread attached {@link android.os.Handler} that could run long processes
 * within the UI thread.</p>
 * <p>Except for {@link com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber} that
 * specifies otherwise all the calls initiated by the library are done on a background thread.</p>
 */
public interface TaskManager {

    /**
     * To get the background executor used in this application for any API that requires one.
     */
    ThreadPoolExecutor getBackgroundExecutor();

    /**
     * To run a task on the UI thread, only use for UI related tasks.
     */
    void runOnMain(@NonNull Runnable runnable);

    /**
     * To run a task on a background thread, only use for non UI related tasks.
     */
    void runInBackground(@NonNull Runnable runnable);

    /**
     * To schedule a background task. It is recommended to use this when extending the core
     * library to avoid Handlers attached to run tasks within the Main Thread when they are
     * triggered.
     */
    long schedule(@NonNull Runnable runnable, long delayMs);

    /**
     * To cancel a scheduled task. The task can only be cancelled if it has not started yet.
     */
    void cancelScheduled(long id);

}
