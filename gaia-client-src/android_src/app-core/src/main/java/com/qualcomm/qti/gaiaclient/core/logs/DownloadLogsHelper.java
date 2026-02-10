/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.logs;

import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.data.DebugInfo;
import com.qualcomm.qti.gaiaclient.core.data.DownloadLogsState;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PanicLogInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransferSetupResponse;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransferredData;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BasicPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.DebugPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3.DataTransferListener;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DebugPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ProtocolSubscriber;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;

public class DownloadLogsHelper {

    private static final String TAG = "DownloadLogsHelper";

    private static final boolean LOG_METHODS = DEBUG.Logs.PLUGIN_HELPER;

    private final DownloadingState mState = new DownloadingState();

    private final DebugPublisher mDebugPublisher = new DebugPublisher();

    private DebugPlugin mDebugPlugin;

    private BasicPlugin mBasicPlugin;

    private File file;

    @SuppressWarnings("FieldCanBeLocal")
    private final ProtocolSubscriber mProtocolSubscriber = new ProtocolSubscriber() {
        @Override
        public void onSizeInfo(SizeInfo info, int payloadSize) {
            if (info == SizeInfo.OPTIMUM_TX_PAYLOAD) {
                Logger.log(LOG_METHODS, TAG, "ProtocolSubscriber->onSizeInfo",
                           new Pair<>("tx_optimum_size", payloadSize));
                mState.setChunkSize(payloadSize);
            }
        }

        @Override
        public void onError(Object info, Reason reason) {
            if (info == SizeInfo.OPTIMUM_TX_PAYLOAD) {
                Logger.log(LOG_METHODS, TAG, "ProtocolSubscriber->onError",
                           new Pair<>("tx_optimum_reason", reason));
                mState.resetChunkSize();
            }
        }
    };

    private final DataTransferListener mDataTransferListener = new DataTransferListener() {
        @Override
        public void onSetUp(TransferSetupResponse setup) {
            DownloadLogsHelper.this.onDataTransferSet(setup.getSession());
        }

        @Override
        public void onDataTransferred(TransferredData data) {
            DownloadLogsHelper.this.onReceivedData(data.getSession(), data.getData());
        }

        @Override
        public void onTransferError(int session, Reason reason) {
            DownloadLogsHelper.this.onTransferError(session, reason);
        }
    };

    public DownloadLogsHelper(PublicationManager publicationManager) {
        publicationManager.register(mDebugPublisher);
        publicationManager.subscribe(mProtocolSubscriber);
    }

    public void start(DebugPlugin debugPlugin, BasicPlugin basicPlugin) {
        Logger.log(LOG_METHODS, TAG, "start");
        this.mDebugPlugin = debugPlugin;
        this.mBasicPlugin = basicPlugin;
    }

    public void stop() {
        Logger.log(LOG_METHODS, TAG, "stop");
        boolean wasDownloading = mState.resetAll();
        if (wasDownloading) {
            mDebugPublisher.publishError(DebugInfo.DOWNLOAD, Reason.DISCONNECTED);
        }
    }

    public void release() {
        Logger.log(LOG_METHODS, TAG, "release");
        this.mDebugPlugin = null;
        this.file = null;
    }

    public void download(File file) {
        Logger.log(LOG_METHODS, TAG, "download");
        boolean wasDownloading = mState.setIsDownloading(true);

        if (wasDownloading) {
            // nothing to do a download is already ongoing
            return;
        }

        if (mDebugPlugin == null) {
            // dispatch an error as this was specifically requested
            onError(Reason.NOT_AVAILABLE);
            return;
        }

        mDebugPublisher.publishDownloadProgress(DownloadLogsState.INITIALISATION, 0);
        this.file = file;
        mDebugPlugin.initTransferSession();
    }

    public void cancel() {
        Logger.log(LOG_METHODS, TAG, "cancel");
        mState.setIsDownloading(false);
    }

    public void onGetPanicLogs(PanicLogInfo info) {
        Logger.log(LOG_METHODS, TAG, "onGetPanicLogs");
        if (!mState.isDownloading()) {
            // transfer session was not fetched by this helper
            return;
        }

        if (info.getSize() == 0) {
            onError(Reason.NOT_AVAILABLE);
            return;
        }

        mState.setSessionId(info.getSessionId());
        mState.setSize(info.getSize());

        setupDataTransfer(info.getSessionId());
    }

    public void onGetPanicLogsError(Reason reason) {
        Logger.log(LOG_METHODS, TAG, "onGetPanicLogsError");
        if (!mState.isDownloading()) {
            // transfer session was not fetched by this helper
            return;
        }

        onError(reason);
    }

    public void onDataTransferSet(int sessionId) {
        Logger.log(LOG_METHODS, TAG, "onDataTransferSet");
        if (!mState.isDownloading()) {
            // not downloading
            return;
        }

        if (mState.getSessionId() != sessionId) {
            // not for this helper
            return;
        }

        requestNextData(sessionId, mState.getOffset(), mState.getChunkLength());
    }

    public void onTransferError(int sessionId, Reason reason) {
        Logger.log(LOG_METHODS, TAG, "onDataTransferError");
        if (!mState.isDownloading()) {
            // not downloading
            return;
        }

        if (mState.getSessionId() != sessionId) {
            // not for this helper
            return;
        }

        onError(reason);
    }

    public void onReceivedData(int sessionId, byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onReceivedData");
        if (!mState.isDownloading()) {
            // not downloading
            return;
        }

        if (mState.getSessionId() != sessionId) {
            // not for this helper
            return;
        }

        mState.add(data);
        mDebugPublisher.publishDownloadProgress(DownloadLogsState.DOWNLOAD, mState.getProgress());

        if (mState.hasMore()) {
            requestNextData(sessionId, mState.getOffset(), mState.getChunkLength());
        }
        else {
            // all data has been downloaded
            mBasicPlugin.unregisterDataTransferListener(mState.getSessionId());
            mDebugPublisher.publishDownloadProgress(DownloadLogsState.WRITING, 100);
            storeData(file);
            mDebugPublisher.publishDownloadProgress(DownloadLogsState.READY, 100);
            mState.resetDownloading();
        }
    }

    private void storeData(File file) {
        try (FileOutputStream output = new FileOutputStream(file)) {
            byte[] data = mState.pollData();
            while (data != null) {
                output.write(data);
                data = mState.pollData();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
            onError(Reason.FILE_WRITING_FAILED);
        }
    }

    private void setupDataTransfer(int sessionId) {
        mBasicPlugin.registerDataTransferListener(sessionId, mDataTransferListener);
        if (!mBasicPlugin.startDataTransfer(sessionId)) {
            onError(Reason.NOT_SUPPORTED);
        }
    }

    private void requestNextData(int sessionId, long offset, long length) {
        if (!mBasicPlugin.transferData(sessionId, offset, length)) {
            onError(Reason.NOT_SUPPORTED);
        }
    }

    private void onError(Reason reason) {
        mState.resetDownloading();
        mDebugPublisher.publishError(DebugInfo.DOWNLOAD, reason);
    }

}
