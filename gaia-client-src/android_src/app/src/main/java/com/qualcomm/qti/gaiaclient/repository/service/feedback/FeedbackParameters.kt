/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.feedback

data class FeedbackParameters(
    val title: String,
    val description: String,
    val reporter: String? = null,
    val client: Client = Client(),
    val device: Device
)
