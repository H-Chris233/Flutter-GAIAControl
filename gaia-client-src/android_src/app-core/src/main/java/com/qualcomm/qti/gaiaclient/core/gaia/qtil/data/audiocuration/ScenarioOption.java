/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import androidx.annotation.Nullable;

public enum ScenarioOption {

    /**
     * single value to represent the "off" option.
     */
    OFF(0x00),
    /**
     * range to represent a mode.
     */
    MODE(0x01, 0x7F), // 0x01 to 0x7F
    /**
     * range to represent undefined values.
     */
    UNDEFINED(0x80, 0xFE), // 0x80 to 0xFE
    /**
     * single value to represent the "keep current mode" option.
     */
    KEEP_CURRENT(0xFF);

    private final int min;

    private final int max;

    private static final ScenarioOption[] VALUES = values();

    ScenarioOption(int value) {
        this.min = value;
        this.max = value;
    }

    ScenarioOption(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getValue() {
        return min;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Nullable
    public static ScenarioOption valueOf(int value) {
        for (ScenarioOption option : VALUES) {
            if (option.is(value)) {
                return option;
            }
        }

        return null;
    }

    public static ScenarioOption[] getValues() {
        int length = VALUES.length;
        ScenarioOption[] copy = new ScenarioOption[length];
        System.arraycopy(VALUES, 0, copy, 0, length);
        return copy;
    }

    private boolean is(int value) {
        return min <= value && value <= max;
    }
}
