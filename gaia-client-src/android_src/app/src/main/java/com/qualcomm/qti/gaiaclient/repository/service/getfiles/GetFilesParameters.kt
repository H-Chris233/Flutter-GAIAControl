/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.getfiles

data class GetFilesParameters(
    val id: String? = null,
    val applicationVersion: String? = null,
    val hardwareVersion: String? = null,
    val filter: Filter? = null
)
