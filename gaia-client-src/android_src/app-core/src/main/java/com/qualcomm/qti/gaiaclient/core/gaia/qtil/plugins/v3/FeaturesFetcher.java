/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Feature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.GetSupportedFeaturesData;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.SupportedFeatures;

import java.util.ArrayList;
import java.util.List;

/**
 * This plugin is used to retrieve the supported QTIL features of a device.
 * It emulates a basic/core plugin that only has the GET_SUPPORTED_FEATURES and GET_SUPPORTED_FEATURES_NEXT commands.
 * Therefore it uses the {@link QTILFeature#BASIC BASIC} feature ID but is not the basic plugin. The basic plugin is
 * only loaded if the basic/core feature is supported - aka within the response to GET_SUPPORTED_FEATURES
 * and GET_SUPPORTED_FEATURES_NEXT.
 */
public class FeaturesFetcher extends V3QTILPlugin {

    private static final String TAG = "FeaturesFetcher";

    private final FeaturesFetcherListener mFeaturesListener;

    private List<Feature> features = null;

    public FeaturesFetcher(@NonNull FeaturesFetcherListener featuresListener, @NonNull GaiaSender sender) {
        super(QTILFeature.BASIC, sender);
        mFeaturesListener = featuresListener;
    }

    @Override
    public void onStarted() {
        sendPacket(COMMANDS.V1_GET_SUPPORTED_FEATURES);
    }

    @Override
    public void onStopped() {
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        if (response.getCommand() == COMMANDS.V1_GET_SUPPORTED_FEATURES
                || response.getCommand() == COMMANDS.V1_GET_SUPPORTED_FEATURES_NEXT) {
            // getting the data from the response
            GetSupportedFeaturesData receivedFeatures = new GetSupportedFeaturesData(response.getData());

            if (features == null) {
                features = new ArrayList<>();
            }

            // keeping the new chunk of supported features
            this.features.addAll(receivedFeatures.getFeatures());

            if (receivedFeatures.hasMoreData()) {
                // request for the next chunk
                sendPacket(COMMANDS.V1_GET_SUPPORTED_FEATURES_NEXT);
            }
            else {
                // all supported features have been received
                mFeaturesListener.onSupported(new SupportedFeatures(features));
                features = null;
            }
        }
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        // unexpected
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        V3ErrorStatus status = errorPacket.getV3ErrorStatus();

        if (errorPacket.getCommand() == COMMANDS.V1_GET_SUPPORTED_FEATURES
                || errorPacket.getCommand() == COMMANDS.V1_GET_SUPPORTED_FEATURES_NEXT) {
            mFeaturesListener.onError(Reason.valueOf(status));
        }
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        if (!(source instanceof V3Packet)) {
            Log.w(TAG, "[onNotAvailable] Packet is not a V3Packet.");
            return;
        }

        V3Packet packet = (V3Packet) source;

        if (packet.getCommand() == COMMANDS.V1_GET_SUPPORTED_FEATURES
                || packet.getCommand() == COMMANDS.V1_GET_SUPPORTED_FEATURES_NEXT) {
            mFeaturesListener.onError(reason);
        }
    }

    private static final class COMMANDS {

        static final int V1_GET_SUPPORTED_FEATURES = 0x01;
        static final int V1_GET_SUPPORTED_FEATURES_NEXT = 0x02;
    }
}
