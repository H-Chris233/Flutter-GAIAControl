/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.earbudfit;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import javax.inject.Singleton;

@Singleton
public abstract class EarbudFitRepositoryData implements EarbudFitRepository {

    private final StickyLiveData<Result<EarbudsFitResults, Reason>> mFitResults = new StickyLiveData<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    @NonNull
    @Override
    public LiveData<Result<EarbudsFitResults, Reason>> getFitResultsLiveData() {
        return mFitResults;
    }

    @Override
    public void reset() {
        updateAndClearFitResults(Result.error(Reason.DISCONNECTED));
    }

    protected void updateAndClearFitResults(Result<EarbudsFitResults, Reason> result) {
        mFitResults.postValue(result);
        handler.post(() -> mFitResults.postValue(null)); // slightly delay the clearance for observers
    }

}
