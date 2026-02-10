/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.voiceprocessing;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVC3MicConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCBypassMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Capability;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancementConfiguration;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.VoiceProcessingSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetSupportedVoiceEnhancementsRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetVoiceEnhancementConfigurationRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetVoiceEnhancementConfigurationRequest;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class VoiceProcessingRepositoryImpl extends VoiceProcessingRepositoryData {

    private final VoiceProcessingSubscriber voiceProcessingSubscriber = new VoiceProcessingSubscriber() {
        @Override
        public void onEnhancements(@NonNull ArrayList<VoiceEnhancement> enhancements) {
            updateEnhancements(enhancements);
        }

        @Override
        public void onConfiguration(@NonNull VoiceEnhancementConfiguration configuration) {
            if (configuration.getCapability() == Capability.CVC_3MIC && configuration instanceof CVC3MicConfiguration) {
                CVC3MicConfiguration cvcConfig = (CVC3MicConfiguration) configuration;
                updateMicrophoneMode(cvcConfig.getMicrophoneMode());
                updateBypassMode(cvcConfig.getBypassMode());
                updateOperationMode(cvcConfig.getOperationMode());
            }
        }

        @Override
        public void onError(VoiceProcessingInfo info, Reason reason) {
            updateAndClearError(info, reason);
        }
    };

    @Inject
    public VoiceProcessingRepositoryImpl() {

    }

    @Override
    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(voiceProcessingSubscriber);
    }

    @Override
    public void fetchEnhancements(Context context) {
        GetSupportedVoiceEnhancementsRequest request = new GetSupportedVoiceEnhancementsRequest();
        GaiaClientService.getRequestManager().execute(context, request);

    }

    @Override
    public void fetchConfiguration(Context context, Capability capability) {
        GetVoiceEnhancementConfigurationRequest request = new GetVoiceEnhancementConfigurationRequest(capability);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    @Override
    public void setBypassMode(Context context, @NonNull CVCBypassMode mode) {
        setCVC3MicConfiguration(context, getMicrophoneMode(), mode);
    }

    @Override
    public void setMicrophoneMode(Context context, int mode) {
        setCVC3MicConfiguration(context, mode, getBypassMode());
    }

    private void setCVC3MicConfiguration(Context context, int microphoneMode, CVCBypassMode mode) {
        VoiceEnhancementConfiguration configuration = new CVC3MicConfiguration(microphoneMode, mode);
        SetVoiceEnhancementConfigurationRequest request = new SetVoiceEnhancementConfigurationRequest(configuration);
        GaiaClientService.getRequestManager().execute(context, request);
    }
}
