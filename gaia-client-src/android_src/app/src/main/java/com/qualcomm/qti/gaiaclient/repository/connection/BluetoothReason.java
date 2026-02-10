/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.connection;

import com.qualcomm.qti.gaiaclient.R;

public enum BluetoothReason {

    NO_BLUETOOTH,
    DISCOVERY_FAILED,
    NO_LOCATION,
    NO_PERMISSIONS,
    UNKNOWN;

    public int getMessageID() {
        switch (this) {
            case NO_BLUETOOTH: // unexpected
                return R.string.discovery_failed_no_bluetooth;
            case NO_LOCATION:
                return R.string.discovery_failed_no_location;
            case NO_PERMISSIONS: // unexpected
                return R.string.discovery_failed_no_permission;
            case DISCOVERY_FAILED:
            case UNKNOWN:
            default:
                return R.string.discovery_failed;
        }
    }

    public int getActionID() {
        switch (this) {
            case NO_LOCATION:
                return R.string.button_enable;

            case NO_PERMISSIONS: // unexpected
            case NO_BLUETOOTH:  // unexpected
            case DISCOVERY_FAILED:
            case UNKNOWN:
            default:
                return R.string.button_dismiss;
        }
    }
}
