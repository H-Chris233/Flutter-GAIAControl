/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.BatteryLevel;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscriber;
import com.qualcomm.qti.gaiaclient.core.publications.core.Subscription;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.CoreSubscription;

import java.util.Set;

public interface BatterySubscriber extends Subscriber {

    @NonNull
    default Subscription getSubscription() {
        return CoreSubscription.BATTERY;
    }

    void onSupportedBatteries(Set<Battery> supported);

    void onBatteryLevels(Set<BatteryLevel> levels);

}
