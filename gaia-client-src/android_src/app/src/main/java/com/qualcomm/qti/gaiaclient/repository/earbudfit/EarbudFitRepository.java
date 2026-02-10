/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.earbudfit;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;
import com.qualcomm.qti.gaiaclient.repository.Result;

public interface EarbudFitRepository {

    void init();

    @NonNull
    LiveData<Result<EarbudsFitResults, Reason>> getFitResultsLiveData();

    void setFitTest(Context context, FitTestState state);

    void reset();
}
