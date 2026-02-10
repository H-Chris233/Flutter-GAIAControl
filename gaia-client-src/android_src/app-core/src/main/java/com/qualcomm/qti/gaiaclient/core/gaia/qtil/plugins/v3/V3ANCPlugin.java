/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.data.ANCInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.ANCState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptedGain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveStateInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.ANCPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ANCPublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

public class V3ANCPlugin extends V3QTILPlugin implements ANCPlugin {

    private static final String TAG = "V3ANCPlugin";

    private static final boolean LOG_METHODS = DEBUG.QTIL.V3_ANC_PLUGIN;

    private final ANCPublisher mANCPublisher = new ANCPublisher();

    public V3ANCPlugin(@NonNull GaiaSender sender) {
        super(QTILFeature.ANC, sender);
    }

    @Override
    public void onStarted() {
        getPublicationManager().register(mANCPublisher);
    }

    @Override
    protected void onStopped() {
        getPublicationManager().unregister(mANCPublisher);
    }

    @Override
    public void fetchANCInfo(@NonNull ANCInfo info) {
        Logger.log(LOG_METHODS, TAG, "fetchANCInfo", new Pair<>("info", info));
        switch (info) {
            case ANC_STATE:
                sendPacket(COMMANDS.V1_GET_ANC_STATE);
                break;

            case ANC_MODE:
                sendPacket(COMMANDS.V1_GET_CURRENT_ANC_MODE);
                break;

            case ANC_MODE_COUNT:
                sendPacket(COMMANDS.V1_GET_NUM_ANC_MODES);
                break;

            case LEAKTHROUGH_GAIN:
                sendPacket(COMMANDS.V1_GET_CONFIGURED_LEAKTHROUGH_GAIN);
                break;

            case ADAPTED_GAIN:
            case ADAPTIVE_STATE:
                Log.w(TAG, "[fetchANCInfo] ANCInfo cannot be fetched: info=" + info);
                break;
        }
    }

    @Override
    public ANCPublisher getANCPublisher() {
        return mANCPublisher;
    }

    @Override
    public void setANCInfo(ANCInfo info, Object value) {
        Logger.log(LOG_METHODS, TAG, "setANCInfo",
                   new Pair<>("info", info), new Pair<>("value", value));
        switch (info) {
            case ANC_STATE:
                if (value instanceof ANCState) {
                    ANCState state = (ANCState) value;
                    sendPacket(COMMANDS.V1_SET_ANC_STATE, state.getValue());
                }
                return;

            case ANC_MODE:
                if (value instanceof Integer) {
                    // casting value into a byte
                    sendPacket(COMMANDS.V1_SET_ANC_MODE, (int) value);
                }
                return;

            case LEAKTHROUGH_GAIN:
                if (value instanceof Gain) {
                    Gain gain = (Gain) value;
                    sendPacket(COMMANDS.V1_SET_LEAKTHROUGH_GAIN, gain.getValue());
                }
                return;

            default:
                Log.w(TAG, "[setANCInfo] unimplemented for info=" + info);
                break;
        }
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        Logger.log(LOG_METHODS, TAG, "onNotification", new Pair<>("packet", packet));
        switch (packet.getCommand()) {
            case NOTIFICATIONS.V1_ANC_STATE_CHANGE:
                publishState(packet.getData());
                break;
            case NOTIFICATIONS.V2_ADAPTIVE_ANC_STATE:
                publishAdaptiveState(packet.getData());
                break;
            case NOTIFICATIONS.V1_ANC_MODE_CHANGE:
                publishMode(packet.getData());
                break;
            case NOTIFICATIONS.V2_ADAPTED_ANC_GAIN:
                publishAdaptedGain(packet.getData());
                break;
            case NOTIFICATIONS.V1_ANC_LEAKTHROUGH_GAIN_CHANGE:
                publishLeakthroughGain(packet.getData());
                break;
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onResponse",
                   new Pair<>("response", response), new Pair<>("sent", sent));
        switch (response.getCommand()) {
            case COMMANDS.V1_GET_ANC_STATE:
                publishState(response.getData());
                break;

            case COMMANDS.V1_GET_CONFIGURED_LEAKTHROUGH_GAIN:
                publishLeakthroughGain(response.getData());
                break;

            case COMMANDS.V1_GET_CURRENT_ANC_MODE:
                publishMode(response.getData());
                break;

            case COMMANDS.V1_GET_NUM_ANC_MODES:
                int COUNT_OFFSET = 0;
                mANCPublisher.publishModeCount(getUINT8(response.getData(), COUNT_OFFSET));
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onError", new Pair<>("packet", errorPacket), new Pair<>("sent", sent));

        V3ErrorStatus status = errorPacket.getV3ErrorStatus();
        onError(errorPacket.getCommand(), sent.getPayload(), Reason.valueOf(status));
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        Logger.log(LOG_METHODS, TAG, "onFailed", new Pair<>("reason", reason), new Pair<>("packet", source));
        if (!(source instanceof V3Packet)) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.");
            return;
        }

        V3Packet packet = (V3Packet) source;
        onError(packet.getCommand(), packet.getPayload(), reason);
    }

