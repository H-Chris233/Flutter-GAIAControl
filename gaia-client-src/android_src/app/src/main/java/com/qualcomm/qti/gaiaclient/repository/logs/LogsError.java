/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.logs;

import com.qualcomm.qti.gaiaclient.core.data.Reason;

public enum LogsError {

    PROTOCOL_ERROR,
    DISCONNECTED,
    FILE_CREATION_FAILED,
    FILE_WRITING_FAILED,
    NOT_SUPPORTED,
    NOT_AVAILABLE,
    NO_DATA,
    NO_DEBUG_PARTITION;

    public static LogsError valueOf(Reason reason) {
        switch (reason) {
            case NOT_SUPPORTED:
                return LogsError.NOT_SUPPORTED;
            case FILE_CREATION_FAILED:
                return LogsError.FILE_CREATION_FAILED;
            case FILE_WRITING_FAILED:
                return FILE_WRITING_FAILED;
            case NOT_AVAILABLE:
                return NOT_AVAILABLE;
            case DISCONNECTED:
                return DISCONNECTED;
            case LOGS_NO_DATA:
                return NO_DATA;
            case LOGS_NO_DEBUG_PARTITION:
                return NO_DEBUG_PARTITION;

            case SENDING_FAILED:
            case AUTHENTICATION:
            case NOTIFICATION_NOT_SUPPORTED:
            case MALFORMED_REQUEST:
            case NO_RESPONSE:
            case UNKNOWN: // unidentified state from the device
                return PROTOCOL_ERROR;

            default:
                // all are unexpected
                return null;
        }
    }

}
