/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.LogInfo;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT32;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class LogSize {

    private static final int PDU_VERSION_OFFSET = 0;
    private static final int LOG_INFO_OFFSET = 1; // PDU_VERSION_OFFSET + UINT8_LENGTH
    private static final int SIZE_OFFSET = 3; // LOG_INFO_OFFSET + UINT16_LENGTH

    private final int responsePduVersion;

    private final LogInfo info;

    private final long size;

    public LogSize(@NonNull byte[] data) {
        this.responsePduVersion = getUINT8(data, PDU_VERSION_OFFSET);
        this.info = LogInfo.valueOf(getUINT16(data, LOG_INFO_OFFSET));
        this.size = getUINT32(data, SIZE_OFFSET);
    }

    public LogSize(int responsePduVersion, LogInfo info, long size) {
        this.responsePduVersion = responsePduVersion;
        this.info = info;
        this.size = size;
    }

    public int getResponsePduVersion() {
        return responsePduVersion;
    }

    public LogInfo getInfo() {
        return info;
    }

    public long getSize() {
        return size;
    }
}
