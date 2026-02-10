/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;

public class TransferSetup {

    private static final int SESSION_OFFSET = 0;
    private static final int TRANSPORT_OFFSET = 2; // SESSION_OFFSET + UINT16_LENGTH
    private static final int PAYLOAD_LENGTH = 3; // TRANSPORT_OFFSET + UINT8_LENGTH

    private final int session;

    private final int transport;

    public TransferSetup(@NonNull byte[] source) {
        this.session = getUINT16(source, SESSION_OFFSET);
        this.transport = getUINT8(source, TRANSPORT_OFFSET);
    }

    public TransferSetup(int session) {
        this.session = session;
        this.transport = TRANSPORTS.GAIA_COMMAND_LINK;
    }

    public int getSession() {
        return session;
    }

    public int getTransport() {
        return transport;
    }

    public byte[] getBytes() {
        byte[] result = new byte[PAYLOAD_LENGTH];
        setUINT16(session, result, SESSION_OFFSET);
        setUINT8(transport, result, TRANSPORT_OFFSET);
        return result;
    }

    private static final class TRANSPORTS {

        static final int NONE = 0x00;
        static final int GAIA_COMMAND_LINK = 0x01;
    }
}
