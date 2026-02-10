/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

public enum EndType {
    SILENT_COMMIT,
    COMPLETE,
    UPGRADE_IN_PROGRESS_WITH_DIFFERENT_ID,
    ABORTED,
    COMPLETE_WITH_SECURITY_UPDATE_FAILED
}
