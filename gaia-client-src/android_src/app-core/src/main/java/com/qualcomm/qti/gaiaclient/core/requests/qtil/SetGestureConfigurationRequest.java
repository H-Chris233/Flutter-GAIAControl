/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.SetGestureConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.GestureConfigurationPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetGestureConfigurationRequest extends Request<Void, Void, Void> {

    @NonNull
    private final SetGestureConfiguration configuration;

    public SetGestureConfigurationRequest(@NonNull SetGestureConfiguration configuration) {
        super(null);
        this.configuration = configuration;
    }

    @Override
    public void run(@Nullable Context context) {
        GestureConfigurationPlugin plugin = getQtilManager().getGestureConfigurationPlugin();
        if (plugin != null) {
            plugin.setInfo(GestureConfigurationInfo.SET_GESTURE_CONFIGURATION, configuration);
            onComplete(null);
        }
        else {
            onError(null);
        }
    }

    @Override
    protected void onEnd() {
    }
}
