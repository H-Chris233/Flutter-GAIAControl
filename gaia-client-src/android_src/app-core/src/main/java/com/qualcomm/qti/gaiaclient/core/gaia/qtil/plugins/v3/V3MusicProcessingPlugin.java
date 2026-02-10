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

import com.qualcomm.qti.gaiaclient.core.data.MusicProcessingInfo;
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
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EQState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.QTILFeature;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.MusicProcessingPlugin;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.MusicProcessingPublisher;

import java.util.ArrayList;
import java.util.List;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getPublicationManager;
import static com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo.BAND_LENGTH;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setSINT16;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

public class V3MusicProcessingPlugin extends V3QTILPlugin implements MusicProcessingPlugin {

    private static final String TAG = "V3MusicProcessingPlugin";

    private final MusicProcessingPublisher mMusicProcessingPublisher = new MusicProcessingPublisher();

    private final PublicationManager mPublicationManager;

    public V3MusicProcessingPlugin(@NonNull GaiaSender sender) {
        this(sender, getPublicationManager());
    }

    @VisibleForTesting
    public V3MusicProcessingPlugin(@NonNull GaiaSender sender, PublicationManager publicationManager) {
        super(QTILFeature.MUSIC_PROCESSING, sender);
        this.mPublicationManager = publicationManager;
    }

    @Override
    protected void onStarted() {
        mPublicationManager.register(mMusicProcessingPublisher);
    }

    @Override
    protected void onStopped() {
        mPublicationManager.unregister(mMusicProcessingPublisher);
    }

    @Override
    public MusicProcessingPublisher getMusicProcessingPublisher() {
        return mMusicProcessingPublisher;
    }

    @Override
    public boolean fetch(MusicProcessingInfo info) {
        switch (info) {
            case AVAILABLE_PRE_SETS:
                sendPacket(COMMANDS.V1_GET_AVAILABLE_EQ_PRE_SETS);
                return true;
            case SELECTED_SET:
                sendPacket(COMMANDS.V1_GET_EQ_SET);
                return true;
            case USER_SET_BANDS_NUMBER:
                sendPacket(COMMANDS.V1_GET_USER_SET_NUMBER_OF_BANDS);
                return true;
            case EQ_STATE:
                sendPacket(COMMANDS.V1_GET_EQ_STATE);
                return true;
            case USER_SET_CONFIGURATION:
            case BAND_CHANGE:
                // not supported: requires parameters
            default:
                Log.w(TAG, "[fetch] Unsupported EQInfo for 'fetch': " + info);
                return false;
        }
    }

    @Override
    public void fetchUserSetBandConfiguration(int bandStart, int bandEnd) {
        if (bandStart < 0 || bandEnd < bandStart) {
            // insufficient parameters
            return;
        }

        int maxLength = getMaxPayloadSize(SizeInfo.OPTIMUM_TX_PAYLOAD);
        final int RESPONSE_FIXED_LENGTH = 2;
        int start = bandStart;
        int maxPerPayload = (maxLength - RESPONSE_FIXED_LENGTH) / BAND_LENGTH;

        while (start <= bandEnd) {
            int remaining = bandEnd - start + 1;
            int request = maxPerPayload - remaining > 0 ? remaining : maxPerPayload;
            int end = start + request - 1;
            sendGetUserConfiguration(start, end);
            start = end + 1;
        }
    }

    @VisibleForTesting
    public void sendGetUserConfiguration(int bandStart, int bandEnd) {
        int LENGTH = 2;
        int BAND_START_OFFSET = 0;
        int BAND_END_OFFSET = 1;
        byte[] parameters = new byte[LENGTH];
        setUINT8(bandStart, parameters, BAND_START_OFFSET);
        setUINT8(bandEnd, parameters, BAND_END_OFFSET);
        sendPacket(COMMANDS.V1_GET_USER_SET_CONFIGURATION, parameters);
    }

    @Override
    public void fetchUserSetBandConfiguration(int band) {
        sendGetUserConfiguration(band, band);
    }

