/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT32;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT32;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;

public class TransportInformation {

    private static final int PARAMETER_OFFSET = 0;
    private static final int VALUE_OFFSET = 1;
    private static final int PAYLOAD_LENGTH = 5; // UINT8_LENGTH + UINT32_LENGTH

    private final ProtocolInfo info;

    private final int parameter;

    private final long value;

    public TransportInformation(@NonNull byte[] data) {
        parameter = getUINT8(data, PARAMETER_OFFSET);
        value = getUINT32(data, VALUE_OFFSET);
        this.info = ProtocolInfo.valueOf(parameter);
    }

    public TransportInformation(ProtocolInfo info, long value) {
        this.parameter = info.getValue();
        this.value = value;
        this.info = info;
    }

    public int getParameter() {
        return parameter;
    }

    public ProtocolInfo getInfo() {
        return info;
    }

    public long getValue() {
        return value;
    }

    public byte[] getBytes() {
        byte[] result = new byte[PAYLOAD_LENGTH];
        setUINT8(parameter, result, PARAMETER_OFFSET);
        setUINT32(value, result, VALUE_OFFSET);
        return result;
    }
}
