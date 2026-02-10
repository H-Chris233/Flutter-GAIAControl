/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo;

public enum SizeInfo {

    /**
     * The maximum payload size a device can send.
     */
    MAX_TX_PAYLOAD(ProtocolInfo.MAX_TX_PACKET_SIZE),
    /**
     * The optimal payload size a device can send.
     */
    OPTIMUM_TX_PAYLOAD(ProtocolInfo.OPTIMUM_TX_PACKET_SIZE),
    /**
     * The maximum payload size oa device can receive.
     */
    MAX_RX_PAYLOAD(ProtocolInfo.MAX_RX_PACKET_SIZE),
    /**
     * The optimal payload size a device can receive.
     */
    OPTIMUM_RX_PAYLOAD(ProtocolInfo.OPTIMUM_RX_PACKET_SIZE);

    private final ProtocolInfo protocolInfo;

    public static final SizeInfo[] VALUES = values();

    SizeInfo(ProtocolInfo protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    /**
     * To get the protocol information that corresponds to this size info.
     */
    public ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    public static SizeInfo valueOf(ProtocolInfo protocolInfo) {
        for (SizeInfo info : VALUES) {
            if (info.protocolInfo == protocolInfo) {
                return info;
            }
        }

        return null;
    }
}
