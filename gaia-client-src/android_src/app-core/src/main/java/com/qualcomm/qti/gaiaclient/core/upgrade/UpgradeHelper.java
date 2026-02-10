/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.upgrade;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpdateOptions;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;

public interface UpgradeHelper {

    void startUpgrade(Context context, @NonNull UpdateOptions options);

    void abort();

    void confirm(UpgradeConfirmation confirmation, @NonNull ConfirmationOptions option);

    void onUpgradeMessage(byte[] data);

    void onAcknowledged();

    void onSendingFailed(Reason error);

    void onErrorResponse(UpgradeGaiaCommand command, GaiaErrorStatus status);

    void onUpgradeConnected();

    void onUpgradeDisconnected();

    void onPlugged(UpgradeHelperListener listener);

    void onUnplugged();

    boolean isFlushed();

    void release();

}
