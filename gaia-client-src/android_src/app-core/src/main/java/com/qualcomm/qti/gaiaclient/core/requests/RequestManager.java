/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

/**
 * To run {@link Request}. This manager ensures that they are run on a background thread.
 */
public interface RequestManager {

    /**
     * To execute a request.
     *
     * @param context
     *         the application context
     * @param request
     *         the request to run.
     */
    void execute(Context context, @SuppressWarnings("rawtypes") Request request);

}
