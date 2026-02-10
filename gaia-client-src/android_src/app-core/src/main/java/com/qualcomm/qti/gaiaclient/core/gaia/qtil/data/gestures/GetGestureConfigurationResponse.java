/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getValueFromBits;

public class GetGestureConfigurationResponse {

    private final boolean hasMoreData;

    private final Gesture gesture;

    private final Set<Configuration> configurations;

    private static final int MORE_DATA_GESTURE_OFFSET = 0;
    private static final int CONFIGURATIONS_OFFSET = 1;
    private static final int MORE_DATA_BIT_OFFSET = 7;
    private static final int GESTURE_ID_BIT_OFFSET = 0;
    private static final int GESTURE_ID_BIT_LENGTH = 7;
    private static final int MORE_DATA_VALUE = 0b1;

    public GetGestureConfigurationResponse(byte[] data) {
        int gestureData = getUINT8(data, MORE_DATA_GESTURE_OFFSET);
        int gestureId = getValueFromBits(gestureData, GESTURE_ID_BIT_OFFSET, GESTURE_ID_BIT_LENGTH);
        int moreData = getValueFromBits(gestureData, MORE_DATA_BIT_OFFSET, 1);

        this.gesture = GestureFactory.getGesture(gestureId);
        this.hasMoreData = moreData == MORE_DATA_VALUE;

        this.configurations = new LinkedHashSet<>();
        for (int i = CONFIGURATIONS_OFFSET; i < data.length; i = i + 2) {
            configurations.add(new Configuration(getUINT16(data, i)));
        }
    }

    public boolean hasMoreData() {
        return hasMoreData;
    }

    public Gesture getGesture() {
        return gesture;
    }

    public Set<Configuration> getConfigurations() {
        return configurations;
    }

}
