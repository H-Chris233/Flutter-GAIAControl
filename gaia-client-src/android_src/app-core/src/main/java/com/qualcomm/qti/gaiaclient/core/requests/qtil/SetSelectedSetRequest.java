/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.MusicProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetSelectedSetRequest extends Request<Void, Void, Void> {

    private final PreSet set;

    public SetSelectedSetRequest(PreSet set) {
        super(null);
        this.set = set;
    }

    @Override
    public void run(@Nullable Context context) {
        MusicProcessingPlugin plugin = getQtilManager().getMusicProcessingPlugin();
        if (plugin != null) {
            plugin.selectSet(set);
        }
        onComplete(null);
    }

    @Override
    protected void onEnd() {
        // no state to clear
    }
}
