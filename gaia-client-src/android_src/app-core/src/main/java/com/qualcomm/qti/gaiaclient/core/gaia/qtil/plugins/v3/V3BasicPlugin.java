/*
 * ************************************************************************************************
 * * © 2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo.FLOW_CONTROL_DISABLED;
import static com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo.FLOW_CONTROL_ENABLED;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.DeviceInfo;
import com.qualcomm.qti.gaiaclient.core.data.FlowControlInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.transport.TransportProtocol.Rfcomm.Frame;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.ProtocolVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.DataBuilder;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.TextData;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ChargerStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.CrossUpdateVersion;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.GaiaVersionData;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.ProtocolInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformation;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.SystemInformationMessage;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransferParameters;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransferSetup;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransferSetupResponse;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransferredData;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.TransportInformation;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures.UserFeatureMessage;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic.userfeatures.UserFeatures;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BasicPlugin;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.PluginStarter;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.DeviceInformationPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ProtocolPublisher;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class V3BasicPlugin extends V3QTILPlugin implements BasicPlugin {

    private static final String TAG = "V3BasicPlugin";

    private static final int NOT_A_FEATURE = 128; // feature IDs are from 0 to 127 (7 bits number)

    private final ConcurrentHashMap<Integer, DataTransferListener> dataTransferListeners = new ConcurrentHashMap<>();

    private final DeviceInformationPublisher deviceInformationPublisher = new DeviceInformationPublisher();

    private final ProtocolPublisher protocolPublisher = new ProtocolPublisher();

    private final ConcurrentHashMap<Integer, PluginStarter> pluginStarters = new ConcurrentHashMap<>();

    private DataBuilder<UserFeatures> userFeaturesBuilder;

    private DataBuilder<List<SystemInformation>> systemInformationBuilder;

    public V3BasicPlugin(@NonNull GaiaSender sender) {
        super(QTILFeature.BASIC, sender);
    }

    @Override // V3QTILPlugin
    public void onStarted() {
        getPublicationManager().register(deviceInformationPublisher);
        getPublicationManager().register(protocolPublisher);

        if (getVersion() >= VERSIONS.V2) {
            setProtocolParameter(ProtocolInfo.PROTOCOL_VERSION, ProtocolVersion.V4);
        }
    }

    @Override // V3QTILPlugin
    protected void onStopped() {
        getPublicationManager().unregister(deviceInformationPublisher);
        getPublicationManager().unregister(protocolPublisher);
    }

    @Override // BasicPlugin
    public void fetchDeviceInfo(DeviceInfo info) {
        switch (info) {
            case GAIA_VERSION:
                sendPacket(COMMANDS.V1_GET_GAIA_VERSION);
                break;

            case APPLICATION_VERSION:
                sendPacket(COMMANDS.V1_GET_APPLICATION_VERSION);
                break;

            case VARIANT_NAME:
                sendPacket(COMMANDS.V1_GET_VARIANT);
                break;

            case USER_FEATURES:
                if (VERSIONS.V3 <= getVersion() && userFeaturesBuilder == null) {
                    userFeaturesBuilder = new DataBuilder<>(UserFeatures::new);
                    sendPacket(COMMANDS.V3_GET_USER_FEATURE);
                }
                break;

            case SERIAL_NUMBER:
                sendPacket(COMMANDS.V1_GET_SERIAL_NUMBER);
                break;

            case SYSTEM_INFORMATION:
                if (VERSIONS.V5 <= getVersion() && systemInformationBuilder == null) {
                    systemInformationBuilder = new DataBuilder<>(SystemInformation::valueOf);
                    sendPacket(COMMANDS.V5_GET_SYSTEM_INFORMATION, new byte[]{0, 0, 0, 0});
                }
                break;
        }
    }

    @Override // BasicPlugin
    public boolean fetchSizeInfo(SizeInfo info) {
        return getProtocolInfo(info.getProtocolInfo());
    }

    @Override // BasicPlugin
    public boolean fetchFlowControlInfo(FlowControlInfo info) {
        return getProtocolInfo(info.getProtocolInfo());
    }

    @Override // BasicPlugin
    public boolean setSize(SizeInfo info, long value) {
        return setProtocolParameter(info.getProtocolInfo(), value);
    }

    @Override // BasicPlugin
    public boolean setFlowControl(FlowControlInfo info, boolean enabled) {
        long value = enabled ? FLOW_CONTROL_ENABLED : FLOW_CONTROL_DISABLED;
        return setProtocolParameter(info.getProtocolInfo(), value);
    }

    @Override // BasicPlugin
    public void registerDataTransferListener(int session, DataTransferListener listener) {
        dataTransferListeners.put(session, listener);
    }

    @Override // BasicPlugin
    public void unregisterDataTransferListener(int session) {
        dataTransferListeners.remove(session);
    }

    @Override // BasicPlugin
    public boolean startDataTransfer(int session) {
        if (getVersion() < VERSIONS.V2) {
            Log.w(TAG, "[setupDataTransfer] not supported by device, requires BASIC plugin " +
                    "version >= 2.");
            return false;
        }

        TransferSetup setup = new TransferSetup(session);
        sendPacket(COMMANDS.V2_DATA_TRANSFER_SETUP, setup.getBytes());
        return true;
    }

    @Override // BasicPlugin
    public boolean transferData(int session, long offset, long size) {
        if (getVersion() < VERSIONS.V2) {
            Log.w(TAG, "[transferData] not supported by device, requires BASIC plugin " +
                    "version >= 2.");
            return false;
        }

        TransferParameters setup = new TransferParameters(session, offset, size);
        sendPacket(COMMANDS.V2_DATA_TRANSFER_GET, setup.getBytes());
        return true;
    }

    public void registerNotification(QTILFeature feature, PluginStarter pluginStarter) {
        pluginStarters.put(feature.getValue(), pluginStarter);
        sendPacket(COMMANDS.V1_REGISTER_NOTIFICATION, feature.getValue());
    }

    public void cancelNotification(QTILFeature feature) {
        sendPacket(COMMANDS.V1_CANCEL_NOTIFICATION, feature.getValue());
    }

    @Override
    public DeviceInformationPublisher getDeviceInformationPublisher() {
        return deviceInformationPublisher;
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        if (packet.getCommand() == NOTIFICATIONS.V1_CHARGER_STATUS) {
            int STATUS_OFFSET = 0;
            ChargerStatus status = ChargerStatus.valueOf(getUINT8(packet.getData(), STATUS_OFFSET));
            deviceInformationPublisher.publishInfo(DeviceInfo.CHARGER_STATUS, status);
        }
        else if (packet.getCommand() == NOTIFICATIONS.V2_UPGRADE_REQUIRED) {
            byte[] payload = packet.getPayload();
            CrossUpdateVersion version =
                    new CrossUpdateVersion(true, getUINT16(payload, 0), getUINT16(payload, 2), getUINT16(payload, 4));
            deviceInformationPublisher.publishInfo(DeviceInfo.CROSS_UPDATE_REQUIRED, version);
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        switch (response.getCommand()) {
            case COMMANDS.V1_GET_GAIA_VERSION:
                GaiaVersionData gaiaVersion = new GaiaVersionData(response.getData());
                deviceInformationPublisher
                        .publishInfo(DeviceInfo.GAIA_VERSION, gaiaVersion.getGaiaVersion());
                break;

            case COMMANDS.V1_GET_SERIAL_NUMBER:
                TextData serialNumber = new TextData(response.getData());
                deviceInformationPublisher
                        .publishInfo(DeviceInfo.SERIAL_NUMBER, serialNumber.getText());
                break;

            case COMMANDS.V1_GET_VARIANT:
                TextData variant = new TextData(response.getData());
                deviceInformationPublisher.publishInfo(DeviceInfo.VARIANT_NAME, variant.getText());
                break;

            case COMMANDS.V1_GET_APPLICATION_VERSION:
                TextData applicationVersion = new TextData(response.getData());
                deviceInformationPublisher
                        .publishInfo(DeviceInfo.APPLICATION_VERSION, applicationVersion.getText());
                break;

            case COMMANDS.V1_REGISTER_NOTIFICATION:
                onNotificationRegistered(response, sent);
                break;

            case COMMANDS.V2_GET_TRANSPORT_INFORMATION:
                TransportInformation info = new TransportInformation(response.getData());
                publishTransportInformation(info);
                break;

            case COMMANDS.V2_SET_TRANSPORT_PARAMETER:
                TransportInformation setInfo = new TransportInformation(response.getData());
                publishTransportInformation(setInfo);
                onTransportInformationSet(setInfo);
                break;

            case COMMANDS.V2_DATA_TRANSFER_SETUP:
                TransferSetupResponse setup = new TransferSetupResponse(response.getData());
                DataTransferListener setupListener = dataTransferListeners.get(setup.getSession());
                if (setupListener != null) {
                    setupListener.onSetUp(setup);
                }
                break;

            case COMMANDS.V2_DATA_TRANSFER_GET:
                TransferredData data = new TransferredData(response.getData());
                DataTransferListener dataListener = dataTransferListeners.get(data.getSession());
                if (dataListener != null) {
                    dataListener.onDataTransferred(data);
                }
                break;

            case COMMANDS.V3_GET_USER_FEATURE:
            case COMMANDS.V3_GET_USER_FEATURE_NEXT:
                UserFeatureMessage message = new UserFeatureMessage(response.getData());
                onReceiveUserFeature(message);
                break;

            case COMMANDS.V5_GET_SYSTEM_INFORMATION:
                SystemInformationMessage systemInformationMessage = new SystemInformationMessage(response.getData());
                onReceiveSystemInfo(systemInformationMessage);
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        V3ErrorStatus status = errorPacket.getV3ErrorStatus();
        Reason reason = Reason.valueOf(status);
        byte[] sentPayload = sent != null ? sent.getPayload() : new byte[]{};

        switch (errorPacket.getCommand()) {
            case COMMANDS.V1_GET_GAIA_VERSION:
                deviceInformationPublisher.publishError(DeviceInfo.GAIA_VERSION, reason);
                break;

            case COMMANDS.V1_GET_SERIAL_NUMBER:
                deviceInformationPublisher.publishError(DeviceInfo.SERIAL_NUMBER, reason);
                break;

            case COMMANDS.V1_GET_VARIANT:
                deviceInformationPublisher.publishError(DeviceInfo.VARIANT_NAME, reason);
                break;

            case COMMANDS.V1_GET_APPLICATION_VERSION:
                deviceInformationPublisher.publishError(DeviceInfo.APPLICATION_VERSION, reason);
                break;

            case COMMANDS.V1_REGISTER_NOTIFICATION:
                onNotificationRegistrationError(sent, reason);
                break;

            case COMMANDS.V1_CANCEL_NOTIFICATION:
                break;

            case COMMANDS.V2_GET_TRANSPORT_INFORMATION:
            case COMMANDS.V2_SET_TRANSPORT_PARAMETER:
                TransportInformation info = new TransportInformation(sentPayload);
                protocolPublisher.publishError(info.getInfo(), reason);
                break;

            case COMMANDS.V2_DATA_TRANSFER_GET:
                onDataTransferError(new TransferParameters(sentPayload), reason);
                break;

            case COMMANDS.V2_DATA_TRANSFER_SETUP:
                onDataTransferError(new TransferSetup(sentPayload), reason);
                break;

            case COMMANDS.V3_GET_USER_FEATURE:
            case COMMANDS.V3_GET_USER_FEATURE_NEXT:
                userFeaturesBuilder = null;
                deviceInformationPublisher.publishError(DeviceInfo.USER_FEATURES, reason);
                break;

            case COMMANDS.V5_GET_SYSTEM_INFORMATION:
                systemInformationBuilder = null;
                deviceInformationPublisher.publishError(DeviceInfo.SYSTEM_INFORMATION, reason);
                break;
        }
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        if (!(source instanceof CommandPacket)) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.");
            return;
        }

        CommandPacket packet = (CommandPacket) source;

        switch (packet.getCommand()) {
            case COMMANDS.V1_GET_GAIA_VERSION:
                deviceInformationPublisher.publishError(DeviceInfo.GAIA_VERSION, reason);
                break;

            case COMMANDS.V1_GET_SERIAL_NUMBER:
                deviceInformationPublisher.publishError(DeviceInfo.SERIAL_NUMBER, reason);
                break;

            case COMMANDS.V1_GET_VARIANT:
                deviceInformationPublisher.publishError(DeviceInfo.VARIANT_NAME, reason);
                break;

            case COMMANDS.V1_GET_APPLICATION_VERSION:
                deviceInformationPublisher.publishError(DeviceInfo.APPLICATION_VERSION, reason);
                break;

            case COMMANDS.V1_REGISTER_NOTIFICATION:
                onNotificationRegistrationError(packet, reason);
                break;

            case COMMANDS.V1_CANCEL_NOTIFICATION:
                break;

            case COMMANDS.V2_GET_TRANSPORT_INFORMATION:
            case COMMANDS.V2_SET_TRANSPORT_PARAMETER:
                TransportInformation info = new TransportInformation(source.getPayload());
                protocolPublisher.publishError(info.getInfo(), reason);
                break;

            case COMMANDS.V2_DATA_TRANSFER_GET:
                onDataTransferError(new TransferParameters(packet.getPayload()), reason);
                break;

            case COMMANDS.V2_DATA_TRANSFER_SETUP:
                onDataTransferError(new TransferSetup(packet.getPayload()), reason);
                break;

            case COMMANDS.V3_GET_USER_FEATURE:
            case COMMANDS.V3_GET_USER_FEATURE_NEXT:
                userFeaturesBuilder = null;
                deviceInformationPublisher.publishError(DeviceInfo.USER_FEATURES, reason);
                break;

            case COMMANDS.V5_GET_SYSTEM_INFORMATION:
                systemInformationBuilder = null;
                deviceInformationPublisher.publishError(DeviceInfo.SYSTEM_INFORMATION, reason);
                break;
        }
    }

    private boolean getProtocolInfo(ProtocolInfo info) {
        if (getVersion() < VERSIONS.V2) {
            Log.w(TAG, "[getProtocolInfo] not supported by device, requires BASIC plugin version " +
                    ">= 2.");
            return false;
        }

        sendPacket(COMMANDS.V2_GET_TRANSPORT_INFORMATION, info.getValue());
        return true;
    }

    private boolean setProtocolParameter(ProtocolInfo info, long value) {
        if (getVersion() < VERSIONS.V2) {
            Log.w(TAG, "[setProtocolParameter] not supported by device, requires BASIC plugin " +
                    "version >= 2.");
            return false;
        }

        if (info != ProtocolInfo.MAX_TX_PACKET_SIZE && info != ProtocolInfo.OPTIMUM_TX_PACKET_SIZE
                && info != ProtocolInfo.TX_FLOW_CONTROL && info != ProtocolInfo.PROTOCOL_VERSION) {
            Log.w(TAG, "[setProtocolParameter] info cannot be set/write on the device, info="
                    + info);
            return false;
        }

        TransportInformation information = new TransportInformation(info, value);
        sendPacket(COMMANDS.V2_SET_TRANSPORT_PARAMETER, information.getBytes());
        return true;
    }

    private void onNotificationRegistered(ResponsePacket response, CommandPacket sent) {
        int feature = getFeatureFromNotificationPackets(response, sent);

        // start corresponding plugin
        PluginStarter starter = pluginStarters.get(feature);
        if (starter != null) {
            starter.start();
        }
    }

    private void onNotificationRegistrationError(CommandPacket sent, Reason reason) {
        int feature = getFeatureFromNotificationPackets(null, sent);

        // indicate error for the plugin
        PluginStarter starter = pluginStarters.get(feature);
        if (starter != null) {
            starter.onError(reason);
        }
    }

    /**
     * If this plugin uses version 2 or higher, this method uses the response packet to get the
     * feature ID. Otherwise it uses the sent packet.
     *
     * @return {@link #NOT_A_FEATURE} if no feature could be found from the arguments.
     */
    private int getFeatureFromNotificationPackets(ResponsePacket response, CommandPacket sent) {
        // get feature ID
        byte[] responseData = response != null ? response.getData() : null;
        byte[] sentData = sent != null ? sent.getPayload() : null;
        int FEATURE_OFFSET = 0;

        // find feature ID
        return getVersion() >= 2 && responseData != null && responseData.length >= 1 ?
               getUINT8(responseData, FEATURE_OFFSET) :
               sentData != null && sentData.length >= 1 ? getUINT8(sentData, FEATURE_OFFSET) : NOT_A_FEATURE;
    }

    private void publishTransportInformation(TransportInformation info) {
        switch (info.getInfo()) {
            case TX_FLOW_CONTROL:
            case RX_FLOW_CONTROL:
                boolean enabled = info.getValue() == FLOW_CONTROL_ENABLED;
                protocolPublisher.publishFlowControlInfo(FlowControlInfo.valueOf(info.getInfo()), enabled);
                break;

            case MAX_TX_PACKET_SIZE:
            case OPTIMUM_TX_PACKET_SIZE:
            case MAX_RX_PACKET_SIZE:
            case OPTIMUM_RX_PACKET_SIZE:
                int payloadSize = getPayloadSize((int) info.getValue());
                protocolPublisher.publishSizeInfo(SizeInfo.valueOf(info.getInfo()), payloadSize);
                break;

            case PROTOCOL_VERSION:
                long protocolVersion = info.getValue();
                protocolPublisher.publishProtocolVersion(protocolVersion);
                break;
        }
    }

    private void onTransportInformationSet(TransportInformation info) {
        if (info.getInfo() == ProtocolInfo.PROTOCOL_VERSION) {
            if (info.getValue() >= ProtocolVersion.V4) {
                // if v4 is set, setting the maximum size this app can receive
                setProtocolParameter(ProtocolInfo.MAX_TX_PACKET_SIZE, Frame.v4maxLength);
                // fetch of other sizes will be triggered by a successful response
            }
            else {
                // fetching the TX size
                getProtocolInfo(ProtocolInfo.MAX_TX_PACKET_SIZE);
            }
        }
        else if (info.getInfo() == ProtocolInfo.MAX_TX_PACKET_SIZE) {
            // fetching other sizes
            getProtocolInfo(ProtocolInfo.OPTIMUM_RX_PACKET_SIZE);
            getProtocolInfo(ProtocolInfo.OPTIMUM_TX_PACKET_SIZE);
            getProtocolInfo(ProtocolInfo.MAX_RX_PACKET_SIZE);
        }
    }

    private void onReceiveUserFeature(UserFeatureMessage message) {
        if (userFeaturesBuilder == null) {
            // unexpected as the record is instantiated when sending the 1st feature command
            userFeaturesBuilder = new DataBuilder<>(UserFeatures::new);
        }

        userFeaturesBuilder.add(message.getData());

        if (message.hasMoreData()) {
            sendPacket(COMMANDS.V3_GET_USER_FEATURE_NEXT, message.getReadingStatus());
        }
        else {
            UserFeatures features = userFeaturesBuilder.build();
            deviceInformationPublisher.publishInfo(DeviceInfo.USER_FEATURES, features);
            userFeaturesBuilder = null; // reset as the data has been consumed.
        }
    }

    private void onReceiveSystemInfo(SystemInformationMessage message) {
        if (systemInformationBuilder == null) {
            // unexpected as the record is instantiated when sending the 1st feature command
            systemInformationBuilder = new DataBuilder<>(SystemInformation::valueOf);
        }

        systemInformationBuilder.add(message.getData());

        if (message.getHasMoreData()) {
            sendPacket(COMMANDS.V5_GET_SYSTEM_INFORMATION, message.getToken());
        }
        else {
            List<SystemInformation> list = systemInformationBuilder.build();
            deviceInformationPublisher.publishInfo(DeviceInfo.SYSTEM_INFORMATION, list);
            systemInformationBuilder = null; // reset as the data has been consumed.
        }
    }

    private void onDataTransferError(TransferSetup setup, Reason reason) {
        DataTransferListener listener = dataTransferListeners.get(setup.getSession());
        if (listener != null) {
            listener.onTransferError(setup.getSession(), reason);
        }
    }

    private void onDataTransferError(TransferParameters parameters, Reason reason) {
        DataTransferListener listener = dataTransferListeners.get(parameters.getSession());
        if (listener != null) {
            listener.onTransferError(parameters.getSession(), reason);
        }
    }

    private static final class COMMANDS {

        static final int V1_GET_GAIA_VERSION = 0x00;
        static final int V1_GET_SUPPORTED_FEATURES = 0x01; // used at initialisation by FeatureFetcher
        static final int V1_GET_SUPPORTED_FEATURES_NEXT = 0x02; // used at initialisation by FeatureFetcher
        static final int V1_GET_SERIAL_NUMBER = 0x03;
        static final int V1_GET_VARIANT = 0x04;
        static final int V1_GET_APPLICATION_VERSION = 0x05;
        static final int V1_REGISTER_NOTIFICATION = 0x07;
        static final int V1_CANCEL_NOTIFICATION = 0x08;
        static final int V2_DATA_TRANSFER_SETUP = 0x09;
        static final int V2_DATA_TRANSFER_GET = 0x0A;
        static final int V2_GET_TRANSPORT_INFORMATION = 0x0C;
        static final int V2_SET_TRANSPORT_PARAMETER = 0x0D;
        static final int V3_GET_USER_FEATURE = 0x0E;
        static final int V3_GET_USER_FEATURE_NEXT = 0x0F;
        static final int V4_GET_DEVICE_BLUETOOTH_ADDRESS = 0x10; // not used as BluetoothDevice provides the address
        static final int V5_GET_SYSTEM_INFORMATION = 0x11;
    }

    private static final class VERSIONS {

        static final int V1 = 0x01;
        static final int V2 = 0x02;
        static final int V3 = 0x03;
        static final int V4 = 0x04;
        static final int V5 = 0x05;
    }

    private static final class NOTIFICATIONS {

        static final int V1_CHARGER_STATUS = 0x00;
        static final int V2_UPGRADE_REQUIRED = 0x01;
    }
}
