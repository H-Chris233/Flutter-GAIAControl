/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade;

import androidx.annotation.NonNull;

/**
 * A factory to instantiate an UpgradeManager.
 */
public final class UpgradeManagerFactory {

    /**
     * To get an UpgradeManager.
     *
     * @param listener
     *         An object that implements the {@link UpgradeListener} interface.
     * @param scheduler
     *         An object that implements the {@link UpgradeTaskScheduler} interface for this
     *         manager to schedule upgrade tasks to ve run later on.
     *
     * @return an instance of an UpgradeManager.
     */
    public static UpgradeManager buildUpgradeManager(@NonNull UpgradeListener listener,
                                                     @NonNull UpgradeTaskScheduler scheduler) {
        return new UpgradeManagerWrapper(listener, scheduler);

    }

}
