/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.PacketSentListener;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.Parameters;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.UpgradeStartAction;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.UpgradeStopAction;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.UpgradePlugin;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.UpgradePublisher;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeGaiaCommand;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelperListener;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeState;
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;

import java.util.concurrent.atomic.AtomicBoolean;

public class V3UpgradePlugin extends V3QTILPlugin implements UpgradePlugin, UpgradeHelperListener {

    private static final String TAG = "V3UpgradePlugin";

    private final UpgradeHelper upgradeHelper;

    private final AtomicBoolean isConnected = new AtomicBoolean(false);

    private final AtomicBoolean isPaused = new AtomicBoolean(false);

    private final UpgradePublisher upgradePublisher = new UpgradePublisher();

    public V3UpgradePlugin(@NonNull GaiaSender sender, UpgradeHelper helper) {
        super(QTILFeature.UPGRADE, sender);
        upgradeHelper = helper;
    }

    @Override
    public void onStarted() {
        // upgrade helper to be plugged in once notifications are registered only
        getPublicationManager().register(upgradePublisher);
    }

    @Override
    protected void onStopped() {
        upgradeHelper.onUnplugged();
        getPublicationManager().register(upgradePublisher);
    }

    @Override // UpgradeHelperListener
    public void cancelAll() {
        super.cancelAll(); // Plugin
    }

    @NonNull
    @Override
    public UpgradeHelper getUpgradeHelper() {
        return upgradeHelper;
    }

    @Override
    public void sendUpgradeMessage(byte[] data) {
        sendPacket(COMMANDS.V1_UPGRADE_CONTROL, data);
    }

    @Override
    public void sendUpgradeMessage(byte[] data, boolean acknowledged, boolean flushed, boolean useRwcp,
                                   PacketSentListener listener) {
        Parameters parameters = new Parameters(
                true,
                acknowledged && !useRwcp,
                flushed,
                useRwcp,
                listener,
                getDefaultTimeout()
        );
        sendPacket(COMMANDS.V1_UPGRADE_CONTROL, data, parameters);
    }

    @Override // UpgradeHelperListener
    public void setUpgradeModeOn(boolean useRwcp) {
        // Using default - Java doesn't support named arguments
        Parameters parameters = new Parameters(
                false,
                true,
                false,
                false,
                null,
                getDefaultTimeout()
        );
        if (useRwcp) {
            Parameters enableRwcpParams = new Parameters();
            sendPacket(COMMANDS.V1_SET_DATA_ENDPOINT_MODE, new byte[]{0x01}, enableRwcpParams);
        }
        sendPacket(COMMANDS.V1_UPGRADE_CONNECT, null, parameters);
    }

    @Override // UpgradeHelperListener
    public void setUpgradeModeOff() {
        isConnected.set(false);
        // Using default - Java doesn't support named arguments
        Parameters parameters = new Parameters(
                false,
                true,
                false,
                false,
                null,
                getDefaultTimeout()
        );
        sendPacket(COMMANDS.V1_UPGRADE_DISCONNECT, null, parameters);
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        switch (packet.getCommand()) {
            case NOTIFICATIONS.V1_UPGRADE_DATA:
                upgradeHelper.onUpgradeMessage(packet.getData());
                break;
            case NOTIFICATIONS.V2_UPGRADE_STOP_REQUEST:
                onUpgradeStopRequest(packet.getData());
                break;
            case NOTIFICATIONS.V2_UPGRADE_START_REQUEST:
                onUpgradeStartRequest(packet.getData());
                break;
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        switch (response.getCommand()) {
            case COMMANDS.V1_UPGRADE_CONNECT:
                isConnected.set(true);
                upgradeHelper.onUpgradeConnected();
                break;

            case COMMANDS.V1_UPGRADE_CONTROL:
                upgradeHelper.onAcknowledged();
                break;

            case COMMANDS.V1_UPGRADE_DISCONNECT:
                upgradeHelper.onUpgradeDisconnected();
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        V3ErrorStatus status = errorPacket.getV3ErrorStatus();

        switch (errorPacket.getCommand()) {
            case COMMANDS.V1_UPGRADE_CONNECT:
                upgradeHelper.onErrorResponse(UpgradeGaiaCommand.CONNECT, status);
                break;
            case COMMANDS.V1_UPGRADE_CONTROL:
                upgradeHelper.onErrorResponse(UpgradeGaiaCommand.CONTROL, status);
                break;
            case COMMANDS.V1_UPGRADE_DISCONNECT:
                upgradeHelper.onErrorResponse(UpgradeGaiaCommand.DISCONNECT, status);
                break;
        }
    }

    @Override
    protected void onFailed(GaiaPacket packet, Reason reason) {
        if (!(packet instanceof V3Packet)) {
            Log.w(TAG, "[onNotAvailable] Packet is not a V3Packet.");
            return;
        }

        upgradeHelper.onSendingFailed(reason);
    }

    public void onPlugged() {
        upgradeHelper.onPlugged(this);
    }

    private void onUpgradeStopRequest(byte[] data) {
        int ACTION_OFFSET = 0;
        UpgradeStopAction action = UpgradeStopAction.valueOf(BytesUtils.getUINT8(data, ACTION_OFFSET));
        // If unable to hold data, only DISCONNECT_UPGRADE is supported.
        if (!mSender.canHoldData()) {
            disconnectUpgrade();
        } else {
            switch (action) {
                case DISCONNECT_UPGRADE:
                    disconnectUpgrade();
                    break;
                case STOP_SENDING_DATA:
                    isPaused.set(true);
                    holdAll();
                    // upgrade helper does not need to be paused as this plugin will hold any packets that are sent
                    break;
            }
        }

        publishPause();
    }

    private void onUpgradeStartRequest(byte[] data) {
        int ACTION_OFFSET = 0;
        UpgradeStartAction action = UpgradeStartAction.valueOf(BytesUtils.getUINT8(data, ACTION_OFFSET));
        // If unable to hold data, only CONNECT_UPGRADE is supported.
        if (!mSender.canHoldData()) {
            upgradeHelper.onPlugged(this);
        }
        else {
            switch (action) {
                case CONNECT_UPGRADE:
                    upgradeHelper.onPlugged(this);
                    break;
                case RESTART_SENDING_DATA:
                    isPaused.set(false);
                    resumeAll();
                    break;
            }
        }

        getTaskManager().schedule(this::publishPause, 500); // small delay to cover up publications on connection
    }

    private void disconnectUpgrade() {
        upgradeHelper.onUnplugged();
        setUpgradeModeOff();
    }

    private void publishPause() {
        if (isPaused.get() || !isConnected.get()) {
            UpgradeProgress progress = UpgradeProgress.state(UpgradeState.PAUSED);
            upgradePublisher.publishProgress(progress);
        }
    }

    private static final class COMMANDS {

        static final int V1_UPGRADE_CONNECT = 0x00;
        static final int V1_UPGRADE_DISCONNECT = 0x01;
        static final int V1_UPGRADE_CONTROL = 0x02;
        static final int V1_SET_DATA_ENDPOINT_MODE = 0x04;
    }

    private static final class NOTIFICATIONS {

        static final int V1_UPGRADE_DATA = 0x00;
        static final int V2_UPGRADE_STOP_REQUEST = 0x01;
        static final int V2_UPGRADE_START_REQUEST = 0x02;
    }
}
