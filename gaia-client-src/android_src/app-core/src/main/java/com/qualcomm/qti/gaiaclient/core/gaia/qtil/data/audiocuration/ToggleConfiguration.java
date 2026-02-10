/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;

public class ToggleConfiguration {

    private static final int DATA_LENGTH = 2;
    private static final int TOGGLE_OFFSET = 0;
    private static final int OPTION_OFFSET = 1;

    private final int toggle;

    private final int optionValue;

    private final ToggleOption option;

    public ToggleConfiguration(int toggle) {
        this(toggle, -1);
    }

    public ToggleConfiguration(byte[] data) {
        this(getUINT8(data, TOGGLE_OFFSET), getUINT8(data, OPTION_OFFSET));
    }

    public ToggleConfiguration(int toggle, int optionValue) {
        this.toggle = toggle;
        this.optionValue = optionValue;
        this.option = ToggleOption.valueOf(optionValue);
    }

    public int getToggle() {
        return toggle;
    }

    public int getOptionValue() {
        return optionValue;
    }

    public ToggleOption getOption() {
        return option;
    }

    public byte[] getBytes() {
        byte[] data = new byte[DATA_LENGTH];
        setUINT8(toggle, data, TOGGLE_OFFSET);
        setUINT8(optionValue, data, OPTION_OFFSET);
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "ToggleConfiguration{" +
                "toggle=" + toggle +
                ", option=" + option +
                '}';
    }
}
