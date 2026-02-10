/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.ANCPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetANCRequest extends Request<Void, Void, Reason> {

    private final ANCInfo info;

    private final Object value;

    public SetANCRequest(RequestListener<Void, Void, Reason> listener,
                         ANCInfo info, Object value) {
        super(listener);
        this.info = info;
        this.value = value;
    }

    @Override
    public void run(@Nullable Context context) {
        ANCPlugin plugin = getQtilManager().getANCPlugin();
        if (plugin != null) {
            plugin.setANCInfo(info, value);
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
