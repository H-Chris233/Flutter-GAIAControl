/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancementConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetVoiceEnhancementConfigurationRequest extends Request<Void, Void, Reason> {

    private final VoiceEnhancementConfiguration configuration;

    public SetVoiceEnhancementConfigurationRequest(VoiceEnhancementConfiguration configuration) {
        super(null);
        this.configuration = configuration;
    }

    @Override
    public void run(@Nullable Context context) {
        VoiceProcessingPlugin plugin = getQtilManager().getVoiceProcessingPlugin();
        if (plugin != null) {
            plugin.setConfiguration(configuration);
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
