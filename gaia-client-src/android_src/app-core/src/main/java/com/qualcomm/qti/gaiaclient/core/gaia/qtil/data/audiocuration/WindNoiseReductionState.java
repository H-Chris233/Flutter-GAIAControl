/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

public enum WindNoiseReductionState {

    WIND_NOT_DETECTED(0x00),
    WIND_DETECTED(0x01);

    private final int value;

    private static final WindNoiseReductionState[] VALUES = values();

    WindNoiseReductionState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static WindNoiseReductionState valueOf(int value) {
        for (WindNoiseReductionState state : VALUES) {
            if (state.value == value) {
                return state;
            }
        }

        return null;
    }
}
