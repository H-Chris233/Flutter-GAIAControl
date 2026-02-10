/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.version;

import static com.qualcomm.qti.gaiaclient.core.gaia.qtil.vendors.QTILVendorIDs.QTIL_V1V2_VENDOR_ID;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.V1V2Plugin;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2PacketFactory;

/**
 * This is used to fetch the GAIA version supported by the device.
 */
public class V2ApiVersionFetcher extends V1V2Plugin {

    private static final String TAG = "V2ApiVersionFetcher";

    private static final int API_VENDOR_ID = QTIL_V1V2_VENDOR_ID;

    private final V2ApiVersionFetcherListener mVersionListener;

    public V2ApiVersionFetcher(@NonNull V2ApiVersionFetcherListener versionListener,
                               @NonNull GaiaSender sender) {
        super(API_VENDOR_ID, sender);
        this.mVersionListener = versionListener;
    }

    @Override
    public void onStarted() {
        fetch();
    }

    @Override
    protected void onStopped() {
    }

    public void fetch() {
        // the versions are fetched using the GAIA V2_GET_API_VERSION command for backward
        //  compatibility
        sendPacket(COMMANDS.GET_API_VERSION);
    }

    public void onIncomingData(byte[] data) {
        onReceiveGaiaPacket(V1V2PacketFactory.buildPacket(data));
    }

    @Override
    protected boolean onCommand(V1V2Packet packet) {
        // unexpected
        return false;
    }

    @Override
    protected void onResponse(V1V2Packet response, @Nullable V1V2Packet sent) {
        if (response.getCommand() == COMMANDS.GET_API_VERSION) {
            V2ApiVersion version = new V2ApiVersion(response.getResponsePayload());
            mVersionListener.onVersion(version);
        }
    }

    @Override
    protected boolean onNotification(V1V2Packet packet) {
        // unexpected
        return false;
    }

    @Override
    protected void onError(V1V2Packet response, @Nullable V1V2Packet sent) {
        V1V2ErrorStatus status = response.getStatus();

        if (response.getCommand() == COMMANDS.GET_API_VERSION) {
            mVersionListener.onError(Reason.valueOf(status));
        }
    }

    @Override
    protected void onFailed(GaiaPacket packet, Reason reason) {
        if (!(packet instanceof V1V2Packet)) {
            Log.w(TAG, "[onFailed] Failed to fetch the API, reason=" + reason);
            return;
        }

        if (((V1V2Packet) packet).getCommand() == COMMANDS.GET_API_VERSION) {
            mVersionListener.onError(reason);
        }
    }

    private static final class COMMANDS {

        // POLLED STATUS commands 0x03nn
        static final int GET_API_VERSION = 0x0300;
    }
}
