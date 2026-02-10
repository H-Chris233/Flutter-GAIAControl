/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.BatteryLevel;
import com.qualcomm.qti.gaiaclient.core.publications.core.Publisher;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BatterySubscriber;

import java.util.Set;

public class BatteryPublisher extends Publisher<BatterySubscriber> {

    @Override
    public Subscription getSubscription() {
        return CoreSubscription.BATTERY;
    }

    public void publishSupportedBatteries(Set<Battery> supported) {
        forEachSubscriber(subscriber -> subscriber.onSupportedBatteries(supported));
    }

    public void publishBatteryLevels(Set<BatteryLevel> levels) {
        forEachSubscriber(subscriber -> subscriber.onBatteryLevels(levels));
    }

}
