/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;

public enum FitInfo {
    /**
     * Information of type {@link EarbudsFitResults FitState}.
     */
    FIT_STATE,
    /**
     * No type information: only used to dispatch errors.
     */
    FIT_TEST
}
