/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.anclegacy;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.EditTextSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.ListSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SwitchSettingData;

public class ANCLegacyViewData extends SettingsViewData<ANCLegacySettings> {

    ANCLegacyViewData(Context context) {
        super(context);
    }

    @Override
    public ANCLegacySettings[] getKeys() {
        return ANCLegacySettings.values();
    }

    @Override
    protected SettingData initSettingData(Context context, ANCLegacySettings key) {
        switch (key) {
            case STATE:
                // visible switch
                SettingData switchData = new SwitchSettingData();
                switchData.setVisible(true);
                return switchData;

            case MODE:
                // list
                SettingData listData = new ListSettingData();
                listData.setVisible(true);
                return listData;

            case LEAKTHROUGH_GAIN:
                // Edit Text
                SettingData editTextData = new EditTextSettingData();
                editTextData.setVisible(true);
                return editTextData;

            case ADAPTIVE_STATE:
            case ADAPTED_GAIN:
            case CATEGORY_STATE_DEPENDANT:
                // default non visible setting
                SettingData settingData = new SettingData();
                settingData.setVisible(false);
                return settingData;

            default:
                // unexpected
                return new SettingData();
        }
    }

}
