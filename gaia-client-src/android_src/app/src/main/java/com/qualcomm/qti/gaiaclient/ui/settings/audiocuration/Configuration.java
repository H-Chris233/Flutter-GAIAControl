/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration;

import static com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ConfigurationOption.CHANGE_TO_OFF;
import static com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ConfigurationOption.DISABLED;
import static com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ConfigurationOption.NO_CHANGE;
import static com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ConfigurationOption.TOGGLE_OFF;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.ACInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Scenario;

import java.util.Arrays;
import java.util.List;

public enum Configuration {

    TOGGLE_1(new ConfigurationOption[]{TOGGLE_OFF}, ACInfo.TOGGLE_CONFIGURATION), // toggle 1 cannot be disabled
    TOGGLE_2(new ConfigurationOption[]{TOGGLE_OFF}, ACInfo.TOGGLE_CONFIGURATION), // toggle 2 cannot be disabled
    TOGGLE_3(new ConfigurationOption[]{TOGGLE_OFF, DISABLED}, ACInfo.TOGGLE_CONFIGURATION),
    IDLE(new ConfigurationOption[]{CHANGE_TO_OFF, NO_CHANGE}, ACInfo.SCENARIO_CONFIGURATION),
    PLAYBACK(new ConfigurationOption[]{CHANGE_TO_OFF, NO_CHANGE}, ACInfo.SCENARIO_CONFIGURATION),
    CALL(new ConfigurationOption[]{CHANGE_TO_OFF, NO_CHANGE}, ACInfo.SCENARIO_CONFIGURATION),
    ASSISTANT(new ConfigurationOption[]{CHANGE_TO_OFF, NO_CHANGE}, ACInfo.SCENARIO_CONFIGURATION),
    LE_STEREO_RECORDING(new ConfigurationOption[]{CHANGE_TO_OFF, NO_CHANGE}, ACInfo.SCENARIO_CONFIGURATION);

    @NonNull
    private final ConfigurationOption[] options;

    @NonNull
    private final ACInfo info;

    private static final Configuration[] VALUES = values();

    Configuration(@NonNull ConfigurationOption[] options, @NonNull ACInfo info) {
        this.options = options;
        this.info = info;
    }

    @NonNull
    public List<ConfigurationOption> getOptions() {
        return Arrays.asList(options);
    }

    @NonNull
    public ACInfo getInfo() {
        return info;
    }

    public int getToggle() {
        switch (this) {
            case TOGGLE_1:
                return 1;
            case TOGGLE_2:
                return 2;
            case TOGGLE_3:
                return 3;

            default:
                return 0;
        }
    }

    public Scenario getScenario() {
        switch (this) {
            case IDLE:
                return Scenario.IDLE;
            case PLAYBACK:
                return Scenario.PLAYBACK_MUSIC;
            case CALL:
                return Scenario.VOICE_CALL;
            case ASSISTANT:
                return Scenario.DIGITAL_ASSISTANT;
            case LE_STEREO_RECORDING:
                return Scenario.LE_STEREO_RECORDING;

            default:
                return null;
        }
    }

    public static Configuration[] getValues() {
        return VALUES;
    }

    public static Configuration valueOf(int toggle) {
        if (toggle == 1) {
            return TOGGLE_1;
        }
        else if (toggle == 2) {
            return TOGGLE_2;
        }
        else if (toggle == 3) {
            return TOGGLE_3;
        }

        return null;
    }

    public static Configuration valueOf(Scenario scenario) {
        switch (scenario) {
            case IDLE:
                return IDLE;
            case PLAYBACK_MUSIC:
                return PLAYBACK;
            case VOICE_CALL:
                return CALL;
            case DIGITAL_ASSISTANT:
                return ASSISTANT;
            case LE_STEREO_RECORDING:
                return LE_STEREO_RECORDING;
            default:
                return null;
        }
    }

    public boolean isToggle() {
        switch (this) {
            case TOGGLE_1:
            case TOGGLE_2:
            case TOGGLE_3:
                return true;

            case IDLE:
            case PLAYBACK:
            case CALL:
            case ASSISTANT:
            case LE_STEREO_RECORDING:
            default:
                return false;
        }
    }
}
