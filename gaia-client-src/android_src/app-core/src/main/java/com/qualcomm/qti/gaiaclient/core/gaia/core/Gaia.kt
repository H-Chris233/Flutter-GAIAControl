/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core

import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.Gatt

/**
 * PDU = Protocol Data Unit
 * GAIA PDU frame:
 * 0 bytes     1           2           3            4                      len+4
 * +-----------+-----------+-----------+-----------+ +-----------+-----------+
 * |        VENDOR         |        COMMAND        | | Optional PAYLOAD  ... |
 * +-----------+-----------+-----------+-----------+ +-----------+-----------+
 */
object GaiaPdu {
    object Vendor { // UINT16
        const val offset = 0
        const val length = 2
    }

    object Command { // UINT16
        const val offset = 2
        const val length = 2
    }

    object Header {
        const val offset = Vendor.offset
        const val length = Vendor.length + Command.length
    }

    object Payload { // array of bytes
        const val offset = 4
        const val minLength = Gatt.Mtu.DEFAULT - Gatt.Att.headerLength - Header.length
    }
}
