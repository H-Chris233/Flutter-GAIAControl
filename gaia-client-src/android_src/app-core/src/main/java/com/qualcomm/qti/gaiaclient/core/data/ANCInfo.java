/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.ANCState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptedGain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveStateInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;

public enum ANCInfo {
    /**
     * Information of type {@link ANCState ANCState}.
     */
    ANC_STATE,
    /**
     * Information of type {@link AdaptiveStateInfo}.
     */
    ADAPTIVE_STATE,
    /**
     * Information of type <code>int</code>.
     */
    ANC_MODE,
    /**
     * Information of type <code>int</code>.
     */
    ANC_MODE_COUNT,
    /**
     * Information of type {@link AdaptedGain}.
     */
    ADAPTED_GAIN,
    /**
     * Information of type {@link Gain}.
     */
    LEAKTHROUGH_GAIN;

    public boolean hasGetter() {
        switch (this) {
            case ANC_STATE:
            case ANC_MODE_COUNT:
            case LEAKTHROUGH_GAIN:
            case ANC_MODE:
                return true;

            default:
                return false;
        }
    }
}
