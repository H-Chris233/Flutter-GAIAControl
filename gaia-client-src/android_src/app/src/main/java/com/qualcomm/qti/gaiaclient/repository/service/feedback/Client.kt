/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.feedback

import android.os.Build
import com.qualcomm.qti.gaiaclient.BuildConfig

data class Client(
    val name: String = "GAIA Client",
    val appVersion: String = BuildConfig.VERSION_NAME,
    val system: String = "Android",
    val systemVersion: String = "Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT}",
    val device: String = if (Build.MODEL.startsWith(Build.MANUFACTURER)) Build.MODEL else "${Build.MANUFACTURER} ${Build.MODEL}"
)
