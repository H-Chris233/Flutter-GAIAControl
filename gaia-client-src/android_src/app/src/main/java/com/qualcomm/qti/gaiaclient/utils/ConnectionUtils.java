/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.utils;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;

/**
 * <p>An util class to match the different states related to a connection to a displayable label
 * .</p>
 */
public final class ConnectionUtils {

    public static String getLabel(Context context, ConnectionState state) {
        return context == null || state == null ? null : context.getString(getResourceID(state));
    }

    private static int getResourceID(ConnectionState state) {
        switch (state) {
            case CONNECTING:
                return R.string.connecting;
            case CONNECTED:
                return R.string.connected;
            case DISCONNECTING:
                return R.string.disconnecting;
            case DISCONNECTED:
            default:
                return R.string.disconnected;
        }
    }

    public static String getLabel(Context context, BluetoothStatus status) {
        return context == null || status == null ? null : context.getString(getResourceID(status));
    }

    private static int getResourceID(BluetoothStatus status) {
        switch (status) {
            case CONNECTION_LOST:
                return R.string.connection_fail_lost;
            case DEVICE_NOT_COMPATIBLE:
                return R.string.connection_fail_device_not_compatible;
            case NO_TRANSPORT_FOUND:
                return R.string.connection_fail_transport_not_found;
            case DEVICE_NOT_FOUND:
                return R.string.connection_fail_device_not_found;
            case NO_BLUETOOTH:
                return R.string.connection_fail_no_bluetooth;
            case TIME_OUT:
                return R.string.connection_fail_time_out;
            case PAIRING_FAILED:
                return R.string.connection_fail_pairing;
            case ALREADY_CONNECTED: // unexpected
            case DISCOVERY_FAILED: // unexpected
            case IN_PROGRESS: // unexpected
            case RECONNECTION_TIME_OUT: // unexpected
            case NO_LOCATION: // unexpected
            case NO_PERMISSIONS: // unexpected
            case CONNECTION_FAILED:
            default:
                return R.string.connection_fail_failed;
        }
    }

}
