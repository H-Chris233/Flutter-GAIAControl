/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.logs;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.core.data.LogInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.features.FeaturesRepository;
import com.qualcomm.qti.gaiaclient.repository.logs.DeviceLogsRepository;
import com.qualcomm.qti.gaiaclient.repository.logs.LogSizes;
import com.qualcomm.qti.gaiaclient.repository.logs.LogsError;
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class LogsSettingsViewModel extends SettingsViewModel<LogsSettings, LogsSettingsViewData> {

    private final FeaturesRepository featuresRepository;

    private final SystemRepository systemRepository;

    private final DeviceLogsRepository deviceLogsRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public LogsSettingsViewModel(@NonNull Application application, ConnectionRepository connectionRepository,
                                 DeviceLogsRepository deviceLogsRepository, FeaturesRepository featuresRepository,
                                 SystemRepository systemRepository) {
        super(application, connectionRepository);
        this.featuresRepository = featuresRepository;
        this.systemRepository = systemRepository;
        this.deviceLogsRepository = deviceLogsRepository;
        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    @Override
    protected LogsSettingsViewData initViewData(Context context) {
        return new LogsSettingsViewData(context);
    }

    void updateData() {
        // check developer options
        systemRepository.checkAndroidDeveloperModeState(getApplication().getApplicationContext());

        // update device logs UI
        if (featuresRepository.isFeatureAvailable(QTILFeature.DEBUG)) {
            // display the sizes
            setSettingVisibility(LogsSettings.LOGS_SIZES, true);
            // check log sizes
            deviceLogsRepository.fetchLogSizes(getApplication().getApplicationContext());
        }
    }

    void getDeviceLogs(Context context) {
        deviceLogsRepository.downloadLogs(context);
    }

    private void onLogsSizeUpdated(Resource<LogSizes, LogsError> resource) {
        LogSizes sizes = resource != null ? resource.getData() : null;
        LogsError error = resource != null ? resource.getError() : null;

        // update sizes display
        String label = sizes != null ? sizes.getLabel(getApplication()) : "";
        setSettingSummary(LogsSettings.LOGS_SIZES, label);
        // update the get logs button visibility depending on logs availability
        updateDeviceLogsVisibility(error == null && sizes != null
                                           && sizes.getSize(LogInfo.LOG_FILE) > 0);
    }

    private void updateDeviceLogsVisibility(boolean available) {
        setSettingVisibility(LogsSettings.DEVICE_LOGS, available);
    }

    private void updateBugReportVisibility(boolean available) {
        setSettingVisibility(LogsSettings.BUG_REPORT, available);
    }

    private class DataObservers extends Observers {
        private final Observer<Resource<LogSizes, LogsError>> logsSizeObserver =
                LogsSettingsViewModel.this::onLogsSizeUpdated;
        private final Observer<Boolean> androidDeveloperModeObserver =
                LogsSettingsViewModel.this::updateBugReportVisibility;

        @Override
        protected void registerObservers() {
            deviceLogsRepository.getLogsSizesLiveData().observeForever(logsSizeObserver);
            systemRepository.getAndroidDeveloperModeLiveData().observeForever(androidDeveloperModeObserver);
        }

        @Override
        protected void unregisterObservers() {
            deviceLogsRepository.getLogsSizesLiveData().removeObserver(logsSizeObserver);
            systemRepository.getAndroidDeveloperModeLiveData().removeObserver(androidDeveloperModeObserver);
        }
    }
}
