/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8;

public class EarbudsFitResults {

    private final FitResult leftStatus;

    private final FitResult rightStatus;

    public EarbudsFitResults(FitResult leftStatus, FitResult rightStatus) {
        this.leftStatus = leftStatus;
        this.rightStatus = rightStatus;
    }

    public EarbudsFitResults(byte[] data) {
        int LEFT_FIT_RESULT_OFFSET = 0;
        int RIGHT_FIT_RESULT_OFFSET = 1;
        this.leftStatus = FitResult.valueOf(getUINT8(data, LEFT_FIT_RESULT_OFFSET));
        this.rightStatus = FitResult.valueOf(getUINT8(data, RIGHT_FIT_RESULT_OFFSET));
    }

    @Nullable
    public FitResult getFitStatus(@NonNull EarbudPosition position) {
        switch (position) {
            case LEFT:
                return leftStatus;
            case RIGHT:
                return rightStatus;
            default:
                return null;
        }
    }
}
