/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.core;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

/**
 * <p>Requests define and implements processes the core library can execute. They are used by a
 * other modules and/or UI to ask the core library for information or actions.</p>
 * <p>Implement the process to run within {@link #run(Context)}.</p>
 * <p>See {@link com.qualcomm.qti.gaiaclient.core.requests.RequestManager} for executing
 * requests. Using those request wrapper and the request manager ensure that requests are not
 * run on the main/ui thread and that their result or feedback is sent on the UI/Main thread.</p>
 * <p>A Request uses an optional {@link RequestListener} to provide feedback during its
 * execution. The extension must call {@link #onProgress(Object)}, {@link #onError(Object)} and
 * {@link #onComplete(Object)} to provide this feedback.</p>
 * <p>A request ends when it hits an error {@link #onError(Object)} or successfully complete
 * {@link #onComplete(Object)}. The Request then frees its resources and call {@link #onEnd()}
 * to give the opportunity to its extensions.</p>
 *
 * @param <Result>
 *         The type of result when the request successfully completes its
 * @param <Progress>
 *         The type of feedback provided during the execution of the request.
 * @param <Error>
 *         The type of error when the execution ends on an error.
 */
public abstract class Request<Result, Progress, Error> {

    /**
     * <p>A tag to display logs from this request.</p>
     */
    private static final String TAG = "Request";
    /**
     * A boolean to turn on and off logs for the Request.
     */
    private static final boolean LOG_METHODS = DEBUG.Request.REQUEST;
    /**
     * The optional listener for the caller to get optional feedback.
     */
    private final RequestListener<Result, Progress, Error> mListener;

    /**
     * A default constructor to set the listener.
     */
    protected Request(@Nullable RequestListener<Result, Progress, Error> listener) {
        this.mListener = listener;
    }

    /**
     * <p>The process to run when this request is executed.</p>
     *
     * @param context
     *         the context of the application.
     */
    public abstract void run(@Nullable Context context);

    /**
     * <p>Call this when the process is successfully finished.</p>
     * <p>When this is called the Request clears its resources and calls {@link #onEnd()} for
     * the children class to also free its resources.</p>
     *
     * @param result
     *         The result of the request.
     */
    protected void onComplete(Result result) {
        Logger.log(LOG_METHODS, TAG, "onComplete", new Pair<>("result", result));
        if (mListener != null) {
            getTaskManager().runOnMain(() -> mListener.onComplete(result));
        }
        onEnd();
    }

    /**
     * <p>Call this when the process finishes unsuccessfully.</p>
     * <p>When this is called the Request clears its resources and calls {@link #onEnd()} for
     * the children class to also free its resources.</p>
     *
     * @param reason
     *         The error that occurred.
     */
    protected void onError(Error reason) {
        Logger.log(LOG_METHODS, TAG, "onPacketError", new Pair<>("reason", reason));
        if (mListener != null) {
            getTaskManager().runOnMain(() -> mListener.onError(reason));
        }
        onEnd();
    }

    /**
     * <p>Call by the implemented process to provide feedback during the request.</p>
     * <p>This only updates the listener of the progress.</p>
     *
     * @param progress
     *         The feedback to provide.
     */
    protected void onProgress(Progress progress) {
        Logger.log(LOG_METHODS, TAG, "onProgress", new Pair<>("progress", progress));
        if (mListener != null) {
            getTaskManager().runOnMain(() -> mListener.onProgress(progress));
        }
    }

    /**
     * <p>This is called when the request ends, either {@link #onError(Object)} or
     * {@link #onComplete(Object)} have been called.</p>
     */
    abstract protected void onEnd();

}
