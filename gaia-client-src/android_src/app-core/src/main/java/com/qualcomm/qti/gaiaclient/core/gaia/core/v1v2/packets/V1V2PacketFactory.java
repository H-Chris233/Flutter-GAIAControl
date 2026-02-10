/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets;

import androidx.annotation.NonNull;

/**
 * Wrap up the creation of new V1V2Packet objects.
 */
public final class V1V2PacketFactory {

    /**
     * To build a V1V2Packet from its raw GAIA PDU. It expects the PDU to have the following format:
     * <blockquote><pre>
     * 0 bytes     1           2           3            4                      len+4
     * +-----------+-----------+-----------+-----------+ +-----------+-----------+
     * |       VENDOR ID       |     COMMAND VALUE     | | Optional PAYLOAD  ... |
     * +-----------+-----------+-----------+-----------+ +-----------+-----------+
     * </pre> </blockquote>
     */
    @NonNull
    public static V1V2Packet buildPacket(byte[] source) {
        return new V1V2Packet(source);
    }

    /**
     * To build a V1V2Packet that only contains the vendor ID and a command.
     */
    @NonNull
    public static V1V2Packet buildPacket(int vendorId, int command) {
        return new V1V2Packet(vendorId, command);
    }

    /**
     * To build a V1V2Packet with a one byte payload.
     */
    @NonNull
    public static V1V2Packet buildPacket(int vendorId, int command, int data) {
        return new V1V2Packet(vendorId, command, data);
    }

    /**
     * To build a V1V2Packet with a vendor ID, a command and a payload.
     */
    @NonNull
    public static V1V2Packet buildPacket(int vendorId, int command, byte[] data) {
        return new V1V2Packet(vendorId, command, data);
    }

    /**
     * To build an acknowledgement packet that acknowledges the packet given as
     * <code>origin</code> with the given <code>status</code>.
     */
    @NonNull
    public static V1V2Packet buildAcknowledgement(V1V2Packet origin, V1V2ErrorStatus status) {
        return new V1V2Packet(origin, status);
    }

}
