/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum PreSetType {
    OFF(0x00),
    USER(0x3F),
    PRE_SET(0x100); // value has no meaning other than not being off or user.

    private final int value;

    PreSetType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PreSetType valueOf(int value) {
        switch (value) {
            case 0x00:
                return OFF;

            case 0x3F:
                return USER;
            default:
                return PRE_SET;
        }
    }

}
