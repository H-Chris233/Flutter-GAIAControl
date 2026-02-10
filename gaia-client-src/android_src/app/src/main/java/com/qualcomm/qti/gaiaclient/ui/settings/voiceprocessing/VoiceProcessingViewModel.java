/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.voiceprocessing;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCBypassMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCOperationMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Capability;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.voiceprocessing.VoiceProcessingRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class VoiceProcessingViewModel extends SettingsViewModel<VoiceProcessingSettings, VoiceProcessingViewData> {

    private final FeaturesRepository featuresRepository;

    private final VoiceProcessingRepository voiceProcessingRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public VoiceProcessingViewModel(@NonNull Application application, ConnectionRepository connectionRepository,
                                    FeaturesRepository featuresRepository,
                                    VoiceProcessingRepository voiceProcessingRepository) {
        super(application, connectionRepository);
        this.featuresRepository = featuresRepository;
        this.voiceProcessingRepository = voiceProcessingRepository;
        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    @Override
    protected VoiceProcessingViewData initViewData(Context context) {
        return new VoiceProcessingViewData(context);
    }

    public void setMicrophoneMode(int number) {
        voiceProcessingRepository.setMicrophoneMode(getApplication(), number);
    }

    public void setBypassMode(CVCBypassMode mode) {
        voiceProcessingRepository.setBypassMode(getApplication(), mode);
    }

    private void onFeatures(ArrayMap<QTILFeature, Integer> features) {
        boolean supported = features != null && features.containsKey(QTILFeature.VOICE_PROCESSING);
        if (supported) {
            if (voiceProcessingRepository.hasData(VoiceProcessingInfo.SUPPORTED_ENHANCEMENTS)) {
                onEnhancements(voiceProcessingRepository.getEnhancements());
            }
            else {
                voiceProcessingRepository.fetchEnhancements(getContext());
            }
        }
    }

    private void onEnhancements(ArrayList<VoiceEnhancement> enhancements) {
        updateCapabilityData(enhancements, VoiceProcessingSettings.CATEGORY_3MIC_CAPABILITY, Capability.CVC_3MIC);
    }

    private void onOperationMode(CVCOperationMode mode) {
        String label = LabelProvider.getForOperationMode(getContext(), mode);
        setSettingSummary(VoiceProcessingSettings.OPERATION_MODE, label);
    }

    private void onMicrophoneMode(Integer mode) {
        String label = LabelProvider.getForMicrophoneMode(getContext(), mode);
        setSettingSummary(VoiceProcessingSettings.MICROPHONE_MODE, label);
        setSettingValue(VoiceProcessingSettings.MICROPHONE_MODE, Integer.toString(mode));
        setSettingVisibility(VoiceProcessingSettings.BYPASS_MODE, mode == 0);
    }

    private void onBypassMode(CVCBypassMode mode) {
        String label = LabelProvider.getForBypassMode(getContext(), mode);
        setSettingSummary(VoiceProcessingSettings.BYPASS_MODE, label);
        setSettingValue(VoiceProcessingSettings.BYPASS_MODE, mode.toString());
    }

    private void updateCapabilityData(ArrayList<VoiceEnhancement> enhancements, VoiceProcessingSettings category,
                                      Capability capability) {
        boolean supported = enhancements != null
                && enhancements.stream().anyMatch(processing -> processing.getCapability() == capability);

        if (supported && !voiceProcessingRepository.hasData(capability)) {
            voiceProcessingRepository.fetchConfiguration(getContext(), capability);
        }
        setSettingVisibility(category, supported);
    }

    private void onError(Pair<VoiceProcessingInfo, Reason> error) {
        if (error == null) {
            return;
        }

        String toast = getString(getStringIdForError(error.first), error.second);
        Toast.makeText(getApplication(), toast, Toast.LENGTH_SHORT).show();
    }

    private Context getContext() {
        return getApplication().getApplicationContext();
    }

    private String getString(int stringId, Reason reason) {
        return getContext().getString(stringId, reason == null ? "null" : reason.toString());
    }

    private int getStringIdForError(VoiceProcessingInfo info) {
        if (info == null) {
            return R.string.voice_processing_error;
        }

        switch (info) {
            case SUPPORTED_ENHANCEMENTS:
                return R.string.voice_processing_error_supported_enhancements;
            case SET_CONFIGURATION:
                return R.string.voice_processing_error_set_configuration;
            case GET_CONFIGURATION:
                return R.string.voice_processing_error_get_configuration;
            default:
                return R.string.voice_processing_error;
        }
    }

    private class DataObservers extends Observers {
        private final Observer<ArrayMap<QTILFeature, Integer>> featuresObserver =
                VoiceProcessingViewModel.this::onFeatures;
        private final Observer<ArrayList<VoiceEnhancement>> enhancementsObserver =
                VoiceProcessingViewModel.this::onEnhancements;
        private final Observer<CVCOperationMode> operationModeObserver = VoiceProcessingViewModel.this::onOperationMode;
        private final Observer<CVCBypassMode> bypassModeObserver = VoiceProcessingViewModel.this::onBypassMode;
        private final Observer<Integer> microphoneModeObserver = VoiceProcessingViewModel.this::onMicrophoneMode;
        private final Observer<Pair<VoiceProcessingInfo, Reason>> errorObserver =
                VoiceProcessingViewModel.this::onError;

        @Override
        protected void registerObservers() {
            featuresRepository.getFeaturesLiveData().observeForever(featuresObserver);
            voiceProcessingRepository.getEnhancementsLiveData().observeForever(enhancementsObserver);
            voiceProcessingRepository.getOperationModeLiveData().observeForever(operationModeObserver);
            voiceProcessingRepository.getBypassModeLiveData().observeForever(bypassModeObserver);
            voiceProcessingRepository.getMicrophoneModeLiveData().observeForever(microphoneModeObserver);
            voiceProcessingRepository.getErrorLiveData().observeForever(errorObserver);
        }

        @Override
        protected void unregisterObservers() {
            featuresRepository.getFeaturesLiveData().removeObserver(featuresObserver);
            voiceProcessingRepository.getEnhancementsLiveData().removeObserver(enhancementsObserver);
            voiceProcessingRepository.getOperationModeLiveData().removeObserver(operationModeObserver);
            voiceProcessingRepository.getBypassModeLiveData().removeObserver(bypassModeObserver);
            voiceProcessingRepository.getMicrophoneModeLiveData().removeObserver(microphoneModeObserver);
            voiceProcessingRepository.getErrorLiveData().removeObserver(errorObserver);
        }
    }

}
