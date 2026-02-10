/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.logs;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.repository.Resource;

public interface DeviceLogsRepository {

    void init();

    void reset(Context context);

    @NonNull
    LiveData<Resource<LogSizes, LogsError>> getLogsSizesLiveData();

    @NonNull
    LiveData<Resource<LogsProgress, LogsError>> getLogsProgressLiveData();

    void fetchLogSizes(Context context);

    void downloadLogs(Context context);

    void cancelDownload(Context context);

    Uri getLogFileUri();
}
