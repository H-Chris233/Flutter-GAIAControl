/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.logs;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

public class LogsSettingsViewData extends SettingsViewData<LogsSettings> {

    LogsSettingsViewData(Context context) {
        super(context);
    }

    @Override
    public LogsSettings[] getKeys() {
        return LogsSettings.values();
    }

    @Override
    protected SettingData initSettingData(Context context, LogsSettings key) {
        switch (key) {
            case LOGS_SIZES:
            case BUG_REPORT:
            case DEVICE_LOGS:
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
