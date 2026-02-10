/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import android.util.Log;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus;

/**
 * This enumeration matches the values of the error status sent in an ERROR packet to the
 * framework status errors.
 */
public enum V3ErrorStatus implements GaiaErrorStatus {

    // not a default status
    /**
     * This status has no meaning for GAIA v3. It is only used in the application to identify a
     * status as not being feature specific.
     */
    FEATURE_SPECIFIC(0xFF),

    // protocol status
    /**
     * Description: An invalid Feature ID was specified.
     */
    FEATURE_NOT_SUPPORTED(0x00),
    /**
     * Description: An invalid PDU Specific ID was specified.
     */
    COMMAND_NOT_SUPPORTED(0x01),
    /**
     * Description: The host is not authenticated to use a Command ID or control.
     */
    NOT_AUTHENTICATED(0x02),
    /**
     * Description: The command was valid, but the device could not successfully carry out the
     * command.
     */
    INSUFFICIENT_RESOURCES(0x03),
    /**
     * Description: The device is in the process of authenticating the host.
     */
    AUTHENTICATING(0x04),
    /**
     * Description: An invalid parameter was used in the command.
     */
    INVALID_PARAMETER(0x05),
    /**
     * Description: The device is not in the correct state to process the command.
     */
    INCORRECT_STATE(0x06),
    /**
     * Description: The command is in progress.
     */
    IN_PROGRESS(0x07);

    /**
     * The minimum value of the range that represents framework status errors.
     */
    private static final int FRAMEWORK_ERROR_VALUE_MIN = 0;
    /**
     * The maximum value of the range that represents framework status errors.
     */
    private static final int FRAMEWORK_ERROR_VALUE_MAX = 127; // commented out in valueOf

    /**
     * The value that represents the error in a GAIA packet.
     */
    private final int mValue;

    /**
     * All the values of the enumeration to loop over them.
     */
    private static final V3ErrorStatus[] VALUES = values();

    /**
     * A constructor to build the enumeration from the values.
     */
    V3ErrorStatus(int value) {
        mValue = value;
    }

    /**
     * To get the value of the status.
     */
    @Override
    public int getValue() {
        return mValue;
    }

    /**
     * Matches the error value to a framework specific status. If the value could not be matched
     * or was identified as feature specific this method returns {@link #FEATURE_SPECIFIC}.
     */
    public static V3ErrorStatus valueOf(int value) {
        if (FRAMEWORK_ERROR_VALUE_MIN <= value && value <= FRAMEWORK_ERROR_VALUE_MAX) {
            for (V3ErrorStatus status : VALUES) {
                if (status.mValue == value) {
                    return status;
                }
            }
            Log.w("V3ErrorStatus", "[valueOf] received an unknown framework error: " + value);
        }

        return FEATURE_SPECIFIC;
    }

    @NonNull
    @Override
    public String toString() {
        return "V3_" + name();
    }

}
