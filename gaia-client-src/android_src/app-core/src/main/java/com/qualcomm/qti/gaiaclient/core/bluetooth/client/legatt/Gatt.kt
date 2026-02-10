/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import java.util.UUID

object Gatt {

    object Mtu {
        /*
         * Default ATT MTU length as defined in Bluetooth Core Specifications 5.4, Volume 3, Part A, Section 5.1.
         * Also defined as default in Android source code under the C Header file `gatt_api.h`.
         * https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/system/stack/include/gatt_api.h
         */
        const val DEFAULT = 23
        /*
         * Maximum assignable ATT MTU length as defined in Bluetooth Core Specifications 5.4, Volume 3, Part 5,
         * Section 3.2.9 and in Android source code under the C Header file `gatt_api.h`.
         * https://cs.android.com/android/platform/superproject/+/master:packages/modules/Bluetooth/system/stack/include/gatt_api.h
         * Note: Android uses a maximum size of 517 to set the MTU size.
         */
        const val MAX = 512
    }

    object Att {
        /*
         * Length of the ATT header for attribute messages when using LE GATT.
         */
        const val headerLength = 3
    }
}

enum class WriteType(val value: Int) {
    DEFAULT(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT),
    NO_RESPONSE(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE),
    SIGNED(BluetoothGattCharacteristic.WRITE_TYPE_SIGNED)
}

enum class Priority(val value: Int) {
    BALANCED(BluetoothGatt.CONNECTION_PRIORITY_BALANCED),
    HIGH(BluetoothGatt.CONNECTION_PRIORITY_HIGH),
    LOW_POWER(BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER)
}

private const val GATT_UUID = "-0000-1000-8000-00805f9b34fb"
private const val QUALCOMM_UUID = "-d102-11e1-9b23-00025b00a5a5"

sealed class GattUuid(prefix: String, suffix: String) {
    val uuid: UUID = UUID.fromString("$prefix$suffix")
}

sealed class ServiceUuid(prefix: String, suffix: String) : GattUuid(prefix, suffix) {
    object Gaia : ServiceUuid("00001100", QUALCOMM_UUID)
}

sealed class CharacteristicUuid(prefix: String, suffix: String) : GattUuid(prefix, suffix) {
    object GaiaCommand : CharacteristicUuid("00001101", QUALCOMM_UUID)
    object GaiaResponse : CharacteristicUuid("00001102", QUALCOMM_UUID)
    object GaiaData : CharacteristicUuid("00001103", QUALCOMM_UUID)
}

sealed class UuidDescriptor(prefix: String, suffix: String) : GattUuid(prefix, suffix) {
    object ClientCharacteristicConfiguration : UuidDescriptor("00002902", GATT_UUID)
}
