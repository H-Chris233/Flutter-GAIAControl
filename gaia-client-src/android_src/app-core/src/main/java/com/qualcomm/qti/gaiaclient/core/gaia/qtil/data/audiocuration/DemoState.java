/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

public enum DemoState {

    OUT(0x00),
    IN(0x01);

    private final int value;

    private static final DemoState[] VALUES = values();

    DemoState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static DemoState valueOf(int value) {
        for (DemoState mode : VALUES) {
            if (mode.value == value) {
                return mode;
            }
        }

        return null;
    }
}
