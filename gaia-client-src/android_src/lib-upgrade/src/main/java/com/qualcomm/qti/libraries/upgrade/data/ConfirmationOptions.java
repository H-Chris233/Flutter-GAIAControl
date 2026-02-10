/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

/**
 * <p>All the options that can apply for all the confirmations used in the upgrade process.</p>
 */
public enum ConfirmationOptions {

    CANCEL(0xFF),
    ABORT(0x01),
    CONFIRM(0x00),
    INTERACTIVE_COMMIT(0x00),
    SILENT_COMMIT(0x02);

    private final int value;

    ConfirmationOptions(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
