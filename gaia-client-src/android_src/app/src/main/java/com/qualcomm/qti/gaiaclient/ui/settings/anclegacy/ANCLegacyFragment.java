/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.anclegacy;

import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.repository.anclegacy.ANCModeLegacy;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ANCLegacyFragment extends SettingsFragment<ANCLegacySettings, ANCLegacyViewData, ANCLegacyViewModel> {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        setEditTextInputTypeAsNumber(ANCLegacySettings.LEAKTHROUGH_GAIN);
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewModel().updateData();
    }

    @Override
    protected int getXmlSettingsId() {
        return R.xml.anc_legacy_settings;
    }

    @Override
    protected ANCLegacyViewModel initViewModel() {
        return new ViewModelProvider(this).get(ANCLegacyViewModel.class);
    }

    @Override
    protected ANCLegacySettings[] getSettings() {
        return ANCLegacySettings.values();
    }

    @Override
    protected boolean onPreferenceChange(ANCLegacySettings key, Preference preference, Object update) {
        if (ANCLegacySettings.MODE.equals(key) && update instanceof String) {
            ANCModeLegacy mode = ANCModeLegacy.valueOf((String) update);
            getViewModel().setMode(mode);
            return false; // view model will update the state depending on request result
        }
        else if (ANCLegacySettings.LEAKTHROUGH_GAIN.equals(key) && update instanceof String) {
            long gain = Long.parseLong((String) update); // long to catch as many characters as
            // possible
            getViewModel().setLeakthroughGain(gain);
            return false; // view model will update the state depending on request result
        }
        else if (ANCLegacySettings.STATE.equals(key) && update instanceof Boolean) {
            getViewModel().setState((boolean) update);
            return false; // view model will update the state depending on request result
        }

        return true;
    }

}
