/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.v3;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.Parameters;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketFactory;

/**
 * <p>A {@link Plugin} that manages v3 packets for a v3 feature. This plugin maps incoming v3 packets as follows:
 * <ul>
 * <li>{@link #onResponse(ResponsePacket, CommandPacket)} : when the packet is a
 * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#RESPONSE}. This is the expected response
 * when sending a COMMAND that the device successfully managed.</li>
 * <li>{@link #onError(ErrorPacket, CommandPacket)} : when the packet is an
 * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#ERROR}. This is the expected response when
 * sending a COMMAND that the device failed to manage.</li>
 * <li>{@link #onNotification(NotificationPacket)}: when the packet is a
 * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#NOTIFICATION}. This is expected as a
 * spontaneous message from the device.</li>
 * </ul></p>
 */
public abstract class V3Plugin extends Plugin {

    /**
     * A tag to use to display logs within this class.
     */
    private static final String TAG = "V3Plugin";

    /**
     * The default time out for v3 COMMAND packets to receive a response.
     */
    public static final int DEFAULT_RESPONSE_TIME_OUT_MS = 10000;

    /**
     * The v3 feature implemented through this plugin.
     */
    private final int feature;

    /**
     * <p>To build a new {@link Plugin} for v3 GAIA packets.</p>
     * <p>The combination <code>vendor ID</code> and <code>feature ID</code> must be unique for the device.</p>
     *
     * @param vendor
     *         the vendor ID to write in the packets to send from this plugin.
     * @param feature
     *         the ID that corresponds to the Plugin manages by the instance.
     * @param sender
     *         the sender this vendor should use to send packets.
     */
    protected V3Plugin(int vendor, int feature, @NonNull GaiaSender sender) {
        super(vendor, sender);
        this.feature = feature;
    }

    /**
     * <p>This method redirects a v3 <code>received</code> packet to
     * {@link #onResponse(ResponsePacket, CommandPacket)}, {@link #onNotification(NotificationPacket)} or
     * {@link #onError(ErrorPacket, CommandPacket)} depending on its type.</p>
     * <p>{@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#COMMAND} packets are unexpected
     * from the device, this method discards them.</p>
     * <p>If the packet is not a v3 packet or the feature does not match this plugin feature, the method discards the
     * message.</p>
     */
    @Override // Plugin
    protected final void onPacketReceived(GaiaPacket received, @Nullable GaiaPacket sent) {
        if (!(received instanceof V3Packet)) {
            // unexpected
            Log.w(TAG,
                  "[onPacketReceived] Unexpected non v3 packet for feature=" + getFeature());
            return;
        }

        V3Packet v3Received = (V3Packet) received;
        CommandPacket v3Sent = sent instanceof CommandPacket ? (CommandPacket) sent : null;

        if (v3Received.getFeature() != getFeature()) {
            Log.w(TAG,
                  String.format("[onPacketReceived] packet received with feature=%1$s for " +
                                        "plugin with feature=%2$s", v3Received.getFeature(),
                                getFeature()));
            return;
        }

        switch (v3Received.getType()) {
            case COMMAND:
                // unexpected
                break;
            case NOTIFICATION:
                onNotification((NotificationPacket) v3Received);
                break;
            case RESPONSE:
                onResponse((ResponsePacket) v3Received, v3Sent);
                break;
            case ERROR:
                // logging the error
                ErrorPacket errorPacket = (ErrorPacket) v3Received;
                V3ErrorStatus status = errorPacket.getV3ErrorStatus();
                int value = errorPacket.getStatusValue();
                Log.w(TAG, String.format(
                        "[onPacketReceived->ERROR] error received: feature=%1$d, command=%2$d," +
                                " status=%3$s, value=%4$d", errorPacket.getFeature(),
                        errorPacket.getCommand(), status, value));
                // dispatching the packet as an error
                onError(errorPacket, v3Sent);
                break;
        }
    }

    /**
     * To get the feature ID managed by this plugin.
     */
    public int getFeature() {
        return feature;
    }

    @Override // Plugin
    protected long getDefaultTimeout() {
        return DEFAULT_RESPONSE_TIME_OUT_MS;
    }

    /**
     * To send a packet with an empty payload. This method builds a v3 packet with the command and calls its parent
     * send method to send it to a device.
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     */
    protected final void sendPacket(int command) {
        send(V3PacketFactory.buildCommandPacket(getVendor(), getFeature(), command));
    }

    /**
     * To send a packet with a one byte payload. This method builds a v3 packet with the given content and calls its
     * parent send method to send it to a device.
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     * @param data
     *         the value to put in the one byte payload - this is going to be cast to the byte primitive type.
     */
    protected void sendPacket(int command, int data) {
        send(V3PacketFactory.buildCommandPacket(getVendor(), getFeature(), command, new byte[]{(byte) data}));
    }

    /**
     * To send a packet with a payload. This method builds a v3 packet with the given content and calls its parent send
     * method to send it to a device.
     *
     * @param command
     *         the ID of the command to be put onto the packet.
     * @param data
     *         the payload bytes.
     */
    protected void sendPacket(int command, byte[] data) {
        send(V3PacketFactory.buildCommandPacket(getVendor(), getFeature(), command, data));
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
        GaiaPacket packet = V3PacketFactory.buildCommandPacket(getVendor(), getFeature(), command, data);
        send(packet, parameters);
    }

    /**
     * <p>This is called when the plugin receives a V3 packet of type
     * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#NOTIFICATION} that matches the vendor
     * and the feature of this plugin.</p>
     *
     * @param packet
     *         the received packet.
     */
    protected abstract void onNotification(NotificationPacket packet);

    /**
     * <p>This is called when the plugin receives a V3 packet of type
     * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#RESPONSE} that matches the vendor and
     * the feature of this plugin.</p>
     * <p>A {@link ResponsePacket} is expected as a successful answer to a {@link CommandPacket}. The Plugin provides a
     * command packet that has been sent that matches the received packet command.</p>
     *
     * @param response
     *         the received packet.
     * @param sent
     *         A sent {@link CommandPacket} that has been sent and matches the received response.
     */
    protected abstract void onResponse(ResponsePacket response, @Nullable CommandPacket sent);


    /**
     * <p>This is called when the plugin receives a V3 packet of type
     * {@link com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3PacketType#ERROR} that matches the vendor and the
     * feature of this plugin.</p> <p>An {@link ErrorPacket} is expected as an unsuccessful answer to a
     * {@link CommandPacket}. The Plugin provides a command packet that has been sent that matches the received packet
     * command.</p>
     *
     * @param errorPacket
     *         the received packet.
     * @param sent
     *         A sent {@link CommandPacket} that has been sent and matches the received packet.
     */
    protected abstract void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent);

}
