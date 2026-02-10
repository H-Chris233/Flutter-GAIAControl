/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.data

import java.util.UUID

data class GattDescription(
    val pairingInduction: Characteristic,
    val notifications: Set<Notification>
) {
    fun getNotification(service: UUID, characteristic: UUID): Notification? =
        notifications.find { it.service == service && it.characteristic == characteristic }
}

data class Characteristic(val service: UUID, val characteristic: UUID)

data class Notification(
    val service: UUID,
    val characteristic: UUID,
    val mandatory: Boolean = true,
    val notify: (ByteArray) -> Unit
)
