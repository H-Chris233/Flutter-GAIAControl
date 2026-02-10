/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.DebugPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class CancelDeviceLogsRequest extends Request<Void, Void, Void> {

    public CancelDeviceLogsRequest() {
        super(null);
    }

    @Override
    public void run(@Nullable Context context) {
        DebugPlugin plugin = getQtilManager().getDebugPlugin();
        if (plugin != null) {
            plugin.cancelDownload();
        }
        onComplete(null);
    }

    @Override
    protected void onEnd() {
        // no state to clear
    }
}
