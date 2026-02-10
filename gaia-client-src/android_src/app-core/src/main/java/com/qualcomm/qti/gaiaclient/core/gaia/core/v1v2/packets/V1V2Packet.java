/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu;
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;

import java.util.Objects;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

/**
 * <p>A v1/v2 packet has the following PDU:
 * <blockquote><pre>
 * 0 bytes     1           2           3            4                      len+4
 * +-----------+-----------+-----------+-----------+ +-----------+-----------+
 * |       VENDOR ID       |     COMMAND VALUE     | | Optional PAYLOAD  ... |
 * +-----------+-----------+-----------+-----------+ +-----------+-----------+
 * </pre> </blockquote></p>
 *
 * <i>Note: PDU = Protocol Data Unit.</i>
 */
public class V1V2Packet extends GaiaPacket {

    /**
     * <p>The mask that represents an acknowledgement.</p>
     * <ul>
     * <li><code>COMMAND & ACKNOWLEDGMENT_MASK > 0</code> to know if the command is an
     * acknowledgement.</li>
     * <li><code>COMMAND | ACKNOWLEDGMENT_MASK</code> to build the acknowledgement command from
     * a command ID.</li>
     * </ul>
     */
    private static final int ACKNOWLEDGMENT_MASK = 0x8000;
    /**
     * <p>The mask that represents a command.</p>
     * <p>Mask used to retrieve a command ID from a packet.</p>
     */
    private static final int COMMAND_MASK = 0x7FFF;
    /**
     * The value for the <code>event</code> when the packet is not a notification.
     */
    private static final int NOT_AN_EVENT = 0x100; // event is from 0 to 255
    /**
     * The offset of the event when the packet is a notification. The event is the first byte of
     * the payload.
     */
    private static final int NOTIFICATION_EVENT_OFFSET = 4;
    /**
     * To identify if the command is a notification, the command must be 0x4003.
     */
    private static final int COMMAND_EVENT_NOTIFICATION = 0x4003;

    /**
     * The command ID of the packet, this does not contain the acknowledgement mask.
     */
    private final int command;
    /**
     * To know if the packet is an acknowledgement.
     */
    private final boolean isAcknowledgement;
    /**
     * The payload of the packet, this is non null but can be empty as the payload is optional.
     */
    @NonNull
    private final byte[] payload;
    /**
     * The event when the packet is a notification.
     */
    private final int event;
    /**
     * The key to use to match packets to this one.
     */
    private final int key;

    /**
     * <p>This constructor extracts the packet information from the byte array. It expects the data
     * to be formatted as follows:
     * <blockquote><pre>
     * 0 bytes     1           2           3            4                      len+4
     * +-----------+-----------+-----------+-----------+ +-----------+-----------+
     * |       VENDOR ID       |     COMMAND VALUE     | | Optional PAYLOAD  ... |
     * +-----------+-----------+-----------+-----------+ +-----------+-----------+
     * </pre> </blockquote></p>
     * <p>In the case of a notification this also extract the notification event.</p>
     */
    V1V2Packet(byte[] data) {
        super(getUINT16(data, GaiaPdu.Vendor.offset));
        int commandId = getUINT16(data, GaiaPdu.Command.offset);
        this.isAcknowledgement = (commandId & ACKNOWLEDGMENT_MASK) > 0;
        this.command = commandId & COMMAND_MASK;
        this.payload = getSubArray(data, GaiaPdu.Payload.offset);
        this.event =
                data.length > NOTIFICATION_EVENT_OFFSET && command == COMMAND_EVENT_NOTIFICATION ?
                        getUINT8(data, NOTIFICATION_EVENT_OFFSET) : NOT_AN_EVENT;
        this.key = Objects.hash(getVendorId(), this.command);
    }

    /**
     * Builds a packet with the given vendor and command. It sets the packet as not an
     * acknowledgement or a notification.
     */
    V1V2Packet(int vendorId, int command) {
        this(vendorId, command, null);
    }

    /**
     * Builds a packet with the given vendor and command. It also builds a payload of length 1
     * that contains the given <code>data</code>.It sets the packet as not an acknowledgement or a
     * notification.
     */
    V1V2Packet(int vendorId, int command, int data) {
        this(vendorId, command, new byte[] {(byte) data});
    }

    /**
     * Builds a packet with the given vendor, command and payload. It sets the packet as not an
     * acknowledgement or a notification.
     */
    V1V2Packet(int vendorId, int command, byte[] data) {
        super(vendorId);
        this.command = command;
        this.isAcknowledgement = false;
        this.payload = data == null ? new byte[0] : data;
        this.event = payload.length >= 1 ? payload[0] : NOT_AN_EVENT;
        this.key = Objects.hash(getVendorId(), this.command);
    }

    /**
     * This constructor builds an acknowledgement packet for the <code>origin</code> packet with
     * the given status.
     */
    V1V2Packet(V1V2Packet origin, V1V2ErrorStatus status) {
        super(origin.getVendorId());
        this.command = origin.getCommand();
        this.isAcknowledgement = true;
        byte[] payload = new byte[1];
        payload[0] = (byte) status.getValue();
        this.payload = payload;
        this.event = NOT_AN_EVENT;
        this.key = Objects.hash(getVendorId(), this.command);
    }

    /**
     * This method matches the first byte of the payload to a status for acknowledgement packets
     * only.
     */
    public V1V2ErrorStatus getStatus() {
        final int STATUS_OFFSET = 0;
        final int STATUS_LENGTH = 1;

        if (!isAcknowledgement() || payload.length < STATUS_LENGTH) {
            return V1V2ErrorStatus.NO_STATUS;
        }
        else {
            return V1V2ErrorStatus.valueOf(payload[STATUS_OFFSET]);
        }
    }

    /**
     * Gives the value of the command for the header of the packet.
     */
    @Override // GaiaPacket
    protected int getCommandValue() {
        return isAcknowledgement ? command | ACKNOWLEDGMENT_MASK : command;
    }

    @NonNull
    @Override // GaiaPacket
    public byte[] getPayload() {
        return payload;
    }

    @Override // GaiaPacket
    public int getKey() {
        return key;
    }

    /**
     * To get the command ID of the packet.
     */
    public int getCommand() {
        return command;
    }

    /**
     * To get the notification event of the packet. If the packet is not a notification this
     * returns {@link #NOT_AN_EVENT}.
     */
    public int getEvent() {
        return event;
    }

    /**
     * @return True if the packet is an acknowledgement, false otherwise.
     */
    public boolean isAcknowledgement() {
        return isAcknowledgement;
    }

    /**
     * @return True if the packet is a notification, false otherwise.
     */
    public boolean isNotification() {
        return command == COMMAND_EVENT_NOTIFICATION;
    }

    /**
     * <p>To get the payload stripped out of any information related to the packet being a
     * response/acknowledgement.</p>
     */
    public byte[] getResponsePayload() {
        final int RESPONSE_OFFSET = 1;
        return getSubArray(getPayload(), RESPONSE_OFFSET);
    }

    /**
     * <p>To get the payload stripped out of any information related to the packet being a
     * notification.</p>
     */
    public byte[] getNotificationPayload() {
        final int NOTIFICATION_OFFSET = 1;
        return getSubArray(getPayload(), NOTIFICATION_OFFSET);
    }

    @NonNull
    @Override // Object
    public String toString() {
        return "V1V2Packet{" +
                ", vendor=" + BytesUtils.getHexadecimalStringFromInt(getVendorId()) +
                ", command=" + BytesUtils.getHexadecimalStringFromInt(command) +
                '}';
    }
}
