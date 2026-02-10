/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT32;

public class PanicLogInfo {

    private static final int SESSION_ID_OFFSET = 0;
    private static final int SIZE_OFFSET = 2; // SESSION_ID_OFFSET + UINT16_LENGTH

    private final int sessionId;

    private final long size;

    public PanicLogInfo(byte[] data) {
        this.sessionId = getUINT16(data, SESSION_ID_OFFSET);
        this.size = getUINT32(data, SIZE_OFFSET);
    }

    public int getSessionId() {
        return sessionId;
    }

    public long getSize() {
        return size;
    }
}
