/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.Nullable;

import java.util.Objects;

public class VoiceEnhancement {

    private final int capabilityId;
    private final int version;
    @Nullable
    private final Capability capability;

    public VoiceEnhancement(int capabilityId, int version) {
        this.capabilityId = capabilityId;
        this.version = version;
        this.capability = Capability.valueOf(capabilityId);
    }

    public VoiceEnhancement(Capability capability, int version) {
        this.capabilityId = capability.getValue();
        this.version = version;
        this.capability = capability;
    }

    public int getCapabilityId() {
        return capabilityId;
    }

    @Nullable
    public Capability getCapability() {
        return capability;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VoiceEnhancement that = (VoiceEnhancement) o;
        return capabilityId == that.capabilityId &&
                version == that.version &&
                capability == that.capability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(capabilityId, version, capability);
    }
}
