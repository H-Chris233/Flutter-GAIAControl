/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade;

/**
 * A task scheduler for the UpgradeManager to request some tasks to be run at a later time.
 */
public interface UpgradeTaskScheduler {

    /**
     * To schedule a task to be run after the given time.
     *
     * @param task
     *         The task to run.
     * @param delayMs
     *         How long in milliseconds the scheduler must wait before to run the task.
     */
    void scheduleTask(Runnable task, long delayMs);

}
