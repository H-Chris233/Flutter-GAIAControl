/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

import androidx.annotation.NonNull;

public class ScenarioConfiguration {

    private static final int DATA_LENGTH = 2;
    private static final int SCENARIO_OFFSET = 0;
    private static final int OPTION_OFFSET = 1;

    private final int scenarioValue;

    private final Scenario scenario;

    private final int optionValue;

    private final ScenarioOption option;

    public ScenarioConfiguration(int scenarioValue) {
        this(scenarioValue, -1);
    }

    public ScenarioConfiguration(@NonNull Scenario scenario) {
        this(scenario, -1);
    }

    public ScenarioConfiguration(byte[] data) {
        this(getUINT8(data, SCENARIO_OFFSET), getUINT8(data, OPTION_OFFSET));
    }

    public ScenarioConfiguration(int scenarioValue, int optionValue) {
        this.scenarioValue = scenarioValue;
        this.optionValue = optionValue;
        this.scenario = Scenario.valueOf(scenarioValue);
        this.option = ScenarioOption.valueOf(optionValue);
    }

    public ScenarioConfiguration(@NonNull Scenario scenario, int optionValue) {
        this.scenarioValue = scenario.getValue();
        this.optionValue = optionValue;
        this.scenario = scenario;
        this.option = ScenarioOption.valueOf(optionValue);
    }

    public int getScenarioValue() {
        return scenarioValue;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public int getOptionValue() {
        return optionValue;
    }

    public ScenarioOption getOption() {
        return option;
    }

    public byte[] getBytes() {
        byte[] data = new byte[DATA_LENGTH];
        setUINT8(scenarioValue, data, SCENARIO_OFFSET);
        setUINT8(optionValue, data, OPTION_OFFSET);
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "ScenarioConfiguration{" +
                ", scenario=" + scenario +
                ", option=" + option +
                '}';
    }
}
