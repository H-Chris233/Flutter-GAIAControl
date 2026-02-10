/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.service

/**
 * A class to encapsulate data for tasks that provide progress during execution.
 *
 * @param P type for the in progress data
 * @param D type for the result data
 * @param E type for the result error data
 */
sealed class Result<P, D, E> {
    val isComplete: Boolean
        get() = this !is Progress

    class Progress<P, D, E>(val progress: P) : Result<P, D, E>()
    class Success<P, D, E>(val data: D) : Result<P, D, E>()
    class Error<P, D, E>(val error: E) : Result<P, D, E>()
}
