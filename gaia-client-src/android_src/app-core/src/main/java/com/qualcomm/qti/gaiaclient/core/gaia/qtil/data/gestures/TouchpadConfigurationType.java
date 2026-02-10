/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public enum TouchpadConfigurationType {

    RESERVED(0, Collections.emptySet()),
    SINGLE_TOUCHPAD(1, Collections.singleton(Touchpad.SINGLE)),
    LEFT_RIGHT_BOTH(2, new LinkedHashSet<>(Arrays.asList(Touchpad.LEFT, Touchpad.RIGHT, Touchpad.BOTH)));

    private final int id;

    private final Set<Touchpad> touchpads;

    private static final TouchpadConfigurationType[] VALUES = values();

    TouchpadConfigurationType(int id, Set<Touchpad> touchpads) {
        this.id = id;
        this.touchpads = touchpads;
    }

    public int getId() {
        return id;
    }

    public Set<Touchpad> getTouchpads() {
        return touchpads;
    }

    @Nullable
    public static TouchpadConfigurationType valueOf(int value) {
        for (TouchpadConfigurationType type : VALUES) {
            if (type.getId() == value) {
                return type;
            }
        }

        return null;
    }
}
