/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import androidx.annotation.NonNull;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

final class RequestManagerImpl {

    void execute(Context context, @SuppressWarnings("rawtypes") @NonNull Request request) {
        // use executor to allow the caller to end its process
        getTaskManager().runInBackground(() -> request.run(context));
    }
}
