/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Reception system information about Bluetooth devices bond state information.
 *
 * This receiver should be used with the following intent filter: [BluetoothDevice.ACTION_BOND_STATE_CHANGED] and
 * [BluetoothDevice.ACTION_PAIRING_REQUEST].
 */
class BondStateReceiver(val device: BluetoothDevice, val onBondStateChange: (state: Bond, previous: Bond) -> Unit) :
    BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (!isDevice(intent, device)) return

        val state = getBondState(intent, BluetoothDevice.EXTRA_BOND_STATE) ?: return
        val previous = getBondState(intent, BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE) ?: Bond.NONE
        onBondStateChange(state, previous)
    }

    private fun getBondState(intent: Intent, extra: String) : Bond? {
        when (intent.action) {
            BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                val state = intent.getIntExtra(extra, -1)
                return Bond.valueOf(state)
            }
            BluetoothDevice.ACTION_PAIRING_REQUEST -> return Bond.BONDING
        }
        return null
    }

    private fun isDevice(intent: Intent, device: BluetoothDevice): Boolean {
        val extraDevice = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
        return extraDevice != null && extraDevice.address.equals(device.address)
    }
}

enum class Bond {
    NONE, BONDING, BONDED;

    companion object {
        fun valueOf(value: Int): Bond? =
            when (value) {
                BluetoothDevice.BOND_NONE -> NONE
                BluetoothDevice.BOND_BONDING -> BONDING
                BluetoothDevice.BOND_BONDED -> BONDED
                else -> null
            }
    }
}
