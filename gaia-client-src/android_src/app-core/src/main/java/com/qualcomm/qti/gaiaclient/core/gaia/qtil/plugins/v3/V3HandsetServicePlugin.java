/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.data.HandsetServiceInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.MultipointType;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.HandsetServicePlugin;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.HandsetServicePublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class V3HandsetServicePlugin extends V3QTILPlugin implements HandsetServicePlugin {

    private static final String TAG = "V3HandsetServicePlugin";

    private static final boolean LOG_METHODS = DEBUG.QTIL.V3_HANDSET_SERVICE_PLUGIN;

    private final HandsetServicePublisher mHandsetServicePublisher = new HandsetServicePublisher();

    public V3HandsetServicePlugin(@NonNull GaiaSender sender) {
        super(QTILFeature.HANDSET_SERVICE, sender);
    }

    @Override
    public void onStarted() {
        getPublicationManager().register(mHandsetServicePublisher);
    }

    @Override
    protected void onStopped() {
        getPublicationManager().unregister(mHandsetServicePublisher);
    }

    @Override
    public HandsetServicePublisher getHandsetServicePublisher() {
        return mHandsetServicePublisher;
    }

    @Override
    public void setInfo(HandsetServiceInfo info, Object value) {
        Logger.log(LOG_METHODS, TAG, "setInfo", new Pair<>("info", info), new Pair<>("value", value));

        if (info == HandsetServiceInfo.MULTIPOINT_TYPE) {
            if (value instanceof MultipointType) {
                MultipointType type = (MultipointType) value;
                sendPacket(COMMANDS.V1_ENABLE_MULTIPOINT, type.getValue());
            }
        }
        else {
            Log.w(TAG, "[setInfo] not implemented for info=" + info);
        }
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        Logger.log(LOG_METHODS, TAG, "onNotification", new Pair<>("packet", packet));

        if (packet.getCommand() == NOTIFICATIONS.V1_MULTIPOINT_ENABLE_CHANGE) {
            publishType(packet.getData());
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onResponse", new Pair<>("response", response), new Pair<>("sent", sent));
        // multipoint type is updated through notifications
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

    private void onError(int command, Reason reason) {
        if (command == COMMANDS.V1_ENABLE_MULTIPOINT) {
            mHandsetServicePublisher.publishError(HandsetServiceInfo.MULTIPOINT_TYPE, reason);
        }
    }

    private void publishType(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishType", new Pair<>("data", data));
        int TYPE_OFFSET = 0;
        MultipointType type = MultipointType.valueOf(getUINT8(data, TYPE_OFFSET));
        mHandsetServicePublisher.publishMultiPointType(type);
    }

    private static final class COMMANDS {

        static final int V1_ENABLE_MULTIPOINT = 0x00;
    }

    private static final class NOTIFICATIONS {

        static final int V1_MULTIPOINT_ENABLE_CHANGE = 0x00;
    }

}
