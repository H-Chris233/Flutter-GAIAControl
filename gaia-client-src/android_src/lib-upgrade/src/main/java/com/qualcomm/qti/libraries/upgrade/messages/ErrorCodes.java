/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.messages;

import android.annotation.SuppressLint;

import androidx.annotation.IntDef;

import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class gives all error codes that can be sent from the device may send. Those codes are
 * encapsulated into an {@link OpCodes.Enum#UPGRADE_ERROR_IND UPGRADE_ERROR_IND} message.</p>
 * <ul>
 * <li>Errors are considered as fatal: the Host should abort the process.</li>
 * <li>Warnings are considered as informational: the device will choose to abort or continue the
 * process.</li>
 * </ul>
 */
@SuppressWarnings({"unused", "deprecation"})
public final class ErrorCodes {

    /**
     * <p>The enumeration of all return codes the Device can send.</p>
     */
    @IntDef(flag = true, value = {Enum.ERROR_UNKNOWN_ID,
                                  Enum.SUCCESS_UNEXPECTED, Enum.OEM_VALIDATION_SUCCESS_UNEXPECTED,
                                  Enum.DATA_TRANSFER_COMPLETE_UNEXPECTED, Enum.HASHING_IN_PROGRESS_UNEXPECTED,
                                  Enum.ERROR_WRONG_VARIANT,
                                  Enum.ERROR_INTERNAL_ERROR_DEPRECATED,
                                  Enum.ERROR_WRONG_PARTITION_NUMBER, Enum.ERROR_PARTITION_SIZE_MISMATCH,
                                  Enum.ERROR_PARTITION_TYPE_NOT_FOUND,
                                  Enum.ERROR_PARTITION_OPEN_FAILED, Enum.ERROR_PARTITION_WRITE_FAILED,
                                  Enum.ERROR_PARTITION_CLOSE_FAILED_1,
                                  Enum.ERROR_SFS_VALIDATION_FAILED, Enum.ERROR_OEM_VALIDATION_FAILED,
                                  Enum.ERROR_UPGRADE_FAILED,
                                  Enum.ERROR_APP_NOT_READY, Enum.ERROR_LOADER_ERROR, Enum.ERROR_UNEXPECTED_LOADER_MSG,
                                  Enum.ERROR_MISSING_LOADER_MSG, Enum.ERROR_BATTERY_LOW,
                                  Enum.ERROR_BAD_LENGTH_PARTITION_PARSE,
                                  Enum.ERROR_BAD_LENGTH_TOO_SHORT, Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER,
                                  Enum.ERROR_BAD_LENGTH_PARTITION_HEADER, Enum.ERROR_BAD_LENGTH_SIGNATURE,
                                  Enum.ERROR_BAD_LENGTH_DATA_HANDLER_RESUME, Enum.ERROR_PARTITION_CLOSE_FAILED_2,
                                  Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER, Enum.ERROR_PARTITION_TYPE_NOT_MATCHING,
                                  Enum.ERROR_PARTITION_TYPE_TWO_DFU, Enum.ERROR_PARTITION_WRITE_FAILED_HEADER,
                                  Enum.ERROR_PARTITION_WRITE_FAILED_DATA, Enum.ERROR_INTERNAL_ERROR_1,
                                  Enum.ERROR_INTERNAL_ERROR_2,
                                  Enum.ERROR_INTERNAL_ERROR_3, Enum.ERROR_INTERNAL_ERROR_4, Enum.ERROR_INTERNAL_ERROR_5,
                                  Enum.ERROR_INTERNAL_ERROR_6, Enum.ERROR_INTERNAL_ERROR_7,
                                  Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE,
                                  Enum.WARN_SYNC_ID_IS_DIFFERENT, Enum.UNKNOWN_ERROR, Enum.ERROR_INVALID_SYNC_ID,
                                  Enum.ERROR_IN_ERROR_STATE,
                                  Enum.ERROR_NO_MEMORY, Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS,
                                  Enum.ERROR_BAD_LENGTH_DEPRECATED,
                                  Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER,
                                  Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1,
                                  Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2,
                                  Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA,
                                  Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER, Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY,
                                  Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE, Enum.ERROR_FILE_TOO_SMALL,
                                  Enum.ERROR_FILE_TOO_BIG, Enum.ERROR_SILENT_COMMIT_NOT_SUPPORTED,
                                  Enum.WARN_SYNC_ID_IS_ZERO, Enum.ERROR_TIME_OUT,
                                  Enum.ERROR_REQUESTED_VERSION_MISMATCH})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface Enum {

        /**
         * <p>This error does not exist in the protocol and is only used by this library to have a
         * default error in
         * case a received value does not match any known return code.</p>
         */
        short UNKNOWN_ERROR = -1;

        /*
         * Error states in the device code. All 3 are unexpected.
         */
        short SUCCESS_UNEXPECTED = 0x00;
        short OEM_VALIDATION_SUCCESS_UNEXPECTED = 0x01;
        short DATA_TRANSFER_COMPLETE_UNEXPECTED = 0x02;
        short HASHING_IN_PROGRESS_UNEXPECTED = 0x03;

        /*
         * Possible errors
         */
        short ERROR_INTERNAL_ERROR_DEPRECATED = 0x10;
        short ERROR_UNKNOWN_ID = 0x11;
        short ERROR_BAD_LENGTH_DEPRECATED = 0x12;
        short ERROR_WRONG_VARIANT = 0x13;
        short ERROR_WRONG_PARTITION_NUMBER = 0x14;
        short ERROR_PARTITION_SIZE_MISMATCH = 0x15;
        short ERROR_PARTITION_TYPE_NOT_FOUND = 0x16;
        short ERROR_PARTITION_OPEN_FAILED = 0x17;
        short ERROR_PARTITION_WRITE_FAILED = 0x18;
        short ERROR_PARTITION_CLOSE_FAILED_1 = 0x19;
        short ERROR_SFS_VALIDATION_FAILED = 0x1A;
        short ERROR_OEM_VALIDATION_FAILED = 0x1B;
        short ERROR_UPGRADE_FAILED = 0x1C;
        short ERROR_APP_NOT_READY = 0x1D;
        short ERROR_LOADER_ERROR = 0x1E;
        short ERROR_UNEXPECTED_LOADER_MSG = 0x1F;
        short ERROR_MISSING_LOADER_MSG = 0x20;
        short ERROR_BATTERY_LOW = 0x21;
        short ERROR_INVALID_SYNC_ID = 0x22;
        short ERROR_IN_ERROR_STATE = 0x23;
        short ERROR_NO_MEMORY = 0x24;
        short ERROR_BAD_LENGTH_PARTITION_PARSE = 0x30;
        short ERROR_BAD_LENGTH_TOO_SHORT = 0x31;
        short ERROR_BAD_LENGTH_UPGRADE_HEADER = 0x32;
        short ERROR_BAD_LENGTH_PARTITION_HEADER = 0x33;
        short ERROR_BAD_LENGTH_SIGNATURE = 0x34;
        short ERROR_BAD_LENGTH_DATA_HANDLER_RESUME = 0x35;
        short ERROR_OEM_VALIDATION_FAILED_HEADERS = 0x38;
        short ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER = 0x39;
        short ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1 = 0x3A;
        short ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2 = 0x3B;
        short ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA = 0x3C;
        short ERROR_OEM_VALIDATION_FAILED_FOOTER = 0x3D;
        short ERROR_OEM_VALIDATION_FAILED_MEMORY = 0x3E;
        short ERROR_PARTITION_CLOSE_FAILED_2 = 0x40;
        short ERROR_PARTITION_CLOSE_FAILED_HEADER = 0x41;
        short ERROR_PARTITION_CLOSE_FAILED_PS_SPACE = 0x42;
        short ERROR_PARTITION_TYPE_NOT_MATCHING = 0x48;
        short ERROR_PARTITION_TYPE_TWO_DFU = 0x49;
        short ERROR_PARTITION_WRITE_FAILED_HEADER = 0x50;
        short ERROR_PARTITION_WRITE_FAILED_DATA = 0x51;
        short ERROR_FILE_TOO_SMALL = 0x58;
        short ERROR_FILE_TOO_BIG = 0x59;
        short ERROR_INTERNAL_ERROR_1 = 0x65;
        short ERROR_INTERNAL_ERROR_2 = 0x66;
        short ERROR_INTERNAL_ERROR_3 = 0x67;
        short ERROR_INTERNAL_ERROR_4 = 0x68;
        short ERROR_INTERNAL_ERROR_5 = 0x69;
        short ERROR_INTERNAL_ERROR_6 = 0x6A;
        short ERROR_INTERNAL_ERROR_7 = 0x6B;
        /**
         * This is expected when the host sends TRANSFER_COMPLETE_RES(SILENT_COMMIT(0x02)) when silent commit is not
         * supported by the device.
         *
         * @since upgrade protocol v4
         */
        short ERROR_SILENT_COMMIT_NOT_SUPPORTED = 0x70;
        /**
         * This is expected when the host has sent {@link OpCodes.Enum#UPGRADE_PUT_EARBUDS_IN_CASE_REQ} and the user has
         * not acted within a certain time, likely to be 60 seconds.
         *
         * @since upgrade protocol v5
         */
        short ERROR_TIME_OUT = 0x71;

        short WARN_APP_CONFIG_VERSION_INCOMPATIBLE = 0x80;
        /**
         * This error means the data is already uploaded onto the device.
         */
        short WARN_SYNC_ID_IS_DIFFERENT = 0x81;
        /**
         * This is expected when the host sends SYNC_REQ with a file identifier of 0.
         * This command would be expected in the case of an ongoing silent commit that needs to be aborted when a
         * host does not know the current identifier.
         *
         * @since upgrade protocol v4
         */
        short WARN_SYNC_ID_IS_ZERO = 0x82;
        /**
         * This is expected when the earbuds need to be upgraded to the same version (e.g. one of the two is new)
         * and the provided upgrade file is different than the one that has been requested by the client.
         */
        short ERROR_REQUESTED_VERSION_MISMATCH = 0x83;
    }

    /**
     * <p>To get the ReturnCode corresponding to the given value.</p>
     *
     * @param code
     *         The value to get the corresponding Return Code.
     *
     * @return The corresponding ReturnCode or {@link Enum#UNKNOWN_ERROR UNKNOWN_ERROR} of the value
     *         is unknown.
     */
    @ErrorCodes.Enum
    public static int getReturnCode(short code) {
        switch (code) {
            case Enum.ERROR_INTERNAL_ERROR_DEPRECATED:
                return Enum.ERROR_INTERNAL_ERROR_DEPRECATED;
            case Enum.ERROR_UNKNOWN_ID:
                return Enum.ERROR_UNKNOWN_ID;
            case Enum.ERROR_WRONG_VARIANT:
                return Enum.ERROR_WRONG_VARIANT;
            case Enum.ERROR_WRONG_PARTITION_NUMBER:
                return Enum.ERROR_WRONG_PARTITION_NUMBER;
            case Enum.ERROR_PARTITION_SIZE_MISMATCH:
                return Enum.ERROR_PARTITION_SIZE_MISMATCH;
            case Enum.ERROR_PARTITION_TYPE_NOT_FOUND:
                return Enum.ERROR_PARTITION_TYPE_NOT_FOUND;
            case Enum.ERROR_PARTITION_OPEN_FAILED:
                return Enum.ERROR_PARTITION_OPEN_FAILED;
            case Enum.ERROR_PARTITION_WRITE_FAILED:
                return Enum.ERROR_PARTITION_WRITE_FAILED;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_1:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_1;
            case Enum.ERROR_SFS_VALIDATION_FAILED:
                return Enum.ERROR_SFS_VALIDATION_FAILED;
            case Enum.ERROR_OEM_VALIDATION_FAILED:
                return Enum.ERROR_OEM_VALIDATION_FAILED;
            case Enum.ERROR_UPGRADE_FAILED:
                return Enum.ERROR_UPGRADE_FAILED;
            case Enum.ERROR_APP_NOT_READY:
                return Enum.ERROR_APP_NOT_READY;
            case Enum.ERROR_LOADER_ERROR:
                return Enum.ERROR_LOADER_ERROR;
            case Enum.ERROR_UNEXPECTED_LOADER_MSG:
                return Enum.ERROR_UNEXPECTED_LOADER_MSG;
            case Enum.ERROR_MISSING_LOADER_MSG:
                return Enum.ERROR_MISSING_LOADER_MSG;
            case Enum.ERROR_BATTERY_LOW:
                return Enum.ERROR_BATTERY_LOW;
            case Enum.ERROR_INVALID_SYNC_ID:
                return Enum.ERROR_INVALID_SYNC_ID;
            case Enum.ERROR_IN_ERROR_STATE:
                return Enum.ERROR_IN_ERROR_STATE;
            case Enum.ERROR_NO_MEMORY:
                return Enum.ERROR_NO_MEMORY;
            case Enum.ERROR_BAD_LENGTH_PARTITION_PARSE:
                return Enum.ERROR_BAD_LENGTH_PARTITION_PARSE;
            case Enum.ERROR_BAD_LENGTH_TOO_SHORT:
                return Enum.ERROR_BAD_LENGTH_TOO_SHORT;
            case Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER:
                return Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER;
            case Enum.ERROR_BAD_LENGTH_PARTITION_HEADER:
                return Enum.ERROR_BAD_LENGTH_PARTITION_HEADER;
            case Enum.ERROR_BAD_LENGTH_SIGNATURE:
                return Enum.ERROR_BAD_LENGTH_SIGNATURE;
            case Enum.ERROR_BAD_LENGTH_DATA_HANDLER_RESUME:
                return Enum.ERROR_BAD_LENGTH_DATA_HANDLER_RESUME;
            case Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS:
                return Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS;
            case Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER:
                return Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1:
                return Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2:
                return Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA:
                return Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA;
            case Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER:
                return Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER;
            case Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY:
                return Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_2:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_2;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE:
                return Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE;
            case Enum.ERROR_PARTITION_TYPE_NOT_MATCHING:
                return Enum.ERROR_PARTITION_TYPE_NOT_MATCHING;
            case Enum.ERROR_PARTITION_TYPE_TWO_DFU:
                return Enum.ERROR_PARTITION_TYPE_TWO_DFU;
            case Enum.ERROR_PARTITION_WRITE_FAILED_HEADER:
                return Enum.ERROR_PARTITION_WRITE_FAILED_HEADER;
            case Enum.ERROR_PARTITION_WRITE_FAILED_DATA:
                return Enum.ERROR_PARTITION_WRITE_FAILED_DATA;
            case Enum.ERROR_FILE_TOO_SMALL:
                return Enum.ERROR_FILE_TOO_SMALL;
            case Enum.ERROR_FILE_TOO_BIG:
                return Enum.ERROR_FILE_TOO_BIG;
            case Enum.ERROR_INTERNAL_ERROR_1:
                return Enum.ERROR_INTERNAL_ERROR_1;
            case Enum.ERROR_INTERNAL_ERROR_2:
                return Enum.ERROR_INTERNAL_ERROR_2;
            case Enum.ERROR_INTERNAL_ERROR_3:
                return Enum.ERROR_INTERNAL_ERROR_3;
            case Enum.ERROR_INTERNAL_ERROR_4:
                return Enum.ERROR_INTERNAL_ERROR_4;
            case Enum.ERROR_INTERNAL_ERROR_5:
                return Enum.ERROR_INTERNAL_ERROR_5;
            case Enum.ERROR_INTERNAL_ERROR_6:
                return Enum.ERROR_INTERNAL_ERROR_6;
            case Enum.ERROR_INTERNAL_ERROR_7:
                return Enum.ERROR_INTERNAL_ERROR_7;
            case Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE:
                return Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE;
            case Enum.WARN_SYNC_ID_IS_DIFFERENT:
                return Enum.WARN_SYNC_ID_IS_DIFFERENT;
            case Enum.ERROR_BAD_LENGTH_DEPRECATED:
                return Enum.ERROR_BAD_LENGTH_DEPRECATED;
            case Enum.ERROR_SILENT_COMMIT_NOT_SUPPORTED:
                return Enum.ERROR_SILENT_COMMIT_NOT_SUPPORTED;
            case Enum.ERROR_TIME_OUT:
                return Enum.ERROR_TIME_OUT;
            case Enum.WARN_SYNC_ID_IS_ZERO:
                return Enum.WARN_SYNC_ID_IS_ZERO;
            case Enum.SUCCESS_UNEXPECTED:
                return Enum.SUCCESS_UNEXPECTED;
            case Enum.OEM_VALIDATION_SUCCESS_UNEXPECTED:
                return Enum.OEM_VALIDATION_SUCCESS_UNEXPECTED;
            case Enum.DATA_TRANSFER_COMPLETE_UNEXPECTED:
                return Enum.DATA_TRANSFER_COMPLETE_UNEXPECTED;
            case Enum.HASHING_IN_PROGRESS_UNEXPECTED:
                return Enum.HASHING_IN_PROGRESS_UNEXPECTED;
            case Enum.ERROR_REQUESTED_VERSION_MISMATCH:
                return Enum.ERROR_REQUESTED_VERSION_MISMATCH;

            case Enum.UNKNOWN_ERROR:
            default:
                return Enum.UNKNOWN_ERROR;
        }
    }

    /**
     * <p>To get a readable message for the given return code.</p>
     *
     * @param code
     *         The code which a readable message is wanted.
     *
     * @return The readable message which corresponds to the given code.
     */
    public static String getReturnCodesMessage(@ErrorCodes.Enum int code) {
        String message;
        switch (code) {
            case Enum.ERROR_INTERNAL_ERROR_DEPRECATED:
                message = "Deprecated error: internal error";
                break;
            case Enum.ERROR_UNKNOWN_ID:
                message = "error: unknown ID";
                break;
            case Enum.ERROR_BAD_LENGTH_DEPRECATED:
                message = "Deprecated error: bad length";
                break;
            case Enum.ERROR_WRONG_VARIANT:
                message = "error: wrong variant";
                break;
            case Enum.ERROR_WRONG_PARTITION_NUMBER:
                message = "error: wrong partition number";
                break;
            case Enum.ERROR_PARTITION_SIZE_MISMATCH:
                message = "error: partition size mismatch";
                break;
            case Enum.ERROR_PARTITION_TYPE_NOT_FOUND:
                message = "error: partition type not found";
                break;
            case Enum.ERROR_PARTITION_OPEN_FAILED:
                message = "error: partition open failed";
                break;
            case Enum.ERROR_PARTITION_WRITE_FAILED:
                message = "error: partition write failed";
                break;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_1:
                message = "Partition close failed type 1";
                break;
            case Enum.ERROR_SFS_VALIDATION_FAILED:
                message = "error: SFS validation failed";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED:
                message = "error: OEM validation failed";
                break;
            case Enum.ERROR_UPGRADE_FAILED:
                message = "error: upgrade failed";
                break;
            case Enum.ERROR_APP_NOT_READY:
                message = "error: application not ready";
                break;
            case Enum.ERROR_LOADER_ERROR:
                message = "error: loader error";
                break;
            case Enum.ERROR_UNEXPECTED_LOADER_MSG:
                message = "error: unexpected loader message";
                break;
            case Enum.ERROR_MISSING_LOADER_MSG:
                message = "error: missing loader message";
                break;
            case Enum.ERROR_BATTERY_LOW:
                message = "error: battery low";
                break;
            case Enum.ERROR_INVALID_SYNC_ID:
                message = "error: invalid sync ID";
                break;
            case Enum.ERROR_IN_ERROR_STATE:
                message = "error: in error state";
                break;
            case Enum.ERROR_NO_MEMORY:
                message = "error: no memory";
                break;
            case Enum.ERROR_BAD_LENGTH_PARTITION_PARSE:
                message = "error: bad length partition parse";
                break;
            case Enum.ERROR_BAD_LENGTH_TOO_SHORT:
                message = "error: bad length too short";
                break;
            case Enum.ERROR_BAD_LENGTH_UPGRADE_HEADER:
                message = "error: bad length upgrade header";
                break;
            case Enum.ERROR_BAD_LENGTH_PARTITION_HEADER:
                message = "error: bad length partition header";
                break;
            case Enum.ERROR_BAD_LENGTH_SIGNATURE:
                message = "error: bad length signature";
                break;
            case Enum.ERROR_BAD_LENGTH_DATA_HANDLER_RESUME:
                message = "error: bad length data handler resume";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_HEADERS:
                message = "error: OEM validation failed headers";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_UPGRADE_HEADER:
                message = "error: OEM validation failed upgrade header";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER1:
                message = "error: OEM validation failed partition header 1";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_HEADER2:
                message = "error: OEM validation failed partition header 2";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_PARTITION_DATA:
                message = "error: OEM validation failed partition data";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_FOOTER:
                message = "error: OEM validation failed footer";
                break;
            case Enum.ERROR_OEM_VALIDATION_FAILED_MEMORY:
                message = "error: OEM validation failed memory";
                break;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_2:
                message = "error: partition close failed type 2";
                break;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_HEADER:
                message = "error: partition close failed header";
                break;
            case Enum.ERROR_PARTITION_CLOSE_FAILED_PS_SPACE:
                message = "error: partition close failed ps space";
                break;
            case Enum.ERROR_PARTITION_TYPE_NOT_MATCHING:
                message = "error: partition type not matching";
                break;
            case Enum.ERROR_PARTITION_TYPE_TWO_DFU:
                message = "error: partition type two DFU";
                break;
            case Enum.ERROR_PARTITION_WRITE_FAILED_HEADER:
                message = "error: partition write failed header";
                break;
            case Enum.ERROR_PARTITION_WRITE_FAILED_DATA:
                message = "error: partition write failed data";
                break;
            case Enum.ERROR_FILE_TOO_SMALL:
                message = "error: file too small";
                break;
            case Enum.ERROR_FILE_TOO_BIG:
                message = "error: file too big";
                break;
            case Enum.ERROR_INTERNAL_ERROR_1:
                message = "error: internal error 1";
                break;
            case Enum.ERROR_INTERNAL_ERROR_2:
                message = "error: internal error 2";
                break;
            case Enum.ERROR_INTERNAL_ERROR_3:
                message = "error: internal error 3";
                break;
            case Enum.ERROR_INTERNAL_ERROR_4:
                message = "error: internal error 4";
                break;
            case Enum.ERROR_INTERNAL_ERROR_5:
                message = "error: internal error 5";
                break;
            case Enum.ERROR_INTERNAL_ERROR_6:
                message = "error: internal error 6";
                break;
            case Enum.ERROR_INTERNAL_ERROR_7:
                message = "error: internal error 7";
                break;
            case Enum.WARN_APP_CONFIG_VERSION_INCOMPATIBLE:
                message = "warning: application configuration version incompatible";
                break;
            case Enum.WARN_SYNC_ID_IS_DIFFERENT:
                message = "warning: Sync ID is different";
                break;
            case Enum.ERROR_SILENT_COMMIT_NOT_SUPPORTED:
                message = "error: silent commit is not supported by the device";
                break;
            case Enum.WARN_SYNC_ID_IS_ZERO:
                message = "warning: a sync ID of 0 was sent";
                break;
            case Enum.ERROR_TIME_OUT:
                message = "error: the upgrade process has timed out on the device";
                break;
            case Enum.SUCCESS_UNEXPECTED:
                return "unexpected device state: success";
            case Enum.DATA_TRANSFER_COMPLETE_UNEXPECTED:
                return "unexpected device state: data transfer complete";
            case Enum.HASHING_IN_PROGRESS_UNEXPECTED:
                return "unexpected device state: hashing in progress";
            case Enum.OEM_VALIDATION_SUCCESS_UNEXPECTED:
                return "unexpected device state: OEM validation success";
            case Enum.ERROR_REQUESTED_VERSION_MISMATCH:
                message = "error: incoming DFU file version does not match requested version";
                break;
            case Enum.UNKNOWN_ERROR:
            default:
                message = "Unknown return code";
                break;
        }

        return "Device " + message + "(" + Utils.getHexadecimalString(code) + ")";
    }
}
