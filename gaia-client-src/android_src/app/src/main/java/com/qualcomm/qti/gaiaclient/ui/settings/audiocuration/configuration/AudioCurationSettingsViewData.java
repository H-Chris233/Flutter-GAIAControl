/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.configuration;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.ListSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SwitchSettingData;

public class AudioCurationSettingsViewData extends SettingsViewData<AudioCurationSettings> {

    AudioCurationSettingsViewData(Context context) {
        super(context);
    }

    @Override
    public AudioCurationSettings[] getKeys() {
        return AudioCurationSettings.values();
    }

    @Override
    protected SettingData initSettingData(Context context, AudioCurationSettings key) {
        switch (key) {
            case ANC_STATE:
            case AUTO_TRANSPARENCY_STATE: // category hidden by default => setting to be visible
            case NOISE_ID_STATE: // category hidden by default => setting to be visible
                // visible switch
                SettingData switchData = new SwitchSettingData();
                switchData.setVisible(true);
                return switchData;

            case ENTER_DEMO:
            case AUTO_TRANSPARENCY_CATEGORY:
            case NOISE_ID_PREF_CATEGORY:
                // default non visible setting
                SettingData settingData = new SettingData();
                settingData.setVisible(false);
                return settingData;

            case TOGGLE_CONFIGURATION_1:
            case TOGGLE_CONFIGURATION_2:
            case TOGGLE_CONFIGURATION_3:
            case IDLE_CONFIGURATION:
            case PLAYBACK_CONFIGURATION:
            case CALL_CONFIGURATION:
            case ASSISTANT_CONFIGURATION:
            case VOICE_RECORDING_CONFIGURATION:
                // non visible list
                SettingData listData = new ListSettingData();
                listData.setVisible(false);
                return listData;

            case AUTO_TRANSPARENCY_RELEASE_TIME: // category hidden by default => setting to be visible
                ListSettingData visListData = new ListSettingData();
                visListData.setVisible(true);
                visListData.setEntries(ReleaseTime.getListEntries(context));
                return visListData;

            default:
                // unexpected
                return new SettingData();
        }
    }

}
