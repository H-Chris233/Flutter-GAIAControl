/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.Preference;

import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

public abstract class PersistentSettingsFragment<S extends PersistentSettings, VD extends SettingsViewData<S>,
        M extends SettingsViewModel<S, VD>> extends SettingsFragment<S, VD, M> {

    private final SharedPreferences.OnSharedPreferenceChangeListener
            mPreferenceChangeListener = (sharedPreferences, key) -> {
        Preference preference = findPreference(key);
        if (preference != null) {
            onSharedPreferenceChange(key, preference);
        }
    };

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        initPersistence();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(mPreferenceChangeListener);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(mPreferenceChangeListener);
    }

    @Override
    protected final S[] getSettings() {
        // only non persistent settings are managed by the parent
        return getNonPersistentSettings();
    }

    protected void setSummaryForPersistentSetting(S key, String summary) {
        Preference preference = findPreference(key);
        if (preference != null) {
            preference.setSummary(summary);
        }
    }

    protected abstract void onSharedPreferenceChange(@NonNull String key,
                                                     @NonNull Preference preference);

    protected abstract S[] getNonPersistentSettings();

    protected abstract S[] getPersistentSettings();

    private void initPersistence() {
        for (S key : getPersistentSettings()) {
            Preference preference = findPreference(key);
            if (preference != null) {
                preference.setPersistent(true);
            }
        }
    }

}
