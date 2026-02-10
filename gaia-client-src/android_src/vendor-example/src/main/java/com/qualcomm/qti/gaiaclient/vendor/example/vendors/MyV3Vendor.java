/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.vendors;

import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.V3Vendor;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;

import androidx.annotation.NonNull;

/*
 * This example shows how to extend the core library to create a specific vendor for GAIA v3.
 *
 * Alternatives are:
 *      - com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.V1V2Vendor for v1/v2 packets
 *      - com.qualcomm.qti.gaiaclient.core.gaia.core.Vendor for raw bytes
 */
public class MyV3Vendor extends V3Vendor {

    /*
     * Constructor needs to call super(int id, @NonNull GaiaSender sender).
     */
    public MyV3Vendor(@NonNull GaiaSender sender,
                      @NonNull PublicationManager publicationManager) {
        super(MyVendorIDs.MY_VENDOR_ID, sender);

        // as an example, the vendor can register its plugins in its constructor
        MyV3Plugin myPlugin = new MyV3Plugin(getVendorId(),
                                             MyVendorIDs.MY_FEATURE_ID, sender,
                                             publicationManager);
        addPlugin(myPlugin);
    }

    @Override
    protected void onStarted() {
        /*
         * Anything that needs to be done when the vendor starts. A vendor starts when it can
         * start to communicate with a device.
         */

        // as an example the vendor can start its plugins.
        startAll();
    }

    @Override
    protected void onStopped() {
        /*
         * Anything that needs to be done when the vendor stops. A vendor stops when it cannot
         * communicate with a device anymore.
         */

        // as an example the vendor can stop its plugins.
        stopAll();

    }

    @Override
    protected void onNotSupported() {
        /*
         * Anything that needs to be done when v3 is not supported.
         * This is followed by onStopped.
         */
    }

    /*
     * No handleData(byte[] data) method is provided as the V3Vendor handles the data and directly
     * calls the plugin that matches the data feature ID.
     * Alternatively, it is possible to implement onUnhandledPacket for packets that could not be
     * matched to a plugin.
     */

    @Override
    protected void onUnhandledPacket(V3Packet packet) {
        // optional for packets with a feature ID that did not have a matching Plugin
    }

    public void doSomething() {
        // getting the plugin that has to do something
        MyV3Plugin myPlugin = (MyV3Plugin) getPlugin(MyVendorIDs.MY_FEATURE_ID);
        // doing something
        myPlugin.sendSomething();
    }
}
