/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.vendors.alternatives;

import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.V1V2Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.vendor.example.vendors.MyVendorIDs;

import androidx.annotation.NonNull;

/*
 * As an alternative, it is possible to extend V1V2Vendor instead of V3Vendor to get V1V2Packets
 * instead of V3Packets.
 */
public class MyV1V2Vendor extends V1V2Vendor {

    /*
     * A V1V2Vendor can use V1V2Plugins to manage V1V2 packets, acknowledgements and notifications.
     */
    private final MyV1V2Plugin myPlugin;

    /*
     * Constructor needs to call super(int id, @NonNull GaiaSender sender).
     */
    public MyV1V2Vendor(@NonNull GaiaSender sender) {
        super(MyVendorIDs.MY_V1V2_VENDOR_ID, sender);
        // similar to MyV3Vendor

        // similarly it can initialise plugins
        myPlugin = new MyV1V2Plugin(getVendorId(), sender);
    }

    @Override
    protected void handlePacket(@NonNull V1V2Packet packet) {
        /*
         * The V1V2 vendor class does not manage a collection of plugins as no defined mechanism
         * exists to do so. An idea to use multiple plugins would be to use command masks.
         * The V1V2Vendor analyses incoming bytes and store them into a V1V2Packet object.
         */

        // use of a v1v2 plugin to manager
        myPlugin.onReceiveGaiaPacket(packet);
    }

    @Override
    protected void onStarted() {
        // similar to MyV3Vendor
        myPlugin.start();
    }

    @Override
    protected void onStopped() {
        // similar to MyV3Vendor
        myPlugin.stop();
    }

    @Override
    protected void onNotSupported() {
        // similar to MyV3Vendor
    }

    private void sendSomething() {
        // similar to MyV3Vendor: the plugin is in charge of sending information
        myPlugin.sendSomething();
    }
}
