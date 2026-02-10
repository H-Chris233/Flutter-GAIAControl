/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

public class UserFeatureSet {

    private final FeatureType type;

    private final int size;

    private final byte[] data;

    public UserFeatureSet(FeatureType type, int size, byte[] data) {
        this.type = type;
        this.size = size;
        this.data = data;
    }

    public FeatureType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public byte[] getData() {
        return data;
    }
}
