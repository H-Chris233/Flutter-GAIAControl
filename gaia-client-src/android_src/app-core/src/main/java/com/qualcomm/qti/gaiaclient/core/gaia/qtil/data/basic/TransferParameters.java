/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT32;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT32;

import androidx.annotation.NonNull;

public class TransferParameters {

    private static final int SESSION_OFFSET = 0;
    private static final int START_OFFSET_OFFSET = 2; // SESSION_OFFSET + UINT16_LENGTH
    private static final int SIZE_OFFSET = 6; // START_OFFSET_OFFSET + UINT32_LENGTH
    private static final int PAYLOAD_LENGTH = 10; // SIZE_OFFSET + UINT32_LENGTH

    private final int session;

    private final long startOffset;

    private final long size;

    public TransferParameters(@NonNull byte[] data) {
        this.session = getUINT16(data, SESSION_OFFSET);
        this.startOffset = getUINT32(data, START_OFFSET_OFFSET);
        this.size = getUINT32(data, SIZE_OFFSET);
    }

    public TransferParameters(int session, long startOffset, long size) {
        this.session = session;
        this.startOffset = startOffset;
        this.size = size;
    }

    public int getSession() {
        return session;
    }

    public long getStartOffset() {
        return startOffset;
    }

    public long getSize() {
        return size;
    }

    public byte[] getBytes() {
        byte[] result = new byte[PAYLOAD_LENGTH];
        setUINT16(session, result, SESSION_OFFSET);
        setUINT32(startOffset, result, START_OFFSET_OFFSET);
        setUINT32(size, result, SIZE_OFFSET);
        return result;
    }

}
