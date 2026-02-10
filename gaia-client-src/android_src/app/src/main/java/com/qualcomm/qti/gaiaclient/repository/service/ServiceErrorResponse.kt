/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.ResponseBody

data class ServiceErrorResponse(val status: String = "", val id: String = "", val message: String = "") {

    companion object {
        fun valueOf(errorBody: ResponseBody): ServiceErrorResponse {
            val gson = Gson()
            val type = object : TypeToken<ServiceErrorResponse>() {}.type
            return try {
                gson.fromJson(errorBody.charStream(), type) ?: ServiceErrorResponse()
            } catch (e: Exception) {
                Log.w("ServiceErrorResponse", "[ServiceErrorResponse->valueOf] Parsing JSON failed: ${e.message}")
                ServiceErrorResponse()
            }
        }
    }
}
