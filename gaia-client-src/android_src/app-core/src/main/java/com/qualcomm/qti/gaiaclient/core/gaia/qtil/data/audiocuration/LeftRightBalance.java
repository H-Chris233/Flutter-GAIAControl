/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;

public class LeftRightBalance {

    private static final int DATA_LENGTH = 2;
    private static final int POSITION_OFFSET = 0;
    private static final int GAIN_OFFSET = 1;

    private final EarbudPosition position;
    private final int gain;

    public LeftRightBalance(byte[] data) {
        this.position = EarbudPosition.valueOf(getUINT8(data, POSITION_OFFSET));
        this.gain = getUINT8(data, GAIN_OFFSET);
    }

    public LeftRightBalance(EarbudPosition position, int gain) {
        this.position = position;
        this.gain = gain;
    }

    public EarbudPosition getPosition() {
        return position;
    }

    public int getGain() {
        return gain;
    }

    public byte[] getBytes() {
        byte[] data = new byte[DATA_LENGTH];
        setUINT8(position.getValue(), data, POSITION_OFFSET);
        setUINT8(gain, data, GAIN_OFFSET);
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "LeftRightBalance{" +
                "position=" + position +
                ", gain=" + gain +
                '}';
    }
}
