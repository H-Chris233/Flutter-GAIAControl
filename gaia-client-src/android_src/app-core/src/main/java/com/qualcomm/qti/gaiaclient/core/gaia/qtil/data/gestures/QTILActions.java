/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import android.os.Parcel;

import java.util.Arrays;

public enum QTILActions implements Action {

    MEDIA_PLAY_PAUSE_TOGGLE(0, 0, 0),
    MEDIA_STOP(1, 0, 1),
    MEDIA_NEXT_TRACK(2, 0, 2),
    MEDIA_PREVIOUS_TRACK(3, 0, 3),
    MEDIA_SEEK_FORWARD(4, 0, 4),
    MEDIA_SEEK_BACKWARD(5, 0, 5),
    VOICE_ACCEPT_CALL(6, 0, 6),
    VOICE_REJECT_CALL(7, 0, 7),
    VOICE_HANG_UP_CALL(8, 1, 0),
    VOICE_TRANSFER_CALL(9, 1, 1),
    VOICE_CALL_CYCLE(10, 1, 2),
    VOICE_JOIN_CALLS(11, 1, 3),
    VOICE_MIC_MUTE_TOGGLE(12, 1, 4),
    GAMING_MODE_TOGGLE(13, 1, 5),
    ANC_ENABLE_TOGGLE(14, 1, 6),
    ANC_NEXT_MODE(15, 1, 7),
    VOLUME_UP(16, 2, 0),
    VOLUME_DOWN(17, 2, 1),
    RECONNECT_MRU_HANDSET(18, 2, 2), /* MOST_RECENT_USED */
    VA_PRIVACY_TOGGLE(19, 2, 3),
    VA_FETCH_QUERY(20, 2, 4),
    VA_PTT(21, 2, 5), /* PUSH_TO_TALK */
    VA_CANCEL(22, 2, 6),
    VA_FETCH(23, 2, 7),
    VA_QUERY(24, 3, 0),
    DISCONNECT_LRU_HANDSET(25, 3, 1), /* MOST_RECENT_USED */
    VOICE_JOIN_CALLS_HANG_UP(26, 3, 2);
    /* SENTINEL(128, 15, 7); */ // unexpected

    private final int id;
    private final int byteOffset;
    private final int bitOffset;

    private static final QTILActions[] VALUES = values();

    QTILActions(int id, int byteOffset, int bitOffset) {
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

    public static int getMaximumByteOffset() {
        return VOICE_JOIN_CALLS_HANG_UP.getByteOffset();
    }

    public static int getMaximumBitOffset() {
        return VOICE_JOIN_CALLS_HANG_UP.getBitOffset();
    }

    public static QTILActions valueOf(int id) {
        return Arrays.stream(VALUES).filter(gesture -> gesture.id == id).findAny().orElse(null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name());
    }

    // necessary for Parcelable
    public static final Creator<QTILActions> CREATOR = new Creator<QTILActions>() {
        @Override
        public QTILActions createFromParcel(Parcel in) {
            return QTILActions.valueOf(in.readString());
        }

        @Override
        public QTILActions[] newArray(int size) {
            return new QTILActions[size];
        }
    };

}
