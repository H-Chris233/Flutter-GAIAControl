/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v1v2;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.data.FlowControlInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.PacketSentListener;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.Parameters;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.V1V2Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2PacketFactory;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.V2ApiVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BasicPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.PluginStarter;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.UpgradePlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3.DataTransferListener;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DeviceInformationPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ProtocolPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.QTILFeaturesPublisher;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeGaiaCommand;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelperListener;
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils;

public class V1V2QTILPlugin extends V1V2Plugin implements BasicPlugin, UpgradePlugin, UpgradeHelperListener {

    private static final String TAG = "V1V2QTILPlugin";

    private static final int VENDOR_ID = 0x000A;

    private final UpgradeHelper mUpgradeHelper;

    private final DeviceInformationPublisher mDeviceInformationPublisher =
            new DeviceInformationPublisher();

    private final QTILFeaturesPublisher mFeaturesPublisher = new QTILFeaturesPublisher();

    private final ProtocolPublisher mProtocolPublisher = new ProtocolPublisher();

    public V1V2QTILPlugin(@NonNull GaiaSender sender, UpgradeHelper helper) {
        super(VENDOR_ID, sender);
        this.mUpgradeHelper = helper;
    }

    @Override // Plugin
    public void onStarted() {
        PublicationManager publicationManager = getPublicationManager();
        publicationManager.register(mDeviceInformationPublisher);
        publicationManager.register(mFeaturesPublisher);
        publicationManager.register(mProtocolPublisher);
        registerNotification(QTILFeature.UPGRADE, null);
    }

    @Override // Plugin
    protected void onStopped() {
        PublicationManager publicationManager = getPublicationManager();
        publicationManager.unregister(mDeviceInformationPublisher);
        publicationManager.unregister(mFeaturesPublisher);
        publicationManager.unregister(mProtocolPublisher);
        mUpgradeHelper.onUnplugged();
    }

    @Override // BasicPlugin
    public void fetchDeviceInfo(DeviceInfo info) {
        if (info == DeviceInfo.GAIA_VERSION) {
            sendPacket(COMMANDS.GET_API_VERSION);
        }
    }

    @Override // BasicPlugin
    public boolean fetchSizeInfo(SizeInfo info) {
        // not supported
        return false;
    }

    @Override // BasicPlugin
    public boolean fetchFlowControlInfo(FlowControlInfo info) {
        // not supported
        return false;
    }

    @Override // BasicPlugin
    public boolean setSize(SizeInfo info, long value) {
        // not supported
        return false;
    }

    @Override // BasicPlugin
    public boolean setFlowControl(FlowControlInfo info, boolean enabled) {
        // not supported
        return false;
    }

    @Override // BasicPlugin
    public void registerDataTransferListener(int session, DataTransferListener listener) {
        // not supported
    }

    @Override // BasicPlugin
    public void unregisterDataTransferListener(int session) {
        // not supported
    }

    @Override // BasicPlugin
    public boolean startDataTransfer(int session) {
        // not supported
        return false;
    }

    @Override // BasicPlugin
    public boolean transferData(int session, long offset, long size) {
        // not supported
        return false;
    }

    @Override // BasicPlugin
    public void registerNotification(QTILFeature feature, PluginStarter pluginStarter) {
        if (!feature.equals(QTILFeature.UPGRADE)) {
            Log.w(TAG, "[registerNotification] Unsupported feature=" + feature);
            return;
        }

        sendPacket(COMMANDS.REGISTER_NOTIFICATION, EVENTS.VMU_PACKET);
    }

    @Override // BasicPlugin
    public void cancelNotification(QTILFeature feature) {
        if (!feature.equals(QTILFeature.UPGRADE)) {
            Log.w(TAG, "[registerNotification] Unsupported feature=" + feature);
            return;
        }

        sendPacket(COMMANDS.CANCEL_NOTIFICATION, EVENTS.VMU_PACKET);
    }

    @Override // BasicPlugin
    public DeviceInformationPublisher getDeviceInformationPublisher() {
        return mDeviceInformationPublisher;
    }

    @NonNull
    @Override // UpgradePlugin
    public UpgradeHelper getUpgradeHelper() {
        return mUpgradeHelper;
    }

    @Override // UpgradeHelperListener
    public void sendUpgradeMessage(byte[] data) {
        sendPacket(COMMANDS.VM_UPGRADE_CONTROL, data);
    }

    @Override // UpgradeHelperListener
    public void sendUpgradeMessage(byte[] data, boolean isAcknowledged,
                                   boolean isFlushed, boolean useRwcp, PacketSentListener listener) {
        GaiaPacket packet = V1V2PacketFactory.buildPacket(getVendor(), COMMANDS.VM_UPGRADE_CONTROL, data);

        // Using default - Java doesn't support named arguments
        Parameters parameters = new Parameters(
                true,
                isAcknowledged,
                isFlushed,
                useRwcp,
                listener,
                DEFAULT_RESPONSE_TIME_OUT_MS
        );
        send(packet, parameters);
    }

    @Override // UpgradeHelperListener
    public void setUpgradeModeOn(boolean useRwcp) {
        if(useRwcp) {
            sendPacket(COMMANDS.SET_DATA_ENDPOINT_MODE);
        }
        sendPacket(COMMANDS.VM_UPGRADE_CONNECT);
    }

    @Override // UpgradeHelperListener
    public void setUpgradeModeOff() {
        sendPacket(COMMANDS.VM_UPGRADE_DISCONNECT);
    }

    @Override // UpgradeHelperListener
    public void cancelAll() {
        super.cancelAll(); // Plugin
    }

