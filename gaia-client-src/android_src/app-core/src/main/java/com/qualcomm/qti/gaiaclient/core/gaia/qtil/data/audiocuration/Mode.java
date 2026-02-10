/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;

public class Mode {

    private final int value;

    private final int featureTypeValue;

    private final FeatureType featureType;

    private final Support adaptationControlSupport;

    private final Support gainControlSupport;

    private final Support howlingDetectionControlSupport;

    public Mode(int featureVersion, byte[] data) {
        int MODE_OFFSET = 0;
        int FEATURE_TYPE_OFFSET = 1;
        int ADAPTATION_CONTROL_SUPPORT_OFFSET = 2;
        int GAIN_CONTROL_SUPPORT_OFFSET = 3;
        int HOWLING_DETECTION_CONTROL_SUPPORT_OFFSET = 4;

        this.value = getUINT8(data, MODE_OFFSET);
        this.featureTypeValue = getUINT8(data, FEATURE_TYPE_OFFSET);
        this.featureType = FeatureType.valueOf(featureTypeValue);
        this.adaptationControlSupport = Support.valueOf(getUINT8(data, ADAPTATION_CONTROL_SUPPORT_OFFSET));
        this.gainControlSupport = Support.valueOf(getUINT8(data, GAIN_CONTROL_SUPPORT_OFFSET));
        this.howlingDetectionControlSupport = featureVersion < 5 ? Support.NotSupported.INSTANCE :
                                              Support.valueOf(getUINT8(data, HOWLING_DETECTION_CONTROL_SUPPORT_OFFSET));
    }

    public Mode(int mode) {
        this.value = mode;
        this.featureTypeValue = -1;
        this.featureType = null;
        this.adaptationControlSupport = null;
        this.gainControlSupport = null;
        this.howlingDetectionControlSupport = null;
    }

    public int getValue() {
        return value;
    }

    public int getFeatureTypeValue() {
        return featureTypeValue;
    }

    public FeatureType getFeatureType() {
        return featureType;
    }

    public Support getAdaptationControlSupport() {
        return adaptationControlSupport;
    }

    public Support getGainControlSupport() {
        return gainControlSupport;
    }

    public Support getHowlingDetectionControlSupport() {
        return howlingDetectionControlSupport;
    }

    @NonNull
    @Override
    public String toString() {
        return "Mode{" +
                "value=" + value +
                ", type=" + featureType +
                ", adaptation=" + adaptationControlSupport +
                ", gain=" + gainControlSupport +
                ", howling=" + howlingDetectionControlSupport +
                '}';
    }
}
