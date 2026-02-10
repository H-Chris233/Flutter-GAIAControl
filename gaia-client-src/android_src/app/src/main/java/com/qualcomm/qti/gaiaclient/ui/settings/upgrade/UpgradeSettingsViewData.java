/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.upgrade;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.repository.service.ServiceAvailabilityImpl;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.EditTextSettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

public class UpgradeSettingsViewData extends SettingsViewData<UpgradeSettings> {

    UpgradeSettingsViewData(Context context) {
        super(context);
    }

    @Override
    public UpgradeSettings[] getKeys() {
        return UpgradeSettings.NON_PERSISTENT;
    }

    @Override
    protected SettingData initSettingData(Context context, UpgradeSettings key) {
        // only the non persistent settings
        switch (key) {
            case DEVELOPER_OPTIONS:
                // not visible
                SettingData settingData = new SettingData();
                settingData.setVisible(false);
                return settingData;

            case CHUNK_SIZE:
                // Edit Text
                SettingData editTextData = new EditTextSettingData();
                editTextData.setVisible(false);
                return editTextData;

            case GAIA_AND_PROTOCOL_VERSIONS:
                // disabled
                SettingData versionsData = new SettingData();
                versionsData.setEnabled(false);
                return versionsData;

            case SELECT_FILE:
                // visible
                SettingData fileData = new SettingData();
                fileData.setVisible(true);
                return fileData;

            case CHECK_FOR_UPDATES:
                SettingData checkData = new SettingData();
                checkData.setVisible(ServiceAvailabilityImpl.INSTANCE.getExists());
                checkData.setEnabled(ServiceAvailabilityImpl.INSTANCE.isAvailable(context));
                return checkData;

            case APPLICATION_VERSION:
            case APPLICATION_BUILD_ID:
            default: // unexpected
                return new SettingData();
        }
    }
}
