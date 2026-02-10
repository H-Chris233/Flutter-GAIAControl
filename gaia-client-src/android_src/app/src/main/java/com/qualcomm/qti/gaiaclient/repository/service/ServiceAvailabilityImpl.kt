/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.content.Context
import com.qualcomm.qti.gaiaclient.BuildConfig

object ServiceAvailabilityImpl : ServiceAvailability {
    override val exists: Boolean = BuildConfig.SERVICE_BASE_URL.isNotEmpty()
    override fun isAvailable(context: Context?): Boolean = BuildConfig.SERVICE_AVAILABILITY
}
