/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.devicelogs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.repository.logs.LogsError;

public enum LogsDialog {

    ERROR_FILE_NOT_FOUND(R.string.logs_error_file_not_found_title,
                         R.string.logs_error_file_not_found_message),
    ERROR_PROTOCOL_ERROR(R.string.logs_error_packet_title,
                         R.string.logs_error_packet_message),
    ERROR_DISCONNECTED(R.string.logs_error_disconnected_title,
                       R.string.logs_error_disconnected_message),
    ERROR_FILE_CREATION_FAILED(R.string.logs_error_file_creation_title,
                               R.string.logs_error_file_creation_message),
    ERROR_FILE_WRITING_FAILED(R.string.logs_error_file_writing_title,
                              R.string.logs_error_file_writing_message),
    ERROR_NOT_SUPPORTED(R.string.logs_error_file_not_supported_title,
                        R.string.logs_error_file_not_supported_message),
    ERROR_NOT_AVAILABLE(R.string.logs_error_file_not_available_title,
                        R.string.logs_error_file_not_available_message),
    ERROR_NO_DATA(R.string.logs_error_file_no_data_title,
                  R.string.logs_error_file_no_data_message),
    ERROR_NO_DEBUG_PARTITION(R.string.logs_error_file_no_debug_partition_title,
                             R.string.logs_error_file_no_debug_partition_message),
    DEFAULT_ERROR(R.string.logs_error_default_title,
                  R.string.logs_error_default_message);

    private final int titleId;

    private final int messageId;

    LogsDialog(int titleId, int messageId) {
        this.titleId = titleId;
        this.messageId = messageId;
    }

    public static LogsDialog valueOf(LogsError error) {
        switch (error) {
            case PROTOCOL_ERROR:
                return ERROR_PROTOCOL_ERROR;
            case DISCONNECTED:
                return ERROR_DISCONNECTED;
            case FILE_CREATION_FAILED:
                return ERROR_FILE_CREATION_FAILED;
            case FILE_WRITING_FAILED:
                return ERROR_FILE_WRITING_FAILED;
            case NOT_SUPPORTED:
                return ERROR_NOT_SUPPORTED;
            case NOT_AVAILABLE:
                return ERROR_NOT_AVAILABLE;
            case NO_DATA:
                return ERROR_NO_DATA;
            case NO_DEBUG_PARTITION:
                return ERROR_NO_DEBUG_PARTITION;
            default:
                return DEFAULT_ERROR;
        }
    }

    public String getTitle(@NonNull Context context) {
        return context.getString(titleId);
    }

    @NonNull
    public String getMessage(@NonNull Context context) {
        return context.getString(messageId);
    }

}
