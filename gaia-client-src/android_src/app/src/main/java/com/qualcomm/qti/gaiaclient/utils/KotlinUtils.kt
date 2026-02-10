/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.utils

fun String?.ifNullOrEmpty(defaultValue: () -> String): String =
    if (this.isNullOrBlank()) defaultValue() else this
