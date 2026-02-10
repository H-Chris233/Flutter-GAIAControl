/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

/**
 * <p>This structure represents a GAIA v3 packet of type ERROR. This structure helps interpreting
 * the packet type in a human readable packet.</p>
 * <p>This type of packet has an error value as the first byte of its payload:
 * <blockquote><pre>
 * Payload bytes
 * 0            1                      len+1
 * +-----------+ +-----------+-----------+
 * | ERROR VAL | | Optional PAYLOAD  ... |
 * +-----------+ +-----------+-----------+
 * </pre> </blockquote></p>
 * <p>The error value can either be framework specific - values from 0 to 127 - or feature
 * related - 128 to 255.</p>
 * <p>If the value is framework specific, it is matched to a {@link V3ErrorStatus} and can be
 * retrieved using {@link #getV3ErrorStatus()}. If the value is feature specific,
 * {@link #getV3ErrorStatus()} returns {@link V3ErrorStatus#FEATURE_SPECIFIC} and the value can
 * be retrieved using {@link #getStatusValue()}. The type of error can also be checked with
 * {@link #isFeatureSpecific()}.</p>
 * <p>The parent {@link V3Packet#getPayload()} method returns the whole payload that includes the
 * status byte.</p>
 */
public class ErrorPacket extends V3Packet {

    /**
     * The offset to identify the error in the payload.
     */
    private static final int STATUS_OFFSET = 0;

    /**
     * The value of the error.
     */
    private final int statusValue;
    /**
     * The value as a framework status.
     */
    private final V3ErrorStatus defaultStatus;

    /**
     * Builds an ERROR type packet from the given parameters. This constructor also gets the
     * error status/value.
     *
     * @param commandDescription
     *         must be of type {@link V3PacketType#ERROR}.
     */
    ErrorPacket(int vendorId, V3Command commandDescription, @NonNull byte[] payload) {
        super(vendorId, commandDescription, payload);
        statusValue = getUINT8(payload, STATUS_OFFSET, -1);
        defaultStatus = V3ErrorStatus.valueOf(statusValue);
    }

    /**
     * To get the status as identified as a framework status.
     *
     * @return The corresponding status if identified, {@link V3ErrorStatus#FEATURE_SPECIFIC}
     * otherwise.
     */
    public V3ErrorStatus getV3ErrorStatus() {
        return defaultStatus;
    }

    /**
     * The exact value of the error as received in the payload.
     */
    public int getStatusValue() {
        return statusValue;
    }

    /**
     * To know if the error is framework specific or feature specific.
     *
     * @return True if the error is feature specific, false otherwise.
     */
    public boolean isFeatureSpecific() {
        return defaultStatus == V3ErrorStatus.FEATURE_SPECIFIC;
    }
}
