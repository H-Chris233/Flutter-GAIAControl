/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic;

public enum ProtocolInfo {

    /**
     * <p>The maximum packet size a device can send.</p>
     * <p>Information is a uint32.</p>
     */
    MAX_TX_PACKET_SIZE(1),

    /**
     * <p>The optimum packet size a device should send - optimum referring to the size that
     * allows a transfer to be at its fastest.</p>
     * <p>Information is a uint32.</p>
     */
    OPTIMUM_TX_PACKET_SIZE(2),

    /**
     * <p>The maximum packet size a device can receive.</p>
     * <p>Information is a uint32.</p>
     * <p>Cannot be set.</p>
     */
    MAX_RX_PACKET_SIZE(3),

    /**
     * <p>The optimum packet size a device should receive - optimum referring to the size that
     * allows a transfer to be at its fastest.</p>
     * <p>Information is a uint32.</p>
     * <p>Cannot be set.</p>
     */
    OPTIMUM_RX_PACKET_SIZE(4),

    /**
     * <p>To know if the transport has flow control from device to client.</p>
     * <p>Information is a uint32 value: 1 for enabled, 0 for disabled.</p>
     */
    TX_FLOW_CONTROL(5),

    /**
     * <p>To know if the transport has flow control from client to device.</p>
     * <p>Information is a uint32 value: 1 for enabled, 0 for disabled.</p>
     * <p>Cannot be set.</p>
     */
    RX_FLOW_CONTROL(6),

    /**
     * <p>Current protocol version.</p>
     * <p>See {@link com.qualcomm.qti.gaiaclient.core.gaia.core.version.ProtocolVersion} to know
     * what this client supports.</p>
     * <p>Information is a uint32.</p>
     */
    PROTOCOL_VERSION(7);

    public static final long FLOW_CONTROL_ENABLED = 1;

    public static final long FLOW_CONTROL_DISABLED = 0;

    private final int value;

    private static final ProtocolInfo[] VALUES = values();

    ProtocolInfo(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ProtocolInfo valueOf(int value) {
        for (ProtocolInfo info : VALUES) {
            if (info.value == value) {
                return info;
            }
        }

        return null;
    }
}
