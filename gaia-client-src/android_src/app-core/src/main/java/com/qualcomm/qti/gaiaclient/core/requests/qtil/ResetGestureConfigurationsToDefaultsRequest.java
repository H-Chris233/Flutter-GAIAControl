/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.GestureConfigurationPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class ResetGestureConfigurationsToDefaultsRequest extends Request<Void, Void, Void> {

    public ResetGestureConfigurationsToDefaultsRequest() {
        super(null);
    }

    @Override
    public void run(@Nullable Context context) {
        GestureConfigurationPlugin plugin = getQtilManager().getGestureConfigurationPlugin();
        if (plugin != null) {
            plugin.resetToDefault();
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
