/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.DebugInfo;
import com.qualcomm.qti.gaiaclient.core.data.LogInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.LogSize;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PanicLogInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.DebugPlugin;
import com.qualcomm.qti.gaiaclient.core.logs.DownloadLogsHelper;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DebugPublisher;

import java.io.File;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class V3DebugPlugin extends V3QTILPlugin implements DebugPlugin {

    private static final String TAG = "V3DebugPlugin";

    private final DebugPublisher mDebugPublisher = new DebugPublisher();

    private final DownloadLogsHelper mDownloadLogsHelper;

    public V3DebugPlugin(@NonNull GaiaSender sender, @NonNull DownloadLogsHelper helper) {
        super(QTILFeature.DEBUG, sender);
        this.mDownloadLogsHelper = helper;
    }

    @Override
    public void fetchLogInfo(LogInfo info) {
        sendPacket(COMMANDS.V1_GET_DEBUG_LOG_INFO, info.getBytes());
    }

    @Override // implements DebugPlugin
    public void downloadLogs(File file) {
        mDownloadLogsHelper.download(file);
    }

    @Override // implements DebugPlugin
    public void cancelDownload() {
        mDownloadLogsHelper.cancel();
    }

    @Override // implements DebugPlugin
    public void initTransferSession() {
        sendPacket(COMMANDS.V1_GET_PANIC_LOG);
    }

    @Override // extends V3QTILPlugin
    protected void onNotification(NotificationPacket packet) {
        // nothing to do
    }

    @Override // extends V3QTILPlugin
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        switch (response.getCommand()) {
            case COMMANDS.V1_GET_PANIC_LOG:
                onGetPanicLogs(response.getData());
                break;
            case COMMANDS.V1_GET_DEBUG_LOG_INFO:
                onGetLogInfo(response.getData());
                break;
        }
    }

    @Override // extends V3QTILPlugin
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        V3ErrorStatus status = errorPacket.getV3ErrorStatus();
        int value = errorPacket.getStatusValue();
        Log.w(TAG, String.format("[onError] error received, status=%1$s, value=%2$d", status,
                                 value));
        Reason reason = getReason(status, value);

        switch (errorPacket.getCommand()) {
            case COMMANDS.V1_GET_DEBUG_LOG_INFO:
                mDebugPublisher.publishError(DebugInfo.LOG_SIZES, reason);
                break;

            case COMMANDS.V1_GET_PANIC_LOG:
                mDownloadLogsHelper.onGetPanicLogsError(reason);
                break;
        }
    }

    @Override // extends V3QTILPlugin
    protected void onStarted() {
        PublicationManager publicationManager = getPublicationManager();
        publicationManager.register(mDebugPublisher);
        mDownloadLogsHelper.start(this, getQtilManager().getBasicPlugin());
    }

    @Override // extends V3QTILPlugin
    protected void onStopped() {
        // stopped when device is disconnected
        PublicationManager publicationManager = getPublicationManager();
        publicationManager.unregister(mDebugPublisher);
        mDownloadLogsHelper.stop();
    }

    @Override // extends V3QTILPlugin
    protected void onFailed(GaiaPacket source, Reason reason) {
        if (!(source instanceof V3Packet)) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.");
            return;
        }

        V3Packet packet = (V3Packet) source;

        switch (packet.getCommand()) {
            case COMMANDS.V1_GET_DEBUG_LOG_INFO:
                mDebugPublisher.publishError(DebugInfo.LOG_SIZES, reason);
                break;

            case COMMANDS.V1_GET_PANIC_LOG:
                mDownloadLogsHelper.onGetPanicLogsError(reason);
                break;
        }
    }

    private void onGetLogInfo(byte[] data) {
        LogSize info = new LogSize(data);
        mDebugPublisher.publishLogSize(info);
    }

    private void onGetPanicLogs(byte[] data) {
        PanicLogInfo info = new PanicLogInfo(data);
        mDownloadLogsHelper.onGetPanicLogs(info);
    }

    private Reason getReason(V3ErrorStatus v3ErrorStatus, int status) {
        if (!v3ErrorStatus.equals(V3ErrorStatus.FEATURE_SPECIFIC)) {
            return Reason.valueOf(v3ErrorStatus);
        }

        switch (status) {
            case ERRORS.V1_NO_DATA:
                return Reason.LOGS_NO_DATA;
            case ERRORS.V1_NO_DEBUG_PARTITION:
                return Reason.LOGS_NO_DEBUG_PARTITION;
            case ERRORS.V1_INVALID_PARAMETERS:
                return Reason.MALFORMED_REQUEST;
            case ERRORS.V1_FAILED_BY_UNKNOWN_REASON:
            default:
                return Reason.UNKNOWN;
        }
    }

    private static final class COMMANDS {

        static final int V1_GET_DEBUG_LOG_INFO = 0x01;
        static final int V1_GET_PANIC_LOG = 0x03;
    }

    private static final class ERRORS {

        static final int V1_NO_DATA = 0x81;
        static final int V1_INVALID_PARAMETERS = 0x85;
        static final int V1_NO_DEBUG_PARTITION = 0x86;
        static final int V1_FAILED_BY_UNKNOWN_REASON = 0x8F;
    }
}
