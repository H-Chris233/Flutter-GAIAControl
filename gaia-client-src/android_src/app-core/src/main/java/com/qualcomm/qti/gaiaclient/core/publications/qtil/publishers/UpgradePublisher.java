/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.ChunkSizeType;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.UpgradeSubscriber;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;

public class UpgradePublisher extends Publisher<UpgradeSubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.UPGRADE;
    }

    public void publishProgress(UpgradeProgress progress) {
        forEachSubscriber(subscriber -> subscriber.onProgress(progress));
    }

    public void publishError(UpgradeFail error) {
        forEachSubscriber(subscriber -> subscriber.onError(error));
    }

    public void publishChunkSize(ChunkSizeType type, int size) {
        forEachSubscriber(subscriber -> subscriber.onChunkSize(type, size));
    }

    public void publishAlert(UpgradeAlert alert, boolean raised) {
        forEachSubscriber(subscriber -> subscriber.onAlert(alert, raised));
    }

}
