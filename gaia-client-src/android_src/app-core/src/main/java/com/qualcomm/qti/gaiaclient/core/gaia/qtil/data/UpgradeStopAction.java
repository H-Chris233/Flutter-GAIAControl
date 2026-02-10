/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import android.util.Log;

import androidx.annotation.NonNull;

public enum UpgradeStopAction {

    DISCONNECT_UPGRADE(0x00),
    STOP_SENDING_DATA(0x01);

    private static final String TAG = "UpgradeStopAction";

    private final int value;

    private static final UpgradeStopAction[] VALUES = values();

    UpgradeStopAction(int value) {
        this.value = value;
    }

    @NonNull
    public static UpgradeStopAction valueOf(int value) {
        for (UpgradeStopAction action : VALUES) {
            if (action.value == value) {
                return action;
            }
        }

        // default behaviour is DISCONNECT_UPGRADE if action is not implemented
        Log.w(TAG, "[valueOf] Unsupported action: value=" + value);
        return DISCONNECT_UPGRADE;
    }

}
