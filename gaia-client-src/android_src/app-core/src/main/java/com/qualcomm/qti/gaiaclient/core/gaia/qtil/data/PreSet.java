/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

public class PreSet {

    private final PreSetType type;

    private final int value;

    public PreSet(int value) {
        type = PreSetType.valueOf(value);
        this.value = value;
    }

    public PreSetType getType() {
        return type;
    }

    public int getValue() {
        return value;
    }
}
