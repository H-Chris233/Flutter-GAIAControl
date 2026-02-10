/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import android.util.Log;

import com.qualcomm.qti.gaiaclient.core.data.EarbudInfo;
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
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverInformation;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverType;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.TextData;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.EarbudPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.EarbudPublisher;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.HandoverPublisher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class V3EarbudPlugin extends V3QTILPlugin implements EarbudPlugin {

    private static final String TAG = "V3EarbudPlugin";

    private final EarbudPublisher mEarbudPublisher = new EarbudPublisher();

    private final HandoverPublisher mHandoverPublisher = new HandoverPublisher();

    public V3EarbudPlugin(@NonNull GaiaSender sender) {
        super(QTILFeature.EARBUD, sender);
    }

    @Override
    public void onStarted() {
        PublicationManager publicationManager = getPublicationManager();
        publicationManager.register(mEarbudPublisher);
        publicationManager.register(mHandoverPublisher);
    }

    @Override
    protected void onStopped() {
        PublicationManager publicationManager = getPublicationManager();
        publicationManager.unregister(mEarbudPublisher);
        publicationManager.register(mHandoverPublisher);
    }

    @Override
    public void fetchEarbudInfo(EarbudInfo info) {
        switch (info) {
            case EARBUD_POSITION:
                sendPacket(COMMANDS.V1_GET_WHAT_PRIMARY_IS);
                break;
            case SECONDARY_SERIAL_NUMBER:
                sendPacket(COMMANDS.V1_GET_SECONDARY_SERIAL_NUMBER);
                break;
        }
    }

    @Override
    public EarbudPublisher getEarbudPublisher() {
        return mEarbudPublisher;
    }

    @Override
    protected void onNotification(NotificationPacket notification) {
        switch (notification.getCommand()) {
            case NOTIFICATIONS.V1_HANDOVER_TO_HAPPEN:
                HandoverInformation info = new HandoverInformation(notification.getData());
                if (info.getType() == HandoverType.STATIC) {
                    publishHandover(info);
                }
                // dynamic handover is ignored as states are kept on the device
                break;

            case NOTIFICATIONS.V2_PRIMARY_EARBUD_CHANGED:
                int POSITION_OFFSET = 0;
                EarbudPosition position =
                        EarbudPosition.valueOf(getUINT8(notification.getData(), POSITION_OFFSET));
                mEarbudPublisher.publishInfo(EarbudInfo.EARBUD_POSITION, position, true);
                break;
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        switch (response.getCommand()) {
            case COMMANDS.V1_GET_WHAT_PRIMARY_IS:
                int POSITION_OFFSET = 0;
                EarbudPosition position =
                        EarbudPosition.valueOf(getUINT8(response.getData(), POSITION_OFFSET));
                mEarbudPublisher.publishInfo(EarbudInfo.EARBUD_POSITION, position, false);
                break;

            case COMMANDS.V1_GET_SECONDARY_SERIAL_NUMBER:
                TextData secondarySerialNumber = new TextData(response.getData());
                mEarbudPublisher
                        .publishInfo(EarbudInfo.SECONDARY_SERIAL_NUMBER, secondarySerialNumber.getText(), false);
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        V3ErrorStatus status = errorPacket.getV3ErrorStatus();

        switch (errorPacket.getCommand()) {
            case COMMANDS.V1_GET_WHAT_PRIMARY_IS:
                mEarbudPublisher.publishError(EarbudInfo.EARBUD_POSITION,
                                              Reason.valueOf(status));
                break;

            case COMMANDS.V1_GET_SECONDARY_SERIAL_NUMBER:
                mEarbudPublisher.publishError(EarbudInfo.SECONDARY_SERIAL_NUMBER,
                                              Reason.valueOf(status));
                break;
        }
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        if (!(source instanceof V3Packet)) {
            Log.w(TAG, "[onNotAvailable] Packet is not a V3Packet.");
            return;
        }

        V3Packet packet = (V3Packet) source;

        switch (packet.getCommand()) {
            case COMMANDS.V1_GET_WHAT_PRIMARY_IS:
                mEarbudPublisher.publishError(EarbudInfo.EARBUD_POSITION, reason);
                break;

            case COMMANDS.V1_GET_SECONDARY_SERIAL_NUMBER:
                mEarbudPublisher.publishError(EarbudInfo.SECONDARY_SERIAL_NUMBER, reason);
                break;
        }
    }

    private void publishHandover(HandoverInformation info) {
        mHandoverPublisher.publishHandoverStart(info);
    }

    private static final class COMMANDS {

        static final int V1_GET_WHAT_PRIMARY_IS = 0x00;
        static final int V1_GET_SECONDARY_SERIAL_NUMBER = 0x01;
    }

    private static final class NOTIFICATIONS {

        static final int V1_HANDOVER_TO_HAPPEN = 0x00;
        static final int V2_PRIMARY_EARBUD_CHANGED = 0x01;
    }

    private static final class VERSIONS {

        static final int V1 = 0x01;
        static final int V2 = 0x02;
    }

}
