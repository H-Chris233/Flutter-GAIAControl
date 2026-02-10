/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.voiceprocessing;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.ListSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

public class VoiceProcessingViewData extends SettingsViewData<VoiceProcessingSettings> {

    VoiceProcessingViewData(Context context) {
        super(context);
    }

    @Override
    public VoiceProcessingSettings[] getKeys() {
        return VoiceProcessingSettings.values();
    }

    @Override
    protected SettingData initSettingData(Context context, VoiceProcessingSettings key) {
        switch (key) {
            case OPERATION_MODE:
                // visible setting
                SettingData typeData = new SettingData();
                typeData.setVisible(true);
                return typeData;

            case MICROPHONE_MODE:
                ListSettingData micData = new ListSettingData();
                micData.setVisible(true);
                micData.setEntries(LabelProvider.getEntriesForMicrophoneMode(context, 4));
                return micData;

            case BYPASS_MODE:
                ListSettingData transpData = new ListSettingData();
                transpData.setVisible(true);
                transpData.setEntries(LabelProvider.getEntriesForBypassModes(context));
                return transpData;

            case CATEGORY_3MIC_CAPABILITY:
                // invisible setting
                SettingData catData = new SettingData();
                catData.setVisible(false);
                return catData;

            default:
                // unexpected
                return new SettingData();
        }
    }

}
