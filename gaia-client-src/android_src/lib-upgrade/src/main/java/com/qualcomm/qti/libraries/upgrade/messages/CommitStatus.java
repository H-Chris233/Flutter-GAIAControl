/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.messages;

import androidx.annotation.Nullable;

public enum CommitStatus {

    UPGRADE_COMMIT_SUCCESS_SECURITY_UPDATE_FAILED((short) 1),
    UPGRADE_COMMIT_AND_SECURITY_UPDATE_SUCCESS((short) 2);

    final short value;

    private static final CommitStatus[] VALUES = values();

    CommitStatus(short value) {
        this.value = value;
    }

    @Nullable
    public static CommitStatus valueOf(short value) {
        for (CommitStatus status : VALUES) {
            if (status.value == value) {
                return status;
            }
        }

        return null;
    }
}
