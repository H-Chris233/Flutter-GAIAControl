/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum EarbudPosition {

    LEFT(0x00),
    RIGHT(0x01);

    private final int value;

    private static final EarbudPosition[] VALUES = values();

    EarbudPosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static EarbudPosition valueOf(int value) {
        for (EarbudPosition position : VALUES) {
            if (position.value == value) {
                return position;
            }
        }

        return null;
    }

}
