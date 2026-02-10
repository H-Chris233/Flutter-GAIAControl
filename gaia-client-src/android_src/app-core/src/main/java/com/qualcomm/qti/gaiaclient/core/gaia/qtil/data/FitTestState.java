/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum FitTestState {

    STOP(0x00),
    START(0x01);

    private final int value;

    private static final FitTestState[] VALUES = values();

    FitTestState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FitTestState valueOf(int value) {
        for (FitTestState state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }
}
