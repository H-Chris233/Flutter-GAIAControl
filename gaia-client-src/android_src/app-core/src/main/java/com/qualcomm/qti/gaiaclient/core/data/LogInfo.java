/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT16;

public enum LogInfo {

    PARTITION_SIZE(0x0000),
    LOG_FILE(0x0001);

    private static final int SESSION_OFFSET = 0;
    private static final int PAYLOAD_LENGTH = 2; // UINT16_LENGTH

    private final int value;

    private static final LogInfo[] VALUES = values();

    LogInfo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public byte[] getBytes() {
        byte[] result = new byte[PAYLOAD_LENGTH];
        setUINT16(value, result, SESSION_OFFSET);
        return result;
    }

    public static LogInfo valueOf(int value) {
        for (LogInfo info : VALUES) {
            if (info.value == value) {
                return info;
            }
        }

        return null;
    }
}
