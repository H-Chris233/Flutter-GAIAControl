/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum HandoverType {

    STATIC(0x00),
    DYNAMIC(0x01);

    private final int value;

    private static final HandoverType[] VALUES = values();

    HandoverType(int value) {
        this.value = value;
    }

    public static HandoverType valueOf(int value) {
        for (HandoverType type : VALUES) {
            if (type.value == value) {
                return type;
            }
        }

        return null;
    }

}
