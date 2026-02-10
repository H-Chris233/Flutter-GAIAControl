/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.GestureConfigurationPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetGestureInfoRequest extends Request<Void, Void, Void> {

    private static final String TAG = "GetGestureInfoRequest";

    @NonNull
    private final GestureConfigurationInfo info;

    @Nullable
    private final Object parameters;

    public GetGestureInfoRequest(@NonNull GestureConfigurationInfo info) {
        super(null);
        this.info = info;
        this.parameters = null;
    }

    public GetGestureInfoRequest(@NonNull GestureConfigurationInfo info, @NonNull Object parameters) {
        super(null);
        this.info = info;
        this.parameters = parameters;
    }

    @Override
    public void run(@Nullable Context context) {
        GestureConfigurationPlugin plugin = getQtilManager().getGestureConfigurationPlugin();
        if (plugin != null) {
            if (parameters == null) {
                plugin.fetchInfo(info);
            }
            else {
                plugin.fetchInfo(info, parameters);
            }
            onComplete(null);
        }
        else {
            Log.w(TAG, "[run] no corresponding plugin");
            onError(null);
        }
    }

    @Override
    protected void onEnd() {
    }
}
