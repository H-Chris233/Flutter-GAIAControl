/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.TwoStatePreference;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.MainActivity;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

import java.util.Collections;
import java.util.Set;

public abstract class SettingsFragment<S extends Settings, VD extends SettingsViewData<S>, VM extends SettingsViewModel<S, VD>>
        extends PreferenceFragmentCompat {

    private SettingsBinding<S, VD> mBinding;

    private VM mViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(getXmlSettingsId(), rootKey);
    }

    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mBinding = initSettingsBinding(getPreferenceManager(), view);
        initManagedSettings();
        mViewModel = initViewModel();
        initObservations(mViewModel, mBinding);
        return view;
    }

    protected SettingsBinding<S, VD> initSettingsBinding(PreferenceManager preferenceManager, View view) {
        return new SettingsBinding<>(preferenceManager, view);
    }

    protected View getRoot() {
        return mBinding.getRoot();
    }

    protected boolean isKey(String key, S expected) {
        return key != null && key.equals(getString(expected.getResourceId()));
    }

    @Nullable
    protected <T extends Preference> T findPreference(@NonNull S key) {
        return findPreference(getString(key.getResourceId()));
    }

    protected boolean getSwitchState(S key) {
        TwoStatePreference preference = findPreference(key);
        return preference != null && preference.isChecked();
    }

    protected void setSwitchState(S key, boolean state) {
        TwoStatePreference preference = findPreference(key);
        if (preference != null) {
            preference.setChecked(state);
        }
    }

    @NonNull
    protected String getText(S key) {
        EditTextPreference preference = findPreference(key);
        String value = preference != null ? preference.getText() : "";
        return value == null ? "" : value;
    }

    @NonNull
    protected Set<String> getMultiSelectionValues(S key) {
        MultiSelectListPreference preference = findPreference(key);
        Set<String> values = preference != null ? preference.getValues() : Collections.emptySet();
        return values != null ? values : Collections.emptySet();
    }

    protected int getEditTextNumber(S key) {
        EditTextPreference preference = findPreference(key);
        String value = preference != null ? preference.getText() : null;
        return value == null ? 1 : Integer.parseInt(value);
    }

    protected void setEditTextInputTypeAsNumber(S settingKey) {
        EditTextPreference preference = findPreference(settingKey);
        if (preference != null) {
            preference.setOnBindEditTextListener(editText -> editText.setInputType(InputType.TYPE_CLASS_NUMBER));
        }
    }

    protected void showAlertDialog(DialogData data) {
        if (data == null) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(data.getTitle())
                .setMessage(data.getMessage())
                .setPositiveButton(data.getPositiveLabel(), data.getPositiveListener())
                .setNegativeButton(R.string.button_cancel, null)
                .setCancelable(true);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    protected final VM getViewModel() {
        return mViewModel;
    }

    protected abstract int getXmlSettingsId();

    protected abstract VM initViewModel();

    protected abstract S[] getSettings();

    /**
     * @return True for the change to be applied to the preference, false otherwise.
     */
    protected abstract boolean onPreferenceChange(S key, Preference preference, Object update);

    private void initManagedSettings() {
        for (S key : getSettings()) {
            Preference preference = findPreference(key);
            if (preference != null) {
                preference.setPersistent(false);
                preference.setOnPreferenceChangeListener(
                        (pref, newValue) -> SettingsFragment.this.onPreferenceChange(key, pref, newValue));
            }
        }
    }

    private void initObservations(VM viewModel, SettingsBinding<S, VD> binding) {
        LifecycleOwner owner = getViewLifecycleOwner();
        viewModel.observeDialogData(owner, this::showAlertDialog);
        viewModel.observeViewData(owner, (viewData) -> binding.observeData(owner, viewData));
        viewModel.observeTitle(getViewLifecycleOwner(), this::updateTitle);
    }

    private void updateTitle(String title) {
        Activity activity = getActivity();

        if (activity instanceof MainActivity && title != null && !title.isEmpty()) {
            ((MainActivity) activity).updateTitle(title);
        }
    }

}
