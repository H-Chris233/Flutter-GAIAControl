/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import java.util.Objects;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getHexadecimalStringFromInt;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getValueFromBits;

/**
 * <p>A V3 command is a 2 bytes value defined as follows:
 * <blockquote><pre>
 * 15 bits                     9       7                           0
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * |        FEATURE ID         | TYPE  |        COMMAND ID         |
 * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
 * </pre></blockquote></p>
 * <p>This class provides an object representation of the command to easily get the feature ID,
 * the type, the command ID and also the 2 bytes value.</p>
 */
public class V3Command {

    // all offsets and lengths of the different fields
    private static final int COMMAND_BIT_OFFSET = 0;
    private static final int COMMAND_LENGTH = 7;
    private static final int TYPE_BIT_OFFSET = 7;
    private static final int TYPE_LENGTH = 2;
    private static final int FEATURE_BIT_OFFSET = 9;
    private static final int FEATURE_LENGTH = 7;

    /**
     * The feature ID of the command.
     */
    private final int feature;
    /**
     * The type of the command.
     */
    private final V3PacketType type;
    /**
     * The command ID of the command.
     */
    private final int command;
    /**
     * The <code>int</code> value of the 2 bytes command.
     */
    private final int value;


    // ====== CONSTRUCTORS ===================================================================

    /**
     * <p>To build a V3 command from its 2 byte value.</p>
     * <p>This constructor identifies the feature, type and command from the given description.</p>
     */
    V3Command(int commandDescription) {
        this.value = commandDescription;
        this.feature = getValueFromBits(commandDescription, FEATURE_BIT_OFFSET, FEATURE_LENGTH);
        this.type = V3PacketType.valueOf(getValueFromBits(commandDescription, TYPE_BIT_OFFSET,
                                                          TYPE_LENGTH));

        this.command = getValueFromBits(commandDescription, COMMAND_BIT_OFFSET, COMMAND_LENGTH);
    }

    /**
     * <p>To build a V3 command from its feature, type and command ID.</p>
     * <p>This constructor calculates the 2 bytes value of the command.</p>
     */
    V3Command(int feature, V3PacketType type, int command) {
        this.feature = feature;
        this.type = type;
        this.command = command;
        this.value = buildCommandValue();
    }


    // ====== GETTERS ===================================================================

    /**
     * To get the type of the command.
     */
    public V3PacketType getType() {
        return type;
    }

    /**
     * To get the 2 bytes value of the command.
     */
    public int getValue() {
        return value;
    }

    /**
     * To get the feature ID of the command.
     */
    public int getFeature() {
        return feature;
    }

    /**
     * To get the command ID of the command.
     */
    public int getCommand() {
        return command;
    }


    // ====== PUBLIC METHODS ===================================================================

    @Override // Object
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null || getClass() != that.getClass()) {
            return false;
        }
        V3Command description = (V3Command) that;
        return command == description.command
                && type == description.type;
    }

    @Override // Object
    public int hashCode() {
        return Objects.hash(command, type);
    }

    @NonNull // Object
    public String toString() {
        return "Command{" +
                "type=" + type +
                ", feature=" + getHexadecimalStringFromInt(feature) +
                ", command=" + getHexadecimalStringFromInt(command) +
                '}';
    }


    // ====== PACKAGE METHODS ===================================================================

    /**
     * <p>This method check if the feature ID and command ID of the given command match the ones
     * of the command.</p>
     * <p>This can be used to match a packet that has been sent to one that has been received to
     * identify a responding packet.</p>
     */
    boolean match(V3Command description) {
        return description != null && command == description.command;
    }


    // ====== PRIVATE METHODS ===================================================================

    /**
     * <p>This method builds the value of the command from its feature ID, type and command ID
     * as per the format that defines a V3 command:
     * <blockquote><pre>
     * 15 bits                     9       7                           0
     * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
     * |        FEATURE ID         | TYPE  |        COMMAND ID         |
     * +---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+---+
     * </pre></blockquote></p>
     */
    private int buildCommandValue() {
        return setValue(feature, FEATURE_BIT_OFFSET)
                + setValue(type.getValue(), TYPE_BIT_OFFSET)
                + setValue(command, COMMAND_BIT_OFFSET);
    }

    /**
     * <p>This method shifts the given data to the given bit offset.</p>
     */
    private int setValue(int data, int bitOffset) {
        return data << bitOffset;
    }
}
