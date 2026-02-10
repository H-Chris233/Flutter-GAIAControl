/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.BITS_IN_BYTE;

import android.os.Parcel;

import androidx.annotation.NonNull;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class GestureContextDefault extends BitItemDefault implements GestureContext {

    @NonNull
    private Set<Action> supportedActions = new LinkedHashSet<>();

    public GestureContextDefault(int id) {
        super(id / BITS_IN_BYTE, id % BITS_IN_BYTE);
    }

    GestureContextDefault(int byteOffset, int bitOffset) {
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
    public static final Creator<GestureContextDefault> CREATOR = new Creator<GestureContextDefault>() {
        @Override
        public GestureContextDefault createFromParcel(Parcel in) {
            return new GestureContextDefault(in.readInt());
        }

        @Override
        public GestureContextDefault[] newArray(int size) {
            return new GestureContextDefault[size];
        }
    };
}
