/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc;

public enum AdaptiveState {

    DISABLED(0x00),
    ENABLED(0x01);

    private final int value;

    private static final AdaptiveState[] VALUES = values();

    AdaptiveState(int value) {
        this.value = value;
    }

    public static AdaptiveState valueOf(int value) {
        for (AdaptiveState state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }
}
