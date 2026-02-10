/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.sending;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;

public class HoldData {

    @NonNull
    private final GaiaPacket packet;
    @NonNull
    private final Parameters parameters;

    public HoldData(@NonNull GaiaPacket packet, Parameters parameters) {
        this.packet = packet;
        this.parameters = parameters;
    }

    @NonNull
    public GaiaPacket getPacket() {
        return packet;
    }

    @NonNull
    public Parameters getParameters() {
        return parameters;
    }
}
