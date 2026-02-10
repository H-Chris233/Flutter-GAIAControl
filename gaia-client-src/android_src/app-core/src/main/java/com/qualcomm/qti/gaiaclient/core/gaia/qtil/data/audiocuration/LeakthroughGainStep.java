/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;

public class LeakthroughGainStep {

    private static final int CURRENT_MODE_OFFSET = 0;
    private static final int CURRENT_STEP_OFFSET = 1;

    private final int currentMode;
    private final int currentStep;

    public LeakthroughGainStep(byte[] data) {
        currentMode = getUINT8(data, CURRENT_MODE_OFFSET);
        currentStep = getUINT8(data, CURRENT_STEP_OFFSET);
    }

    public LeakthroughGainStep(int currentMode, int currentStep) {
        this.currentMode = currentMode;
        this.currentStep = currentStep;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    @NonNull
    @Override
    public String toString() {
        return "LeakthroughGainStep{" +
                "mode=" + currentMode +
                ", step=" + currentStep +
                '}';
    }
}
