/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

public class GetGestureConfigurationParameters {

    private final int gestureId;

    private final int offset;

    private static final int DATA_LENGTH = 2;
    private static final int GESTURE_OFFSET = 0;
    private static final int OFFSET_OFFSET = 1;

    public GetGestureConfigurationParameters(int gestureId, int offset) {
        this.gestureId = gestureId;
        this.offset = offset;
    }

    public byte[] getPayload() {
        byte[] data = new byte[DATA_LENGTH];
        setUINT8(gestureId, data, GESTURE_OFFSET);
        setUINT8(offset, data, OFFSET_OFFSET);

        return data;
    }

}
