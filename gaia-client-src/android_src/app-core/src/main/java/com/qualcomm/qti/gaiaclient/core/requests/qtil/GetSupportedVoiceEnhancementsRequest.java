/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class GetSupportedVoiceEnhancementsRequest extends Request<Void, Void, Reason> {

    public GetSupportedVoiceEnhancementsRequest() {
        super(null);
    }

    @Override
    public void run(@Nullable Context context) {
        VoiceProcessingPlugin plugin = getQtilManager().getVoiceProcessingPlugin();
        if (plugin != null) {
            plugin.getSupportedEnhancements();
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
