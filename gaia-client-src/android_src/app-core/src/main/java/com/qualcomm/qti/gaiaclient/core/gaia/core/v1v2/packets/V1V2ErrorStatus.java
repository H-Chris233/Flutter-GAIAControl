/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus;

/**
 * All acknowledgement status as defined for GAIA v1 and v2.
 */
public enum V1V2ErrorStatus implements GaiaErrorStatus {

    // app status
    NO_STATUS(-2),
    UNKNOWN(-1),

    // protocol status
    SUCCESS(0x00),
    COMMAND_NOT_SUPPORTED(0x01),
    NOT_AUTHENTICATED(0x02),
    INSUFFICIENT_RESOURCES(0x03),
    AUTHENTICATING(0x04),
    INVALID_PARAMETER(0x05),
    INCORRECT_STATE(0x06),
    IN_PROGRESS(0x07);

    private final int mValue;

    private static final V1V2ErrorStatus[] VALUES = values();

    V1V2ErrorStatus(int value) {
        mValue = value;
    }

    @Override
    public int getValue() {
        return mValue;
    }

    public static V1V2ErrorStatus valueOf(int value) {
        for (V1V2ErrorStatus status : VALUES) {
            if (status.mValue == value) {
                return status;
            }
        }

        return UNKNOWN;
    }

    @NonNull
    @Override
    public String toString() {
        return "V1V2_" + name();
    }
}
