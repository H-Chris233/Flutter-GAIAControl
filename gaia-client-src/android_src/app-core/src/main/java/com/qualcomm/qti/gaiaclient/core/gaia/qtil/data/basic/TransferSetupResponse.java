/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT16;

import androidx.annotation.NonNull;

public class TransferSetupResponse {

    private static final int SESSION_OFFSET = 0;
    private static final int PAYLOAD_LENGTH = 2; // UINT16_LENGTH

    private final int session;


    public TransferSetupResponse(@NonNull byte[] source) {
        this.session = getUINT16(source, SESSION_OFFSET);
    }

    public int getSession() {
        return session;
    }

    public byte[] getBytes() {
        byte[] result = new byte[PAYLOAD_LENGTH];
        setUINT16(session, result, SESSION_OFFSET);
        return result;
    }
}
