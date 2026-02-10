/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

public enum FeatureTypes {
    APPLICATION_FEATURE(0x01),
    UNKNOWN(0x100);

    private static final FeatureTypes[] VALUES = values();

    private final int value;

    FeatureTypes(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FeatureTypes valueOf(int value) {
        for (FeatureTypes type : VALUES) {
            if (type.value == value) {
                return type;
            }
        }

        return UNKNOWN;
    }
}
