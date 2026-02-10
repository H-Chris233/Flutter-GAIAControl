/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.Objects;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.copyArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSubArray;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.setUINT8;

public abstract class VoiceEnhancementConfiguration {

    private static final int CAPABILITY_OFFSET = 0;
    private static final int VALUES_OFFSET = 1;

    private final int capabilityValue;

    @Nullable
    private final Capability capability;

    @Nullable
    private final byte[] configurationValues;

    protected VoiceEnhancementConfiguration(@NonNull Capability capability) {
        this.capability = capability;
        this.capabilityValue = capability.getValue();
        this.configurationValues = null;
    }

    protected VoiceEnhancementConfiguration(int capabilityValue, @Nullable Capability capability, byte[] values) {
        this.capabilityValue = capabilityValue;
        this.capability = capability;
        this.configurationValues = values;
    }

    public int getCapabilityValue() {
        return capabilityValue;
    }

    @Nullable
    public Capability getCapability() {
        return capability;
    }

    public byte[] getConfigurationValues() {
        return configurationValues;
    }

    public byte[] getSetterBytes() {
        byte[] values = getSetterValues();
        byte[] result = new byte[values.length + 1];
        setUINT8(capabilityValue, result, CAPABILITY_OFFSET);
        copyArray(values, result, VALUES_OFFSET);
        return result;
    }

    public abstract byte[] getSetterValues();

    public static VoiceEnhancementConfiguration getConfiguration(byte[] data) {
        int capabilityValue = getUINT8(data, CAPABILITY_OFFSET);
        Capability capability = Capability.valueOf(capabilityValue);
        byte[] values = getSubArray(data, VALUES_OFFSET);

        if (capability == Capability.CVC_3MIC) {
            return new CVC3MicConfiguration(values);
        }
        return new VoiceEnhancementConfiguration(capabilityValue, capability, values) {
            @Override
            public byte[] getSetterValues() {
                return new byte[0];
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof VoiceEnhancementConfiguration)) {
            // VoiceEnhancementConfiguration is abstract -> only children
            return false;
        }
        VoiceEnhancementConfiguration that = (VoiceEnhancementConfiguration) o;
        return capabilityValue == that.capabilityValue &&
                capability == that.capability &&
                (configurationValues == null ||
                        that.configurationValues == null ||
                        Arrays.equals(configurationValues, that.configurationValues));
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(capabilityValue, capability);
        result = 31 * result + Arrays.hashCode(configurationValues);
        return result;
    }
}
