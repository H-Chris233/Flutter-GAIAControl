/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;

import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * <p>A GAIA v3 packet has the following PDU:
 * <blockquote><pre>
 * 0 bytes     1           2           3            4                      len+4
 * +-----------+-----------+-----------+-----------+ +-----------+-----------+
 * |       VENDOR ID       |  COMMAND DESCRIPTION  | | Optional PAYLOAD  ... |
 * +-----------+-----------+-----------+-----------+ +-----------+-----------+
 * </pre> </blockquote></p>
 *
 * <i>Note: PDU = Protocol Data Unit.</i>
 */
public class V3Packet extends GaiaPacket {

    static final int VENDOR_OFFSET = 0;
    static final int COMMAND_DESCRIPTION_OFFSET = 2;
    static final int PAYLOAD_OFFSET = 4;

    /**
     * The V3 command of the packet - contains feature, type and command ID.
     */
    private final V3Command commandDescription;
    /**
     * The payload of the packet, this is non null but can be empty as the payload is optional.
     */
    @NonNull
    private final byte[] payload;
    /**
     * The key to use to match packets to this one.
     */
    private final int key;


    // ====== CONSTRUCTORS ===================================================================

    /**
     * Builds a V3 packet that contains the given parameters. This constructor builds the
     * {@link V3Command} description that corresponds to the given <code>feature</code>,
     * <code>type</code> and <code>command</code>.
     */
    V3Packet(int vendorId, int feature, V3PacketType type, int command) {
        this(vendorId, new V3Command(feature, type, command), new byte[0]);
    }

    /**
     * Builds a V3 packet that contains the given parameters. This constructor builds the
     * {@link V3Command} description that corresponds to the given <code>feature</code>,
     * <code>type</code> and <code>command</code>.
     */
    V3Packet(int vendorId, int feature, V3PacketType type, int command, byte[] data) {
        this(vendorId, new V3Command(feature, type, command), data != null ? data : new byte[0]);
    }

    /**
     * Builds a V3 packet that contains the given parameters.
     */
    V3Packet(int vendorId, V3Command commandDescription, @NonNull byte[] payload) {
        super(vendorId);
        this.commandDescription = commandDescription;
        this.payload = payload;
        this.key = Objects.hash(vendorId, commandDescription.getFeature(),
                                commandDescription.getCommand());
    }


    // ====== GETTERS ===================================================================

    /**
     * To get the feature ID of the command description.
     */
    public int getFeature() {
        return commandDescription.getFeature();
    }

    /**
     * To get the command ID of the command description.
     */
    public int getCommand() {
        return commandDescription.getCommand();
    }

    /**
     * To get the packet type contained in the command description.
     */
    public V3PacketType getType() {
        return commandDescription.getType();
    }

    /**
     * To get the int value that corresponds to the 2 bytes of the command description.
     */
    @Override // GaiaPacket
    protected int getCommandValue() {
        return commandDescription.getValue();
    }

    /**
     * To get the payload of the packet.
     *
     * @return The bytes contained in the payload, this can be empty as the payload is optional.
     */
    @NonNull // GaiaPacket
    public byte[] getPayload() {
        return payload;
    }

    @Override // GaiaPacket
    public int getKey() {
        return key;
    }


    // ====== OTHER PUBLIC METHODS ===============================================================

    @NonNull
    @Override // object
    public String toString() {
        return "Packet{" +
                "version=V3" +
                ", vendor=" + BytesUtils.getHexadecimalStringFromInt(getVendorId()) +
                ", command=" + commandDescription +
                '}';
    }
}
