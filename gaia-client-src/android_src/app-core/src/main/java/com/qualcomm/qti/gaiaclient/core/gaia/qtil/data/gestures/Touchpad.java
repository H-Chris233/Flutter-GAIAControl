/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import android.os.Parcel;
import android.os.Parcelable;

public enum Touchpad implements Parcelable {

    SINGLE(0),
    RIGHT(1),
    LEFT(2),
    BOTH(3);

    private final int id;

    private static final Touchpad[] VALUES = values();

    Touchpad(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Touchpad valueOf(int id) {
        for (Touchpad touchpad : VALUES) {
            if (touchpad.id == id) {
                return touchpad;
            }
        }

        return null;
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
    public static final Creator<Touchpad> CREATOR = new Creator<Touchpad>() {
        @Override
        public Touchpad createFromParcel(Parcel in) {
            return Touchpad.valueOf(in.readString());
        }

        @Override
        public Touchpad[] newArray(int size) {
            return new Touchpad[size];
        }
    };
}