    @Override
    public void setUserSetGains(int bandStart, int bandEnd, double[] gains) {
        int count = bandEnd - bandStart + 1;

        if (gains == null) {
            Log.w(TAG, "[setUserSetGain] insufficient parameters: no gains provided");
            return;
        }

        if (bandStart < 0) {
            Log.w(TAG, String.format("[setUserSetGain] insufficient parameters: start band is negative: band=$1%d",
                                     bandStart));
            return;
        }

        if (bandEnd < 0) {
            Log.w(TAG,
                  String.format("[setUserSetGain] insufficient parameters: end band is negative: band=$1%d", bandEnd));
            return;
        }

        if (bandEnd < bandStart) {
            Log.w(TAG, String.format(
                    "[setUserSetGain] insufficient parameters: start band is higher than the end band: start=$1%d, end=$2%d",
                    bandStart, bandEnd));
            return;
        }

        if (gains.length < count) {
            Log.w(TAG,
                  String.format(
                          "[setUserSetGain] insufficient parameters: not enough gains provided: expected=$1%d, provided=$2%d",
                          count, gains.length));
            return;
        }

        int FIXED_LENGTH = 2;
        int BAND_START_OFFSET = 0;
        int BAND_END_OFFSET = 1;
        int GAINS_OFFSET = 2;
        int GAIN_LENGTH = 2;

        byte[] parameters = new byte[FIXED_LENGTH + count * GAIN_LENGTH];
        setUINT8(bandStart, parameters, BAND_START_OFFSET);
        setUINT8(bandEnd, parameters, BAND_END_OFFSET);
        for (int i = 0; i < count; i++) {
            setSINT16(BandInfo.formatGain(gains[i]), parameters, GAINS_OFFSET + i * GAIN_LENGTH);
        }

        sendPacket(COMMANDS.V1_SET_USER_SET_CONFIGURATION, parameters);
    }

    @Override
    public void selectSet(PreSet set) {
        sendPacket(COMMANDS.V1_SET_EQ_SET, set.getValue());
    }

    @Override
    protected void onResponse(ResponsePacket response, @Nullable CommandPacket sent) {
        switch (response.getCommand()) {
            case COMMANDS.V1_GET_EQ_STATE:
                publishState(response.getData());
                break;
            case COMMANDS.V1_GET_AVAILABLE_EQ_PRE_SETS:
                publishAvailablePreSets(response.getData());
                break;
            case COMMANDS.V1_GET_EQ_SET:
                publishEqSet(response.getData());
                break;
            case COMMANDS.V1_GET_USER_SET_NUMBER_OF_BANDS:
                publishBandsNumber(response.getData());
                break;
            case COMMANDS.V1_GET_USER_SET_CONFIGURATION:
                publishSetConfiguration(response.getData());
                break;
        }
    }

    @Override
    protected void onNotification(NotificationPacket packet) {
        switch (packet.getCommand()) {
            case NOTIFICATIONS.V1_EQ_STATE_CHANGE:
                publishState(packet.getData());
                break;
            case NOTIFICATIONS.V1_EQ_SET_CHANGE:
                publishEqSet(packet.getData());
                break;
            case NOTIFICATIONS.V1_USER_EQ_BAND_CHANGE:
                publishBandChange(packet.getData());
                break;
        }
    }

