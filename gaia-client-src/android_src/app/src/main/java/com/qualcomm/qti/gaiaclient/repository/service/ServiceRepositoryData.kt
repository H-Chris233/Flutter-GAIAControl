/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.qualcomm.qti.gaiaclient.repository.service.download.DownloadProgress
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters
import com.qualcomm.qti.gaiaclient.repository.service.feedback.Issue
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesParameters
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import com.qualcomm.qti.gaiaclient.utils.FileUtils
import java.io.File

private const val TAG = "DownloadRepositoryData"

abstract class ServiceRepositoryData protected constructor() : ServiceRepository {

    private val handler = Handler(Looper.getMainLooper())

    abstract fun executeSendFeedback(
        liveData: MutableLiveData<Result<Unit, Issue, ServiceError>>,
        parameters: FeedbackParameters
    )

    final override fun sendFeedback(parameters: FeedbackParameters): LiveData<Result<Unit, Issue, ServiceError>> {
        val liveData: StickyLiveData<Result<Unit, Issue, ServiceError>> = StickyLiveData()
        liveData.postValue(Result.Progress(Unit))
        executeSendFeedback(liveData, parameters)
        return liveData
    }

    final override fun getFiles(parameters: GetFilesParameters): LiveData<Result<Unit, List<FileData>, ServiceError>> {
        val liveData: StickyLiveData<Result<Unit, List<FileData>, ServiceError>> = StickyLiveData()
        liveData.postValue(Result.Progress(Unit))
        executeGetFiles(liveData, parameters)
        return liveData
    }

    abstract fun executeGetFiles(
        liveData: MutableLiveData<Result<Unit, List<FileData>, ServiceError>>,
        parameters: GetFilesParameters
    )

    final override fun downloadFile(context: Context, fileData: FileData)
            : LiveData<Result<DownloadProgress, Uri, ServiceError>> {
        val liveData: StickyLiveData<Result<DownloadProgress, Uri, ServiceError>> = StickyLiveData()
        liveData.postValue(Result.Progress(DownloadProgress(0)))
        handler.post { startDownloadFile(context, fileData, liveData) }
        return liveData
    }

    abstract fun executeDownloadFile(
        liveData: MutableLiveData<Result<DownloadProgress, Uri, ServiceError>>,
        downloadId: String, file: File, uri: Uri
    )

    private fun startDownloadFile(
        context: Context, fileData: FileData,
        liveData: MutableLiveData<Result<DownloadProgress, Uri, ServiceError>>
    ) {
        // creating the file
        val file = FileUtils.createFile(context, FILES_PATH, FILE_NAME)
        val uri = FileUtils.getUriFromFile(context, file, FileUtils.FILE_PROVIDER_AUTHORITY)

        // checking file and uri
        if (file == null) {
            // no permission required for app storage => internal error
            Log.w(TAG, "[startDownloadFile] cannot create file: file is null")
            liveData.value = Result.Error(ServiceError.Internal.FileError)
            return
        }
        if (uri == null) {
            // no permission required for app storage => internal error
            Log.w(TAG, "[startDownloadFile] cannot process: uri is null")
            liveData.value = Result.Error(ServiceError.Internal.UriError)
            return
        }

        if (file.exists()) {
            file.delete()
        }

        // file is ok -> download can start
        executeDownloadFile(liveData, fileData.id, file, uri)
    }

    companion object {
        private const val FILES_PATH = "upgrade_files"
        private const val FILE_NAME = "upgrade_file.bin" // always the same name => minor internal storage load
    }
}
