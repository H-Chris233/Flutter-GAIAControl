/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

import android.annotation.SuppressLint;

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;

import com.qualcomm.qti.libraries.upgrade.messages.ErrorCodes;
import com.qualcomm.qti.libraries.upgrade.utils.Utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>This class provides information for errors which occur during the Upgrade process.</p>
 */
@SuppressWarnings("unused")
public class UpgradeError {

    /**
     * <p>The type of the error.</p>
     */
    private final @ErrorTypes
    int mError;
    /**
     * lo
     * <p>If the error occurs on the device, the ReturnCode it provided.</p>
     */
    private final @ErrorCodes.Enum
    int mBoardError;
    /**
     * If the error is coming from an exception.
     */
    private final UpgradeException mException;

    /**
     * <p>To build a simple UpgradeError based on its error type.</p>
     *
     * @param type
     *         The type of the error.
     */
    public UpgradeError(@ErrorTypes int type) {
        this.mError = type;
        this.mBoardError = ErrorCodes.Enum.UNKNOWN_ERROR;
        this.mException = null;
    }

    /**
     * <p>To build a {@link UpgradeError.ErrorTypes#RECEIVED_ERROR_FROM_DEVICE
     * RECEIVED_ERROR_FROM_DEVICE} upgrade error
     * object.</p>
     * <p>As it is not possible to have a second constructor with only one int parameter even with a
     * different
     * annotation constraint - the {@link ErrorCodes.Enum ErrorCodes enumeration}
     * instead of {@link ErrorTypes ErrorTypes enumeration}. This method constraints the choice of
     * the type to
     * {@link ErrorTypes#RECEIVED_ERROR_FROM_DEVICE RECEIVED_ERROR_FROM_DEVICE} only.</p>
     *
     * @param type
     *         The type of the error, expected type:
     *         {@link ErrorTypes#RECEIVED_ERROR_FROM_DEVICE RECEIVED_ERROR_FROM_DEVICE}.
     * @param boardError
     *         The error which occurs on the device.
     */
    public UpgradeError(@IntRange(from = ErrorTypes.RECEIVED_ERROR_FROM_DEVICE, to =
            ErrorTypes.RECEIVED_ERROR_FROM_DEVICE)
                        @ErrorTypes int type, @ErrorCodes.Enum int boardError) {
        this.mError = type;
        this.mBoardError = boardError;
        this.mException = null;
    }

    /**
     * <p>To build a {@link UpgradeError.ErrorTypes#EXCEPTION EXCEPTION} Upgrade Error object.</p>
     *
     * @param exception
     *         The {@link UpgradeException} which occurs during the upgrade process.
     */
    public UpgradeError(UpgradeException exception) {
        this.mError = ErrorTypes.EXCEPTION;
        this.mBoardError = ErrorCodes.Enum.UNKNOWN_ERROR;
        this.mException = exception;
    }

    /**
     * <p>To get the error type for this object.</p>
     *
     * @return the defined error type for this upgrade error object.
     */
    public @ErrorTypes
    int getError() {
        return mError;
    }

    /**
     * <p>To get the return code the device sent.</p> <p>If the error type is not {@link
     * ErrorTypes#RECEIVED_ERROR_FROM_DEVICE RECEIVED_ERROR_FROM_DEVICE} the returned error is
     * {@link
     * ErrorCodes.Enum#UNKNOWN_ERROR UNKNOWN_ERROR}.</p>
     *
     * @return the return code associated with this Upgrade Error.
     */
    public @ErrorCodes.Enum
    int getReturnCode() {
        return mBoardError;
    }

    /**
     * <p>To get the Exception associated with this Upgrade Error.</p> <p>If the error type is not
     * {@link
     * ErrorTypes#EXCEPTION EXCEPTION}, this method returns null.</p>
     *
     * @return the Exception associated with this Upgrade Error.
     */
    public UpgradeException getException() {
        return mException;
    }

