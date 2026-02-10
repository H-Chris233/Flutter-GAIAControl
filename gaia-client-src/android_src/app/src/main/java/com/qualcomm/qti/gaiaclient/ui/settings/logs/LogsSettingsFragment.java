/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.logs;

import android.content.Intent;
import android.provider.Settings;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.Navigator;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LogsSettingsFragment extends SettingsFragment<LogsSettings, LogsSettingsViewData, LogsSettingsViewModel> {

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().updateData();
    }

    @Override
    protected int getXmlSettingsId() {
        return R.xml.logs_settings;
    }

    @Override
    protected LogsSettingsViewModel initViewModel() {
        return new ViewModelProvider(this).get(LogsSettingsViewModel.class);
    }

    @Override
    protected LogsSettings[] getSettings() {
        return LogsSettings.values();
    }

    @Override
    protected boolean onPreferenceChange(LogsSettings key, Preference preference, Object update) {
        // nothing to do
        return true;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (isKey(key, LogsSettings.BUG_REPORT)) {
            goToAndroidDeveloperSettings();
            return true;
        }
        else if (isKey(key, LogsSettings.DEVICE_LOGS)) {
            getViewModel().getDeviceLogs(getContext().getApplicationContext());
            Navigator.navigate(getRoot(), LogsSettingsFragmentDirections.toDownloadLogs());
            return true;
        }
        return super.onPreferenceTreeClick(preference);
    }

    private void goToAndroidDeveloperSettings() {
        startActivity(new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS));
    }

}
