/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.EarbudPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetEarbudInformationRequest extends Request<Void, Void, Void> {

    @NonNull
    private final EarbudInfo info;

    public GetEarbudInformationRequest(@NonNull EarbudInfo info) {
        super(null);
        this.info = info;
    }

    @Override
    public void run(@Nullable Context context) {
        EarbudPlugin earbudPlugin = getQtilManager().getEarbudPlugin();
        if (earbudPlugin != null) {
            earbudPlugin.fetchEarbudInfo(info);
        }

        onComplete(null);
    }

    @Override
    protected void onEnd() {
    }
}
