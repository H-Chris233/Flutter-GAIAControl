/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.Nullable;

public enum CVCBypassMode {

    BYPASS_VOICE(0x00),
    BYPASS_EXTERNAL(0x01),
    BYPASS_INTERNAL(0x02);

    private static final CVCBypassMode[] VALUES = values();

    private final int value;

    CVCBypassMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public static CVCBypassMode valueOf(int value) {
        for (CVCBypassMode mode : VALUES) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }

}
