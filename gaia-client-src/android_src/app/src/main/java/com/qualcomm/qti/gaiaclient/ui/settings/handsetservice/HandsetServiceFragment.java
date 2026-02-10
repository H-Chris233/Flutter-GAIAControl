/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.handsetservice;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HandsetServiceFragment
        extends SettingsFragment<HandsetServiceSettings, HandsetServiceViewData, HandsetServiceViewModel> {

    @Override
    protected int getXmlSettingsId() {
        return R.xml.handset_service_settings;
    }

    @Override
    protected HandsetServiceViewModel initViewModel() {
        return new ViewModelProvider(this).get(HandsetServiceViewModel.class);
    }

    @Override
    protected HandsetServiceSettings[] getSettings() {
        return HandsetServiceSettings.values();
    }

    @Override
    protected boolean onPreferenceChange(HandsetServiceSettings key, Preference preference, Object update) {
        if (HandsetServiceSettings.MULTIPOINT_ENABLE.equals(key) && update instanceof Boolean) {
            getViewModel().setMultipointEnable((boolean) update);
            return false; // view model will update the state depending on request result
        }

        return true;
    }

}
