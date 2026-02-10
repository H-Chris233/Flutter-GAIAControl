/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.devicelogs;

import android.app.Application;
import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.DownloadLogsState;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.repository.logs.DeviceLogsRepository;
import com.qualcomm.qti.gaiaclient.repository.logs.LogsError;
import com.qualcomm.qti.gaiaclient.repository.logs.LogsProgress;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEventType;
import com.qualcomm.qti.gaiaclient.ui.common.progress.DialogViewData;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressAction;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class DownloadLogsViewModel extends ProgressViewModel<LogsDialog, Void> {

    private final DeviceLogsRepository deviceLogsRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public DownloadLogsViewModel(@NonNull Application application, DeviceLogsRepository deviceLogsRepository) {
        super(application);
        this.deviceLogsRepository = deviceLogsRepository;
        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
        deviceLogsRepository.reset(getApplication().getApplicationContext());
    }

    @Override
    protected void onDialogCancelled(LogsDialog dialogId) {
        // all dialogs are errors => going back to the previous screen when dialog is cancelled
        navigateBack();
    }

    @Override
    protected void onDialogOption(LogsDialog dialogId, @NonNull Void aVoid) {
        // all dialogs are errors => going back to the previous screen when user selects an option
        navigateBack();
    }

    @Override
    protected void onAction(ProgressAction action) {
        if (action == null) {
            return;
        }

        switch (action) {
            case CANCEL:
                deviceLogsRepository.cancelDownload(getApplication().getApplicationContext());
                navigateBack();
                break;

            case DONE:
                Uri uri = deviceLogsRepository.getLogFileUri();

                if (uri == null) {
                    showDialog(LogsDialog.ERROR_FILE_NOT_FOUND, null);
                    return;
                }

                shareFile(uri);
                break;
        }
    }

    private void navigateBack() {
        sendEvent(new NavigationEvent(NavigationEventType.NAVIGATE_BACK));
    }

    private void shareFile(@NonNull Uri uri) {
        sendEvent(new ShareFileNavigationEvent(uri));
    }

    private void onLogsProgress(Resource<LogsProgress, LogsError> resource) {
        if (resource == null) {
            return;
        }

        switch (resource.getType()) {
            case DATA:
                onLogsProgress(resource.getData());
                break;
            case ERROR:
                onLogsError(resource.getError());
                break;
        }
    }

    private void onLogsProgress(LogsProgress progress) {
        if (progress == null) {
            return;
        }

        DownloadLogsState state = progress.getState();
        if (state != null) {
            updateProgress(getMessageFromState(state), state == DownloadLogsState.DOWNLOAD,
                           progress.getProgress());
            if (state == DownloadLogsState.READY) {
                onProgressDone("");
            }
        }
    }

    @NonNull
    private String getMessageFromState(DownloadLogsState state) {
        if (state == null) {
            return "";
        }

        switch (state) {
            case DOWNLOAD:
                return getApplication().getString(R.string.logs_state_download);
            case WRITING:
                return getApplication().getString(R.string.logs_state_preparing_file);
            case READY:
                return getApplication().getString(R.string.logs_state_file_ready);
            case INITIALISATION:
                return getApplication().getString(R.string.logs_state_init);
            default:
                return "";
        }
    }

    private void onLogsError(LogsError error) {
        if (error == null) {
            return;
        }

        showDialog(LogsDialog.valueOf(error), error);
    }

    private void showDialog(@NonNull LogsDialog dialogId, @Nullable LogsError error) {
        Context context = getApplication().getBaseContext();
        String message = dialogId.getMessage(context) + (error == null ? "" : ("\nError: " + error));

        DialogViewData<LogsDialog, Void> data =
                new DialogViewData<>(dialogId, dialogId.getTitle(context), message);
        data.setCancelable(true);
        data.setPositiveOption(LogDialogOption.OK);

        showDialog(data);
    }

    private class DataObservers extends Observers {
        private final Observer<Resource<LogsProgress, LogsError>> progressObserver =
                DownloadLogsViewModel.this::onLogsProgress;

        @Override
        protected void registerObservers() {
            deviceLogsRepository.getLogsProgressLiveData().observeForever(progressObserver);
        }

        @Override
        protected void unregisterObservers() {
            deviceLogsRepository.getLogsProgressLiveData().removeObserver(progressObserver);
        }
    }
}
