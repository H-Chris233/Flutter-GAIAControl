/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackResponse
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesParameters
import com.qualcomm.qti.gaiaclient.repository.service.getfiles.GetFilesResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.http.Streaming

interface Service {
    @Headers("Content-Type: application/json", "Accept: application/json")
    @HTTP(method = "POST", path = "builds", hasBody = true)
    fun getFiles(@Query("token") token: String, @Body parameters: GetFilesParameters): Call<GetFilesResponse?>?

    @Streaming
    @GET("download")
    fun downloadFile(@Query("token") token: String, @Query("id") id: String): Call<ResponseBody?>?

    @Headers("Content-Type: application/json", "Accept: application/json")
    @HTTP(method = "POST", path = "feedback", hasBody = true)
    fun sendFeedback(@Query("token") token: String, @Body parameters: FeedbackParameters): Call<FeedbackResponse?>?
}
