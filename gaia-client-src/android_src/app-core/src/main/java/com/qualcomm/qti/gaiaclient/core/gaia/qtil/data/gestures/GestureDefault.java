/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.BITS_IN_BYTE;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class GestureDefault extends BitItemDefault implements Gesture {

    @NonNull
    private Set<Action> supportedActions = new LinkedHashSet<>();

    public GestureDefault(int id) {
        super(id / BITS_IN_BYTE, id % BITS_IN_BYTE);
    }

    GestureDefault(int byteOffset, int bitOffset) {
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

    public void addSupportedAction(Action action) {
        supportedActions.add(action);
    }

    public void addSupportedActions(Collection<Action> actions) {
        this.supportedActions.addAll(actions);
    }

    public void setSupportedActions(@NonNull Set<Action> actions) {
        this.supportedActions = actions;
    }

    @Override
    public Set<Action> getSupportedActions() {
        return new LinkedHashSet<>(supportedActions);
    }

    // necessary for Parcelable
    public static final Creator<GestureDefault> CREATOR = new Creator<GestureDefault>() {
        @Override
        public GestureDefault createFromParcel(Parcel in) {
            return new GestureDefault(in.readInt());
        }

        @Override
        public GestureDefault[] newArray(int size) {
            return new GestureDefault[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "GestureDefault{" +
                "actions=" + supportedActions +
                '}';
    }
}
