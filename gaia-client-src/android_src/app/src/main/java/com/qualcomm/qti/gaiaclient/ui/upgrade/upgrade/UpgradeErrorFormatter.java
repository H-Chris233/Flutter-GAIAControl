/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.upgrade;

import static com.qualcomm.qti.libraries.upgrade.utils.Utils.getHexadecimalString;
import static com.qualcomm.qti.libraries.upgrade.utils.Utils.getHexadecimalStringTwoBytes;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeErrorStatus;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeError;
import com.qualcomm.qti.libraries.upgrade.messages.ErrorCodes;

public class UpgradeErrorFormatter {

    @NonNull
    private final Context context;

    @NonNull
    private final UpgradeFail fail;

    public UpgradeErrorFormatter(@NonNull Context context, @NonNull UpgradeFail fail) {
        this.context = context;
        this.fail = fail;
    }

    public String format() {
        UpgradeErrorStatus status = fail.getErrorStatus();

        if (status == UpgradeErrorStatus.GAIA_RESPONSE_ERROR) {
            GaiaErrorStatus gaiaStatus = fail.getGaiaStatus();
            String codeLabel = gaiaStatus == null ? ""
                                                  : formatCode(gaiaStatus.toString(),
                                                               getHexadecimalStringTwoBytes(gaiaStatus.getValue()));
            return getLabel(getString(R.string.upgrade_error_protocol),
                            formatReferences("GAIA_RESPONSE_ERROR", codeLabel));
        }
        else if (status == UpgradeErrorStatus.UPGRADE_PROCESS_ERROR) {
            Pair<String, String> labels = formatUpgradeError(fail.getError());
            String reference = formatReferences(status.toString(), labels.second);
            return getLabel(labels.first, reference);
        }
        else {
            return getLabel(getUpgradeErrorStatusLabel(status), status.toString());
        }
    }

    public Pair<String, String> formatUpgradeError(UpgradeError error) {
        if (error == null) {
            return new Pair<>(getString(R.string.upgrade_error_process_default), null);
        }

        @UpgradeError.ErrorTypes int type = error.getError();

        if (type == UpgradeError.ErrorTypes.RECEIVED_ERROR_FROM_DEVICE) {
            @ErrorCodes.Enum int deviceError = error.getReturnCode();
            String label = getString(R.string.upgrade_error_device_error);
            String reference = formatReferences("RECEIVED_ERROR_FROM_DEVICE",
                                                formatCode(getReturnCodeLabel(deviceError),
                                                           getHexadecimalString(deviceError)));
            return new Pair<>(label, reference);
        }
        else {
            return formatErrorType(type);
        }
    }

    private Pair<String, String> formatErrorType(@UpgradeError.ErrorTypes int type) {
        switch (type) {
            case UpgradeError.ErrorTypes.ERROR_BOARD_NOT_READY:
                return new Pair<>(getString(R.string.upgrade_error_board_not_ready), "ERROR_BOARD_NOT_READY");

            case UpgradeError.ErrorTypes.WRONG_DATA_PARAMETER:
                return new Pair<>(getString(R.string.upgrade_error_protocol), "WRONG_DATA_PARAMETER");

            case UpgradeError.ErrorTypes.UNSUPPORTED_PROTOCOL_VERSION:
                return new Pair<>(getString(R.string.upgrade_error_protocol), "UNSUPPORTED_PROTOCOL_VERSION");

            case UpgradeError.ErrorTypes.UNSUPPORTED_CONFIRMATION_OPTION:
                return new Pair<>(getString(R.string.upgrade_error_unsupported_confirmation),
                                  "UNSUPPORTED_CONFIRMATION_OPTION");

            case UpgradeError.ErrorTypes.EXCEPTION:
                return new Pair<>(getString(R.string.upgrade_error_exception), "EXCEPTION");

            case UpgradeError.ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING:
                return new Pair<>(getString(R.string.upgrade_error_upgrade_already_in_progress),
                                  "AN_UPGRADE_IS_ALREADY_PROCESSING");

            case UpgradeError.ErrorTypes.NO_DATA:
                return new Pair<>(getString(R.string.upgrade_error_file), "NO_DATA");

            case UpgradeError.ErrorTypes.RECEIVED_ERROR_FROM_DEVICE: //unexpected
                return new Pair<>(getString(R.string.upgrade_error_process_default), "RECEIVED_ERROR_FROM_DEVICE");

            default:
                return new Pair<>(getString(R.string.upgrade_error_process_default),
                                  formatCode("UNKNOWN", Integer.toString(type)));
        }
    }


