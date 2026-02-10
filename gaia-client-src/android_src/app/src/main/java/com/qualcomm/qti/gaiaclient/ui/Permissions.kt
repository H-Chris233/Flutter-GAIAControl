/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.StringRes
import androidx.core.app.ActivityCompat
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.utils.ACTIONS

enum class Permissions(
    val id: String,
    val type: PermissionType,
    val supported: Boolean
) {
    ACCESS_COARSE_LOCATION(
        Manifest.permission.ACCESS_COARSE_LOCATION, PermissionType.RUNTIME,
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.P
    ),
    ACCESS_FINE_LOCATION(
        Manifest.permission.ACCESS_FINE_LOCATION, PermissionType.RUNTIME,
        Build.VERSION_CODES.Q <= Build.VERSION.SDK_INT && Build.VERSION.SDK_INT <= Build.VERSION_CODES.R
    ),
    BLUETOOTH(
        Manifest.permission.BLUETOOTH, PermissionType.NORMAL,
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.R
    ),
    BLUETOOTH_ADMIN(
        Manifest.permission.BLUETOOTH_ADMIN, PermissionType.NORMAL,
        Build.VERSION.SDK_INT <= Build.VERSION_CODES.R
    ),
    BLUETOOTH_SCAN(
        Manifest.permission.BLUETOOTH_SCAN, PermissionType.RUNTIME,
        Build.VERSION_CODES.S <= Build.VERSION.SDK_INT
    ),
    BLUETOOTH_CONNECT(
        Manifest.permission.BLUETOOTH_CONNECT, PermissionType.RUNTIME,
        Build.VERSION_CODES.S <= Build.VERSION.SDK_INT
    );

    val isRuntime: Boolean = type == PermissionType.RUNTIME

    fun isGranted(context: Context): Boolean =
        ActivityCompat.checkSelfPermission(context, id) == PackageManager.PERMISSION_GRANTED

    enum class Categories(
        val permissions: Array<Permissions>, val requestCode: Int,
        @get:StringRes val rationaleMessage: Int,
        @get:StringRes val errorMessage: Int
    ) {
        /**
         * This category only contains one runtime permission at a time depending on Android SDK version:
         *    - SDK <= 28 (P): ACCESS_COARSE_LOCATION
         *    - 29 (Q) <= SDK <= 30 (R): ACCESS_FINE_LOCATION
         *    - 31 (S) <= SDK: BLUETOOTH_SCAN
         */
        BLUETOOTH_DISCOVERY(
            arrayOf(ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, BLUETOOTH_SCAN).filter { it.supported }
                .toTypedArray(),
            ACTIONS.PERMISSIONS.REQUEST_CATEGORY_BLUETOOTH_DISCOVERY,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                R.string.alert_bluetooth_discovery_location_permissions_message
            else R.string.alert_bluetooth_discovery_permissions_message,
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
                R.string.error_bluetooth_discovery_location_permissions_message
            else R.string.error_bluetooth_discovery_permissions_message,
        ),

        /**
         * This category is as follows:
         *    - SDK <= 30 (R): normal permissions, BLUETOOTH, BLUETOOTH_ADMIN
         *    - 31 (S) <= SDK: runtime permission, BLUETOOTH_SCAN
         */
        BLUETOOTH(
            arrayOf(Permissions.BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_CONNECT).filter { it.supported }
                .toTypedArray(),
            ACTIONS.PERMISSIONS.REQUEST_CATEGORY_BLUETOOTH_CONNECTING,
            R.string.alert_bluetooth_permissions_message,
            R.string.error_bluetooth_permissions_message
        );

        val isRuntime: Boolean = permissions.any { it.isRuntime }

        val ids: Array<String> = permissions.map { it.id }.toTypedArray()

        fun isGranted(context: Context): Boolean {
            for (permission in permissions) {
                if (!permission.isGranted(context)) {
                    return false
                }
            }
            return true
        }

        fun contains(id: String): Boolean = ids.contains(id)

        companion object {
            @JvmStatic
            fun valueFrom(requestCode: Int): Categories? = values().find { it.requestCode == requestCode }
        }
    }
}

/**
 * Android categorizes permissions into different types. Each permission's type indicates the scope of restricted data
 * that the app can access, the scope of restricted actions that the app can perform.
 * Other types exist but are not used by this app.
 */
enum class PermissionType {
    /**
     * These permissions allow access to data and actions that extend beyond the app's sandbox.
     * The data and actions present very little risk to the user's privacy, and the operation of other apps.
     */
    NORMAL,

    /**
     * Runtime permissions, also known as dangerous permissions, give the app additional access to restricted data, and
     * allow the app to perform restricted actions that more substantially affect the system and other apps.
     * These permissions need to be requested at runtime before accessing the restricted data or performing
     * restricted actions.
     */
    RUNTIME
}

/**
 * The grant states of a permission.
 */
enum class PermissionState {
    /**
     * The user has granted the permission.
     */
    GRANTED,

    /**
     * The user has denied the permission once.
     */
    DENIED,

    /**
     * The user has denied the permission more than once.
     */
    DENIED_PERMANENTLY
}
