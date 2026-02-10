/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class AbortUpgradeRequest extends Request<Void, Void, Void> {

    public AbortUpgradeRequest() {
        super(null);
    }

    @Override
    public void run(@Nullable Context context) {
        // getting the UpgradeHelper to force the abort even when a device is disconnected
        UpgradeHelper helper = getQtilManager().getUpgradeHelper();
        helper.abort();
        onComplete(null);
    }

    @Override
    protected void onEnd() {
    }

}