    private void onError(int command, byte[] sentData, Reason reason) {
        switch (command) {
            case COMMANDS.V1_GET_ANC_STATE:
            case COMMANDS.V1_SET_ANC_STATE:
                mANCPublisher.publishError(ANCInfo.ANC_STATE, reason);
                break;

            case COMMANDS.V1_GET_NUM_ANC_MODES:
                mANCPublisher.publishError(ANCInfo.ANC_MODE_COUNT, reason);
                break;

            case COMMANDS.V1_GET_CURRENT_ANC_MODE:
            case COMMANDS.V1_SET_ANC_MODE:
                mANCPublisher.publishError(ANCInfo.ANC_MODE, reason);
                break;

            case COMMANDS.V1_GET_CONFIGURED_LEAKTHROUGH_GAIN:
            case COMMANDS.V1_SET_LEAKTHROUGH_GAIN:
                mANCPublisher.publishError(ANCInfo.LEAKTHROUGH_GAIN, reason);
                break;
        }
    }

    private void publishState(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishState", new Pair<>("data", data));
        int STATE_OFFSET = 0;
        ANCState state = ANCState.valueOf(getUINT8(data, STATE_OFFSET));
        mANCPublisher.publishState(state);
    }

    private void publishAdaptiveState(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishAdaptiveState", new Pair<>("data", data));
        int POSITION_OFFSET = 0;
        int ADAPTIVE_STATE_OFFSET = 1;
        AdaptiveStateInfo state = new AdaptiveStateInfo(
                EarbudPosition.valueOf(getUINT8(data, POSITION_OFFSET)),
                AdaptiveState.valueOf(getUINT8(data, ADAPTIVE_STATE_OFFSET)));
        mANCPublisher.publishAdaptiveState(state);
    }

    private void publishMode(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishMode", new Pair<>("data", data));
        int MODE_OFFSET = 0;
        int value = getUINT8(data, MODE_OFFSET); // UINT8
        mANCPublisher.publishMode(value);
    }

    private void publishAdaptedGain(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishAdaptiveState", new Pair<>("data", data));
        int POSITION_OFFSET = 0;
        int GAIN_OFFSET = 1;
        EarbudPosition position = EarbudPosition.valueOf(getUINT8(data, POSITION_OFFSET));
        int value = getUINT8(data, GAIN_OFFSET);
        Gain gain = new Gain(value);
        AdaptedGain adaptedGain = new AdaptedGain(position, gain);
        mANCPublisher.publishAdaptedGain(adaptedGain);
    }

    private void publishLeakthroughGain(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishLeakthroughGain", new Pair<>("data", data));
        int GAIN_OFFSET = 0;
        int value = getUINT8(data, GAIN_OFFSET);
        Gain gain = new Gain(value);
        mANCPublisher.publishLeakthroughGain(gain);
    }

    private static final class COMMANDS {

        static final int V1_GET_ANC_STATE = 0x01;
        static final int V1_SET_ANC_STATE = 0x02;
        static final int V1_GET_NUM_ANC_MODES = 0x03;
        static final int V1_GET_CURRENT_ANC_MODE = 0x04;
        static final int V1_SET_ANC_MODE = 0x05;
        static final int V1_GET_CONFIGURED_LEAKTHROUGH_GAIN = 0x06;
        static final int V1_SET_LEAKTHROUGH_GAIN = 0x07;
    }

    private static final class NOTIFICATIONS {

        static final int V1_ANC_STATE_CHANGE = 0x00;
        static final int V1_ANC_MODE_CHANGE = 0x01;
        static final int V1_ANC_LEAKTHROUGH_GAIN_CHANGE = 0x02;
        static final int V2_ADAPTIVE_ANC_STATE = 0x03;
        static final int V2_ADAPTED_ANC_GAIN = 0x04;
    }

}
