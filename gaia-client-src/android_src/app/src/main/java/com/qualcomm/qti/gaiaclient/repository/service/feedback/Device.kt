/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.feedback

data class Device(
    val applicationVersion: String = "",
    val applicationBuildId: String = "",
    val hardwareVersion: String? = null,
    val logs: String? = null
)
