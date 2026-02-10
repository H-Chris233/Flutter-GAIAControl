/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.EarbudFitPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetEarbudFitTestRequest extends Request<Void, Void, Void> {

    private final FitTestState state;

    public SetEarbudFitTestRequest(FitTestState state) {
        super(null);
        this.state = state;
    }

    @Override
    public void run(@Nullable Context context) {
        EarbudFitPlugin plugin = getQtilManager().getEarbudFitPlugin();
        if (plugin != null) {
            plugin.setFitTest(state);
        }

        onComplete(null);
    }

    @Override
    protected void onEnd() {
        // no state to clear
    }
}
