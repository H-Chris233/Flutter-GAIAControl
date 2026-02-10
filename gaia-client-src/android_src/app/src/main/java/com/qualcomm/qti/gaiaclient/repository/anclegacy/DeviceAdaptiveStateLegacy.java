/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.anclegacy;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;

public class DeviceAdaptiveStateLegacy {

    private AdaptiveState state;

    private Gain gain;

    DeviceAdaptiveStateLegacy() {
        this.state = null;
        this.gain = null;
    }

    public boolean isStatic() {
        return state == AdaptiveState.DISABLED;
    }

    public boolean isAdaptive() {
        return state == AdaptiveState.ENABLED;
    }

    public AdaptiveState getState() {
        return state;
    }

    Gain getGain() {
        return gain;
    }

    int getGainValue() {
        return gain == null ? 0 : gain.getValue();
    }

    void setState(AdaptiveState state) {
        this.state = state;
    }

    void setGain(Gain gain) {
        this.gain = gain;
    }
}
