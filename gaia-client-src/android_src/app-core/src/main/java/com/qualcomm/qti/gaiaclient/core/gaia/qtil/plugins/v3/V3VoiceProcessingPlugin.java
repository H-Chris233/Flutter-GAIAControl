/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.VoiceProcessingInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.CommandPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ErrorPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.NotificationPacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.ResponsePacket;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3Packet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Capability;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.SupportedEnhancements;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancement;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceEnhancementConfiguration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.VoiceProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.VoiceProcessingPublisher;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.util.ArrayList;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;

public class V3VoiceProcessingPlugin extends V3QTILPlugin implements VoiceProcessingPlugin {

    private static final String TAG = "V3VoiceProcessingPlugin";

    private static final boolean LOG_METHODS = DEBUG.QTIL.V3_VOICE_PROCESSING_PLUGIN;

    private final VoiceProcessingPublisher mVoiceProcessingPublisher = new VoiceProcessingPublisher();

    private final PublicationManager publicationManager;

    private ArrayList<VoiceEnhancement> enhancements = null;

    private int supportedEnhancementsOffset = 0;

    public V3VoiceProcessingPlugin(@NonNull GaiaSender sender) {
        this(sender, getPublicationManager());
    }

    @VisibleForTesting
    public V3VoiceProcessingPlugin(@NonNull GaiaSender sender, PublicationManager publicationManager) {
        super(QTILFeature.VOICE_PROCESSING, sender);
        this.publicationManager = publicationManager;
    }

    @Override
    public void onStarted() {
        publicationManager.register(mVoiceProcessingPublisher);
    }

    @Override
    protected void onStopped() {
        publicationManager.unregister(mVoiceProcessingPublisher);
    }

    @Override
    public VoiceProcessingPublisher getVoiceProcessingPublisher() {
        return mVoiceProcessingPublisher;
    }

    @Override
    public void getSupportedEnhancements() {
        if (enhancements == null) {
            enhancements = new ArrayList<>();
            supportedEnhancementsOffset = 0;
            sendPacket(COMMANDS.V1_GET_SUPPORTED_ENHANCEMENTS, supportedEnhancementsOffset);
        }
        // otherwise: enhancements are being fetched, wait for the operation to end
    }

    @Override
    public void getConfiguration(Capability capability) {
        if (capability == null) {
            Log.w(TAG, "[getConfiguration] capability is null.");
            return;
        }
        sendPacket(COMMANDS.V1_GET_CONFIG_ENHANCEMENT, capability.getValue());
    }

    @Override
    public void setConfiguration(VoiceEnhancementConfiguration configuration) {
        if (configuration == null) {
            Log.w(TAG, "[setConfiguration] configuration is null.");
            return;
        }
        sendPacket(COMMANDS.V1_SET_CONFIG_ENHANCEMENT, configuration.getSetterBytes());
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        Logger.log(LOG_METHODS, TAG, "onNotification", new Pair<>("packet", packet));
        if (packet.getCommand() == NOTIFICATIONS.V1_ENHANCEMENT_MODE_CHANGE) {
            publishConfiguration(packet.getData());
        }
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        Logger.log(LOG_METHODS, TAG, "onResponse", new Pair<>("response", response), new Pair<>("sent", sent));
        switch (response.getCommand()) {
            case COMMANDS.V1_GET_SUPPORTED_ENHANCEMENTS:
                onSupportedEnhancements(response.getData());
                break;
            case COMMANDS.V1_GET_CONFIG_ENHANCEMENT:
                publishConfiguration(response.getData());
                break;
            case COMMANDS.V1_SET_CONFIG_ENHANCEMENT:
                // nothing to do
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

    private void publishConfiguration(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "publishConfiguration", new Pair<>("data", data));
        VoiceEnhancementConfiguration configuration = VoiceEnhancementConfiguration.getConfiguration(data);
        mVoiceProcessingPublisher.publishConfiguration(configuration);
    }

    private void onSupportedEnhancements(byte[] data) {
        Logger.log(LOG_METHODS, TAG, "onSupportedEnhancements", new Pair<>("data", data));

        if (enhancements == null) {
            Log.w(TAG, "[onSupportedEnhancements] received unexpected packet.");
            return;
        }

        SupportedEnhancements supported = new SupportedEnhancements(data);
        supported.getEnhancements().forEach(enhancement -> {
            if (!enhancements.contains(enhancement)) {
                enhancements.add(enhancement);
            }
        });

        if (supported.hasMoreData()) {
            // fetches the next chunk of enhancements
            this.supportedEnhancementsOffset += supported.getEnhancements().size();
            sendPacket(COMMANDS.V1_GET_SUPPORTED_ENHANCEMENTS, supportedEnhancementsOffset);
        }
        else {
            // all supported enhancements have been received, they can be published
            mVoiceProcessingPublisher.publishEnhancements(enhancements);
            this.enhancements = null;
            this.supportedEnhancementsOffset = 0;
        }
    }

    private void onError(int command, Reason reason) {
        switch (command) {
            case COMMANDS.V1_GET_SUPPORTED_ENHANCEMENTS:
                mVoiceProcessingPublisher.publishError(VoiceProcessingInfo.SUPPORTED_ENHANCEMENTS, reason);
                break;
            case COMMANDS.V1_GET_CONFIG_ENHANCEMENT:
                mVoiceProcessingPublisher.publishError(VoiceProcessingInfo.GET_CONFIGURATION, reason);
                break;
            case COMMANDS.V1_SET_CONFIG_ENHANCEMENT:
                mVoiceProcessingPublisher.publishError(VoiceProcessingInfo.SET_CONFIGURATION, reason);
                break;
        }
    }

    private static final class COMMANDS {

        static final int V1_GET_SUPPORTED_ENHANCEMENTS = 0x00;
        static final int V1_SET_CONFIG_ENHANCEMENT = 0x01;
        static final int V1_GET_CONFIG_ENHANCEMENT = 0x02;
    }

    private static final class NOTIFICATIONS {

        static final int V1_ENHANCEMENT_MODE_CHANGE = 0x00;
    }

}
