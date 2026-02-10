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

import com.qualcomm.qti.gaiaclient.core.data.FitInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.EarbudFitPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.EarbudFitPublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;

public class V3EarbudFitPlugin extends V3QTILPlugin implements EarbudFitPlugin {

    private static final String TAG = "V3EarbudFitPlugin";

    private static final boolean LOG_METHODS = DEBUG.QTIL.V3_EARBUD_FIT_PLUGIN;

    private final EarbudFitPublisher mEarbudFitPublisher = new EarbudFitPublisher();

    public V3EarbudFitPlugin(@NonNull GaiaSender sender) {
        super(QTILFeature.EARBUD_FIT, sender);
    }

    @Override
    public void onStarted() {
        getPublicationManager().register(mEarbudFitPublisher);
    }

    @Override
    protected void onStopped() {
        getPublicationManager().unregister(mEarbudFitPublisher);
    }

    @Override
    public EarbudFitPublisher getEarbudFitPublisher() {
        return mEarbudFitPublisher;
    }

    @Override
    public void setFitTest(FitTestState state) {
        sendPacket(COMMANDS.V3_SET_FIT_TEST, state.getValue());
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        Logger.log(LOG_METHODS, TAG, "onNotification", new Pair<>("packet", packet));

        if (packet.getCommand() == NOTIFICATIONS.V1_FIT_STATUS) {
            publishFitState(packet.getData());
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onResponse", new Pair<>("response", response), new Pair<>("sent", sent));
        if (response.getCommand() == COMMANDS.V3_SET_FIT_TEST) {
            // nothing to dispatch
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

    private void onError(int command, Reason reason) {
        if (command == COMMANDS.V3_SET_FIT_TEST) {
            mEarbudFitPublisher.publishError(FitInfo.FIT_TEST, reason);
        }
    }

    private void publishFitState(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishFitState", new Pair<>("data", data));
        EarbudsFitResults results = new EarbudsFitResults(data);
        mEarbudFitPublisher.publishFitResults(results);
    }

    private static final class COMMANDS {

        static final int V3_SET_FIT_TEST = 0x00;
    }

    private static final class NOTIFICATIONS {

        static final int V1_FIT_STATUS = 0x00;
    }

}
