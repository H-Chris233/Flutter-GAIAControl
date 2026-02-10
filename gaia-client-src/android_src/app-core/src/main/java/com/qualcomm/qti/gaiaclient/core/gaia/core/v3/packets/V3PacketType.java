/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import android.util.Log;

/**
 * This enumeration defines the type of received command as per the GAIA definition. V3 commands
 * contain a 2 bits value that represents the type of command. This enumeration provides all the
 * existing values.
 */
public enum V3PacketType {

    /**
     * <p>A COMMAND is expected to be sent to the device.</p>
     * <p>It is unexpected to receive a COMMAND from the device.</p>
     */
    COMMAND(0b00),
    /**
     * <p>A NOTIFICATION is expected to be received from the device as a spontaneous message.</p>
     * <p>It is unexpected to send a NOTIFICATION to the device.</p>
     */
    NOTIFICATION(0b01),
    /**
     * <p>A RESPONSE is expected to be received from the device as a successful answer to a
     * COMMAND.</p>
     * <p>It is unexpected to send a RESPONSE to the device.</p>
     */
    RESPONSE(0b10),
    /**
     * <p>An ERROR is expected to be received from the device as an unsuccessful answer to a
     * COMMAND.</p>
     * <p>It is unexpected to send an ERROR to the device.</p>
     */
    ERROR(0b11);

    private static final String TAG = "V3PacketType";

    /**
     * The 2 bits value of the type.
     */
    private final int mValue;

    /**
     * A static array of all the values for looping around them.
     */
    private static final V3PacketType[] VALUES = values();

    /**
     * To build each value of the enumeration with their 2 bits value.
     */
    V3PacketType(int value) {
        this.mValue = value;
    }

    /**
     * <p>To identify a type from their 2 bits value.</p>
     * <p>If the value was not identified, this would return {@link #ERROR}. This is an
     * unexpected behaviour as all 2 bits values are covered by this enumeration.</p>
     *
     * @param value
     *         A 2 bits value to match to a {@link V3PacketType}.
     */
    public static V3PacketType valueOf(int value) {
        for (V3PacketType type : VALUES) {
            if (type.mValue == value) {
                return type;
            }
        }

        // unexpected -> returns ERROR
        Log.w(TAG, "[valueOf] not type matches the given value: " + value);
        return ERROR;
    }

    /**
     * To get the 2 bits value that corresponds to the type.
     */
    public int getValue() {
        return mValue;
    }
}
