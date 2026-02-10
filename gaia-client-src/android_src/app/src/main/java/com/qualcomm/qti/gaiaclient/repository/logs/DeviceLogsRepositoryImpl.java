/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.logs;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.DebugInfo;
import com.qualcomm.qti.gaiaclient.core.data.DownloadLogsState;
import com.qualcomm.qti.gaiaclient.core.data.LogInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.LogSize;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.DebugSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.CancelDeviceLogsRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.DownloadDeviceLogsRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetLogInfoRequest;

import java.io.File;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class DeviceLogsRepositoryImpl extends DeviceLogsRepositoryData {

    private final RequestListener<Void, Void, Reason> mDownloadLogsListener =
            new RequestListener<Void, Void, Reason>() {
                @Override
                public void onProgress(Void aVoid) {
                }

                @Override
                public void onComplete(Void aVoid) {
                    // request initiated
                }

                @Override
                public void onError(Reason reason) {
                    updateProgressError(LogsError.valueOf(reason));
                }
            };

    private final RequestListener<Void, Void, Reason> mLogInfoListener =
            new RequestListener<Void, Void, Reason>() {
                @Override
                public void onProgress(Void aVoid) {
                    // nothing to do
                }

                @Override
                public void onComplete(Void aVoid) {
                    // request initiated
                }

                @Override
                public void onError(Reason reason) {
                    updateLogSizesError(LogsError.valueOf(reason));
                }
            };

    private final DebugSubscriber mDebugSubscriber = new DebugSubscriber() {
        @Override
        public void onLogSize(LogSize size) {
            LogSizes sizes = LogSizes.addLogSize(getLogSizes(), size);
            updateLogSizes(sizes);
        }

        @Override
        public void onDownloadProgress(DownloadLogsState state, double progress) {
            if (state == DownloadLogsState.DOWNLOAD) {
                // soft update of the live data as a new one is expected soon
                postProgress(new LogsProgress(DownloadLogsState.DOWNLOAD, progress));
            }
            else {
                // state update: hard update as no other update is expected soon
                setProgress(new LogsProgress(state, progress));
            }
        }

        @Override
        public void onError(DebugInfo info, Reason reason) {
            switch (info) {
                case DOWNLOAD:
                    updateProgressError(LogsError.valueOf(reason));
                    break;
                case LOG_SIZES:
                    updateLogSizesError(LogsError.valueOf(reason));
                    break;
            }
        }
    };

    @Inject
    public DeviceLogsRepositoryImpl() {
    }

    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mDebugSubscriber);
    }

    public void fetchLogSizes(Context context) {
        fetchLogInfo(context, LogInfo.PARTITION_SIZE);
        fetchLogInfo(context, LogInfo.LOG_FILE);
    }

    @Override
    protected void executeDownload(Context context, File file) {
        DownloadDeviceLogsRequest request = new DownloadDeviceLogsRequest(mDownloadLogsListener, file);
        GaiaClientService.getRequestManager().execute(context, request);
    }

    public void cancelDownload(Context context) {
        CancelDeviceLogsRequest request = new CancelDeviceLogsRequest();
        GaiaClientService.getRequestManager().execute(context, request);
    }

    public void reset(Context context) {
        cancelDownload(context);
        super.reset(context);
    }

    private void fetchLogInfo(Context context, LogInfo info) {
        GetLogInfoRequest request = new GetLogInfoRequest(mLogInfoListener, info);
        GaiaClientService.getRequestManager().execute(context, request);
    }
}
