/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.vendors.alternatives;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.V1V2Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2PacketFactory;
import com.qualcomm.qti.gaiaclient.vendor.example.vendors.MyVendorIDs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/*
 * V1v2 packets can be managed using a V1V2Plugin.
 * This is an example of implementation.
 */
public class MyV1V2Plugin extends V1V2Plugin {

    /*
     * The implementation can use the core publication system to notify the application.
     * This vendor uses an example publisher to demonstrate the system.
     */
    MyV1V2Plugin(int vendor, @NonNull GaiaSender sender) {
        super(vendor, sender);
    }

    @Override
    protected void onStarted() {
        /*
         * Anything that needs to be done when the plugin starts.
         * A plugin is started by its vendor.
         */
    }

    @Override
    protected void onStopped() {
        /*
         * Anything that needs to be done when the plugin stops.
         * A plugin is stopped by its vendor or when the application terminates.
         */
    }

    @Override
    protected void onResponse(V1V2Packet response, @Nullable V1V2Packet sent) {
        // When a plugin receives a successful v1/v2 acknowledgement it calls this method
        // this can be implemented by looking at the command ID of the packet
        switch (response.getCommand()) {
        }
    }

    @Override
    protected boolean onNotification(V1V2Packet packet) {
        // When a plugin receives a packet notification - command 0x4003 - it calls this method
        // this can be implemented by looking at the event ID of the packet
        switch (packet.getEvent()) {
        }

        // For v1/v2 a notification is a command packet that must be acknowledged, this method must
        // return false if it has not acknowledged a packet, true otherwise
        return false;
    }

    @Override
    protected void onError(V1V2Packet response, @Nullable V1V2Packet sent) {
        // When a plugin receives an unsuccessful v1/v2 acknowledgement it calls this method
        // this can be implemented by looking at the status and the command ID of the response
        V1V2ErrorStatus status = response.getStatus();

        switch (response.getCommand()) {
        }
    }

    @Override
    protected boolean onCommand(V1V2Packet packet) {
        // This is called when a plugin receives a packet that is neither an acknowledgement or a
        // notification
        // this can be implemented by looking at the command ID of the packet
        switch (packet.getCommand()) {
        }

        // a command must be acknowledged, this method must return false if it has not
        // acknowledged a packet, true otherwise
        return false;
    }

    @Override
    protected void onFailed(GaiaPacket packet, Reason reason) {
        // this is called when a packet could not be sent to the device.
    }

    /*
     * Example to send a packet.
     */
    void sendSomething() {
        V1V2Packet packet = V1V2PacketFactory.buildPacket(getVendor(),
                                                          MyVendorIDs.MY_V1V2_COMMAND_ID);
        send(packet);
    }

    /*
     * Example to send an acknowledgement.
     */
    void sendAnAcknowledgement(V1V2Packet packet) {
        sendAcknowledgement(packet, V1V2ErrorStatus.SUCCESS);
    }
}
