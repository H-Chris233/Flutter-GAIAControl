/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum EQState {

    NOT_PRESENT(0x00),
    PRESENT(0x01);

    private final int value;

    private static final EQState[] VALUES = values();

    EQState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EQState valueOf(int value) {
        for (EQState state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }

}
