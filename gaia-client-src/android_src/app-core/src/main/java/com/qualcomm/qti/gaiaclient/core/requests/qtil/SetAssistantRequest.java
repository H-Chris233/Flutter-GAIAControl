/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceUIPlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class SetAssistantRequest extends Request<Void, Void, Reason> {

    @NonNull
    private final VoiceAssistant assistant;

    public SetAssistantRequest(@NonNull VoiceAssistant assistant,
                               RequestListener<Void, Void, Reason> listener) {
        super(listener);
        this.assistant = assistant;
    }

    @Override
    public void run(@Nullable Context context) {
        VoiceUIPlugin plugin = getQtilManager().getVoiceUIPlugin();
        if (plugin != null) {
            plugin.setSelectedAssistant(assistant);
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
