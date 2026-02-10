/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.earbudfit;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.data.FitInfo;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudsFitResults;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.FitTestState;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.EarbudFitSubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.SetEarbudFitTestRequest;
import com.qualcomm.qti.gaiaclient.repository.Result;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class EarbudFitRepositoryImpl extends EarbudFitRepositoryData {

    private final EarbudFitSubscriber mSubscriber = new EarbudFitSubscriber() {

        @Override
        public void onFitResults(EarbudsFitResults results) {
            updateAndClearFitResults(Result.success(results));
        }

        @Override
        public void onFitError(FitInfo info, Reason reason) {
            if (info == FitInfo.FIT_TEST) {
                updateAndClearFitResults(Result.error(reason));
            }
        }
    };

    @Inject
    public EarbudFitRepositoryImpl() {
    }

    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mSubscriber);
    }

    @Override
    public void setFitTest(Context context, FitTestState state) {
        SetEarbudFitTestRequest request = new SetEarbudFitTestRequest(state);
        GaiaClientService.getRequestManager().execute(context, request);
    }

}
