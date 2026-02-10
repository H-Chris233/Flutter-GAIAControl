/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.app.Application
import android.content.Context
import android.text.Html
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.qualcomm.qti.gaiaclient.BuildConfig
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.InfoUtils
import com.qualcomm.qti.gaiaclient.repository.service.Result
import com.qualcomm.qti.gaiaclient.repository.service.ServiceError
import com.qualcomm.qti.gaiaclient.repository.service.ServiceRepository
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackError
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters
import com.qualcomm.qti.gaiaclient.repository.service.feedback.Issue
import com.qualcomm.qti.gaiaclient.utils.ifNullOrEmpty
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SendFeedbackViewModel @Inject constructor(
    application: Application, private val serviceRepository: ServiceRepository,
    private val infoRepository: DeviceInformationRepository
) :
    AndroidViewModel(application) {

    private val dataObserver = DataObserver()

    private var _issue: Issue? = null
    val link: String
        get() = _issue?.link ?: ""

    private val _progress: MutableLiveData<FeedbackStatus> = MutableLiveData()
    private val _parameters: MutableLiveData<FeedbackParameters> = MutableLiveData()
    private val _content: MutableLiveData<FeedbackContent> = MutableLiveData()
    private val _action: MutableLiveData<Action> = MutableLiveData()

    val progress: LiveData<FeedbackStatus> = _progress
    val content: LiveData<FeedbackContent> = _content
    val action: LiveData<Action> = _action

    override fun onCleared() {
        super.onCleared()
        dataObserver.stop()
    }

    fun setContent(form: FeedbackForm) {
        val parameters = form.buildParameters(
            applicationVersion = InfoUtils.readApplicationVersion(infoRepository).ifNullOrEmpty { "" },
            applicationBuildId = InfoUtils.readApplicationBuildId(infoRepository).ifNullOrEmpty { "" }
        )

        _parameters.value = parameters
        _content.value = FeedbackContent(parameters)
    }

    fun sendFeedback() {
        if (dataObserver.isRunning) {
            // unexpected
            return
        }

        val parameters: FeedbackParameters = _parameters.value ?: return
        val context: Context = getApplication()
        _progress.value = FeedbackStatus.Progress(context.getString(R.string.feedback_sending))
        dataObserver.start(serviceRepository.sendFeedback(parameters))
    }

    fun onClick(action: Action) {
        when (action) {
            is Action.CopyFeedback, is Action.CopyLink, is Action.Done -> _action.value = action
            is Action.TryAgain -> sendFeedback()
        }
    }

    private fun onResult(result: Result<Unit, Issue, ServiceError>?) {
        val context: Context = getApplication()
        when (result) {
            is Result.Progress -> _progress.value =
                FeedbackStatus.Progress(context.getString(R.string.feedback_sending))
            is Result.Success -> {
                val issue = result.data
                _issue = issue
                _progress.value = FeedbackStatus.Complete(getApplication(), issue)
            }
            is Result.Error -> {
                val error = result.error
                val message = getErrorMessage(error)
                _progress.value = FeedbackStatus.Error(
                    getApplication(),
                    message.first,
                    message.second,
                    error.toString()
                )
            }
            else -> {}
        }
    }

    private fun getErrorMessage(error: ServiceError): Pair<String, String> {
        val context: Context = getApplication()
        return when (error) {
            is ServiceError.RequestError.UnknownHost -> Pair(
                context.getString(R.string.feedback_error_host_not_found_title),
                context.getString(R.string.feedback_error_host_not_found_message)
            )
            is ServiceError.Internal, is ServiceError.RequestError.Exception,
            is ServiceError.ResponseError.UnexpectedFormat,
            is ServiceError.ResponseError.EmptyResponse ->
                Pair(
                    context.getString(R.string.feedback_error_internal_title),
                    context.getString(R.string.feedback_error_internal_message, BuildConfig.VERSION_NAME)
                )
            is ServiceError.RequestError.Timeout -> Pair(
                context.getString(R.string.feedback_error_timeout_title),
                context.getString(R.string.feedback_error_timeout_message)
            )
            is ServiceError.ResponseError.HtmlError -> Pair(
                context.getString(R.string.feedback_error_html_title, error.code.toString()),
                Html.fromHtml(error.content, Html.FROM_HTML_MODE_COMPACT).toString()
            )
            is ServiceError.ResponseError.ApiError -> when (error.error) {
                is FeedbackError.NoToken, is FeedbackError.InvalidToken -> Pair(
                    context.getString(R.string.feedback_error_internal_title),
                    context.getString(R.string.feedback_error_internal_message)
                )
                is FeedbackError.NoFeedback -> Pair(
                    context.getString(R.string.feedback_error_no_feedback_title),
                    context.getString(R.string.feedback_error_no_feedback_message)
                )
                is FeedbackError.ServerError -> Pair(
                    context.getString(R.string.feedback_error_server_title),
                    context.getString(R.string.feedback_error_server_message, BuildConfig.VERSION_NAME)
                )
                is FeedbackError.TrackingSystemUnavailable -> Pair(
                    context.getString(R.string.feedback_error_tracking_system_unavailable_title),
                    context.getString(R.string.feedback_tracking_system_unavailable_message)
                )
                else -> Pair(
                    context.getString(R.string.feedback_error_default_title),
                    context.getString(R.string.feedback_error_default_message)
                )
            }
        }
    }

    private inner class DataObserver {
        private val progressObserver = Observer<Result<Unit, Issue, ServiceError>> {
            if (it.isComplete) stop()
            this@SendFeedbackViewModel.onResult(it)
        }

        private var liveData: LiveData<Result<Unit, Issue, ServiceError>>? = null

        val isRunning: Boolean
            get() = liveData != null

        fun start(liveData: LiveData<Result<Unit, Issue, ServiceError>>) {
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
