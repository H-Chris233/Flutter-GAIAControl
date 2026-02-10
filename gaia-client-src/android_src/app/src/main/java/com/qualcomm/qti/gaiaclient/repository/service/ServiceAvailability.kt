/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.content.Context

interface ServiceAvailability {
    val exists: Boolean
    fun isAvailable(context: Context?): Boolean
}