    @Override // V1V2Plugin
    protected boolean onCommand(V1V2Packet packet) {
        // unexpected
        return false;
    }

    @Override // V1V2Plugin
    protected void onResponse(V1V2Packet response, @Nullable V1V2Packet sent) {
        switch (response.getCommand()) {
            case COMMANDS.GET_API_VERSION:
                V2ApiVersion versions = new V2ApiVersion(response.getResponsePayload());
                mDeviceInformationPublisher.publishInfo(DeviceInfo.GAIA_VERSION, versions.getGaiaVersion());
                mProtocolPublisher.publishProtocolVersion(versions.getProtocolVersion());
                break;

            case COMMANDS.REGISTER_NOTIFICATION:
                if (sent != null) {
                    onNotificationRegistered(sent);
                }
                break;

            case COMMANDS.VM_UPGRADE_CONNECT:
                mUpgradeHelper.onUpgradeConnected();
                break;

            case COMMANDS.VM_UPGRADE_CONTROL:
                mUpgradeHelper.onAcknowledged();
                break;

            case COMMANDS.VM_UPGRADE_DISCONNECT:
                mUpgradeHelper.onUpgradeDisconnected();
                break;
        }
    }

    @Override // V1V2Plugin
    protected boolean onNotification(V1V2Packet packet) {
        if (packet.getEvent() == EVENTS.VMU_PACKET) {
            sendAcknowledgement(packet, V1V2ErrorStatus.SUCCESS, mUpgradeHelper.isFlushed());
            mUpgradeHelper.onUpgradeMessage(packet.getNotificationPayload());
            return true;
        }

        return false;
    }

    @Override // V1V2Plugin
    protected void onError(V1V2Packet response, @Nullable V1V2Packet sent) {
        V1V2ErrorStatus status = response.getStatus();
        Reason reason = Reason.valueOf(status);

        switch (response.getCommand()) {
            case COMMANDS.GET_API_VERSION:
                mDeviceInformationPublisher.publishError(DeviceInfo.GAIA_VERSION, reason);
                mProtocolPublisher.publishError(ProtocolInfo.PROTOCOL_VERSION, reason);
                break;

            case COMMANDS.REGISTER_NOTIFICATION:
                if (sent != null) {
                    onNotificationRegistrationError(sent, reason);
                }
                break;

            case COMMANDS.VM_UPGRADE_CONNECT:
                mUpgradeHelper.onErrorResponse(UpgradeGaiaCommand.CONNECT, status);
                break;
            case COMMANDS.VM_UPGRADE_CONTROL:
                mUpgradeHelper.onErrorResponse(UpgradeGaiaCommand.CONTROL, status);
                break;
            case COMMANDS.VM_UPGRADE_DISCONNECT:
                mUpgradeHelper.onErrorResponse(UpgradeGaiaCommand.DISCONNECT, status);
                break;
        }
    }

    @Override // V1V2Plugin
    protected void onFailed(GaiaPacket source, Reason reason) {
        if (!(source instanceof V1V2Packet)) {
            Log.w(TAG, "[onNotAvailable] Packet is not a V1V2Packet.");
            return;
        }

        V1V2Packet packet = (V1V2Packet) source;

        switch (packet.getCommand()) {
            case COMMANDS.GET_API_VERSION:
                mDeviceInformationPublisher.publishError(DeviceInfo.GAIA_VERSION, reason);
                mProtocolPublisher.publishError(ProtocolInfo.PROTOCOL_VERSION, reason);
                break;

            case COMMANDS.REGISTER_NOTIFICATION:
                onNotificationRegistrationError(packet, reason);
                break;

            case COMMANDS.VM_UPGRADE_CONNECT:
            case COMMANDS.VM_UPGRADE_CONTROL:
            case COMMANDS.VM_UPGRADE_DISCONNECT:
                mUpgradeHelper.onSendingFailed(reason);
                break;
        }
    }

    private void onNotificationRegistered(V1V2Packet sent) {
        int event = sent.getEvent();
        if (event != EVENTS.VMU_PACKET) {
            return;
        }

        mUpgradeHelper.onPlugged(V1V2QTILPlugin.this);
        mFeaturesPublisher.publishFeatureSupported(QTILFeature.UPGRADE, 0); // version not supported for V1/V2
    }

    private void onNotificationRegistrationError(V1V2Packet sent, Reason reason) {
        int event = sent.getEvent();
        Log.w(TAG, String.format("[onNotificationRegistrationError] failed for event=%1$s, with " +
                                         "reason=%2$s",
                                 BytesUtils.getHexadecimalStringFromInt(event), reason));
        if (event != EVENTS.VMU_PACKET) {
            return;
        }

        mFeaturesPublisher.publishFeatureNotSupported(QTILFeature.UPGRADE,
                                                      Reason.NOTIFICATION_NOT_SUPPORTED);
    }

    private static final class COMMANDS {

        // POLLED STATUS commands 0x03nn
        static final int GET_API_VERSION = 0x0300;

        // DFU commands 0x064n
        static final int VM_UPGRADE_CONNECT = 0x0640;
        static final int VM_UPGRADE_DISCONNECT = 0x0641;
        static final int VM_UPGRADE_CONTROL = 0x0642;

        // NOTIFICATIONS commands 0x40nn
        static final int REGISTER_NOTIFICATION = 0x4001;
        static final int GET_NOTIFICATION = 0x4081;
        static final int CANCEL_NOTIFICATION = 0x4002;
        static final int SET_DATA_ENDPOINT_MODE = 0x04;
    }

    private static final class EVENTS {

        static final int VMU_PACKET = 0x12;
    }

}
