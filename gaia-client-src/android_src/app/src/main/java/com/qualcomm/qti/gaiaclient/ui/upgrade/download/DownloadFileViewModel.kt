/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.download

import android.app.Application
import android.content.Context
import android.net.Uri
import android.text.Html
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.BuildConfig
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.repository.service.Result
import com.qualcomm.qti.gaiaclient.repository.service.ServiceError
import com.qualcomm.qti.gaiaclient.repository.service.ServiceRepository
import com.qualcomm.qti.gaiaclient.repository.service.download.DownloadFileError
import com.qualcomm.qti.gaiaclient.repository.service.download.DownloadProgress
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeOptions
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    application: Application, private val serviceRepository: ServiceRepository,
    private val upgradeRepository: UpgradeRepository
) : AndroidViewModel(application) {

    private val dataObserver: DownloadFileObserver = DownloadFileObserver()

    private lateinit var fileData: FileData

    private lateinit var uri: Uri
    private lateinit var upgradeOptions: UpgradeOptions

    private val _progress: MutableLiveData<DownloadStatus> = MutableLiveData()
    private val _content: MutableLiveData<FileContent> = MutableLiveData()
    private val _action: MutableLiveData<Action> = MutableLiveData()

    val progress: LiveData<DownloadStatus> = _progress
    val content: LiveData<FileContent> = _content
    val action: LiveData<Action> = _action

    override fun onCleared() {
        super.onCleared()
        cancelDownload()
    }

    fun setData(file: FileData, options: UpgradeOptions) {
        if (!::fileData.isInitialized) {
            this.fileData = file
        }
        if (!::upgradeOptions.isInitialized) {
            this.upgradeOptions = options
        }

        _content.value = FileContent(getApplication(), fileData)
    }

    fun downloadFile() {
        val context: Context = getApplication()

        if (dataObserver.isRunning) {
            // unexpected
            return
        }

        _progress.value =
            DownloadStatus.Progress(context.getString(R.string.download_file_state_init), 0)

        dataObserver.start(serviceRepository.downloadFile(getApplication(), fileData))
    }

    fun cancelDownload() {
        serviceRepository.cancelDownload()
        dataObserver.stop()
    }

    fun onClick(action: Action) {
        when (action) {
            is Action.StartUpgrade -> {
                if (::uri.isInitialized) {
                    upgradeRepository.startUpgrade(getApplication(), uri, upgradeOptions)
                    _action.value = action
                } else {
                    // unexpected
                    return
                }
            }
            is Action.TryAgain -> {
                downloadFile()
            }
        }
    }

    private fun onResult(result: Result<DownloadProgress, Uri, ServiceError>?) {
        val context: Context = getApplication()
        when (result) {
            is Result.Progress -> {
                val title = context.getString(R.string.download_file_state_downloading)
                val progress = result.progress.progress
                _progress.value = DownloadStatus.Progress(title, progress)
            }
            is Result.Success -> {
                val title = context.getString(R.string.download_file_state_file_ready)
                uri = result.data
                _progress.value = DownloadStatus.Complete(getApplication(), title)
            }
            is Result.Error -> {
                val error = result.error
                val message = getErrorMessage(error)
                _progress.value =
                    DownloadStatus.Error(getApplication(), message.first, message.second, error.toString())
            }
            else -> {}
        }
    }

    private fun getErrorMessage(error: ServiceError): Pair<String, String> {
        val context: Context = getApplication()
        return when (error) {
            is ServiceError.RequestError.UnknownHost -> Pair(
                context.getString(R.string.download_file_error_host_not_found_title),
                context.getString(R.string.download_file_error_host_not_found_message)
            )
            is ServiceError.Internal, is ServiceError.RequestError.Exception,
            is ServiceError.ResponseError.UnexpectedFormat,
            is ServiceError.ResponseError.EmptyResponse -> Pair(
                context.getString(R.string.download_file_error_internal_title),
                context.getString(R.string.download_file_error_internal_message, BuildConfig.VERSION_NAME)
            )
            is ServiceError.RequestError.Timeout -> Pair(
                context.getString(R.string.download_file_error_timeout_title),
                context.getString(R.string.download_file_error_timeout_message)
            )
            is ServiceError.ResponseError.HtmlError -> Pair(
                context.getString(R.string.download_file_error_html_title, error.code.toString()),
                Html.fromHtml(error.content, Html.FROM_HTML_MODE_COMPACT).toString()
            )
            is ServiceError.ResponseError.ApiError -> when (error.error) {
                is DownloadFileError.NoToken, is DownloadFileError.InvalidToken -> Pair(
                    context.getString(R.string.download_file_error_internal_title),
                    context.getString(R.string.download_file_error_internal_message, BuildConfig.VERSION_NAME)
                )
                is DownloadFileError.NoId -> Pair(
                    context.getString(R.string.download_file_error_no_id_title),
                    context.getString(R.string.download_file__error_no_id_message, BuildConfig.VERSION_NAME)
                )
                is DownloadFileError.FileNotFound -> Pair(
                    context.getString(R.string.download_file_error_not_found_title),
                    context.getString(R.string.download_file_error_not_found_message)
                )
                is DownloadFileError.ServerError -> Pair(
                    context.getString(R.string.download_file_error_server_title),
                    context.getString(R.string.download_file_error_server_message, BuildConfig.VERSION_NAME)
                )
                is DownloadFileError.ServiceNotAvailable -> Pair(
                    context.getString(R.string.download_file_error_service_unavailable_title),
                    context.getString(R.string.download_file_error_service_unavailable_message)
                )
                is DownloadFileError.UnidentifiedError -> Pair(
                    context.getString(R.string.download_file_error_default_title),
                    context.getString(R.string.download_file_error_default_message)
                )
                else -> Pair(
                    context.getString(R.string.download_file_error_default_title),
                    context.getString(R.string.download_file_error_default_message)
                )
            }
        }
    }

    private inner class DownloadFileObserver {
        private val progressObserver = Observer<Result<DownloadProgress, Uri, ServiceError>> {
            if (it.isComplete) stop()
            this@DownloadViewModel.onResult(it)
        }

        private var liveData: LiveData<Result<DownloadProgress, Uri, ServiceError>>? = null

        val isRunning: Boolean
            get() = liveData != null

        fun start(liveData: LiveData<Result<DownloadProgress, Uri, ServiceError>>) {
            this.liveData?.removeObserver(progressObserver)
            liveData.observeForever(progressObserver)
            this.liveData = liveData
        }

        fun stop() {
            liveData?.removeObserver(progressObserver)
            liveData = null
        }
    }
}
