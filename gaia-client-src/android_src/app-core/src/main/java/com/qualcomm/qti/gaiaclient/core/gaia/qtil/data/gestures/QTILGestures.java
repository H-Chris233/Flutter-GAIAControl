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

public enum QTILGestures implements Gesture {

    TAP(0, 0, 0),
    SWIPE_UP(1, 0, 1),
    SWIPE_DOWN(2, 0, 2),
    TAP_AND_SWIPE_UP(3, 0, 3),
    TAP_AND_SWIP_DOWN(4, 0, 4),
    DOUBLE_TAP(5, 0, 5),
    LONG_PRESS(6, 0, 6),
    EXTENDED_LONG_PRESS(7, 0, 7);
    /* SENTINEL(128, 15, 7); */ // unexpected

    private final int id;
    private final int byteOffset;
    private final int bitOffset;

    private static final QTILGestures[] VALUES = values();

    /**
     * Explicit constructor to have the values in a human readable way.
     */
    QTILGestures(int id, int byteOffset, int bitOffset) {
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
        return EXTENDED_LONG_PRESS.getByteOffset();
    }

    public static int getMaximumBitOffset() {
        return EXTENDED_LONG_PRESS.getBitOffset();
    }

    public static QTILGestures valueOf(int id) {
        return Arrays.stream(VALUES).filter(gesture -> gesture.id == id).findAny().orElse(null);
    }

    @Override
    public Set<Action> getSupportedActions() {
        Set<Action> actions = new LinkedHashSet<>(Arrays.asList(QTILActions.values()));

        if (this != EXTENDED_LONG_PRESS) {
            actions.removeAll(Arrays.asList(QTILActions.VA_FETCH_QUERY, QTILActions.MEDIA_SEEK_BACKWARD,
                                            QTILActions.MEDIA_SEEK_FORWARD));
        }

        return actions;
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
    public static final Creator<QTILGestures> CREATOR = new Creator<QTILGestures>() {
        @Override
        public QTILGestures createFromParcel(Parcel in) {
            return QTILGestures.valueOf(in.readString());
        }

        @Override
        public QTILGestures[] newArray(int size) {
            return new QTILGestures[size];
        }
    };
}
