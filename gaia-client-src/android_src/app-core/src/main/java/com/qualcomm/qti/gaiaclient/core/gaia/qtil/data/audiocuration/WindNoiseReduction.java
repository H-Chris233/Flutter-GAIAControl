/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;

public class WindNoiseReduction {

    private static final int LEFT_DETECTION_OFFSET = 0;
    private static final int RIGHT_DETECTION_OFFSET = 1;

    private final WindNoiseReductionState windDetectionOnLeft;
    private final WindNoiseReductionState windDetectionOnRight;

    public WindNoiseReduction(byte[] data) {
        this.windDetectionOnLeft = WindNoiseReductionState.valueOf(getUINT8(data, LEFT_DETECTION_OFFSET));
        this.windDetectionOnRight = WindNoiseReductionState.valueOf(getUINT8(data, RIGHT_DETECTION_OFFSET));
    }

    public WindNoiseReduction(WindNoiseReductionState windDetectionOnLeft,
                              WindNoiseReductionState windDetectionOnRight) {
        this.windDetectionOnLeft = windDetectionOnLeft;
        this.windDetectionOnRight = windDetectionOnRight;
    }

    public WindNoiseReductionState getWindDetectionOnLeft() {
        return windDetectionOnLeft;
    }

    public WindNoiseReductionState getWindDetectionOnRight() {
        return windDetectionOnRight;
    }

    @NonNull
    @Override
    public String toString() {
        return "WindNoiseReduction{" +
                "onLeft=" + windDetectionOnLeft +
                ", onRight=" + windDetectionOnRight +
                '}';
    }
}
