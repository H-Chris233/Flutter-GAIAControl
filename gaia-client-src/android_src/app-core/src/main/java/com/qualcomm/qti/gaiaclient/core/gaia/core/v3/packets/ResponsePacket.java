/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import androidx.annotation.NonNull;

/**
 * This structure represents a GAIA v3 packet of type RESPONSE. This structure helps
 * interpreting the packet type in a human readable packet.
 */
public class ResponsePacket extends V3Packet {

    /**
     * Builds a RESPONSE type packet that contains the given parameters.
     *
     * @param commandDescription
     *         must be of type {@link V3PacketType#RESPONSE}.
     */
    ResponsePacket(int vendorId, V3Command commandDescription,
                   @NonNull byte[] payload) {
        super(vendorId, commandDescription, payload);
    }

    /**
     * To get the data provided in the response. For v3 this corresponds to the payload.
     */
    @NonNull
    public byte[] getData() {
        return getPayload();
    }
}
