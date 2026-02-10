/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.BatteryLevel;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.SupportedBatteries;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.BatteryPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.BatteryPublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.util.Set;

public class V3BatteryPlugin extends V3QTILPlugin implements BatteryPlugin {

    private static final String TAG = "V3BatteryPlugin";

    private static final boolean LOG_METHODS = DEBUG.QTIL.V3_BATTERY_PLUGIN;

    private final BatteryPublisher mBatteryPublisher = new BatteryPublisher();

    public V3BatteryPlugin(@NonNull GaiaSender sender) {
        super(QTILFeature.BATTERY, sender);
    }

    @Override
    public void onStarted() {
        getPublicationManager().register(mBatteryPublisher);
    }

    @Override
    protected void onStopped() {
        getPublicationManager().unregister(mBatteryPublisher);
    }

    @Override
    public void fetchSupportedBatteries() {
        Logger.log(LOG_METHODS, TAG, "fetchSupportedBatteries");

        sendPacket(COMMANDS.V1_GET_SUPPORTED_BATTERIES);
    }

    @Override
    public void fetchBatteryLevels(Set<Battery> batteries) {
        Logger.log(LOG_METHODS, TAG, "fetchBatteryLevels", new Pair<>("batteries_count", batteries.size()));

        byte[] payload = new byte[batteries.size()];
        int offset = 0;

        for (Battery battery : batteries) {
            setUINT8(battery.getValue(), payload, offset);
            offset++;
        }

        sendPacket(COMMANDS.V1_GET_BATTERY_LEVELS, payload);
    }

    @Override
    public BatteryPublisher getBatteryPublisher() {
        return mBatteryPublisher;
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onResponse", new Pair<>("response", response), new Pair<>("sent", sent));

        switch (response.getCommand()) {
            case COMMANDS.V1_GET_SUPPORTED_BATTERIES:
                onSupportedBatteries(response.getData());
                break;
            case COMMANDS.V1_GET_BATTERY_LEVELS:
                onBatteryLevels(response.getData());
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onError", new Pair<>("packet", errorPacket), new Pair<>("sent", sent));
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        Logger.log(LOG_METHODS, TAG, "onFailed", new Pair<>("reason", reason), new Pair<>("packet", source));
    }

    private void onSupportedBatteries(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onSupportedBatteries", new Pair<>("data", data));

        SupportedBatteries supported = new SupportedBatteries(data);
        mBatteryPublisher.publishSupportedBatteries(supported.getSupported());
    }

    private void onBatteryLevels(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onBatteryLevels", new Pair<>("data", data));

        Set<BatteryLevel> levels = BatteryLevel.buildSet(data);
        mBatteryPublisher.publishBatteryLevels(levels);
    }

    private static final class COMMANDS {

        static final int V1_GET_SUPPORTED_BATTERIES = 0x00;
        static final int V1_GET_BATTERY_LEVELS = 0x01;
    }
}
