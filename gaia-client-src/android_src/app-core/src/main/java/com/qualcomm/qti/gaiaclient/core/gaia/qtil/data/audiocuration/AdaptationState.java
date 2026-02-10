/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

public enum AdaptationState {

    PAUSED(0x00),
    RESUMED(0x01);

    private final int value;

    private static final AdaptationState[] VALUES = values();

    AdaptationState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static AdaptationState valueOf(int value) {
        for (AdaptationState state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }
}
