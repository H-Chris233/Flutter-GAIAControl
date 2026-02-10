/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3.V3AudioCurationPlugin.VERSIONS;

public enum Scenario {

    IDLE(0x01, VERSIONS.V1),
    PLAYBACK_MUSIC(0x02, VERSIONS.V1),
    VOICE_CALL(0x03, VERSIONS.V1),
    DIGITAL_ASSISTANT(0x04, VERSIONS.V1),
    LE_STEREO_RECORDING(0x05, VERSIONS.V3);

    private final int value;

    private final int featureVersion;

    private static final Scenario[] VALUES = values();

    Scenario(int value, int featureVersion) {
        this.value = value;
        this.featureVersion = featureVersion;
    }

    public int getValue() {
        return value;
    }

    public int getFeatureVersion() {
        return featureVersion;
    }

    @Nullable
    public static Scenario valueOf(int value) {
        for (Scenario scenario : VALUES) {
            if (scenario.value == value) {
                return scenario;
            }
        }

        return null;
    }

    public static Scenario[] getValues() {
        int length = VALUES.length;
        Scenario[] copy = new Scenario[length];
        System.arraycopy(VALUES, 0, copy, 0, length);
        return copy;
    }
}
