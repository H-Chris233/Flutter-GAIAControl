/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service.feedback

import android.util.Log
import com.qualcomm.qti.gaiaclient.repository.service.ApiErrorType
import com.qualcomm.qti.gaiaclient.repository.service.ServiceErrorResponse

private const val TAG = "FeedbackError"

sealed class FeedbackError(val status: Int, val id: String) : ApiErrorType {

    /**
     * Error sent when no token was provided for the server to process the request.
     */
    object NoToken : FeedbackError(400, "NO_TOKEN")

    /**
     * Error sent when the request did not provide a feedback title or a description.
     */
    object NoFeedback : FeedbackError(400, "NO_FEEDBACK")

    /**
     * Error sent when the provided token for the server to process the request is invalid.
     */
    object InvalidToken : FeedbackError(401, "INVALID_TOKEN")

    /**
     * Error sent when the server encountered a problem.
     */
    object ServerError : FeedbackError(500, "SERVER_ERROR")

    /**
     * Error sent when the server could not reach the tracking system to create a ticket.
     */
    object TrackingSystemUnavailable : FeedbackError(503, "TRACKING_SYSTEM_UNAVAILABLE")

    /**
     * Used when the error sent by the server cannot be identified.
     */
    class UnidentifiedError(val error: ServiceErrorResponse) : FeedbackError(error.status.toInt(), error.id) {
        override fun toString(): String {
            return "$TAG($status, $id)"
        }
    }

    companion object {
        fun valueOf(error: ServiceErrorResponse): FeedbackError {
            return when (error.id) {
                NoToken.id -> NoToken
                NoFeedback.id -> NoFeedback
                InvalidToken.id -> InvalidToken
                ServerError.id -> ServerError
                TrackingSystemUnavailable.id -> TrackingSystemUnavailable
                else -> {
                    val label = "status=\"${error.status}\", id=\"${error.id}\""
                    Log.w(TAG, "[FeedbackError->valueOf] Unidentified error ID: $label")
                    UnidentifiedError(error)
                }
            }
        }
    }

    override fun toString(): String {
        return "$TAG.$id"
    }
}
