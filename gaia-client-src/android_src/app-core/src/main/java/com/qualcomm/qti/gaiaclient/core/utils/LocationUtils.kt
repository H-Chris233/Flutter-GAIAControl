/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.utils

import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings

object LocationUtils {
    @JvmStatic
    fun isLocationEnabled(context: Context): Boolean {
        return if (Build.VERSION_CODES.P <= Build.VERSION.SDK_INT) {
            val manager = context.getSystemService(LocationManager::class.java)
            manager != null && manager.isLocationEnabled
        } else {
            val mode = Settings.Secure.getInt(
                context.contentResolver, Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }
}
