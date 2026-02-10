/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.HandsetServiceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.HandsetServicePlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetHandsetServiceRequest extends Request<Void, Void, Reason> {

    private final HandsetServiceInfo info;

    private final Object value;

    public SetHandsetServiceRequest(HandsetServiceInfo info, Object value) {
        super(null);
        this.info = info;
        this.value = value;
    }

    @Override
    public void run(@Nullable Context context) {
        HandsetServicePlugin plugin = getQtilManager().getHandsetServicePlugin();
        if (plugin != null) {
            plugin.setInfo(info, value);
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
