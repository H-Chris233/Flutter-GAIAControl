/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.tasks;

import java.lang.ref.WeakReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;

public class ScheduleExecutor {

    private final ScheduledExecutorService mScheduleExecutor = Executors.newScheduledThreadPool(2);

    private final ConcurrentHashMap<Long, ScheduledRunnable> mDelayedTasks =
            new ConcurrentHashMap<>();

    private final IdCreator mIdCreator = new IdCreator();

    public long execute(@NonNull Runnable action, long delayInMs) {
        long id = mIdCreator.nextId();
        ScheduledRunnable scheduled = new ScheduledRunnable(action, id);
        mDelayedTasks.put(id, scheduled);
        mScheduleExecutor.schedule(scheduled, delayInMs, TimeUnit.MILLISECONDS);
        return id;
    }

    public void cancel(long id) {
        ScheduledRunnable scheduled = mDelayedTasks.remove(id);
        if (scheduled != null) {
            scheduled.cancel();
        }
    }

    private class ScheduledRunnable implements Runnable {

        private final AtomicBoolean mmIsRunning = new AtomicBoolean(true);

        @NonNull
        private final Runnable mmAction;

        private final long mmId;

        private final WeakReference<ConcurrentHashMap<Long, ScheduledRunnable>> mmTasksReference =
                new WeakReference<>(mDelayedTasks);

        ScheduledRunnable(@NonNull Runnable action, long id) {
            this.mmAction = action;
            this.mmId = id;
        }

        void cancel() {
            mmIsRunning.set(false);
        }

        @Override
        public void run() {
            if (mmIsRunning.get()) {
                ConcurrentHashMap<Long, ScheduledRunnable> scheduledTasks =
                        mmTasksReference.get();
                if (scheduledTasks != null) {
                    scheduledTasks.remove(mmId);
                }
                getTaskManager().runInBackground(mmAction);
            }
        }
    }

}
