/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public enum Capability {

    NONE(0x00),
    CVC_3MIC(0x01);

    private final int value;

    private static final Capability[] VALUES = values();

    Capability(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static Capability valueOf(int value) {
        for (Capability capability : VALUES) {
            if (capability.value == value) {
                return capability;
            }
        }

        return null;
    }

}
