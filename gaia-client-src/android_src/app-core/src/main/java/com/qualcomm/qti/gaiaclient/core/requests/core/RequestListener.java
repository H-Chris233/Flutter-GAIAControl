/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.core;

/**
 * To get feedback during the execution of a {@link Request}.
 *
 * @param <Result>
 *         The result provided by a {@link Request} when it completes.
 * @param <Progress>
 *         The type of progress provided by a {@link Request} during its execution.
 * @param <Error>
 *         The type of error sent by a {@link Request} when it hits an error and cannot
 *         successfully complete.
 */
public interface RequestListener<Result, Progress, Error> {

    /**
     * Called by a {@link Request} when it has some feedback to provide during its execution.
     */
    void onProgress(Progress progress);

    /**
     * Called by a {@link Request} when it has successfully completed.
     */
    void onComplete(Result result);

    /**
     * Called by a {@link Request} when it has encountered an error.
     */
    void onError(Error error);

}
