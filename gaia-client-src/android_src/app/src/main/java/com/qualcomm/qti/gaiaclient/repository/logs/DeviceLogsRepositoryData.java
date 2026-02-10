/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.logs;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.DownloadLogsState;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.utils.FileUtils;

import java.io.File;

public abstract class DeviceLogsRepositoryData implements DeviceLogsRepository {

    private static final String FILES_PATH = "device_logs";

    private static final String FILE_NAME = "device_logs.xed";

    private final StickyLiveData<Resource<LogSizes, LogsError>> logsSizesLiveData = new StickyLiveData<>();

    private final StickyLiveData<Resource<LogsProgress, LogsError>> logsProgressLiveData = new StickyLiveData<>();

    private final StickyLiveData<Uri> logsUriLiveData = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<Resource<LogSizes, LogsError>> getLogsSizesLiveData() {
        return logsSizesLiveData;
    }

    @NonNull
    @Override
    public LiveData<Resource<LogsProgress, LogsError>> getLogsProgressLiveData() {
        return logsProgressLiveData;
    }

    @Override
    public void reset(Context context) {
        logsProgressLiveData.setValue(null);
        logsUriLiveData.setValue(null);
    }

    @Override
    public Uri getLogFileUri() {
        return logsUriLiveData.getValue();
    }

    @Override
    public final void downloadLogs(Context context) {
        postProgress(new LogsProgress(DownloadLogsState.INITIALISATION, 0));

        // creating the file
        File file = FileUtils.createFile(context, FILES_PATH, FILE_NAME);
        Uri uri = FileUtils.getUriFromFile(context, file, FileUtils.FILE_PROVIDER_AUTHORITY);

        if (file == null || uri == null) {
            updateProgressError(LogsError.FILE_CREATION_FAILED);
            return;
        }

        updateLogsUri(uri);
        executeDownload(context, file);
    }

    protected void updateLogSizes(LogSizes data) {
        logsSizesLiveData.postValue(Resource.data(data));
    }

    protected void updateLogSizesError(LogsError error) {
        logsSizesLiveData.postValue(Resource.error(logsSizesLiveData.getValue(), error));
    }

    protected void setProgress(LogsProgress progress) {
        logsProgressLiveData.postValue(Resource.data(progress));
    }

    protected void postProgress(LogsProgress progress) {
        logsProgressLiveData.postValue(Resource.data(progress));
    }

    protected void updateProgressError(LogsError error) {
        logsProgressLiveData.postValue(Resource.error(logsProgressLiveData.getValue(), error));
    }

    protected void updateLogsUri(Uri uri) {
        logsUriLiveData.postValue(uri);
    }

    @Nullable
    protected LogSizes getLogSizes() {
        Resource<LogSizes, LogsError> resource = logsSizesLiveData.getValue();
        return resource == null ? null : resource.getData();
    }

    protected abstract void executeDownload(Context context, File file);

}
