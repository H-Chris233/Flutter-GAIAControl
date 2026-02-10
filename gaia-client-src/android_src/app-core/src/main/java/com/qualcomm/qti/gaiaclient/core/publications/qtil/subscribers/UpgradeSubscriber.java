/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.ChunkSizeType;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;

import androidx.annotation.NonNull;

public interface UpgradeSubscriber extends Subscriber {

    @NonNull
    @Override
    default Subscription getSubscription() {
        return CoreSubscription.UPGRADE;
    }

    void onProgress(UpgradeProgress progress);

    void onError(UpgradeFail error);

    void onChunkSize(ChunkSizeType type, int size);

    void onAlert(UpgradeAlert alert, boolean raised);
}
