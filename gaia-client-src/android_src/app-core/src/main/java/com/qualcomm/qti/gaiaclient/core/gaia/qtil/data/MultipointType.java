/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum MultipointType {

    SINGLE_POINT(0x00),
    MULTI_POINT(0x01);

    private final int value;

    private static final MultipointType[] VALUES = values();

    MultipointType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MultipointType valueOf(int value) {
        for (MultipointType state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }

    public static MultipointType valueOf(boolean enabled) {
        return enabled ? SINGLE_POINT : MULTI_POINT;
    }
}
