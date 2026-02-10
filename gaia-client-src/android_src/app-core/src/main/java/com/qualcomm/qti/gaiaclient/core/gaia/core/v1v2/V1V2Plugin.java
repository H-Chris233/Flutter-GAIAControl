/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.Parameters;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2PacketFactory;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getHexadecimalStringFromBytes;

/**
 * <p>A {@link Plugin} that manages v1/v2 packets. This plugin maps incoming v1/v2 GAIA packets as follows:
 * <ul>
 * <li>{@link #onResponse(V1V2Packet, V1V2Packet)}: when the packet is a successful acknowledgement.</li>
 * <li>{@link #onError(V1V2Packet, V1V2Packet)}: when the packet is an unsuccessful acknowledgement.</li>
 * <li>{@link #onNotification(V1V2Packet)}: when the packet is a notification.</li>
 * <li>{@link #onCommand(V1V2Packet)}: packet that is not a notification or an acknowledgement.</li>
 * </ul></p>
 */
public abstract class V1V2Plugin extends Plugin {

    /**
     * A tag to use to display logs within this class.
     */
    private static final String TAG = "V1V2Plugin";

    /**
     * The default time out for v1/v2 packets used to trigger warnings when an acknowledgement has not been received
     * for a command.
     */
    public static final int DEFAULT_RESPONSE_TIME_OUT_MS = 30000;

    /**
     * To build a new {@link Plugin} for v1/v2 GAIA version.
     *
     * @param vendor
     *         Needs to know the vendor ID it needs to use to send packets.
     * @param sender
     *         the sender this vendor should use to send packets.
     */
    public V1V2Plugin(int vendor, @NonNull GaiaSender sender) {
        super(vendor, sender);
    }

    @Override // Plugin
    protected final void onPacketReceived(GaiaPacket received, @Nullable GaiaPacket sent) {
        if (!(received instanceof V1V2Packet)) {
            Log.w(TAG,
                  "[onReceiveGaiaPacket] Unexpected non v1v2 packet.");
            return;
        }

        V1V2Packet v1v2Received = (V1V2Packet) received;
        V1V2Packet v1v2Sent = sent instanceof V1V2Packet ? (V1V2Packet) sent : null;

        if (v1v2Received.isAcknowledgement()) {
            // acknowledgement packet: is it successful?
            onAcknowledgementReceived(v1v2Received, v1v2Sent);
        }
        // not an ACK packet: requires to be acknowledged

        else if (v1v2Received.isNotification()) {
            if (!onNotification(v1v2Received)) {
                Log.i(TAG, "Notification not managed by implementation, manager sends " +
                        "COMMAND_NOT_SUPPORTED acknowledgement, received bytes= "
                        + getHexadecimalStringFromBytes(v1v2Received.getPayload()));
                sendAcknowledgement(v1v2Received, V1V2ErrorStatus.COMMAND_NOT_SUPPORTED);
            }
        }
        else {
            if (!onCommand(v1v2Received)) {
                Log.i(TAG, "Packet has not been managed by implementation, manager sends " +
                        "COMMAND_NOT_SUPPORTED acknowledgement, received bytes= "
                        + getHexadecimalStringFromBytes(v1v2Received.getPayload()));
                sendAcknowledgement(v1v2Received, V1V2ErrorStatus.COMMAND_NOT_SUPPORTED);
            }
        }
    }

    @Override // Plugin
    protected long getDefaultTimeout() {
        return DEFAULT_RESPONSE_TIME_OUT_MS;
    }

    /**
     * To send a packet with an empty payload.
     * This method builds a v1/v2 packet with the command and calls its parent send method to send it to a device.
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     */
    protected void sendPacket(int command) {
        send(V1V2PacketFactory.buildPacket(getVendor(), command));
    }

    /**
     * To send a packet with a one byte payload.
     * This method builds a v1/v2 packet with the given content and calls its parent send method to send it to a device.
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     * @param data
     *         the data to put in the one byte payload.
     */
    protected void sendPacket(int command, int data) {
        send(V1V2PacketFactory.buildPacket(getVendor(), command, data));
    }

