/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.util.Log
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import okhttp3.ResponseBody
import java.util.concurrent.atomic.AtomicBoolean

abstract class Request(private val onComplete: () -> Unit) {

    private val cancelled = AtomicBoolean(false)

    fun execute() {
        GaiaClientService.getTaskManager().runInBackground {
            if (!cancelled.get()) {
                executeRequest()
            }
            onComplete()
        }
    }

    protected abstract fun executeRequest()

    fun cancel() {
        cancelled.set(true)
        onCancelled()
    }

    protected abstract fun onCancelled()

    fun parseResponseError(
        code: Int,
        errorBody: ResponseBody?,
        parseErrorId: (ServiceErrorResponse) -> ApiErrorType
    ): ServiceError {
        if (errorBody == null) {
            Log.w(javaClass.simpleName, "[onResponseError] error is null")
            return ServiceError.ResponseError.EmptyResponse
        }

        return when (errorBody.contentType().toString().lowercase()) {
            "text/html" -> ServiceError.ResponseError.HtmlError(code, errorBody.string())
            "application/json" -> {
                val errorResponse = ServiceErrorResponse.valueOf(errorBody)
                ServiceError.ResponseError.ApiError(parseErrorId(errorResponse))
            }
            else -> ServiceError.ResponseError.UnexpectedFormat
        }
    }
}
