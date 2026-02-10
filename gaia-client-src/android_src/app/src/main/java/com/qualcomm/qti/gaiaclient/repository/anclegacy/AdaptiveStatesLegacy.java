/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.anclegacy;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptedGain;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveState;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.AdaptiveStateInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.anc.Gain;

public class AdaptiveStatesLegacy {

    private static final String TAG = "AdaptiveStatesLegacy";

    private final DeviceAdaptiveStateLegacy leftState;

    private final DeviceAdaptiveStateLegacy rightState;

    AdaptiveStatesLegacy() {
        this.leftState = new DeviceAdaptiveStateLegacy();
        this.rightState = new DeviceAdaptiveStateLegacy();
    }

    @Nullable
    public AdaptiveState getState(EarbudPosition position) {
        switch (position) {
            case LEFT:
                return leftState.getState();

            case RIGHT:
                return rightState.getState();
        }

        return null;
    }

    public boolean isAdaptive(EarbudPosition position) {
        switch (position) {
            case LEFT:
                return leftState.isAdaptive();

            case RIGHT:
                return rightState.isAdaptive();
        }

        return false;
    }

    public int getGainValue(EarbudPosition position) {
        switch (position) {
            case LEFT:
                return leftState.getGainValue();

            case RIGHT:
                return rightState.getGainValue();
        }

        return 0;
    }

    @Nullable
    public Gain getGain(EarbudPosition position) {
        switch (position) {
            case LEFT:
                return leftState.getGain();

            case RIGHT:
                return rightState.getGain();
        }

        return null;
    }

    void updateState(@NonNull AdaptiveStateInfo info) {
        EarbudPosition position = info.getPosition();
        if (position == null) {
            Log.w(TAG, "[updateState] position is null");
            return;
        }

        switch (position) {
            case LEFT:
                leftState.setState(info.getState());
                break;

            case RIGHT:
                rightState.setState(info.getState());
                break;
        }
    }

    void updateGain(@NonNull AdaptedGain info) {
        EarbudPosition position = info.getPosition();
        if (position == null) {
            Log.w(TAG, "[updateState] position is null");
            return;
        }

        switch (position) {
            case LEFT:
                leftState.setGain(info.getGain());
                break;

            case RIGHT:
                rightState.setGain(info.getGain());
                break;
        }
    }

    public boolean isAdaptiveDisabled() {
        return leftState.isStatic() && rightState.isStatic();
    }
}
