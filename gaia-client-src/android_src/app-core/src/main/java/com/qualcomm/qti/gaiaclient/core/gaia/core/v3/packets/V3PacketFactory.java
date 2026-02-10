/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet.COMMAND_DESCRIPTION_OFFSET;
import static com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet.PAYLOAD_OFFSET;
import static com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet.VENDOR_OFFSET;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;

/**
 * Wrap up the creation of V3 Packet objects.
 */
public final class V3PacketFactory {

    /**
     * <p>To build a V3Packet from its raw GAIA PDU. It expects the PDU to have the following
     * format:
     * <blockquote><pre>
     * 0 bytes     1           2           3            4                      len+4
     * +-----------+-----------+-----------+-----------+ +-----------+-----------+
     * |       VENDOR ID       |     COMMAND VALUE     | | Optional PAYLOAD  ... |
     * +-----------+-----------+-----------+-----------+ +-----------+-----------+
     * </pre> </blockquote></p>
     * <p>Where the COMMAND VALUE has the following format:
     * <blockquote><pre>
     * 15 bits                     9       7                           0
     * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
     * |        FEATURE ID         | TYPE  |        COMMAND ID         |
     * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
     * </pre></blockquote></p>
     */
    @NonNull
    public static V3Packet buildPacket(byte[] source) {
        int vendor = getUINT16(source, VENDOR_OFFSET);
        int commandValue = getUINT16(source, COMMAND_DESCRIPTION_OFFSET);
        byte[] payload = getSubArray(source, PAYLOAD_OFFSET);
        V3Command command = new V3Command(commandValue);

        switch (command.getType()) {
            case NOTIFICATION:
                return new NotificationPacket(vendor, command, payload);
            case RESPONSE:
                return new ResponsePacket(vendor, command, payload);
            case ERROR:
                return new ErrorPacket(vendor, command, payload);
            case COMMAND:
                return new CommandPacket(vendor, command, payload);
            default:
                return new V3Packet(vendor, command, payload);
        }
    }

    /**
     * To build a packet of type
     * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#COMMAND} with
     * an empty payload.
     */
    @NonNull
    public static CommandPacket buildCommandPacket(int vendorId, int feature, int command) {
        return new CommandPacket(vendorId, feature, command);
    }

    /**
     * To build a packet of type
     * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#COMMAND} with
     * a payload.
     */
    @NonNull
    public static CommandPacket buildCommandPacket(int vendorId, int feature, int command,
                                                   byte[] data) {
        return new CommandPacket(vendorId, feature, command, data);
    }

}
