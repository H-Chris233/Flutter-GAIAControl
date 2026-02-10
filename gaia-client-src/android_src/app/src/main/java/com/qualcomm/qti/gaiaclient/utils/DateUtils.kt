/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val DATE_ISO_8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX"
const val FILE_DATE_PATTERN = "EEE, d MMM yyyy HH:mm:ss"

fun String?.parseIsoDate(): Date? {
    this ?: return null

    return try {
        SimpleDateFormat(DATE_ISO_8601_PATTERN, Locale.getDefault()).parse(this)
    } catch (_: java.lang.Exception) {
        null
    }
}

fun Date?.format(pattern: String): String? =
    if (this == null) null else SimpleDateFormat(pattern, Locale.getDefault()).format(this)
