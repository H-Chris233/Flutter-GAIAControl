/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.files

import android.app.Application
import android.text.Html
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.BuildConfig
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.InfoUtils
import com.qualcomm.qti.gaiaclient.repository.service.Result
import com.qualcomm.qti.gaiaclient.repository.service.ServiceError
import com.qualcomm.qti.gaiaclient.repository.service.ServiceRepository
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.FileData
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesError
import com.qualcomm.qti.gaiaclient.ui.common.MessageData
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GetFilesViewModel @Inject constructor(
    application: Application,
    private val serviceRepository: ServiceRepository,
    private val informationRepository: DeviceInformationRepository
) :
    AndroidViewModel(application) {
    private val _files = StickyLiveData<List<FileViewData>>()
    val files: LiveData<List<FileViewData>> = _files

    private val _message = StickyLiveData<MessageData>()
    val message: LiveData<MessageData> = _message

    private val _isRefreshing = StickyLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    private var dataObserver: FilesDataObserver = FilesDataObserver()

    override fun onCleared() {
        super.onCleared()
        dataObserver.stop()
    }

    fun fetchFiles(filters: FilesFilters) {
        updateData(Result.Progress(Unit))
        val applicationVersion = informationRepository.versions.value?.application ?: ""
        val applicationBuildId = InfoUtils.readApplicationBuildId(informationRepository) ?: ""
        val parameters = filters.buildGetFilesParameters(applicationBuildId, applicationVersion)
        dataObserver.start(serviceRepository.getFiles(parameters))
    }

    private fun updateData(result: Result<Unit, List<FileData>, ServiceError>) {
        // update the visual helper on the refreshing state
        _isRefreshing.postValue(!result.isComplete)

        // get view data
        val items = if (result is Result.Success) result.data else emptyList()
        val message = getEmptyListMessage(items.isEmpty(), result)

        // notify data observers
        _files.postValue(items.map { FileViewData(it) }.toList())
        _message.postValue(message)
    }

    private fun getEmptyListMessage(
        isListEmpty: Boolean,
        result: Result<Unit, List<FileData>, ServiceError>
    ): MessageData {
        val show = isListEmpty || result is Result.Error

        val title = getString(
            when (result) {
                is Result.Progress -> R.string.get_files_in_progress_title
                is Result.Error, is Result.Success -> R.string.get_files_empty_title
            }
        )

        val message: String =
            when (result) {
                is Result.Progress -> getString(R.string.get_files_in_progress_message)
                is Result.Success -> getString(
                    if (isListEmpty) R.string.get_files_no_match else R.string.get_files_error_default
                )
                is Result.Error -> getErrorMessage(result.error)
            }

        val refs: String? = when (result) {
            is Result.Error -> result.error.toString()
            else -> null
        }

        return MessageData(show, title, message, refs)
    }

    private fun getErrorMessage(error: ServiceError): String {
        return when (error) {
            is ServiceError.RequestError.UnknownHost -> getString(R.string.get_files_error_host_not_found)
            is ServiceError.Internal, is ServiceError.RequestError.Exception,
            is ServiceError.ResponseError.UnexpectedFormat,
            is ServiceError.ResponseError.EmptyResponse -> getApplication<Application>().getString(
                    R.string.get_files_error_not_recoverable, BuildConfig.VERSION_NAME
                )
            is ServiceError.RequestError.Timeout -> getString(R.string.get_files_error_timeout)
            is ServiceError.ResponseError.HtmlError ->
                Html.fromHtml(error.content, Html.FROM_HTML_MODE_COMPACT).toString()
            is ServiceError.ResponseError.ApiError -> when (error.error) {
                is GetFilesError.NoToken, is GetFilesError.InvalidToken -> getString(R.string.get_files_error_not_recoverable)
                is GetFilesError.NoId -> getString(R.string.get_files_error_no_id)
                is GetFilesError.UnableToResolveId -> getString(R.string.get_files_error_unable_to_resolve_id)
                is GetFilesError.ServerError -> getString(R.string.get_files_error_server_error)
                is GetFilesError.ServiceNotAvailable -> getString(R.string.get_files_error_service_unavailable)
                is GetFilesError.UnidentifiedError -> getString(R.string.get_files_error_default)
                else -> getString(R.string.get_files_error_default)
            }
        }
    }

    private fun getString(resourceId: Int): String {
        val application = getApplication<Application>()
        return application.getString(resourceId)
    }

    private inner class FilesDataObserver {
        private val observer = Observer<Result<Unit, List<FileData>, ServiceError>> {
            if (it.isComplete) stop()
            updateData(it)
        }

        private var liveData: LiveData<Result<Unit, List<FileData>, ServiceError>>? = null

        fun start(liveData: LiveData<Result<Unit, List<FileData>, ServiceError>>) {
            this.liveData?.removeObserver(observer)
            liveData.observeForever(observer)
            this.liveData = liveData
        }

        fun stop() {
            liveData?.removeObserver(observer)
            liveData = null
        }
    }
}
