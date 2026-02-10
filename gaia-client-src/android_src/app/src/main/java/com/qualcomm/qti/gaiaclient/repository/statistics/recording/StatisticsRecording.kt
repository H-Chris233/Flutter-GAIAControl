/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics.recording

import android.content.Context
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticID
import java.io.File

interface StatisticsRecording {
    fun isRecording(): Boolean
    fun isRecording(category: CategoryID): Boolean
    fun startRecording(context: Context, category: CategoryID): Boolean
    fun stopRecording(category: CategoryID): File?
    fun stopAllRecording(): List<File>

    fun eraseAllExpired(context: Context)

    fun logRow(context: Context,
               category: CategoryID,
               statisticsValues: HashMap<StatisticID, ByteArray>): Boolean
}
