/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc;

/**
 * Gain is a UINT8: values are from 0 to 255.
 */
public class Gain {

    public static final int GAIN_MIN = 0; // 0x00

    public static final int GAIN_MAX = 255; // 0xFF

    private final int gain;

    public Gain(int gain) {
        this.gain = Math.max(gain, GAIN_MIN);
    }

    public int getValue() {
        return gain;
    }
}
