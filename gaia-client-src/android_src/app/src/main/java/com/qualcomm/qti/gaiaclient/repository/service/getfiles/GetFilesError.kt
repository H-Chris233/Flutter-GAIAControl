/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.getfiles

import android.util.Log
import com.qualcomm.qti.gaiaclient.repository.service.ApiErrorType
import com.qualcomm.qti.gaiaclient.repository.service.ServiceErrorResponse

private const val TAG = "GetFilesError"

sealed class GetFilesError(val status: Int, val id: String) : ApiErrorType {

    /**
     * Error sent when no token was provided for the server to process the request.
     */
    object NoToken : GetFilesError(400, "NO_TOKEN")

    /**
     * Error sent when no id, applicationVersion or hardwareVersion were provided for the server to identify a
     * build to base a set of files.
     */
    object NoId : GetFilesError(400, "NO_ID")

    /**
     * Error sent when no chip family could be identified for the supplied id, applicationVersion or
     * hardwareVersion.
     */
    object UnableToResolveId : GetFilesError(400, "UNABLE_TO_RESOLVE_ID")

    /**
     * Error sent when the provided token for the server to process the request is invalid.
     */
    object InvalidToken : GetFilesError(401, "INVALID_TOKEN")

    /**
     * Error sent when The server encountered a problem.
     */
    object ServerError : GetFilesError(500, "SERVER_ERROR")

    /**
     * Error sent when Server cannot access the resources.
     */
    object ServiceNotAvailable : GetFilesError(503, "SERVICE_NOT_AVAILABLE")

    /**
     * Used when the error sent by the server cannot be identified.
     */
    class UnidentifiedError(val error: ServiceErrorResponse) : GetFilesError(error.status.toInt(), error.id) {
        override fun toString(): String {
            return "$TAG($status, $id)"
        }
    }

    companion object {
        fun valueOf(error: ServiceErrorResponse): GetFilesError {
            return when (error.id) {
                NoToken.id -> NoToken
                InvalidToken.id -> InvalidToken
                NoId.id -> NoId
                UnableToResolveId.id -> UnableToResolveId
                ServerError.id -> ServerError
                ServiceNotAvailable.id -> ServiceNotAvailable
                else -> {
                    val label = "status=\"${error.status}\", id=\"${error.id}\""
                    Log.w(TAG, "[GetFilesError->valueOf] Unidentified error ID: $label")
                    UnidentifiedError(error)
                }
            }
        }
    }

    override fun toString(): String {
        return "$TAG.$id"
    }
}
