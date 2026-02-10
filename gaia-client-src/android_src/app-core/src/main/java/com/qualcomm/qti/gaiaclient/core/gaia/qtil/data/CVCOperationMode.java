/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.Nullable;

public enum CVCOperationMode {

    MODE_2MIC(0x00),
    MODE_3MIC(0x01);

    private static final CVCOperationMode[] VALUES = values();

    private final int value;

    CVCOperationMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public static CVCOperationMode valueOf(int value) {
        for (CVCOperationMode mode : VALUES) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }
}