    /**
     * <p>All different errors which can occur when using the upgrade library.</p>
     */
    @IntDef(flag = true, value = {ErrorTypes.ERROR_BOARD_NOT_READY, ErrorTypes.WRONG_DATA_PARAMETER,
                                  ErrorTypes.RECEIVED_ERROR_FROM_DEVICE, ErrorTypes.EXCEPTION,
                                  ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING, ErrorTypes.NO_DATA,
                                  ErrorTypes.UNSUPPORTED_PROTOCOL_VERSION, ErrorTypes.UNSUPPORTED_CONFIRMATION_OPTION})
    @Retention(RetentionPolicy.SOURCE)
    @SuppressLint("ShiftFlags") // values are more readable this way
    public @interface ErrorTypes {

        /**
         * <p>This error occurs when there is an attempt to start the upgrade but the device is not
         * ready to process.</p>
         */
        int ERROR_BOARD_NOT_READY = 1;
        /**
         * <p>This error occurs when a received upgrade packet from the device does not match the
         * expected data: too much
         * information, not enough, etc.</p>
         */
        int WRONG_DATA_PARAMETER = 2;
        /**
         * <p>This error occurs when the device notifies that an error or a warning occurs
         * internally during its upgrade process.</p>
         */
        int RECEIVED_ERROR_FROM_DEVICE = 3;
        /**
         * <p>This error is reported when a {@link UpgradeException UpgradeExveption} occurs during
         * the process.</p>
         */
        int EXCEPTION = 4;
        /**
         * <p>This error occurs when there is an attempt to start the upgrade but the UpgradeManager
         * is already
         * processing an upgrade.</p>
         */
        int AN_UPGRADE_IS_ALREADY_PROCESSING = 5;
        /**
         * <p>This error occurs when the data to upload is empty or does not exist.</p>
         */
        int NO_DATA = 6;
        /**
         * <p>This error occurs when the device has a more recent protocol version than this library
         * is
         * implementing.</p>
         *
         * @since 1.3
         */
        int UNSUPPORTED_PROTOCOL_VERSION = 7;
        /**
         * <p>This error occurs when trying to use a {@link ConfirmationOptions ConfirmationOptions} that is not
         * supported for the {@link ConfirmationType ConfirmationType} it is used with.</p>
         *
         * @since upgrade protocol v4
         */
        int UNSUPPORTED_CONFIRMATION_OPTION = 8;
    }

    /**
     * <p>To get a human readable information for this error object.</p>
     *
     * @return A message built on all information contains in the error object.
     */
    public String getString() {
        switch (mError) {
            case ErrorTypes.ERROR_BOARD_NOT_READY:
                return "The process has been aborted: the device is not ready to start the upgrade. Note: this was" +
                        " attempted every 2 seconds for 5 times.";

            case ErrorTypes.WRONG_DATA_PARAMETER:
                return "The device does not send the expected parameter(s).";

            case ErrorTypes.RECEIVED_ERROR_FROM_DEVICE:
                return "An error occurs on the device during the upgrade process."
                        + "\n\t- Received error code: " + Utils.getHexadecimalString(mBoardError)
                        + "\n\t- Received error message: " + ErrorCodes.getReturnCodesMessage(mBoardError);

            case ErrorTypes.EXCEPTION:
                StringBuilder strBuilder = new StringBuilder();
                strBuilder.append("An Exception has occurred");
                if (mException != null) {
                    strBuilder.append(": ").append(mException.toString());
                }
                return strBuilder.toString();

            case ErrorTypes.AN_UPGRADE_IS_ALREADY_PROCESSING:
                return "Attempt to start an upgrade failed: an upgrade is already processing.";

            case ErrorTypes.NO_DATA:
                return "The provided data is null, empty or does not exist.";

            case ErrorTypes.UNSUPPORTED_PROTOCOL_VERSION:
                return "The device uses a protocol version not supported by this library.";

                case ErrorTypes.UNSUPPORTED_CONFIRMATION_OPTION:
                    return "The selection confirmation option is not supported for the confirmation it was used with.";

            default:
                return "An error has occurred during the upgrade process.";
        }
    }
}
