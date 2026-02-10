/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.statistics.recording

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CategoryID
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import java.io.File
import java.util.concurrent.ConcurrentHashMap

abstract class StatisticsRecordingData : StatisticsRecording {
    protected class FileInfo(val fileHandle: File) {
        var lines: Int = 0
    }

    private var openFiles = ConcurrentHashMap<CategoryID, StickyLiveData<FileInfo>>()

    override fun isRecording(): Boolean {
        return openFiles.size > 0
    }

    override fun isRecording(category: CategoryID): Boolean {
        return openFiles.containsKey(category)
    }

    override fun stopAllRecording(): List<File> {
        val files = openFiles.mapNotNull {
            return@mapNotNull it.value.value?.fileHandle
        }
        openFiles = ConcurrentHashMap()
        return files
    }

    protected fun removeFileEntry(category: CategoryID): Boolean {
        if (isRecording(category)) {
            openFiles.remove(category)
            return true
        }
        return false
    }

    protected fun fileForCategory(category: CategoryID): File? {
        return getFile(category)
    }

    protected fun newFileEntry(category: CategoryID, file: File): Boolean {
        if (isRecording(category)) {
            return false
        }

        val entry = FileInfo(file)
        val liveData = StickyLiveData<FileInfo>(entry)
        openFiles[category] = liveData
        return true
    }

    protected fun incrementLineCount(category: CategoryID): Int? {
        val fileData = getFileData(category)
        val entry = fileData?.value
        if (entry != null) {
            entry.lines += 1
            fileData.postValue(entry)
            return entry.lines
        }
        return null
    }

    private fun getFileData(category: CategoryID): StickyLiveData<FileInfo>? {
        return openFiles[category]
    }

    private fun getFile(category: CategoryID): File? {
        val fileData = getFileData(category)
        return fileData?.value?.fileHandle
    }
}

