/*
 * ************************************************************************************************
 * * © 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.os.Build
import android.util.Log

object BluetoothUtils {
    private const val TAG = "BluetoothUtils"

    @JvmStatic
    fun getBluetoothAdapter(context: Context?): BluetoothAdapter? {
        if (context == null || context.applicationContext == null) {
            Log.i(
                TAG, "[getBluetoothAdapter] context is null, BluetoothAdapter will be initialised with " +
                        "BluetoothAdapter.getDefaultAdapter()"
            )
        }

        val manager: BluetoothManager? = context?.applicationContext?.getSystemService(BluetoothManager::class.java)
        val adapter: BluetoothAdapter? = manager?.adapter

        if (adapter != null) {
            return adapter
        }

        val message: String = if (context == null || context.applicationContext == null) "context is null"
        else if (manager == null) "BluetoothManager is null"
        else "BluetoothManager.adapter is null"

        Log.i(
            TAG, "[getBluetoothAdapter] $message, BluetoothAdapter to be initialised with " +
                    "BluetoothAdapter.getDefaultAdapter()"
        )

        return manager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    @JvmStatic
    fun isBluetoothEnabled(context: Context?): Boolean {
        val adapter = getBluetoothAdapter(context)
        return adapter != null && adapter.isEnabled
    }

    /**
     *
     * This method requests the [BluetoothDevice] that corresponds to
     * the given Bluetooth address from the system.
     *
     * @param adapter
     * The BluetoothAdapter to use to request Bluetooth resources from the system.
     * @param address
     * The Bluetooth address to find the corresponding [BluetoothDevice] for.
     *
     * @return The corresponding [BluetoothDevice] or `null` if
     * it could not be found.
     */
    @JvmStatic
    fun findBluetoothDevice(adapter: BluetoothAdapter, address: String): BluetoothDevice? {
        if (address.isEmpty()) {
            Log.w(TAG, "[findBluetoothDevice] Bluetooth address null or empty.")
            return null
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            Log.w(TAG, "[findBluetoothDevice] Unknown BT address.")
            return null
        }
        val device = adapter.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "[findBluetoothDevice] No corresponding remote device.")
            return null
        }
        return device
    }

    @JvmStatic
    fun areScanningPermissionsGranted(context: Context): Boolean {
        val permissions = when {
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.P -> arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
            )
            Build.VERSION.SDK_INT <= Build.VERSION_CODES.R -> arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN
            )
            else -> arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        }
        return SystemUtils.arePermissionsGranted(context, permissions)
    }

    @SuppressLint("MissingPermission")
    @JvmStatic
    fun getDeviceName(device: BluetoothDevice): String {
        return when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> device.alias ?: device.name
            else -> device.name
        } ?: ""
    }
}