    private String getUpgradeErrorStatusLabel(UpgradeErrorStatus status) {
        switch (status) {
            case GAIA_INITIALISATION_ERROR:
            case PACKET_ERROR:
                return getString(R.string.upgrade_error_protocol);

            case FILE_ERROR:
                return getString(R.string.upgrade_error_file);

            case RECONNECTION_ERROR:
                return getString(R.string.upgrade_error_reconnection);

            case UPGRADE_PROCESS_ERROR: // unexpected
            case GAIA_RESPONSE_ERROR: // unexpected
            default:
                return getString(R.string.upgrade_error_default);
        }
    }

    @NonNull
    private String formatReferences(@Nullable String head, @Nullable String tail) {
        boolean headEmpty = head == null || head.isEmpty() || head.equals("null");
        boolean tailEmpty = tail == null || tail.isEmpty() || tail.equals("null");

        if (headEmpty && tailEmpty) {
            return "";
        }
        else if (!headEmpty && tailEmpty) {
            return head;
        }
        else if (headEmpty && !tailEmpty) {
            return tail;
        }
        else {
            return getString(R.string.upgrade_error_references, head, tail);
        }
    }

    private String getReturnCodeLabel(@ErrorCodes.Enum int code) {
        switch (code) {
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_DEPRECATED:
                return "ERROR_INTERNAL_ERROR_DEPRECATED";
            case ErrorCodes.Enum.ERROR_UNKNOWN_ID:
                return "ERROR_UNKNOWN_ID";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_DEPRECATED:
                return "ERROR_BAD_LENGTH_DEPRECATED";
            case ErrorCodes.Enum.ERROR_WRONG_VARIANT:
                return "ERROR_WRONG_VARIANT";
            case ErrorCodes.Enum.ERROR_WRONG_PARTITION_NUMBER:
                return "ERROR_WRONG_PARTITION_NUMBER";
            case ErrorCodes.Enum.ERROR_PARTITION_SIZE_MISMATCH:
                return "ERROR_PARTITION_SIZE_MISMATCH";
            case ErrorCodes.Enum.ERROR_PARTITION_TYPE_NOT_FOUND:
                return "ERROR_PARTITION_TYPE_NOT_FOUND";
            case ErrorCodes.Enum.ERROR_PARTITION_OPEN_FAILED:
                return "ERROR_PARTITION_OPEN_FAILED";
            case ErrorCodes.Enum.ERROR_PARTITION_WRITE_FAILED:
                return "ERROR_PARTITION_WRITE_FAILED";
            case ErrorCodes.Enum.ERROR_PARTITION_CLOSE_FAILED_1:
                return "ERROR_PARTITION_CLOSE_FAILED_1";
            case ErrorCodes.Enum.ERROR_SFS_VALIDATION_FAILED:
                return "ERROR_SFS_VALIDATION_FAILED";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED:
                return "ERROR_OEM_VALIDATION_FAILED";
            case ErrorCodes.Enum.ERROR_UPGRADE_FAILED:
                return "ERROR_UPGRADE_FAILED";
            case ErrorCodes.Enum.ERROR_APP_NOT_READY:
                return "ERROR_APP_NOT_READY";
            case ErrorCodes.Enum.ERROR_LOADER_ERROR:
                return "ERROR_LOADER_ERROR";
            case ErrorCodes.Enum.ERROR_UNEXPECTED_LOADER_MSG:
                return "ERROR_UNEXPECTED_LOADER_MSG";
            case ErrorCodes.Enum.ERROR_MISSING_LOADER_MSG:
                return "ERROR_MISSING_LOADER_MSG";
            case ErrorCodes.Enum.ERROR_BATTERY_LOW:
                return "ERROR_BATTERY_LOW";
            case ErrorCodes.Enum.ERROR_INVALID_SYNC_ID:
                return "ERROR_INVALID_SYNC_ID";
            case ErrorCodes.Enum.ERROR_IN_ERROR_STATE:
                return "ERROR_IN_ERROR_STATE";
            case ErrorCodes.Enum.ERROR_NO_MEMORY:
                return "ERROR_NO_MEMORY";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_PARTITION_PARSE:
                return "ERROR_BAD_LENGTH_PARTITION_PARSE";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_TOO_SHORT:
                return "ERROR_BAD_LENGTH_TOO_SHORT";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER:
                return "ERROR_BAD_LENGTH_UPGRADE_HEADER";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_PARTITION_HEADER:
                return "ERROR_BAD_LENGTH_PARTITION_HEADER";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_SIGNATURE:
                return "ERROR_BAD_LENGTH_SIGNATURE";
            case ErrorCodes.Enum.ERROR_BAD_LENGTH_DATA_HANDLER_RESUME:
                return "ERROR_BAD_LENGTH_DATA_HANDLER_RESUME";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS:
                return "ERROR_OEM_VALIDATION_FAILED_HEADERS";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER:
                return "ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1:
                return "ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2:
                return "ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA:
                return "ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER:
                return "ERROR_OEM_VALIDATION_FAILED_FOOTER";
            case ErrorCodes.Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY:
                return "ERROR_OEM_VALIDATION_FAILED_MEMORY";
            case ErrorCodes.Enum.ERROR_PARTITION_CLOSE_FAILED_2:
                return "ERROR_PARTITION_CLOSE_FAILED_2";
            case ErrorCodes.Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER:
                return "ERROR_PARTITION_CLOSE_FAILED_HEADER";
            case ErrorCodes.Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE:
                return "ERROR_PARTITION_CLOSE_FAILED_PS_SPACE";
            case ErrorCodes.Enum.ERROR_PARTITION_TYPE_NOT_MATCHING:
                return "ERROR_PARTITION_TYPE_NOT_MATCHING";
            case ErrorCodes.Enum.ERROR_PARTITION_TYPE_TWO_DFU:
                return "ERROR_PARTITION_TYPE_TWO_DFU";
            case ErrorCodes.Enum.ERROR_PARTITION_WRITE_FAILED_HEADER:
                return "ERROR_PARTITION_WRITE_FAILED_HEADER";
            case ErrorCodes.Enum.ERROR_PARTITION_WRITE_FAILED_DATA:
                return "ERROR_PARTITION_WRITE_FAILED_DATA";
            case ErrorCodes.Enum.ERROR_FILE_TOO_SMALL:
                return "ERROR_FILE_TOO_SMALL";
            case ErrorCodes.Enum.ERROR_FILE_TOO_BIG:
                return "ERROR_FILE_TOO_BIG";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_1:
                return "ERROR_INTERNAL_ERROR_1";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_2:
                return "ERROR_INTERNAL_ERROR_2";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_3:
                return "ERROR_INTERNAL_ERROR_3";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_4:
                return "ERROR_INTERNAL_ERROR_4";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_5:
                return "ERROR_INTERNAL_ERROR_5";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_6:
                return "ERROR_INTERNAL_ERROR_6";
            case ErrorCodes.Enum.ERROR_INTERNAL_ERROR_7:
                return "ERROR_INTERNAL_ERROR_7";
            case ErrorCodes.Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE:
                return "WARN_APP_CONFIG_VERSION_INCOMPATIBLE";
            case ErrorCodes.Enum.WARN_SYNC_ID_IS_DIFFERENT:
                return "WARN_SYNC_ID_IS_DIFFERENT";
            case ErrorCodes.Enum.ERROR_SILENT_COMMIT_NOT_SUPPORTED:
                return "ERROR_SILENT_COMMIT_NOT_SUPPORTED";
            case ErrorCodes.Enum.WARN_SYNC_ID_IS_ZERO:
                return "WARN_SYNC_ID_IS_ZERO";
            case ErrorCodes.Enum.ERROR_TIME_OUT:
                return "ERROR_TIME_OUT";
            case ErrorCodes.Enum.SUCCESS_UNEXPECTED:
                return "SUCCESS_UNEXPECTED";
            case ErrorCodes.Enum.DATA_TRANSFER_COMPLETE_UNEXPECTED:
                return "DATA_TRANSFER_COMPLETE_UNEXPECTED";
            case ErrorCodes.Enum.HASHING_IN_PROGRESS_UNEXPECTED:
                return "HASHING_IN_PROGRESS_UNEXPECTED";
            case ErrorCodes.Enum.OEM_VALIDATION_SUCCESS_UNEXPECTED:
                return "OEM_VALIDATION_SUCCESS_UNEXPECTED";
            case ErrorCodes.Enum.ERROR_REQUESTED_VERSION_MISMATCH:
                return "ERROR_REQUESTED_VERSION_MISMATCH";
            case ErrorCodes.Enum.UNKNOWN_ERROR:
            default:
                return "UNKNOWN_ERROR";
        }
    }

    private String getLabel(String label, String reference) {
        return context.getString(R.string.upgrade_error_format, label, reference);
    }

    private String formatCode(String label, String code) {
        return getString(R.string.upgrade_error_code, label, code);
    }

    private String getString(@StringRes int id, Object value1, Object value2) {
        return context.getString(id, value1, value2);
    }

    private String getString(@StringRes int id) {
        return context.getString(id);
    }
}
