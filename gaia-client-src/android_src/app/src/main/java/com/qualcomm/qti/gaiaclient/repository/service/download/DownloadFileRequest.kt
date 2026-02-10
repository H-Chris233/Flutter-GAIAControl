/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.download

import android.util.Log
import com.qualcomm.qti.gaiaclient.repository.service.Request
import com.qualcomm.qti.gaiaclient.repository.service.ServiceError
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

private const val TAG = "DownloadFileRequest"
private const val INITIAL_PROGRESS = 1L

class DownloadFileRequest(
    private val call: Call<ResponseBody?>?, private val file: File,
    private val onProgress: (Int) -> Unit,
    private val onError: (ServiceError) -> Unit,
    private val onSuccess: () -> Unit,
    onComplete: () -> Unit
) : Request(onComplete) {
    // must run on background thread
    override fun executeRequest() {
        if (call == null) {
            Log.w(TAG, "[executeRequest] call is null")
            onError(ServiceError.Internal.CallFailed)
            return
        }

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                this@DownloadFileRequest.onResponse(response)
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                this@DownloadFileRequest.onFailure(t)
            }
        })
    }

    private fun onResponse(response: Response<ResponseBody?>) {
        onProgress(INITIAL_PROGRESS.toInt())
        val code = response.code()

        if (response.isSuccessful) {
            if (code != 200) {
                Log.w(TAG, "[onResponse] Unexpected successful response code: expected=200, code=$code")
                onError(ServiceError.Internal.UnexpectedStatus(code))
                return
            }

            val responseBody = response.body()
            if (responseBody == null) {
                Log.w(TAG, "[onResponse] responseBody is null")
                onError(ServiceError.ResponseError.EmptyResponse)
                return
            }

            readDownloadFileResponse(responseBody, file)
        } else {
            onError(parseResponseError(code, response.errorBody()) { DownloadFileError.valueOf(it) })
        }
    }

    private fun onFailure(t: Throwable) {
        Log.w(TAG, "[onFailure] exception: ${t.message}")
        onError(ServiceError.RequestError.valueOf(t))
    }

    private fun readDownloadFileResponse(body: ResponseBody, file: File) {
        try {
            body.byteStream().use { inputStream ->
                file.outputStream().use { outputStream ->
                    val buffer = ByteArray(32768)
                    val fileSize = body.contentLength()
                    var offset = 0
                    var read = 0

                    while (read > -1 && offset.toLong() < fileSize) {
                        // read bytes
                        read = inputStream.read(buffer)
                        outputStream.write(buffer, 0, read)
                        offset += read

                        // update progress
                        val progress = INITIAL_PROGRESS + ((100L - INITIAL_PROGRESS) * offset.toLong() / fileSize)
                        if (progress > 1) {
                            onProgress(progress.toInt())
                        }
                    }

                    when {
                        offset.toLong() < fileSize -> throw FileTooShortException()
                        offset.toLong() > fileSize -> throw FileTooLongException()
                    }

                    outputStream.flush()
                }
            }

            onSuccess()
        } catch (exception: Exception) {
            exception.printStackTrace()
            onError(ServiceError.RequestError.valueOf(exception))
        }
    }

    override fun onCancelled() {
        call?.cancel()
    }

}

class FileTooShortException : Exception()
class FileTooLongException : Exception()
