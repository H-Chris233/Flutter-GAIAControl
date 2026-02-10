/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.util.Log
import retrofit2.Call
import retrofit2.Response

private const val TAG = "SingleRequest"

class SingleRequest<T>(
    private val call: Call<T?>?,
    private val onError: (ServiceError) -> Unit,
    private val onSuccess: (T) -> Unit,
    private val parseErrorId: (ServiceErrorResponse) -> ApiErrorType
) : Request({}) {

    override fun executeRequest() {
        if (call == null) {
            Log.e(TAG, "[executeRequest] call is null")
            onError(ServiceError.Internal.CallFailed)
            return
        }

        val response: Response<T?>

        try {
            response = call.execute()
        } catch (e: java.lang.Exception) {
            Log.e(TAG, "[executeRequest] exception occurred during call execution: ${e.message}")
            onError(ServiceError.RequestError.valueOf(e))
            return
        }

        parseResponse(response)
    }

    override fun onCancelled() {
        // unexpected
        call?.cancel()
    }

    private fun parseResponse(response: Response<T?>) {
        val code = response.code()

        if (response.isSuccessful) {
            if (code != 200) {
                Log.w(TAG, "[parseFeedbackResponse] Unexpected successful response code: expected=200, code=$code")
                onError(ServiceError.Internal.UnexpectedStatus(code))
                return
            }

            val body: T? = response.body()
            if (body == null) {
                Log.w(TAG, "[parseFeedbackResponse] responseBody is null")
                onError(ServiceError.ResponseError.EmptyResponse)
                return
            }

            return onSuccess(body)
        } else {
            onError(parseResponseError(code, response.errorBody(), parseErrorId))
        }
    }

}
