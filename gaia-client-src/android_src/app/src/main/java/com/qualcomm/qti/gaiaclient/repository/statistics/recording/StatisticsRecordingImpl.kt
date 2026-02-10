/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics.recording

import android.content.Context
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.StatisticID
import com.qualcomm.qti.gaiaclient.ui.settings.statistics.definitions.StatisticsCategories
import com.qualcomm.qti.gaiaclient.utils.FileUtils
import java.io.File
import java.text.DecimalFormat

class StatisticsRecordingImpl : StatisticsRecordingData() {

    private val MAX_FILE_AGE_MSECS: Double = 200000.0 // 60 * 60 * 24 * 1000 = 1 day
    private val MAX_LINES_IN_FILE: Int = 1000
    private val FILES_PATH = "statistics_recordings"

    override fun stopRecording(category: CategoryID): File? {
        if (isRecording(category)) {
            val file = fileForCategory(category) ?: return null
            removeFileEntry(category)
            return file
        }
        return null
    }

    override fun startRecording(context: Context, category: CategoryID): Boolean {
        if (isRecording(category)) {
            return false
        }

        eraseAllExpired(context)

        val categoryDescription = StatisticsCategories.fromIdentifier(category.value) ?: return false

        val filename = filename(category)
        val file = FileUtils.createFile(context, FILES_PATH, filename)

        if (!newFileEntry(category, file)) {
            return false
        }

        // Write title row

        val allStatistics = categoryDescription.allStatistics
        val values = mutableListOf<String>()
        values.add("Timestamp")

        allStatistics.forEach {
            val heading = it.getTitle(context)
            values.add(heading)
        }

        val row = values.joinToString(separator = ",", postfix = "\n")
        file.writeText(row)
        incrementLineCount(category)

        return true
    }

    override fun eraseAllExpired(context: Context) {
        val allFiles = FileUtils.listFilesInDirectory(context, FILES_PATH)

        allFiles.forEach {
            val fileAgeInMsecs = System.currentTimeMillis() - it.lastModified()
            if (fileAgeInMsecs > MAX_FILE_AGE_MSECS) {
                FileUtils.deleteFile(it)
            }
        }
    }

    override fun logRow(
        context: Context,
        category: CategoryID,
        statisticsValues: HashMap<StatisticID, ByteArray>
    ): Boolean {
        if (!isRecording(category)) {
            return false
        }

        val categoryDescription = StatisticsCategories.fromIdentifier(category.value) ?: return false
        val file = fileForCategory(category) ?: return false

        val allStatistics = categoryDescription.allStatistics
        val values = mutableListOf<String>()
        values.add(currentTimestamp())

        allStatistics.forEach {
            val bytes = statisticsValues[StatisticID(it.identifier)]
            values.add(
                if (bytes != null && bytes.isNotEmpty()) {
                    val stringValue = it.getValue(context, bytes) ?: ""
                    stringValue
                } else {
                    ""
                }
            )
        }

        val row = values.joinToString(separator = ",", postfix = "\n")
        file.appendText(row)
        val lineCount = incrementLineCount(category) ?: MAX_LINES_IN_FILE
        if (lineCount >= MAX_LINES_IN_FILE) {
            stopRecording(category)
        }
        return true
    }

    private fun convertToValidFileName(filename: String): String {
        return filename.toCharArray()
            .filter { it.isLetterOrDigit() || it == '.' || it == '-' }
            .joinToString(separator = "")
    }

    private fun filename(category: CategoryID): String {
        val categoryDescription = StatisticsCategories.fromIdentifier(category.value)
        val timeStamp = currentTimestamp()
        val filename = "$categoryDescription-$timeStamp"
        val normalized = convertToValidFileName(filename)
        return "$normalized.csv"
    }

    private fun currentTimestamp(): String {
        val df = DecimalFormat("#.##")
        val secs = System.currentTimeMillis() / 1000.0
        return df.format(secs)
    }
}
