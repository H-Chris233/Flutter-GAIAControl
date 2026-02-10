/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;

public class TransferredData {

    private static final int SESSION_OFFSET = 0;
    private static final int DATA_OFFSET = 2; // SESSION_OFFSET + UINT16_LENGTH

    private final int session;

    private final byte[] data;

    public TransferredData(byte[] source) {
        this.session = getUINT16(source, SESSION_OFFSET);
        this.data = getSubArray(source, DATA_OFFSET);
    }

    public int getSession() {
        return session;
    }

    public byte[] getData() {
        return data;
    }
}
