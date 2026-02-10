/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import androidx.annotation.NonNull;

import java.util.Objects;

public class TouchpadConfiguration {

    private final int value;

    private final TouchpadConfigurationType type;

    public TouchpadConfiguration(TouchpadConfigurationType type) {
        this.value = type.getId();
        this.type = type;
    }

    public TouchpadConfiguration(int value, TouchpadConfigurationType type) {
        this.value = value;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public TouchpadConfigurationType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TouchpadConfiguration that = (TouchpadConfiguration) o;
        return value == that.value && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    @NonNull
    @Override
    public String toString() {
        return "TouchpadConfiguration{" +
                "value=" + value +
                ", type=" + type +
                '}';
    }
}
