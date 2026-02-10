/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getValueFromBits;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.data.GestureConfigurationInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureFactory;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GetGestureConfigurationParameters;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GetGestureConfigurationResponse;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.SetGestureConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.TouchpadConfigurationType;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.GestureConfigurationPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.GestureConfigurationPublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class V3GestureConfigurationPlugin extends V3QTILPlugin implements GestureConfigurationPlugin {

    private static final String TAG = "V3GestureConfigurationPlugin";

    private static final boolean LOG_METHODS = DEBUG.QTIL.V3_GESTURE_CONFIGURATION_PLUGIN;

    private final GestureConfigurationPublisher gestureConfigurationPublisher = new GestureConfigurationPublisher();

    private final PublicationManager publicationManager;

    private final ConcurrentHashMap<Integer, Set<Configuration>> supportedConfigurations = new ConcurrentHashMap<>();

    public V3GestureConfigurationPlugin(@NonNull GaiaSender sender) {
        this(sender, getPublicationManager());
    }

    @VisibleForTesting
    public V3GestureConfigurationPlugin(@NonNull GaiaSender sender, PublicationManager publicationManager) {
        super(QTILFeature.GESTURE_CONFIGURATION, sender);
        this.publicationManager = publicationManager;
    }

    @Override
    public void onStarted() {
        publicationManager.register(gestureConfigurationPublisher);
    }

    @Override
    protected void onStopped() {
        publicationManager.unregister(gestureConfigurationPublisher);
    }

    @Override
    public GestureConfigurationPublisher getGestureConfigurationPublisher() {
        return gestureConfigurationPublisher;
    }

    @Override
    public boolean fetchInfo(GestureConfigurationInfo info) {
        Logger.log(LOG_METHODS, TAG, "fetchInfo", new Pair<>("info", info));

        switch (info) {
            case SUPPORTED_GESTURES:
                sendPacket(COMMANDS.V1_GET_SUPPORTED_GESTURES);
                return true;

            case SUPPORTED_CONTEXTS:
                sendPacket(COMMANDS.V1_GET_SUPPORTED_CONTEXTS);
                return true;

            case SUPPORTED_ACTIONS:
                sendPacket(COMMANDS.V1_GET_SUPPORTED_ACTIONS);
                return true;

            case TOUCHPAD_CONFIGURATION:
                sendPacket(COMMANDS.V1_GET_NUMBER_OF_TOUCHPADS);
                return true;

            case RESET:
            case SET_GESTURE_CONFIGURATION:
                Log.w(TAG, "[fetchInfo] This information cannot be fetched: info=" + info);
                return false;

            case GET_GESTURE_CONFIGURATION:
            default:
                Log.w(TAG, "[fetchInfo] This information requires some  parameters to be fetched: info=" + info);
                return false;
        }
    }

    @Override
    public boolean fetchInfo(GestureConfigurationInfo info, Object parameter) {
        Logger.log(LOG_METHODS, TAG, "fetchInfo", new Pair<>("info", info), new Pair<>("parameter", parameter));

        switch (info) {
            case GET_GESTURE_CONFIGURATION:
                if (parameter instanceof Integer) {
                    int id = (Integer) parameter;
                    sendGetConfiguration(id, 0);
                }
                return true;

            case SET_GESTURE_CONFIGURATION:
            case RESET:
                Log.w(TAG, "[fetchInfo] This information cannot be fetched: info=" + info);
                return false;

            case SUPPORTED_GESTURES:
            case SUPPORTED_CONTEXTS:
            case SUPPORTED_ACTIONS:
            case TOUCHPAD_CONFIGURATION:
            default:
                Log.w(TAG, "[fetchInfo] That information does not require any parameters to be fetched: info=" + info);
                return false;
        }
    }

    @Override
    public boolean setInfo(GestureConfigurationInfo info, Object value) {
        Logger.log(LOG_METHODS, TAG, "setInfo", new Pair<>("info", info), new Pair<>("value", value));

        switch (info) {
            case SET_GESTURE_CONFIGURATION:
                if (value instanceof SetGestureConfiguration) {
                    SetGestureConfiguration setter = (SetGestureConfiguration) value;
                    List<byte[]> payloads = setter.getPayloads(getMaxPayloadSize(SizeInfo.OPTIMUM_RX_PAYLOAD));
                    payloads.forEach((payload) -> sendPacket(COMMANDS.V1_SET_CONFIGURATION_FOR_GESTURE, payload));
                }
                return true;

            case GET_GESTURE_CONFIGURATION:
            case SUPPORTED_GESTURES:
            case SUPPORTED_CONTEXTS:
            case SUPPORTED_ACTIONS:
            case TOUCHPAD_CONFIGURATION:
            case RESET:
            default:
                Log.w(TAG, "[fetchInfo] This information cannot be set: info=" + info);
                return false;
        }
    }

    @Override
    public void resetToDefault() {
        Logger.log(LOG_METHODS, TAG, "resetToDefault");

        sendPacket(COMMANDS.V1_RESET_CONFIGURATION_TO_DEFAULT);
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        Logger.log(LOG_METHODS, TAG, "onNotification", new Pair<>("packet", packet));

        switch (packet.getCommand()) {
            case NOTIFICATIONS.V1_GESTURE_CONFIGURATION_CHANGED:
                onConfigurationChanged(packet.getData());
                break;
            case NOTIFICATIONS.V1_CONFIGURATION_RESET_TO_DEFAULT:
                onConfigurationReset(packet.getData());
                break;
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onResponse", new Pair<>("response", response), new Pair<>("sent", sent));

        switch (response.getCommand()) {
            case COMMANDS.V1_GET_NUMBER_OF_TOUCHPADS:
                onGetNumberOfTouchpads(response.getData());
                break;
            case COMMANDS.V1_GET_SUPPORTED_GESTURES:
                onGetGestures(response.getData());
                break;
            case COMMANDS.V1_GET_SUPPORTED_CONTEXTS:
                onGetContexts(response.getData());
                break;
            case COMMANDS.V1_GET_SUPPORTED_ACTIONS:
                onGetActions(response.getData());
                break;
            case COMMANDS.V1_GET_CONFIGURATION_FOR_GESTURE:
                onGetConfiguration(response.getData());
                break;
            case COMMANDS.V1_SET_CONFIGURATION_FOR_GESTURE:
                onSetConfiguration(response.getData());
                break;
            case COMMANDS.V1_RESET_CONFIGURATION_TO_DEFAULT:
                onResetToDefault(response.getData());
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onError", new Pair<>("packet", errorPacket), new Pair<>("sent", sent));

        V3ErrorStatus status = errorPacket.getV3ErrorStatus();
        onError(errorPacket.getCommand(), Reason.valueOf(status));
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        Logger.log(LOG_METHODS, TAG, "onFailed", new Pair<>("reason", reason), new Pair<>("packet", source));

        if (!(source instanceof V3Packet)) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.");
            return;
        }

        V3Packet packet = (V3Packet) source;
        onError(packet.getCommand(), reason);
    }

    private void onConfigurationChanged(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onConfigurationChanged", new Pair<>("data", data));

        int GESTURE_OFFSET = 0;
        int GESTURE_BIT_OFFSET = 0;
        int GESTURE_BIT_LENGTH = 7;
        int value = getUINT8(data, GESTURE_OFFSET);
        int id = getValueFromBits(value, GESTURE_BIT_OFFSET, GESTURE_BIT_LENGTH);
        Gesture gesture = GestureFactory.getGesture(id);

        gestureConfigurationPublisher.publishConfigurationChanged(gesture);
    }

    private void onConfigurationReset(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onConfigurationReset", new Pair<>("data", data));

        gestureConfigurationPublisher.publishConfigurationsReset();
    }

    private void onGetNumberOfTouchpads(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onGetNumberOfTouchpads", new Pair<>("data", data));

        int NUMBER_OFFSET = 0;
        int value = getUINT8(data, NUMBER_OFFSET);
        TouchpadConfiguration configuration =
                new TouchpadConfiguration(value, TouchpadConfigurationType.valueOf(value));

        gestureConfigurationPublisher.publishNumberOfTouchpads(configuration);
    }

    private void onGetGestures(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onGetGestures", new Pair<>("data", data));

        Set<Gesture> gestures = GestureFactory.getGestures(data);
        gestureConfigurationPublisher.publishGestures(gestures);
    }

    private void onGetContexts(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onGetContexts", new Pair<>("data", data));

        Set<GestureContext> contexts = GestureFactory.getGestureContexts(data);
        gestureConfigurationPublisher.publishGestureContexts(contexts);
    }

    private void onGetActions(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onGetActions", new Pair<>("data", data));

        Set<Action> actions = GestureFactory.getActions(data);
        gestureConfigurationPublisher.publishActions(actions);
    }

    private void onGetConfiguration(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onGetConfiguration", new Pair<>("data", data));

        GetGestureConfigurationResponse receivedConfig = new GetGestureConfigurationResponse(data);
        Gesture gesture = receivedConfig.getGesture();
        Set<Configuration> configurations = supportedConfigurations.get(gesture.getId());

        if (configurations == null) {
            configurations = receivedConfig.getConfigurations();
            supportedConfigurations.put(gesture.getId(), configurations);
        }
        else {
            // Set.addAll(...) does NOT maintain order => adding configuration one after each other in order
            for (Configuration conf : receivedConfig.getConfigurations()) {
                //noinspection UseBulkOperation
                configurations.add(conf);
            }
        }

        if (receivedConfig.hasMoreData()) {
            int offset = configurations.size();
            sendGetConfiguration(gesture.getId(), offset);
        }
        else {
            supportedConfigurations.remove(gesture.getId());
            gestureConfigurationPublisher.publishConfiguration(gesture, configurations);
        }
    }

    private void onSetConfiguration(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onSetConfiguration", new Pair<>("data", data));

        // nothing to do
    }

    private void onResetToDefault(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onResetToDefault", new Pair<>("data", data));

        // nothing to do
    }

    private void onError(int command, Reason reason) {
        switch (command) {
            case COMMANDS.V1_GET_NUMBER_OF_TOUCHPADS:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.TOUCHPAD_CONFIGURATION, reason);
                break;
            case COMMANDS.V1_GET_SUPPORTED_GESTURES:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.SUPPORTED_GESTURES, reason);
                break;
            case COMMANDS.V1_GET_SUPPORTED_CONTEXTS:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.SUPPORTED_CONTEXTS, reason);
                break;
            case COMMANDS.V1_GET_SUPPORTED_ACTIONS:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.SUPPORTED_ACTIONS, reason);
                break;
            case COMMANDS.V1_GET_CONFIGURATION_FOR_GESTURE:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.GET_GESTURE_CONFIGURATION, reason);
                break;
            case COMMANDS.V1_SET_CONFIGURATION_FOR_GESTURE:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.SET_GESTURE_CONFIGURATION, reason);
                break;
            case COMMANDS.V1_RESET_CONFIGURATION_TO_DEFAULT:
                gestureConfigurationPublisher.publishError(GestureConfigurationInfo.RESET, reason);
                break;
        }
    }

    private void sendGetConfiguration(int gestureID, int offset) {
        GetGestureConfigurationParameters parameters = new GetGestureConfigurationParameters(gestureID, offset);
        sendPacket(COMMANDS.V1_GET_CONFIGURATION_FOR_GESTURE, parameters.getPayload());
    }

    private static final class COMMANDS {

        static final int V1_GET_NUMBER_OF_TOUCHPADS = 0x00;
        static final int V1_GET_SUPPORTED_GESTURES = 0x01;
        static final int V1_GET_SUPPORTED_CONTEXTS = 0x02;
        static final int V1_GET_SUPPORTED_ACTIONS = 0x03;
        static final int V1_GET_CONFIGURATION_FOR_GESTURE = 0x04;
        static final int V1_SET_CONFIGURATION_FOR_GESTURE = 0x05;
        static final int V1_RESET_CONFIGURATION_TO_DEFAULT = 0x06;
    }

    private static final class NOTIFICATIONS {

        static final int V1_GESTURE_CONFIGURATION_CHANGED = 0x00;
        static final int V1_CONFIGURATION_RESET_TO_DEFAULT = 0x01;
    }

}