    /**
     * To send a packet with a payload.
     * This method builds a v1/v2 packet with the given content and calls its parent send method to send it to a device.
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     * @param data
     *         the payload bytes.
     */
    protected void sendPacket(int command, byte[] data) {
        send(V1V2PacketFactory.buildPacket(getVendor(), command, data));
    }

    /**
     * <p>To send a packet with a payload and parametrise the sending of the packet. This method builds the
     * corresponding v3 packet and sends the packet using the given parameters.</p>
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     * @param data
     *         the packet payload.
     * @param parameters
     *         To specify any parameters for sending the packet such as the timeout within which to expect a response
     *         from the device.
     */
    protected void sendPacket(int command, byte[] data, Parameters parameters) {
        GaiaPacket packet = V1V2PacketFactory.buildPacket(getVendor(), command, data);
        send(packet, parameters);
    }

    /**
     * To send an acknowledgement for the <code>origin</code> packet with the given status.
     */
    protected void sendAcknowledgement(V1V2Packet origin, V1V2ErrorStatus status) {
        send(V1V2PacketFactory.buildAcknowledgement(origin, status));
    }

    /**
     * To send an acknowledgement for the <code>origin</code> packet with the given status.
     *
     * @param flushed
     *         This allows the acknowledgement to be flushed, see
     *         {@link #send(GaiaPacket, Parameters)} for more information.
     */
    protected void sendAcknowledgement(V1V2Packet origin, V1V2ErrorStatus status,
                                       boolean flushed) {
        GaiaPacket packet = V1V2PacketFactory.buildAcknowledgement(origin, status);

        // Using default - Java doesn't support named arguments
        Parameters parameters = new Parameters(
                true,
                false,
                flushed,
                false,
                null,
                DEFAULT_RESPONSE_TIME_OUT_MS
        );
        send(packet, parameters);
    }

    /**
     * Called when this plugin receives a command packet - a command packet is a packet that is not an acknowledgement.
     *
     * @param packet
     *         The received packet.
     *
     * @return command packets must be acknowledged. This should return true when it has acknowledged the packet. If it
     *         returns false the parent plugin acknowledges with {@link V1V2ErrorStatus#COMMAND_NOT_SUPPORTED}.
     */
    protected abstract boolean onCommand(V1V2Packet packet);

    /**
     * Called when this plugin receives a V1/V2 packet acknowledgement with a successful status.
     *
     * @param response
     *         The successful packet that has been received.
     * @param sent
     *         The packet that was sent and matches the command of the acknowledgement. If no packet were sent, this is
     *         null.
     */
    protected abstract void onResponse(V1V2Packet response, @Nullable V1V2Packet sent);

    /**
     * Called when this plugin receives a V1/V2 packet that matches the notification command.
     *
     * @param packet
     *         The notification packet that has been received.
     *
     * @return notification packets must be acknowledged. This should return true when it has acknowledged the packet.
     *         If it returns false the parent plugin acknowledges with {@link V1V2ErrorStatus#COMMAND_NOT_SUPPORTED}.
     */
    protected abstract boolean onNotification(V1V2Packet packet);

    /**
     * Called when this plugin receives a V1/V2 packet acknowledgement with an unsuccessful status.
     *
     * @param response
     *         The error packet that has been received.
     * @param sent
     *         The packet that was sent and matches the command of the acknowledgement. If no packet were sent, this is
     *         null.
     */
    protected abstract void onError(V1V2Packet response, @Nullable V1V2Packet sent);

    /**
     * Checks the status of the acknowledgement and depending on its success this method calls
     * {@link #onResponse(V1V2Packet, V1V2Packet)} or {@link #onError(V1V2Packet, V1V2Packet)}.
     */
    private void onAcknowledgementReceived(V1V2Packet received, @Nullable V1V2Packet sent) {
        V1V2ErrorStatus status = received.getStatus();
        if (status == V1V2ErrorStatus.SUCCESS) {
            onResponse(received, sent);
        }
        else {
            onError(received, sent);
        }
    }
}
