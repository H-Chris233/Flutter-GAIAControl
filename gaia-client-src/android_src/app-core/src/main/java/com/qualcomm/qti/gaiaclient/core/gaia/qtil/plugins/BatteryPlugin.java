/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.BatteryPublisher;

import java.util.Set;

public interface BatteryPlugin {

    void fetchSupportedBatteries();

    void fetchBatteryLevels(Set<Battery> batteries);

    // to force the BatteryPlugin to implement BatteryPublisher
    // this is unused
    BatteryPublisher getBatteryPublisher();

}
