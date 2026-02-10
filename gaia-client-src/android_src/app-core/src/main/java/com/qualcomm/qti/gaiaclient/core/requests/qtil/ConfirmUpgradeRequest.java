/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.requests.qtil;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.UpgradePlugin;
import com.qualcomm.qti.gaiaclient.core.requests.core.Request;
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getQtilManager;

public class ConfirmUpgradeRequest extends Request<Void, Void, Void> {

    private final UpgradeConfirmation mConfirmation;

    @NonNull
    private final ConfirmationOptions option;

    public ConfirmUpgradeRequest(UpgradeConfirmation confirmation, @NonNull ConfirmationOptions option,
                                 @NonNull RequestListener<Void, Void, Void> listener) {
        super(listener);
        this.mConfirmation = confirmation;
        this.option = option;
    }

    @Override
    public void run(@Nullable Context context) {
        UpgradePlugin plugin = getQtilManager().getUpgradePlugin();
        if (plugin != null) {
            plugin.confirm(mConfirmation, option);
            onComplete(null);
        }
        else {
            onError(null);
        }
    }

    @Override
    protected void onEnd() {
    }

}
