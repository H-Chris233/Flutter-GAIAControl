/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import androidx.annotation.NonNull;

/**
 * This structure represents a GAIA v3 packet of type COMMAND. This structure helps interpreting
 * the packet type in a human readable packet.
 */
public class CommandPacket extends V3Packet {

    /**
     * Builds a COMMAND type packet that contains the given parameters. This constructor builds the
     * {@link V3Command} COMMAND that corresponds to the given <code>feature</code> and
     * <code>command</code>.
     */
    CommandPacket(int vendorId, int feature, int command) {
        super(vendorId, feature, V3PacketType.COMMAND, command);
    }

    /**
     * Builds a COMMAND type packet that contains the given parameters. This constructor builds the
     * {@link V3Command} COMMAND that corresponds to the given <code>feature</code> and
     * <code>command</code>.
     */
    CommandPacket(int vendorId, int feature, int command, @NonNull byte[] payload) {
        super(vendorId, feature, V3PacketType.COMMAND, command, payload);
    }

    /**
     * Builds a COMMAND type packet that contains the given parameters.
     *
     * @param commandDescription
     *         must be of type {@link V3PacketType#COMMAND}.
     */
    CommandPacket(int vendorId, V3Command commandDescription,
                  @NonNull byte[] payload) {
        super(vendorId, commandDescription, payload);
    }

}
