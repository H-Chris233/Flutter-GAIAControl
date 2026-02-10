/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.ui.settings.upgrade

import android.net.Uri
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.viewModels
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeOptions
import com.qualcomm.qti.gaiaclient.ui.Navigator
import com.qualcomm.qti.gaiaclient.ui.settings.common.PersistentSettingsFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpgradeSettingsFragment :
    PersistentSettingsFragment<UpgradeSettings, UpgradeSettingsViewData, UpgradeSettingsViewModel>() {

    private val viewModel: UpgradeSettingsViewModel by viewModels()

    private val fileSelectionLauncher =
        registerForActivityResult(GetContent()) { result: Uri? -> onFileSelectionResult(result) }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        super.onCreatePreferences(savedInstanceState, rootKey)
        setEditTextInputTypeAsNumber(UpgradeSettings.CHUNK_SIZE)
    }

    override fun onResume() {
        super.onResume()

        viewModel.initChunkSize()
        viewModel.updateDeviceInformation() // refreshes device information if the device was updated
        viewModel.setUseDefaultTransferOptions(getSwitchState(UpgradeSettings.PERSISTENT_USE_DEFAULT), false)
        viewModel.initDeveloperOptions()
        viewModel.checkForServiceAvailability(context)
    }

    override fun getXmlSettingsId(): Int {
        return R.xml.upgrade_settings
    }

    override fun initViewModel(): UpgradeSettingsViewModel {
        viewModel.setObservers(viewLifecycleOwner) { show: Boolean -> updateTransferOptionsVisibility(show) }
        return viewModel
    }

    override fun getNonPersistentSettings(): Array<UpgradeSettings> {
        return UpgradeSettings.NON_PERSISTENT
    }

    override fun getPersistentSettings(): Array<UpgradeSettings> {
        return UpgradeSettings.PERSISTENT
    }

    override fun onPreferenceChange(key: UpgradeSettings?, preference: Preference, update: Any): Boolean {
        if (UpgradeSettings.CHUNK_SIZE == key && update is String) {
            val size: Long = update.toLong() // long to catch as many characters as possible
            viewModel.setChunkSize(size)
            return false // view model will update the state depending on validation
        }
        return true
    }

    override fun onSharedPreferenceChange(key: String, preference: Preference) {
        if (isKey(key, UpgradeSettings.PERSISTENT_USE_DEFAULT)
            && preference is SwitchPreference
        ) {
            val value = preference.isChecked
            viewModel.setUseDefaultTransferOptions(value, true)
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {
        val key = preference.key
        if (isKey(key, UpgradeSettings.SELECT_FILE)) {
            startFileSelection()
            return true
        } else if (isKey(key, UpgradeSettings.CHECK_FOR_UPDATES)) {
            val selection = UpgradeOptions(
                getSwitchState(UpgradeSettings.PERSISTENT_USE_DEFAULT),
                getEditTextNumber(UpgradeSettings.CHUNK_SIZE),
                getSwitchState(UpgradeSettings.PERSISTENT_IS_LOGGED),
                getSwitchState(UpgradeSettings.PERSISTENT_IS_UPLOAD_FLUSHED),
                getSwitchState(UpgradeSettings.PERSISTENT_IS_UPLOAD_ACKNOWLEDGED),
                // TODO: Add switch to make configurable.
                true
            )
            Navigator.navigate(root, UpgradeSettingsFragmentDirections.toDownloadSettings(selection))
            return true
        }
        // nothing to do
        return false
    }

    private fun onFileSelectionResult(result: Uri?) {
        if (result != null) {
            val selection = UpgradeOptions(
                getSwitchState(UpgradeSettings.PERSISTENT_USE_DEFAULT),
                getEditTextNumber(UpgradeSettings.CHUNK_SIZE),
                getSwitchState(UpgradeSettings.PERSISTENT_IS_LOGGED),
                getSwitchState(UpgradeSettings.PERSISTENT_IS_UPLOAD_FLUSHED),
                getSwitchState(UpgradeSettings.PERSISTENT_IS_UPLOAD_ACKNOWLEDGED),
                // TODO: Add switch to make configurable.
                true
            )

            // start upgrade
            viewModel.startUpgrade(context, result, selection)
            Navigator.navigate(root, UpgradeSettingsFragmentDirections.toUpgrade())
        }
    }

    private fun startFileSelection() {
        fileSelectionLauncher.launch("*/*") // mime type to be anything
    }

    private fun updateTransferOptionsVisibility(show: Boolean) {
        for (setting in UpgradeSettings.TRANSFER_SETTINGS) {
            if (setting.isPersistent) {
                val preference = findPreference<Preference>(setting.getId(context))
                if (preference != null) {
                    preference.isVisible = show
                }
            }
        }
    }
}
