/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import com.qualcomm.qti.gaiaclient.repository.service.download.DownloadProgress
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters
import com.qualcomm.qti.gaiaclient.repository.service.feedback.Issue
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesParameters

interface ServiceRepository {
    fun init() {}
    fun reset() {}

    fun sendFeedback(parameters: FeedbackParameters): LiveData<Result<Unit, Issue, ServiceError>>

    fun getFiles(parameters: GetFilesParameters): LiveData<Result<Unit, List<FileData>, ServiceError>>

    fun downloadFile(
        context: Context,
        fileData: FileData
    ): LiveData<Result<DownloadProgress, Uri, ServiceError>>

    fun cancelDownload()
}
