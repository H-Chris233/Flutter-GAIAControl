/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures;

class FeatureType {

    private final int value;

    private final FeatureTypes type;

    public FeatureType(int value) {
        this.value = value;
        this.type = FeatureTypes.valueOf(value);
    }

    public FeatureType(FeatureTypes type) {
        this.value = type.getValue();
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public FeatureTypes getType() {
        return type;
    }
}
