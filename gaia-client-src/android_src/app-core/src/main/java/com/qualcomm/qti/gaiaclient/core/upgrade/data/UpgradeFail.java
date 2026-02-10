/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.upgrade.data;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeError;

public class UpgradeFail {

    @NonNull
    private final UpgradeErrorStatus upgradeStatus;

    private final UpgradeError error;

    private final GaiaErrorStatus gaiaStatus;

    public UpgradeFail(@NonNull UpgradeErrorStatus upgradeStatus) {
        this.upgradeStatus = upgradeStatus;
        this.error = null;
        this.gaiaStatus = null;
    }

    public UpgradeFail(GaiaErrorStatus gaiaStatus) {
        this.upgradeStatus = UpgradeErrorStatus.GAIA_RESPONSE_ERROR;
        this.error = null;
        this.gaiaStatus = gaiaStatus;
    }

    public UpgradeFail(UpgradeError error) {
        this.upgradeStatus = UpgradeErrorStatus.UPGRADE_PROCESS_ERROR;
        this.error = error;
        this.gaiaStatus = null;
    }

    @NonNull
    public UpgradeErrorStatus getErrorStatus() {
        return upgradeStatus;
    }

    public UpgradeError getError() {
        return error;
    }

    public GaiaErrorStatus getGaiaStatus() {
        return gaiaStatus;
    }
}
