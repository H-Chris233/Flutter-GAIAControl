/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.version;

/**
 * All the protocol versions the core library knows about.
 */
public final class ProtocolVersion {

    /**
     * Version of the protocol used for GAIA V1/V2.
     */
    public static final long V1 = 0x01;

    /**
     * Unused version.
     */
    public static final long V2 = 0x02;

    /**
     * Version used for GAIA V3 that introduces the split of a command UINT16 in bits that
     * contains a feature ID, a packet type and a command ID.
     */
    public static final long V3 = 0x03;

    /**
     * Version used for GAIA V3 that introduces the data length extension flag.
     */
    public static final long V4 = 0x04;

}
