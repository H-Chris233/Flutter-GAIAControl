/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.upgrade.UpgradeHelper;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpdateOptions;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;

public interface UpgradePlugin {

    @NonNull
    UpgradeHelper getUpgradeHelper();

    default void startUpgrade(Context context, @NonNull UpdateOptions options) {
        getUpgradeHelper().startUpgrade(context, options);
    }

    default void confirm(UpgradeConfirmation confirmation, @NonNull ConfirmationOptions option) {
        getUpgradeHelper().confirm(confirmation, option);
    }
}
