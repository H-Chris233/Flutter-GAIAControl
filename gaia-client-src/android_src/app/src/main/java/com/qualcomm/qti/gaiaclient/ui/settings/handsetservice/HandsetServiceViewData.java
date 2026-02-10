/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.handsetservice;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SwitchSettingData;

public class HandsetServiceViewData extends SettingsViewData<HandsetServiceSettings> {

    HandsetServiceViewData(Context context) {
        super(context);
    }

    @Override
    public HandsetServiceSettings[] getKeys() {
        return HandsetServiceSettings.values();
    }

    @Override
    protected SettingData initSettingData(Context context, HandsetServiceSettings key) {
        switch (key) {
            case MULTIPOINT_ENABLE:
                // visible switch setting
                SettingData switchData = new SwitchSettingData();
                switchData.setVisible(true);
                return switchData;

            default:
                // unexpected
                return new SettingData();
        }
    }

}
