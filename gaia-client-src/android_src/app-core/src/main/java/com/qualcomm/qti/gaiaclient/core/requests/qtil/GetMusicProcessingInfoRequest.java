/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.MusicProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.MusicProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetMusicProcessingInfoRequest extends Request<Void, Void, Reason> {

    private final MusicProcessingInfo info;

    public GetMusicProcessingInfoRequest(MusicProcessingInfo info,
                                         RequestListener<Void, Void, Reason> listener) {
        super(listener);
        this.info = info;
    }

    @Override
    public void run(@Nullable Context context) {
        MusicProcessingPlugin plugin = getQtilManager().getMusicProcessingPlugin();
        if (plugin != null) {
            if (plugin.fetch(info)) {
                onComplete(null);
            }
        }
        onError(Reason.NOT_SUPPORTED);
    }

    @Override
    protected void onEnd() {
        // no state to clear
    }
}
