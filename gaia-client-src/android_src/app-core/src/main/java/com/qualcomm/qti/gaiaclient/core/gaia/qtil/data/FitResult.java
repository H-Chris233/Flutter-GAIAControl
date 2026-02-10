/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum FitResult {

    GOOD_FIT(0x01),
    BAD_FIT(0x02),
    FAIL(0x03);

    private final int value;

    private static final FitResult[] VALUES = values();

    FitResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FitResult valueOf(int value) {
        for (FitResult mode : VALUES) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }
}
