/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import android.util.Log;

import androidx.annotation.NonNull;

public enum UpgradeStartAction {

    CONNECT_UPGRADE(0x00),
    RESTART_SENDING_DATA(0x01);

    private static final String TAG = "UpgradeStartAction";

    private final int value;

    private static final UpgradeStartAction[] VALUES = values();

    UpgradeStartAction(int value) {
        this.value = value;
    }

    @NonNull
    public static UpgradeStartAction valueOf(int value) {
        for (UpgradeStartAction action : VALUES) {
            if (action.value == value) {
                return action;
            }
        }

        // default behaviour is CONNECT_UPGRADE if action is not implemented
        Log.w(TAG, "[valueOf] Unsupported action: value=" + value);
        return CONNECT_UPGRADE;
    }

}
