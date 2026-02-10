/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.LogInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.DebugPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetLogInfoRequest extends Request<Void, Void, Reason> {

    @NonNull
    private final LogInfo info;


    public GetLogInfoRequest(@Nullable RequestListener<Void, Void, Reason> listener, @NonNull LogInfo info) {
        super(listener);
        this.info = info;
    }

    @Override
    public void run(@Nullable Context context) {
        DebugPlugin plugin = getQtilManager().getDebugPlugin();
        if (plugin != null) {
            plugin.fetchLogInfo(info);
            onComplete(null);
        }
        else {
            onError(Reason.NOT_SUPPORTED);
        }
    }

    @Override
    protected void onEnd() {
    }
}
