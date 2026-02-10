/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common

import android.content.Context

interface Settings {
    val resourceId: Int

    fun getId(context: Context?): String {
        return context?.getString(resourceId) ?: ""
    }
}
