/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc;

public enum ANCState {

    DISABLE(0x00),
    ENABLE(0x01);

    private final int value;

    private static final ANCState[] VALUES = values();

    ANCState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ANCState valueOf(int value) {
        for (ANCState state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }
}
