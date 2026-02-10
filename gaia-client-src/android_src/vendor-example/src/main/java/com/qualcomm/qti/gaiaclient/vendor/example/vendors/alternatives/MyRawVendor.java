/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.vendors.alternatives;

import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.Vendor;
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;
import com.qualcomm.qti.gaiaclient.vendor.example.vendors.MyVendorIDs;

import androidx.annotation.NonNull;

/*
 * As an alternative, it is possible to extend Vendor instead of V3Vendor to get raw data
 * instead of V3Packets.
 */
public class MyRawVendor extends Vendor {

    public MyRawVendor(@NonNull GaiaSender sender) {
        super(MyVendorIDs.MY_RAW_VENDOR_ID, sender);
        // similar to MyV3Vendor
    }

    @Override
    public void handleData(byte[] data) {
        /*
         * The Vendor class does not manage a collection of plugins so the data is provided
         * directly to the Vendor.
         * "data" is the raw GAIA PDU bytes as received by the core library.
         * It is expected to contain a 4 bytes headers followed by an optional payload. The
         * header contains 2 bytes that represents the vendor ID and 2 other bytes that can be
         * used as command ID.
         *
         * The vendor handles the data and acts upon its content.
         */
    }

    @Override
    protected void onStarted(int gaiaVersion) {
        // similar to MyV3Vendor
    }

    @Override
    protected void onStopped() {
        // similar to MyV3Vendor
    }

    /*
     * "onNotSupported" is not called as this vendor must handle the GAIA version itself.
     * The vendor can subscribe to DeviceInformationSubscriber and listen for
     * DeviceInfo.HANDSHAKE_API_VERSION to get the GAIA version.
     */

    /*
     * An example to send some data to a device.
     */
    private void sendSomething() {
        // per convention the data must be at least 4 bytes long.
        byte[] data = new byte[4];

        // per convention the first 2 bytes contain the vendor ID.
        BytesUtils.setUINT16(getVendorId(), data, 0);
        // per convention the last 2 bytes of the header contain a command ID.
        BytesUtils.setUINT16(MyVendorIDs.MY_RAW_COMMAND_ID, data, 2);

        // use the GaiaSender to send some data
        getSender().sendData(data);
    }
}
