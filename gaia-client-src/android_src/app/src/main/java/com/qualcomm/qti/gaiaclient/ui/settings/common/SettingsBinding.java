/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

public class SettingsBinding<S extends Settings, VD extends SettingsViewData<S>> {

    private final PreferenceManager mPreferenceManager;

    private final View mRoot;

    protected SettingsBinding(PreferenceManager preferenceManager, View view) {
        mPreferenceManager = preferenceManager;
        mRoot = view;
    }

    protected final View getRoot() {
        return mRoot;
    }

    protected final void observeData(LifecycleOwner owner, VD viewData) {
        if (viewData == null) {
            return;
        }

        for (S setting : viewData.getKeys()) {
            Preference preference = getPreference(setting);

            if (preference != null) {
                observeDefaultSettings(viewData, setting, preference, owner);
                observeCustomSettings(viewData, setting, preference, owner);
            }
        }
    }

    protected void observeCustomSettings(@NonNull VD viewData, @NonNull S setting,
                                         @NonNull Preference preference, LifecycleOwner owner) {
    }

    private void observeDefaultSettings(@NonNull VD viewData, @NonNull S setting,
                                        @NonNull Preference preference, LifecycleOwner owner) {
        viewData.observeVisibility(setting, owner, preference::setVisible);
        viewData.observeEnable(setting, owner, preference::setEnabled);
        viewData.observeSummary(setting, owner, preference::setSummary);

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            viewData.observeValue(setting, owner, listPreference::setValue);
            viewData.observeEntries(setting, owner, listPreference::setEntries);
            viewData.observeEntryValues(setting, owner, listPreference::setEntryValues);
        }

        if (preference instanceof EditTextPreference) {
            EditTextPreference edit = (EditTextPreference) preference;
            viewData.observeValue(setting, owner, edit::setText);
        }

        if (preference instanceof TwoStatePreference) {
            TwoStatePreference twoStatePreference = (TwoStatePreference) preference;
            viewData.observeChecked(setting, owner, twoStatePreference::setChecked);
        }
    }

    private Preference getPreference(S setting) {
        Context context = mPreferenceManager.getContext();
        return mPreferenceManager.findPreference(setting.getId(context));
    }
}
