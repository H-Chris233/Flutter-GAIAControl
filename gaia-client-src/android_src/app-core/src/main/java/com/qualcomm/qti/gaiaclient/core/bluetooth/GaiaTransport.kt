/*
 * ************************************************************************************************
 * * © 2023-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothType

interface GaiaTransport {
    /**
     * The maximum number of bytes that can be sent over the transport.
     * This number includes any transport protocol overhead.
     */
    val maxSize: Int
    val bluetoothType: BluetoothType
    val isConnected: Boolean
    fun sendData(bytes: ByteArray, isFlushed: Boolean, useRwcp: Boolean, listener: SendListener?): Long
    fun canHoldData(): Boolean
    fun holdData(ids: Collection<Long>?)
    fun resumeData(ids: Collection<Long>?)
    fun cancelData(ids: Collection<Long>?)
}
