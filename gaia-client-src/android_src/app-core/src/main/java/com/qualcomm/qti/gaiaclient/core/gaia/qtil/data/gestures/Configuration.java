/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import java.util.Objects;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getValueFromBits;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setValueAsBits;

public class Configuration {

    private final Touchpad touchpad;

    private final GestureContext context;

    private final Action action;

    private final int value;

    private static final int TOUCHPAD_BIT_OFFSET = 14;
    private static final int TOUCHPAD_BIT_LENGTH = 2;
    private static final int CONTEXT_BIT_OFFSET = 7;
    private static final int CONTEXT_BIT_LENGTH = 7;
    private static final int ACTION_BIT_OFFSET = 0;
    private static final int ACTION_BIT_LENGTH = 7;

    public Configuration(int value) {
        this.value = value;
        touchpad = Touchpad.valueOf(getValueFromBits(value, TOUCHPAD_BIT_OFFSET, TOUCHPAD_BIT_LENGTH));
        context = GestureFactory.getGestureContext(getValueFromBits(value, CONTEXT_BIT_OFFSET, CONTEXT_BIT_LENGTH));
        action = GestureFactory.getAction(getValueFromBits(value, ACTION_BIT_OFFSET, ACTION_BIT_LENGTH));
    }

    public Configuration(Touchpad touchpad, GestureContext context, Action action) {
        this.touchpad = touchpad;
        this.context = context;
        this.action = action;
        this.value = buildUintValue(touchpad, context, action);
    }

    public Touchpad getTouchpad() {
        return touchpad;
    }

    public GestureContext getContext() {
        return context;
    }

    public Action getAction() {
        return action;
    }

    public int getValue() {
        return value;
    }

    @VisibleForTesting
    public static int buildUintValue(Touchpad touchpad, GestureContext context, Action action) {
        int result = 0;
        result = setValueAsBits(touchpad.getId(), result, TOUCHPAD_BIT_OFFSET);
        result = setValueAsBits(context.getId(), result, CONTEXT_BIT_OFFSET);
        result = setValueAsBits(action.getId(), result, ACTION_BIT_OFFSET);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Configuration that = (Configuration) o;
        return value == that.value &&
                touchpad == that.touchpad &&
                Objects.equals(context, that.context) &&
                Objects.equals(action, that.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(touchpad, context, action, value);
    }

    @NonNull
    @Override
    public String toString() {
        return "Configuration{" +
                "touchpad=" + touchpad +
                ", context=" + context +
                ", action=" + action +
                ", value=" + value +
                '}';
    }
}
