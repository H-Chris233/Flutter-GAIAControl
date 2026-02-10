/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setValueAsBits;

public class SetGestureConfiguration {

    private final int gestureID;

    private final Set<Configuration> configurations;

    private static final int HEADER_LENGTH = 2;
    private static final int CONFIGURATION_LENGTH = 2;
    private static final int GESTURE_FIELD_OFFSET = 0;
    private static final int OFFSET_OFFSET = 1;
    private static final int CONFIGURATIONS_OFFSET = 2;

    private static final int MORE_DATA_BIT_OFFSET = 7;

    public SetGestureConfiguration(int gestureID, Set<Configuration> configurations) {
        this.gestureID = gestureID;
        this.configurations = configurations;
    }

    public List<byte[]> getPayloads(int maxLength) {
        List<byte[]> result = new ArrayList<>();

        if (configurations == null || configurations.isEmpty()) {
            result.add(initPayload(0, gestureID, 0));
            return result;
        }

        final int TOTAL = configurations.size();
        final int MAXIMUM = (maxLength - HEADER_LENGTH) / CONFIGURATION_LENGTH;

        byte[] payload = null;
        int payloadOffset = 0;
        int maxListLength = getMaxListLength(maxLength);

        Configuration[] configs = configurations.toArray(new Configuration[0]);

        for (int i = 0; i < TOTAL; i++) {
            Configuration configuration = configs[i];
            int remaining = TOTAL - i;

            if (payload == null) {
                // new payload
                int listLength = Math.min(remaining * CONFIGURATION_LENGTH, maxListLength);
                payload = initPayload(listLength,
                                      buildGestureField(gestureID, remaining > MAXIMUM),
                                      i);
                payloadOffset = CONFIGURATIONS_OFFSET;
            }

            // add configuration
            int value = configuration.getValue();
            setUINT16(value, payload, payloadOffset);

            payloadOffset += CONFIGURATION_LENGTH;

            if (i == TOTAL - 1 /*last configuration*/
                    || maxListLength <= payloadOffset - HEADER_LENGTH /*max payload length reached*/) {
                // payload done
                result.add(payload);
                payload = null;
                payloadOffset = 0;
            }
        }

        return result;
    }

    private static byte[] initPayload(int listLength, int gestureField, int offset) {
        byte[] payload = new byte[HEADER_LENGTH + listLength];
        setUINT8(gestureField, payload, GESTURE_FIELD_OFFSET);
        setUINT8(offset, payload, OFFSET_OFFSET);
        return payload;
    }

    private static int getMaxListLength(int maxLength) {
        int length = maxLength - HEADER_LENGTH;
        int modulo = length % CONFIGURATION_LENGTH;
        return modulo == 0 ? length : length - modulo;
    }

    private static int buildGestureField(int gesture, boolean hasMoreData) {
        return hasMoreData ? setValueAsBits(1, gesture, MORE_DATA_BIT_OFFSET) : gesture;
    }

}
