/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.upgrade;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.BuildConfig;
import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformation;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.Device;
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.DeviceInformationRepository;
import com.qualcomm.qti.gaiaclient.repository.deviceinfo.Versions;
import com.qualcomm.qti.gaiaclient.repository.service.ServiceAvailabilityImpl;
import com.qualcomm.qti.gaiaclient.repository.upgrade.SizeType;
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeOptions;
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UpgradeSettingsViewModel extends SettingsViewModel<UpgradeSettings, UpgradeSettingsViewData> {

    private final StickyLiveData<Boolean> mShowTransferOptions = new StickyLiveData<>();

    private final DeviceInformationRepository deviceInfoRepository;

    private final UpgradeRepository upgradeRepository;

    private final ConnectionRepository connectionRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public UpgradeSettingsViewModel(@NonNull Application application, ConnectionRepository connectionRepository,
                                    DeviceInformationRepository deviceInfoRepository,
                                    UpgradeRepository upgradeRepository) {
        super(application, connectionRepository);
        this.deviceInfoRepository = deviceInfoRepository;
        this.upgradeRepository = upgradeRepository;
        this.connectionRepository = connectionRepository;
        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    @Override
    protected UpgradeSettingsViewData initViewData(Context context) {
        return new UpgradeSettingsViewData(context);
    }

    @Override
    protected void onConnectionStateUpdated(ConnectionState state) {
        boolean connected = state == ConnectionState.CONNECTED;

        for (UpgradeSettings key : UpgradeSettings.values()) {
            if (key == UpgradeSettings.CHECK_FOR_UPDATES) {
                setSettingEnable(key, connected && ServiceAvailabilityImpl.INSTANCE.isAvailable(getApplication()));
            }
            else if (key != UpgradeSettings.GAIA_AND_PROTOCOL_VERSIONS
                    && key != UpgradeSettings.APPLICATION_BUILD_ID
                    && key != UpgradeSettings.APPLICATION_VERSION) {
                setSettingEnable(key, connected);
            }
        }
    }

    void setObservers(LifecycleOwner owner, Observer<Boolean> transferOptionsObserver) {
        mShowTransferOptions.observe(owner, transferOptionsObserver);
    }

    void initDeveloperOptions() {
        setSettingVisibility(UpgradeSettings.DEVELOPER_OPTIONS, BuildConfig.UPGRADE_DEVELOPER_OPTIONS);
    }

    void updateDeviceInformation() {
        deviceInfoRepository.fetchDeviceInfo(getApplication().getApplicationContext(), DeviceInfo.APPLICATION_VERSION);
        deviceInfoRepository.fetchDeviceInfo(getApplication().getApplicationContext(), DeviceInfo.SYSTEM_INFORMATION);
    }

    void setUseDefaultTransferOptions(boolean use, boolean changedByUser) {
        if (!changedByUser || use) {
            // set to use default, no confirmation necessary
            showTransferOptions(!use);
        }
        else {
            // unsafe operation: requires user confirmation
            showTransferOptions(false); // cancel and only update if the user confirms
            String title =
                    getApplication()
                            .getString(R.string.settings_upgrade_transfer_default_dialog_title);
            String message =
                    getApplication()
                            .getString(R.string.settings_upgrade_transfer_default_dialog_message);
            String positiveLabel = getApplication().getString(R.string.button_confirm);
            setAlert(title, message, positiveLabel,
                     (dialogInterface, i) -> showTransferOptions(true));
        }
    }

    void startUpgrade(Context context, Uri fileLocation, UpgradeOptions options) {
        upgradeRepository.startUpgrade(context, fileLocation, options);
    }

    void initChunkSize() {
        updateChunkSize(upgradeRepository.getSize(SizeType.DEFAULT));
    }

    void setChunkSize(long value) {
        int maximum = upgradeRepository.getSize(SizeType.MAX);

        if (value < 1 || maximum < value) {
            // size must be within 1 to max size supported by device
            String title =
                    getApplication().getString(
                            R.string.settings_upgrade_chunk_size_alert_out_of_range_title);
            String message =
                    getApplication().getString(
                            R.string.settings_upgrade_chunk_size_alert_out_of_range_message,
                            String.valueOf(maximum));
            int alternativeValue = value < 1 ? 1 : maximum;
            String positiveLabel =
                    getApplication().getString(
                            R.string.settings_upgrade_chunk_size_alert_out_of_range_positive_label,
                            alternativeValue);
            setAlert(title, message, positiveLabel,
                     (dialogInterface, i) -> updateChunkSize(alternativeValue));
            return;
        }

        // value is within the bounds, it can be set on the UI
        updateChunkSize((int) value); // max size is 65k, cast is ok
    }

    private void showTransferOptions(boolean visible) {
        mShowTransferOptions.postValue(visible);
        for (UpgradeSettings setting : UpgradeSettings.TRANSFER_SETTINGS) {
            if (!setting.isPersistent()) {
                setSettingVisibility(setting, visible);
            }
        }
    }

    private void updateChunkSize(Integer value) {
        if (value == null) {
            // nothing to display
            return;
        }

        String label =
                getApplication().getResources()
                        .getQuantityString(R.plurals.settings_upgrade_chunk_size_details,
                                           value, value);
        setSettingSummary(UpgradeSettings.CHUNK_SIZE, label);
        setSettingValue(UpgradeSettings.CHUNK_SIZE, String.valueOf(value));
    }

    private void onVersions(Versions versions) {
        if (versions == null) {
            return;
        }

        updateGaiaVersions(versions.getGaia(), versions.getProtocol());
        updateApplicationVersion(versions.getApplication());
    }

    private void updateGaiaVersions(Integer gaiaVersion, Long protocolVersion) {
        // getting the labels
        String gaiaVersionLabel = gaiaVersion == null ?
                                  getApplication().getString(R.string.info_unknown) :
                                  String.valueOf(gaiaVersion);
        String protocolVersionLabel = protocolVersion == null ?
                                      getApplication().getString(R.string.info_unknown)
                                                              : String.valueOf(protocolVersion);

        // updating UI
        String summary =
                getApplication().getString(R.string.settings_upgrade_versions_summary,
                                           gaiaVersionLabel, protocolVersionLabel);
        setSettingSummary(UpgradeSettings.GAIA_AND_PROTOCOL_VERSIONS, summary);
    }

    private void updateApplicationVersion(String version) {
        // get the data from the result
        String versionLabel = version != null ? version : getApplication()
                .getString(R.string.info_unknown);

        // update data value
        setSettingSummary(UpgradeSettings.APPLICATION_VERSION, versionLabel);
    }

    private void onSystemInformation(List<SystemInformation> infos) {
        if (infos == null || infos.isEmpty()) {
            return;
        }

        infos.forEach(info -> {
            if (info instanceof SystemInformation.ApplicationBuildId) {
                SystemInformation.ApplicationBuildId id = (SystemInformation.ApplicationBuildId) info;
                updateApplicationBuildId(id);
            }
        });
    }

    private void updateApplicationBuildId(SystemInformation.ApplicationBuildId id) {
        String value = id.getValue().getText();

        // update data value
        setSettingSummary(UpgradeSettings.APPLICATION_BUILD_ID, value);
    }

    public void checkForServiceAvailability(Context context) {
        Device device = connectionRepository.getDevice();
        boolean connected = device != null && device.getState() == ConnectionState.CONNECTED;
        setSettingEnable(UpgradeSettings.CHECK_FOR_UPDATES,
                         connected && ServiceAvailabilityImpl.INSTANCE.isAvailable(context));
    }

    private class DataObservers extends Observers {
        private final Observer<Versions> versionsObserver = UpgradeSettingsViewModel.this::onVersions;
        private final Observer<List<SystemInformation>> systemInformationObserver =
                UpgradeSettingsViewModel.this::onSystemInformation;

        @Override
        protected void registerObservers() {
            deviceInfoRepository.getVersions().observeForever(versionsObserver);
            deviceInfoRepository.getSystemInformation().observeForever(systemInformationObserver);
        }

        @Override
        protected void unregisterObservers() {
            deviceInfoRepository.getVersions().removeObserver(versionsObserver);
            deviceInfoRepository.getSystemInformation().removeObserver(systemInformationObserver);
        }
    }
}
