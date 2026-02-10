/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import androidx.annotation.Nullable;

public enum Feature {

    ANC(0x01);

    private final int value;

    private static final Feature[] VALUES = values();

    Feature(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Nullable
    public static Feature valueOf(int value) {
        for (Feature feature : VALUES) {
            if (feature.value == value) {
                return feature;
            }
        }

        return null;
    }

    public static Feature[] getValues() {
        int length = VALUES.length;
        Feature[] copy = new Feature[length];
        System.arraycopy(VALUES, 0, copy, 0, length);
        return copy;
    }
}
