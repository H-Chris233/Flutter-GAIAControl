/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.download

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.preference.EditTextPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import com.google.android.material.datepicker.MaterialDatePicker
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.Navigator
import com.qualcomm.qti.gaiaclient.ui.settings.common.PersistentSettingsFragment
import com.qualcomm.qti.gaiaclient.ui.upgrade.files.FilesFilters
import com.qualcomm.qti.gaiaclient.ui.upgrade.tags.Tag
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadSettingsFragment :
    PersistentSettingsFragment<DownloadSettings, DownloadSettingsViewData, DownloadSettingsViewModel>() {

    private val args: DownloadSettingsFragmentArgs by navArgs()

    override fun getXmlSettingsId(): Int = R.xml.download_settings

    override fun getNonPersistentSettings(): Array<DownloadSettings> = DownloadSettings.NON_PERSISTENT

    override fun getPersistentSettings(): Array<DownloadSettings> = DownloadSettings.PERSISTENT

    override fun initViewModel(): DownloadSettingsViewModel {
        return ViewModelProvider(this)[DownloadSettingsViewModel::class.java]
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)

        val hardwarePreference: EditTextPreference? = findPreference(DownloadSettings.HARDWARE_VERSION)
        if (hardwarePreference != null) {
            updateHardwareVersionPreference(hardwarePreference)
        }

        val includeTagsPreference: MultiSelectListPreference? = findPreference(DownloadSettings.INCLUDE_TAGS)
        if (includeTagsPreference != null) {
            updateTagsPreference(includeTagsPreference)
        }

        val excludeTagsPreference: MultiSelectListPreference? = findPreference(DownloadSettings.EXCLUDE_TAGS)
        if (excludeTagsPreference != null) {
            updateTagsPreference(excludeTagsPreference)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        if (isKey(preference.key, DownloadSettings.CONTINUE)) {
            val filters = FilesFilters(
                hardwareVersion = getText(DownloadSettings.HARDWARE_VERSION),
                withTags = getMultiSelectionValues(DownloadSettings.INCLUDE_TAGS).toTypedArray(),
                withoutTags = getMultiSelectionValues(DownloadSettings.EXCLUDE_TAGS).toTypedArray(),
                createdAfter = findPreference<DatePreference>(DownloadSettings.CREATED_AFTER)?.dateIso8601 ?: ""
            )
            Navigator.navigate(root, DownloadSettingsFragmentDirections.toGetFiles(args.options, filters))
            return true
        }

        if (isKey(preference.key, DownloadSettings.CREATED_AFTER)) {
            showDatePicker((preference as DatePreference).dateInMs)
        }

        return super.onPreferenceTreeClick(preference)
    }

    override fun onSharedPreferenceChange(key: String, preference: Preference) {
        if (isKey(key, DownloadSettings.HARDWARE_VERSION) && preference is EditTextPreference) {
            updateHardwareVersionPreference(preference)
        } else if (isKey(key, DownloadSettings.INCLUDE_TAGS) && preference is MultiSelectListPreference) {
            updateTagsPreference(preference)
        } else if (isKey(key, DownloadSettings.EXCLUDE_TAGS) && preference is MultiSelectListPreference) {
            updateTagsPreference(preference)
        }
    }

    private fun updateHardwareVersionPreference(preference: EditTextPreference) {
        val text = preference.text
        preference.summary = if (text != null && text.isNotEmpty()) text else
            context?.getString(R.string.settings_download_no_hardware_version) ?: ""
    }

    private fun updateTagsPreference(preference: MultiSelectListPreference) {
        // persistent settings: entries cannot be set with SettingsViewData
        val entries = Tag.entries()
        preference.entries = entries.first
        preference.entryValues = entries.second
        // update summary depending on selection
        val selection = preference.values
        preference.summary =
            if (selection.isNotEmpty()) selection.joinToString(separator = ", ") else context?.getString(R.string.settings_download_filter_tags_no_selection)
    }

    private fun showDatePicker(dateInMs: Long) {
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.settings_download_created_after_dialog_title)
        if (dateInMs != DatePreference.NO_SELECTION) {
            builder.setSelection(dateInMs)
        }

        val datePicker = builder.build()
        datePicker.addOnPositiveButtonClickListener {
            findPreference<DatePreference>(DownloadSettings.CREATED_AFTER)?.dateInMs = it
        }
        datePicker.show(parentFragmentManager, DatePreference.DATE_PICKER_TAG)
    }

    override fun onPreferenceChange(key: DownloadSettings, preference: Preference, update: Any): Boolean {
        return true
    }
}
