/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.data;

/**
 * <p>The errors that can happen with a RFCOMM Bluetooth connection.</p>
 */
public enum BluetoothStatus {

    /**
     * Occurs when attempting to use a Bluetooth device that cannot be found.
     */
    DEVICE_NOT_FOUND,
    /**
     * <p>Occurs when connecting to the device failed.</p>
     */
    CONNECTION_FAILED,
    /**
     * <p>Occurs when the connection to a device is lost.</p>
     */
    CONNECTION_LOST,
    /**
     * <p>Occurs when trying to connect a device that is already connected.</p>
     */
    ALREADY_CONNECTED,
    /**
     * <p>Occurs when attempting to use a {@link android.bluetooth.BluetoothDevice
     * BluetoothDevice} that is not of type
     * {@link android.bluetooth.BluetoothDevice#DEVICE_TYPE_CLASSIC DEVICE_TYPE_CLASSIC} or
     * {@link android.bluetooth.BluetoothDevice#DEVICE_TYPE_DUAL DEVICE_TYPE_DUAL}.</p>
     */
    DEVICE_NOT_COMPATIBLE,
    /**
     * <p>Occurs when no transport supported by both a device and the application can be found.</p>
     */
    NO_TRANSPORT_FOUND,
    /**
     * <p>Occurs for asynchronous requests that are in process.</p>
     */
    IN_PROGRESS,
    /**
     * <p>Occurs when the Bluetooth of the Android device is not accessible. This can be because
     * the app is not allowed to use Bluetooth, the Bluetooth is off or the Android device does
     * not support Bluetooth.</p>
     */
    NO_BLUETOOTH,
    /**
     * <p>Occurs when a connection request times out: fetching UUIDs, connection, etc.</p>
     */
    TIME_OUT,
    /**
     * <p>Occurs when failing to start the discovery of available devices.</p>
     */
    DISCOVERY_FAILED,
    /**
     * <p>when attempts to reconnect have continuously failed over a defined time.</p>
     */
    RECONNECTION_TIME_OUT,
    /**
     * <p>Occurs when the Location services is either unavailable or disabled. Some Bluetooth features requires
     * the location services to be on.</p>
     */
    NO_LOCATION,
    /**
     * <p>Occurs when the permissions required for some actions - such as location for scanning for devices - have not
     * be granted to the application.</p>
     */
    NO_PERMISSIONS,
    /**
     * <p>Occurs when a request cannot be carried over due to an expected state.</p>
     */
    INCORRECT_STATE,
    /**
     * <p>Occurs when the connection fails due to pairing.</p>
     */
    PAIRING_FAILED,
    /**
     * Occurs when a Bluetooth operation could not be executed.
     */
    OPERATION_FAILED
}
