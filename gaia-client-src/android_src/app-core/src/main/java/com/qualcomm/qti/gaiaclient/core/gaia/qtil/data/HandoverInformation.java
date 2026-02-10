/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class HandoverInformation {

    private final HandoverType type;

    private final long delayInSeconds;

    private static final int HANDOVER_TYPE_OFFSET = 0;
    private static final int DELAY_OFFSET = 1;
    private static final byte DELAY_DEFAULT_IN_SECONDS = 5;

    public HandoverInformation(byte[] payload) {
        type = HandoverType.valueOf(getUINT8(payload, HANDOVER_TYPE_OFFSET));
        delayInSeconds = getUINT8(payload, DELAY_OFFSET, DELAY_DEFAULT_IN_SECONDS);
    }

    public HandoverInformation(HandoverType type, long delayInSeconds) {
        this.type = type;
        this.delayInSeconds = delayInSeconds;
    }

    public HandoverType getType() {
        return type;
    }

    public long getDelayInSeconds() {
        return delayInSeconds;
    }

    @NonNull
    @Override
    public String toString() {
        return "HandoverInformation{" +
                "type=" + type +
                ", delayInSeconds=" + delayInSeconds +
                '}';
    }
}
