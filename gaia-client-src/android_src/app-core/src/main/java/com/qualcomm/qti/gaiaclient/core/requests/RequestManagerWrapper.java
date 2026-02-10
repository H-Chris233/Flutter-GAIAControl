/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import androidx.annotation.NonNull;

public final class RequestManagerWrapper implements RequestManager {

    private final RequestManagerImpl mManager = new RequestManagerImpl();

    @Override
    public void execute(Context context, @SuppressWarnings("rawtypes") @NonNull Request request) {
        mManager.execute(context, request);
    }
}
