/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.earbudfit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitResult;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.repository.earbudfit.EarbudFitRepository;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class EarbudFitViewModel extends AndroidViewModel {

    private final StickyLiveData<EarbudFitViewData> mLiveData = new StickyLiveData<>();

    private final EarbudFitRepository earbudFitRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public EarbudFitViewModel(@NonNull Application application, EarbudFitRepository earbudFitRepository) {
        super(application);
        this.earbudFitRepository = earbudFitRepository;
        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    void setObserver(@NonNull LifecycleOwner owner,
                     @NonNull Observer<EarbudFitViewData> dataObserver) {
        mLiveData.observe(owner, dataObserver);
    }

    void onAction(EarbudFitAction action) {
        EarbudFitState state = getState();

        if (action == EarbudFitAction.CANCEL && state == EarbudFitState.PROGRESS) {
            mLiveData.postValue(
                    new EarbudFitViewData(getApplication().getString(R.string.earbud_fit_cancelled_title),
                                          getApplication().getString(R.string.earbud_fit_cancelled_message),
                                          EarbudFitState.COMPLETE));
            earbudFitRepository.setFitTest(getApplication(), FitTestState.STOP);
        }
        else if (action == EarbudFitAction.START && state != EarbudFitState.PROGRESS) {
            mLiveData.postValue(
                    new EarbudFitViewData(getApplication().getString(R.string.earbud_fit_in_progress_title),
                                          getApplication().getString(R.string.earbud_fit_in_progress_message),
                                          EarbudFitState.PROGRESS));
            earbudFitRepository.setFitTest(getApplication(), FitTestState.START);
        }
        else if (action == EarbudFitAction.NAVIGATE_BACK && state == EarbudFitState.PROGRESS) {
            earbudFitRepository.setFitTest(getApplication(), FitTestState.STOP);
        }
    }

    private void onResult(Result<EarbudsFitResults, Reason> result) {
        if (result == null) {
            return;
        }

        switch (result.getStatus()) {
            case SUCCESS:
                onFitResults(result.getData());
                break;

            case FAIL:
                mLiveData.postValue(
                        new EarbudFitViewData(getApplication().getString(R.string.earbud_fit_failed_title),
                                              getApplication().getString(R.string.earbud_fit_failed_message),
                                              EarbudFitState.COMPLETE));
                break;

            case IN_PROGRESS:
                // unexpected
                break;
        }
    }

    private void onFitResults(EarbudsFitResults results) {
        FitResult leftResult = results != null ? results.getFitStatus(EarbudPosition.LEFT) : null;
        FitResult rightResult = results != null ? results.getFitStatus(EarbudPosition.RIGHT) : null;

        String message = buildResultMessage(leftResult, rightResult);
        ResultsViewData resultsViewData = new ResultsViewData(getResultImage(leftResult), getResultImage(rightResult));

        mLiveData.postValue(
                new EarbudFitViewData(getApplication().getString(R.string.earbud_fit_complete_title), message,
                                      EarbudFitState.COMPLETE, resultsViewData));
    }

    private String buildResultMessage(@Nullable FitResult leftResult, @Nullable FitResult rightResult) {
        String message = leftResult == rightResult ?
                         getResultMessageForBoth(leftResult) :
                         getApplication().getString(R.string.earbud_fit_combine_left_right,
                                                    getResultMessage(EarbudPosition.LEFT, leftResult),
                                                    getResultMessage(EarbudPosition.RIGHT, rightResult));

        return getApplication().getString(R.string.earbud_fit_complete_message, message);
    }

    private String getResultMessage(EarbudPosition position, @Nullable FitResult result) {
        String positionLabel =
                position == EarbudPosition.LEFT ? getApplication().getString(R.string.earbud_fit_position_left) :
                position == EarbudPosition.RIGHT ? getApplication().getString(R.string.earbud_fit_position_right) :
                getApplication().getString(R.string.earbud_fit_position_unknown);

        if (result == FitResult.GOOD_FIT) {
            return getApplication().getString(R.string.earbud_fit_result_good, positionLabel);
        }
        else if (result == FitResult.BAD_FIT) {
            return getApplication().getString(R.string.earbud_fit_result_bad, positionLabel);
        }
        else {
            return getApplication().getString(R.string.earbud_fit_result_fail, positionLabel);
        }
    }

    private String getResultMessageForBoth(@Nullable FitResult result) {
        if (result == FitResult.GOOD_FIT) {
            return getApplication().getString(R.string.earbud_fit_result_good_both);
        }
        else if (result == FitResult.BAD_FIT) {
            return getApplication().getString(R.string.earbud_fit_result_bad_both);
        }
        else {
            return getApplication().getString(R.string.earbud_fit_result_fail_both);
        }
    }

    private int getResultImage(@Nullable FitResult result) {
        if (result == FitResult.GOOD_FIT) {
            return R.drawable.ic_test_good_32dp;
        }
        else if (result == FitResult.BAD_FIT) {
            return R.drawable.ic_test_bad_32dp;
        }
        else {
            return R.drawable.ic_test_fail_32dp;
        }
    }

    private EarbudFitState getState() {
        EarbudFitViewData data = mLiveData.getValue();
        return data != null ? data.getState() : null;
    }

    private class DataObservers extends Observers {
        private final Observer<Result<EarbudsFitResults, Reason>> fitResultsObserver =
                EarbudFitViewModel.this::onResult;

        @Override
        protected void registerObservers() {
            earbudFitRepository.getFitResultsLiveData().observeForever(fitResultsObserver);
        }

        @Override
        protected void unregisterObservers() {
            earbudFitRepository.getFitResultsLiveData().removeObserver(fitResultsObserver);
        }
    }

}
