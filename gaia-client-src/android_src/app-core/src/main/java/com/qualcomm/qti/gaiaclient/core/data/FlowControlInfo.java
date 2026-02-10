/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo;

public enum FlowControlInfo {
    /**
     * Is enabled if the flow control is managed by the transport layer when the device sends some
     * data.
     */
    TX(ProtocolInfo.TX_FLOW_CONTROL),
    /**
     * Is enabled if the flow control is managed by the transport layer when the device receives
     * some data.
     */
    RX(ProtocolInfo.RX_FLOW_CONTROL);

    private final ProtocolInfo protocolInfo;

    private static final FlowControlInfo[] VALUES = values();

    FlowControlInfo(ProtocolInfo protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    /**
     * To get the protocol information that corresponds to this control flow info.
     */
    public ProtocolInfo getProtocolInfo() {
        return protocolInfo;
    }

    public static FlowControlInfo valueOf(ProtocolInfo protocolInfo) {
        for (FlowControlInfo info : VALUES) {
            if (info.protocolInfo == protocolInfo) {
                return info;
            }
        }

        return null;
    }
}
