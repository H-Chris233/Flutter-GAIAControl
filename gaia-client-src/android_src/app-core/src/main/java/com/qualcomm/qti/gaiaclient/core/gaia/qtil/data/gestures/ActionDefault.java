/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import android.os.Parcel;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.BITS_IN_BYTE;

public class ActionDefault extends BitItemDefault implements Action {

    public ActionDefault(int id) {
        super(id / BITS_IN_BYTE, id % BITS_IN_BYTE);
    }

    ActionDefault(int byteOffset, int bitOffset) {
        super(byteOffset, bitOffset);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
    }

    // necessary for Parcelable
    public static final Creator<ActionDefault> CREATOR = new Creator<ActionDefault>() {
        @Override
        public ActionDefault createFromParcel(Parcel in) {
            return new ActionDefault(in.readInt());
        }

        @Override
        public ActionDefault[] newArray(int size) {
            return new ActionDefault[size];
        }
    };
}
