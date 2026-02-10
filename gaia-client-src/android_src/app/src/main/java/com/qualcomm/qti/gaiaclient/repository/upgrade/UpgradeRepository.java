/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.upgrade;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;

public interface UpgradeRepository {

    void init();

    @NonNull
    LiveData<Result<UpgradeProgress, UpgradeFail>> getUpgradeLiveData();

    @NonNull
    LiveData<Integer> getSizeLiveData(SizeType type);

    @NonNull
    LiveData<Pair<UpgradeAlert, Boolean>> getAlertsLiveData();

    int getSize(SizeType type);

    void startUpgrade(Context context, Uri file, UpgradeOptions options);

    void abortUpgrade(Context context);

    void confirmUpgrade(Context context, UpgradeConfirmation confirmation, @NonNull ConfirmationOptions option);

    void reset();

}
