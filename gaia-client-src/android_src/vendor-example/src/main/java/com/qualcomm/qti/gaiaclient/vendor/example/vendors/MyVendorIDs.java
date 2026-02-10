/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.vendor.example.vendors;

/*
 * Note: All the values used here are for illustrations purposes and might already be attributed
 * to some vendors.
 * All values must be unique within their namespace.
 */
public class MyVendorIDs {

    // --------------------------------------------------------------------------
    // region 1. VENDORS

    /*
     * Implementing Vendor commands requires the vendor to use their own vendor IDs.
     * Refer to the GAIA Programmer's guide to get information for defining this vendor.
     * A vendor is unique for a connection.
     */

    public static final int MY_VENDOR_ID = 0x0000;
    public static final int MY_RAW_VENDOR_ID = 0x0001;
    public static final int MY_V1V2_VENDOR_ID = 0x0002;


    // endregion ----------------------------------------------------------------
    // region 2. V3 FEATURES

    /*
     * V3 features are mapped to a 7bits integer and are implemented using Plugins.
     * A feature ID is unique for a vendor.
     */

    public static final int MY_FEATURE_ID = 0x00;


    // endregion ----------------------------------------------------------------
    // region 3. COMMANDS

    /*
     * - V3: commands are mapped to a 7bits integer and are implemented within plugins. They are
     *          unique for a (vendor, feature).
     * - V1/V2 and raw: commands
     */

    public static final int MY_V3_COMMAND_ID = 0x00;
    public static final int MY_RAW_COMMAND_ID = 0x0000;
    public static final int MY_V1V2_COMMAND_ID = 0x0000;


    // endregion
}
