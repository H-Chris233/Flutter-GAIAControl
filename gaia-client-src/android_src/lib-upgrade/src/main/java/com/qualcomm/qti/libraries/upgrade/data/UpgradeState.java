/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A class to store the different states for the upgrade process synchronously if the caller of
 * the library uses it through different threads.
 */
public final class UpgradeState {

    /**
     * To know if the upgrade process is currently running.
     */
    private final AtomicBoolean isUpgrading = new AtomicBoolean(false);
    /**
     * To know if the upgrade process is currently aborting.
     */
    private final AtomicBoolean isAborting = new AtomicBoolean(false);
    /**
     * To know if the upgrade process should be aborting.
     */
    private final AtomicBoolean mustAbort = new AtomicBoolean(false);
    /**
     * To know how many attempts to start the upgrade have been made.
     */
    private final AtomicInteger startAttempts = new AtomicInteger(0);
    /**
     * To log the full process in the Android logs.
     */
    private final AtomicBoolean showLogs = new AtomicBoolean(false);
    /**
     * To know if the upgrade must be restarted after an error or a process issue.
     */
    private final AtomicBoolean mustRestartUpgrade = new AtomicBoolean(false);

    /**
     * Empty constructor to build a new instance of this class.
     */
    public UpgradeState() {
    }

    /**
     * To know if the upgrade process is currently running.
     */
    public boolean isUpgrading() {
        return isUpgrading.get();
    }

    /**
     * To update the state as upgrading or not.
     * This method also compares the expected state to the current value prior to set it.
     * It returns True if the operation was successful, meaning that the current value was the
     * expected value and the current value is now the given value.
     */
    public boolean setIsUpgrading(boolean expected, boolean value) {
        return isUpgrading.compareAndSet(expected, value);
    }

    /**
     * To know if the upgrade process is currently aborting.
     */
    public boolean isAborting() {
        return isAborting.get();
    }

    /**
     * To update the state as aborting or not.
     */
    public void setIsAborting(boolean isAborting) {
        this.isAborting.set(isAborting);
    }

    /**
     * To know if the full process must be logged.
     */
    public boolean showLogs() {
        return showLogs.get();
    }

    /**
     * To update the state to show logs or not.
     */
    public void setShowLogs(boolean show) {
        this.showLogs.set(show);
    }

    /**
     * To know if the upgrade must be restarted after an error or a process issue.
     */
    public boolean mustRestartUpgrade() {
        return mustRestartUpgrade.get();
    }

    /**
     * To set the state as to be restarted or not.
     */
    public void setMustRestartUpgrade(boolean must) {
        this.mustRestartUpgrade.set(must);
    }

    /**
     * To set the state as to be aborted or not.
     */
    public void setMustAbort(boolean must) {
        this.mustAbort.set(must);
    }

    /**
     * To know how many attempts to start the upgrade have been made.
     */
    public int getStartAttempts() {
        return startAttempts.get();
    }

    /**
     * To reset the number of attempts to 0.
     */
    public void resetStartAttempts() {
        this.startAttempts.set(0);
    }

    /**
     * To increment the attempts by one.
     */
    public void incrementStartAttempts() {
        this.startAttempts.incrementAndGet();
    }

    /**
     * To know if the upgrade process should be aborted.
     */
    public boolean mustAbort() {
        return mustAbort.get();
    }

    /**
     * To reset all the states for the upgrade.
     */
    public void reset() {
        setIsUpgrading(isUpgrading(), false);
        setIsAborting(false);
        setMustAbort(false);
        resetStartAttempts();
        setShowLogs(false);
        setMustRestartUpgrade(false);
    }
}
