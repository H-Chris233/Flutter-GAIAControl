/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;

import java.util.Objects;

public class FeatureState {

    private final Feature feature;

    private final State state;

    private static final int TYPE_OFFSET = 0;
    private static final int STATE_OFFSET = 1;

    public FeatureState(byte[] payload) {
        // use '-1' as default value as '0' has a correspondence in the enumerations
        feature = Feature.valueOf(getUINT8(payload, TYPE_OFFSET, -1));
        state = State.valueOf(getUINT8(payload, STATE_OFFSET, -1));
    }

    public FeatureState(Feature feature) {
        this(feature, null);
    }

    public FeatureState(Feature feature, State state) {
        this.feature = feature;
        this.state = state;
    }

    public Feature getFeature() {
        return feature;
    }

    public State getState() {
        return state;
    }

    public byte[] getSetterBytes() {
        int LENGTH = 2;
        byte[] parameters = new byte[LENGTH];
        if (feature != null) {
            setUINT8(feature.getValue(), parameters, TYPE_OFFSET);
        }
        if (state != null) {
            setUINT8(state.getValue(), parameters, STATE_OFFSET);
        }
        return parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FeatureState that = (FeatureState) o;
        return feature == that.feature && Objects.equals(state, that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(feature, state);
    }

    @NonNull
    @Override
    public String toString() {
        return "FeatureState{" +
                "feature=" + feature +
                ", state=" + state +
                '}';
    }
}
