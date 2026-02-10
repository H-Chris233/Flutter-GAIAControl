/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.ANCPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetANCInfoRequest extends Request<Void, Void, Void> {

    @NonNull
    private final ANCInfo info;

    public GetANCInfoRequest(@NonNull ANCInfo info) {
        super(null);
        this.info = info;
    }

    @Override
    public void run(@Nullable Context context) {
        ANCPlugin plugin = getQtilManager().getANCPlugin();
        if (plugin != null) {
            plugin.fetchANCInfo(info);
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
