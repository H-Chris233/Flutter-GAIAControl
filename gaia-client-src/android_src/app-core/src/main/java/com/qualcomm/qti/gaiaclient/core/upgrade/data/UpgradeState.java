/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.upgrade.data;

import java.util.Arrays;

public enum UpgradeState {

    INITIALISATION,
    UPLOAD,
    VALIDATION,
    REBOOT,
    VERIFICATION,
    COMPLETE,
    END,
    RECONNECTING,
    ABORTING,
    ABORTED,
    PAUSED;

    private static final UpgradeState[] UPGRADING_STATES = new UpgradeState[]{UpgradeState.INITIALISATION,
                                                                              UpgradeState.UPLOAD,
                                                                              UpgradeState.VALIDATION,
                                                                              UpgradeState.REBOOT,
                                                                              UpgradeState.VERIFICATION,
                                                                              UpgradeState.RECONNECTING,
                                                                              UpgradeState.PAUSED};

    public boolean isEnd() {
        return !Arrays.asList(UPGRADING_STATES).contains(this);
    }

}
