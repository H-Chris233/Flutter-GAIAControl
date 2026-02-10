/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import android.os.Parcel;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public enum QTILGestureContexts implements GestureContext {

    PASSTHROUGH_GLOBAL(0, 0, 0),
    MEDIA_PLAYER_STREAMING(1, 0, 1),
    MEDIA_PLAYER_IDLE(2, 0, 2),
    VOICE_IN_CALL(3, 0, 3),
    VOICE_INCOMING(4, 0, 4),
    VOICE_OUTGOING(5, 0, 5),
    VOICE_IN_CALL_WITH_HELD(6, 0, 6),
    HANDSET_DISCONNECTED(7, 0, 7),
    HANDSET_CONNECTED(8, 1, 0);
    /* SENTINEL(128, 15, 7); */ // unexpected

    private final int id;
    private final int byteOffset;
    private final int bitOffset;

    private static final QTILGestureContexts[] VALUES = values();

    /**
     * Explicit constructor to have the values in a human readable way.
     */
    QTILGestureContexts(int id, int byteOffset, int bitOffset) {
        this.id = id;
        this.byteOffset = byteOffset;
        this.bitOffset = bitOffset;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public int getByteOffset() {
        return byteOffset;
    }

    @Override
    public int getBitOffset() {
        return bitOffset;
    }

    public static QTILGestureContexts getMaximumValue() {
        return HANDSET_CONNECTED;
    }

    public static int getMaximumByteOffset() {
        return getMaximumValue().getByteOffset();
    }

    public static int getMaximumBitOffset() {
        return getMaximumValue().getBitOffset();
    }

    public static QTILGestureContexts valueOf(int id) {
        return Arrays.stream(VALUES).filter(gesture -> gesture.id == id).findAny().orElse(null);
    }

    @Override
    public Set<Action> getSupportedActions() {
        QTILActions[] actions = new QTILActions[0];
        switch (this) {
            case PASSTHROUGH_GLOBAL:
                actions = new QTILActions[]{QTILActions.ANC_ENABLE_TOGGLE, QTILActions.ANC_NEXT_MODE,
                                            QTILActions.VOLUME_UP, QTILActions.VOLUME_DOWN,
                                            QTILActions.VA_PRIVACY_TOGGLE, QTILActions.VA_FETCH_QUERY,
                                            QTILActions.VA_PTT, QTILActions.VA_CANCEL, QTILActions.VA_FETCH,
                                            QTILActions.VA_QUERY};
                break;

            case MEDIA_PLAYER_STREAMING:
                actions = new QTILActions[]{QTILActions.MEDIA_PLAY_PAUSE_TOGGLE, QTILActions.MEDIA_STOP,
                                            QTILActions.MEDIA_NEXT_TRACK, QTILActions.MEDIA_PREVIOUS_TRACK,
                                            QTILActions.MEDIA_SEEK_FORWARD, QTILActions.MEDIA_SEEK_BACKWARD};
                break;

            case MEDIA_PLAYER_IDLE:
                actions = new QTILActions[]{QTILActions.MEDIA_PLAY_PAUSE_TOGGLE};
                break;

            case VOICE_IN_CALL:
                actions = new QTILActions[]{QTILActions.VOICE_HANG_UP_CALL, QTILActions.VOICE_TRANSFER_CALL,
                                            QTILActions.VOICE_CALL_CYCLE, QTILActions.VOICE_JOIN_CALLS,
                                            QTILActions.VOICE_MIC_MUTE_TOGGLE, QTILActions.VOICE_JOIN_CALLS_HANG_UP};
                break;

            case VOICE_IN_CALL_WITH_HELD:
                actions = new QTILActions[]{QTILActions.VOICE_CALL_CYCLE};
                break;

            case VOICE_INCOMING:
                actions = new QTILActions[]{QTILActions.VOICE_ACCEPT_CALL, QTILActions.VOICE_REJECT_CALL,
                                            QTILActions.VOICE_HANG_UP_CALL, QTILActions.VOICE_TRANSFER_CALL,
                                            QTILActions.VOICE_CALL_CYCLE, QTILActions.VOICE_JOIN_CALLS,
                                            QTILActions.VOICE_MIC_MUTE_TOGGLE, QTILActions.VOICE_JOIN_CALLS_HANG_UP};
                break;

            case VOICE_OUTGOING:
                actions = new QTILActions[]{QTILActions.VOICE_HANG_UP_CALL, QTILActions.VOICE_TRANSFER_CALL,
                                            QTILActions.VOICE_MIC_MUTE_TOGGLE, QTILActions.VOICE_CALL_CYCLE,
                                            QTILActions.VOICE_JOIN_CALLS, QTILActions.VOICE_JOIN_CALLS_HANG_UP};
                break;

            case HANDSET_CONNECTED:
                actions = new QTILActions[]{QTILActions.GAMING_MODE_TOGGLE, QTILActions.DISCONNECT_LRU_HANDSET};
                break;

            case HANDSET_DISCONNECTED:
                actions = new QTILActions[]{QTILActions.RECONNECT_MRU_HANDSET};
                break;
        }

        return new LinkedHashSet<>(Arrays.asList(actions));
    }

    @Override
    public int describeContents() {
        return ordinal();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name());
    }

    // necessary for Parcelable
    public static final Creator<QTILGestureContexts> CREATOR = new Creator<QTILGestureContexts>() {
        @Override
        public QTILGestureContexts createFromParcel(Parcel in) {
            return QTILGestureContexts.valueOf(in.readString());
        }

        @Override
        public QTILGestureContexts[] newArray(int size) {
            return new QTILGestureContexts[size];
        }
    };
}
