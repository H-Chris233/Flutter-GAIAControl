/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;

public class AdaptiveStateInfo {

    private final EarbudPosition position;

    private final AdaptiveState state;

    public AdaptiveStateInfo(EarbudPosition position, AdaptiveState state) {
        this.position = position;
        this.state = state;
    }

    public EarbudPosition getPosition() {
        return position;
    }

    public AdaptiveState getState() {
        return state;
    }
}
