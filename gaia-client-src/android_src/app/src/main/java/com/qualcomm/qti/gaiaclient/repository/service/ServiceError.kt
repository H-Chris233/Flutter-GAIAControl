/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

import java.net.SocketTimeoutException
import java.net.UnknownHostException

sealed class ServiceError {

    sealed class Internal : ServiceError() { // Internal app/process error, can't do anything
        object FileError : Internal()
        object UriError : Internal()
        object NotInitialised : Internal()
        object CallFailed : Internal()

        class UnexpectedStatus(val status: Int) : Internal() {
            override fun toString(): String {
                return "Internal.UnexpectedStatus($status)"
            }
        }

        override fun toString(): String {
            return "Internal.${javaClass.kotlin.simpleName}"
        }
    }

    sealed class RequestError : ServiceError() {
        object UnknownHost : RequestError() // cannot find the host: check connection, VPN and URL
        object Timeout : RequestError() // server took too long, try again

        class Exception(val exception: String) : RequestError() {
            override fun toString(): String {
                return "Exception(${exception})"
            }
        }

        override fun toString(): String {
            return "RequestError.${javaClass.kotlin.simpleName}"
        }

        companion object {
            fun valueOf(t: Throwable?): ServiceError {
                return when (t) {
                    is UnknownHostException -> UnknownHost
                    is SocketTimeoutException -> Timeout
                    else -> Exception("${t?.javaClass?.kotlin?.simpleName}")
                }
            }
        }
    }

    sealed class ResponseError : ServiceError() {
        object EmptyResponse : ResponseError()
        object UnexpectedFormat : ResponseError()

        class HtmlError(val code: Int, val content: String) : ResponseError() {
            override fun toString(): String {
                return "ResponseError.HtmlError($code)"
            }
        }

        class ApiError(val error: ApiErrorType) : ResponseError() {
            override fun toString(): String {
                return error.toString()
            }
        }

        override fun toString(): String {
            return "ResponseError.${javaClass.kotlin.simpleName}"
        }
    }

    override fun toString(): String {
        return "${javaClass.kotlin.simpleName}"
    }
}

interface ApiErrorType // can be implemented by requests for specific errors
