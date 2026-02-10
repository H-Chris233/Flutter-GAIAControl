/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.download

import android.util.Log
import com.qualcomm.qti.gaiaclient.repository.service.ApiErrorType
import com.qualcomm.qti.gaiaclient.repository.service.ServiceErrorResponse

private const val TAG = "DownloadFileError"

sealed class DownloadFileError(val status: Int, val id: String) : ApiErrorType {

    /**
     * Error sent when no token was provided for the server to process the request.
     */
    object NoToken : DownloadFileError(400, "NO_TOKEN")

    /**
     * No id was provided for the server to identify the file to download.
     */
    object NoId : DownloadFileError(400, "NO_ID")

    /**
     * The file for the supplied ID cannot be found.
     */
    object FileNotFound : DownloadFileError(400, "FILE_NOT_FOUND")

    /**
     * Error sent when the provided token for the server to process the request is invalid.
     */
    object InvalidToken : DownloadFileError(401, "INVALID_TOKEN")

    /**
     * Error sent when The server encountered a problem.
     */
    object ServerError : DownloadFileError(500, "SERVER_ERROR")

    /**
     * Error sent when Server cannot access the resource.
     */
    object ServiceNotAvailable : DownloadFileError(503, "SERVICE_NOT_AVAILABLE")

    /**
     * Used when the error sent by the server cannot be identified.
     */
    class UnidentifiedError(val error: ServiceErrorResponse) : DownloadFileError(error.status.toInt(), error.id) {
        override fun toString(): String {
            return "$TAG($status, $id)"
        }
    }

    companion object {
        fun valueOf(error: ServiceErrorResponse): DownloadFileError {
            return when (error.id) {
                NoToken.id -> NoToken
                InvalidToken.id -> InvalidToken
                NoId.id -> NoId
                FileNotFound.id -> FileNotFound
                ServerError.id -> ServerError
                ServiceNotAvailable.id -> ServiceNotAvailable
                else -> {
                    val label = "status=\"${error.status}\", id=\"${error.id}\""
                    Log.w(TAG, "[DownloadFileError->valueOf] Unidentified error ID: $label")
                    UnidentifiedError(error)
                }
            }
        }
    }

    override fun toString(): String {
        return "$TAG.$id"
    }
}