    @Override
    protected void onError(ErrorPacket errorPacket, @Nullable CommandPacket sent) {
        V3ErrorStatus status = errorPacket.getV3ErrorStatus();

        switch (errorPacket.getCommand()) {
            case COMMANDS.V1_GET_EQ_STATE:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.EQ_STATE, Reason.valueOf(status));
                break;
            case COMMANDS.V1_GET_AVAILABLE_EQ_PRE_SETS:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.AVAILABLE_PRE_SETS, Reason.valueOf(status));
                break;
            case COMMANDS.V1_GET_EQ_SET:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.SELECTED_SET, Reason.valueOf(status));
                break;
            case COMMANDS.V1_GET_USER_SET_NUMBER_OF_BANDS:
                mMusicProcessingPublisher
                        .publishError(MusicProcessingInfo.USER_SET_BANDS_NUMBER, Reason.valueOf(status));
                break;
            case COMMANDS.V1_GET_USER_SET_CONFIGURATION:
                mMusicProcessingPublisher
                        .publishError(MusicProcessingInfo.USER_SET_CONFIGURATION, Reason.valueOf(status));
                break;
        }
    }

    @Override
    protected void onFailed(GaiaPacket source, Reason reason) {
        if (!(source instanceof V3Packet)) {
            Log.w(TAG, "[onFailed] Packet is not a V3Packet.");
            return;
        }

        V3Packet packet = (V3Packet) source;

        switch (packet.getCommand()) {
            case COMMANDS.V1_GET_EQ_STATE:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.EQ_STATE, reason);
                break;
            case COMMANDS.V1_GET_AVAILABLE_EQ_PRE_SETS:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.AVAILABLE_PRE_SETS, reason);
                break;
            case COMMANDS.V1_GET_EQ_SET:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.SELECTED_SET, reason);
                break;
            case COMMANDS.V1_GET_USER_SET_NUMBER_OF_BANDS:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.USER_SET_BANDS_NUMBER, reason);
                break;
            case COMMANDS.V1_GET_USER_SET_CONFIGURATION:
                mMusicProcessingPublisher.publishError(MusicProcessingInfo.USER_SET_CONFIGURATION, reason);
                break;
        }
    }


    private void publishState(byte[] data) {
        int STATE_OFFSET = 0;
        EQState state = EQState.valueOf(getUINT8(data, STATE_OFFSET));
        mMusicProcessingPublisher.publishEqState(state);
    }

    private void publishAvailablePreSets(byte[] data) {
        final int COUNT_OFFSET = 0;
        final int PRE_SETS_OFFSET = 1;
        List<PreSet> presets = new ArrayList<>();
        int count = getUINT8(data, COUNT_OFFSET);

        if (data.length < count + PRE_SETS_OFFSET) {
            Log.w(TAG,
                  String.format("[publishAvailablePreSets] not enough argument: length=%1$d, count=%2$d", data.length,
                                count));
            mMusicProcessingPublisher.publishError(MusicProcessingInfo.AVAILABLE_PRE_SETS, Reason.MALFORMED_REQUEST);
            return;
        }

        for (int i = 0; i < count; i++) {
            presets.add(new PreSet(getUINT8(data, PRE_SETS_OFFSET + i)));
        }
        mMusicProcessingPublisher.publishAvailablePreSets(presets);
    }

    private void publishEqSet(byte[] data) {
        final int SET_OFFSET = 0;
        PreSet info = new PreSet(getUINT8(data, SET_OFFSET));
        mMusicProcessingPublisher.publishSelectedSet(info);
    }

    private void publishBandsNumber(byte[] data) {
        final int COUNT_OFFSET = 0;
        int count = getUINT8(data, COUNT_OFFSET);
        mMusicProcessingPublisher.publishBandsNumber(count);
    }

    private void publishSetConfiguration(byte[] data) {
        int BAND_START_OFFSET = 0;
        int BAND_END_OFFSET = 1;
        int BANDS_OFFSET = 2;
        int startBand = getUINT8(data, BAND_START_OFFSET);
        int endBand = getUINT8(data, BAND_END_OFFSET);
        int bandCount = endBand - startBand;
        List<BandInfo> bands = new ArrayList<>();

        if (endBand < startBand) {
            Log.w(TAG,
                  String.format("[publishSetConfiguration] Bands error: End band (%1$d) is less than start band (%2$d)",
                                endBand, startBand));
            mMusicProcessingPublisher
                    .publishError(MusicProcessingInfo.USER_SET_CONFIGURATION, Reason.MALFORMED_REQUEST);
            return;
        }

        int expectedLength = BANDS_OFFSET + BAND_LENGTH * bandCount;
        if (data.length < expectedLength) {
            Log.w(TAG,
                  String.format("[publishSetConfiguration] Data length error: length=%1$d, expected=%2$d",
                                data.length, expectedLength));
            mMusicProcessingPublisher
                    .publishError(MusicProcessingInfo.USER_SET_CONFIGURATION, Reason.MALFORMED_REQUEST);
            return;
        }

        for (int i = 0; i <= bandCount; i++) {
            bands.add(new BandInfo(startBand + i, getSubArray(data, BANDS_OFFSET + (i * BAND_LENGTH), BAND_LENGTH)));
        }

        mMusicProcessingPublisher.publishUserSetConfiguration(bands);
    }

    private void publishBandChange(byte[] data) {
        final int COUNT_OFFSET = 0;
        final int BANDS_OFFSET = 1;
        int count = getUINT8(data, COUNT_OFFSET);

        int expectedLength = count + 1;
        if (data.length < expectedLength) {
            Log.w(TAG,
                  String.format("[publishBandChange] Data length error: length=%1$d, expected=%2$d",
                                data.length, expectedLength));
            mMusicProcessingPublisher.publishError(MusicProcessingInfo.BAND_CHANGE, Reason.MALFORMED_REQUEST);
            return;
        }

        int[] bands = extractBands(data, BANDS_OFFSET, count);
        mMusicProcessingPublisher.publishBandChanged(bands);
    }

    private int[] extractBands(byte[] bands, int offset, int length) {
        if (bands.length < offset + length || offset < 0 || length < 0) {
            return new int[0];
        }

        int[] result = new int[length];
        for (int i = 0; i < length; i++) {
            result[i] = getUINT8(bands, i + offset);
        }
        return result;
    }

    private static final class COMMANDS {

        static final int V1_GET_EQ_STATE = 0x00;
        static final int V1_GET_AVAILABLE_EQ_PRE_SETS = 0x01;
        static final int V1_GET_EQ_SET = 0x02;
        static final int V1_SET_EQ_SET = 0x03;
        static final int V1_GET_USER_SET_NUMBER_OF_BANDS = 0x04;
        static final int V1_GET_USER_SET_CONFIGURATION = 0x05;
        static final int V1_SET_USER_SET_CONFIGURATION = 0x06;
    }

    private static final class NOTIFICATIONS {

        static final int V1_EQ_STATE_CHANGE = 0x00;
        static final int V1_EQ_SET_CHANGE = 0x01;
        static final int V1_USER_EQ_BAND_CHANGE = 0x02;
    }
}
