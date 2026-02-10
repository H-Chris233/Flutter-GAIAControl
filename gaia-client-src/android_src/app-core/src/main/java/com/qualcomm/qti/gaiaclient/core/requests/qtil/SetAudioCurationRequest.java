/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.AudioCurationPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

public class SetAudioCurationRequest extends Request<Void, Void, Reason> {

    private final ACInfo info;

    private final Object value;

    public SetAudioCurationRequest(RequestListener<Void, Void, Reason> listener,
                                   ACInfo info, Object value) {
        super(listener);
        this.info = info;
        this.value = value;
    }

    @Override
    public void run(@Nullable Context context) {
        AudioCurationPlugin plugin = getQtilManager().getAudioCurationPlugin();
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
