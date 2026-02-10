/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins

import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.AudioCurationPublisher
import com.qualcomm.qti.gaiaclient.core.data.ACInfo

interface AudioCurationPlugin {
    val audioCurationPublisher: AudioCurationPublisher // to force the plugin to implement AudioCurationPublisher
    fun fetchInfo(info: ACInfo): Boolean
    fun fetchInfo(info: ACInfo, parameter: Any): Boolean
    fun setInfo(info: ACInfo, value: Any): Boolean
}
