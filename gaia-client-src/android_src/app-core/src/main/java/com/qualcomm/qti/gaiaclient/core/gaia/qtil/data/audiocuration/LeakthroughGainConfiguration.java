/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

import androidx.annotation.NonNull;

public class LeakthroughGainConfiguration {

    private static final int CURRENT_MODE_OFFSET = 0;
    private static final int STEPS_NUMBER_OFFSET = 1;
    private static final int STEP_SIZE_IN_DB_OFFSET = 2;
    private static final int MINIMUM_VALUE_IN_DB_OFFSET = 3;
    private static final int INITIAL_STEP_VALUE_OFFSET = 4;

    private final int currentMode;
    private final int stepsNumber;
    private final int stepSizeInDB;
    private final int minimumGainInDB;
    private final int initialStep;

    public LeakthroughGainConfiguration(byte[] data) {
        currentMode = getUINT8(data, CURRENT_MODE_OFFSET);
        stepsNumber = getUINT8(data, STEPS_NUMBER_OFFSET);
        stepSizeInDB = getUINT8(data, STEP_SIZE_IN_DB_OFFSET);
        minimumGainInDB = getSINT8(data, MINIMUM_VALUE_IN_DB_OFFSET);
        initialStep = getUINT8(data, INITIAL_STEP_VALUE_OFFSET);
    }

    public LeakthroughGainConfiguration(int currentMode, int stepsNumber, int stepSizeInDB, int minimumGainInDB,
                                        int initialStep) {
        this.currentMode = currentMode;
        this.stepsNumber = stepsNumber;
        this.stepSizeInDB = stepSizeInDB;
        this.minimumGainInDB = minimumGainInDB;
        this.initialStep = initialStep;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public int getStepsNumber() {
        return stepsNumber;
    }

    public int getStepSizeInDB() {
        return stepSizeInDB;
    }

    public int getMinimumGainInDB() {
        return minimumGainInDB;
    }

    public int getInitialStep() {
        return initialStep;
    }

    @NonNull
    @Override
    public String toString() {
        return "LeakthroughGainConfiguration{" +
                "mode=" + currentMode +
                ", steps=" + stepsNumber +
                ", stepSizeInDB=" + stepSizeInDB +
                ", minimumGainInDB=" + minimumGainInDB +
                ", initialStep=" + initialStep +
                '}';
    }
}
