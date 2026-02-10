/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.battery;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.BatteryLevel;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BatteryRepositoryData implements BatteryRepository {

    private final StickyLiveData<Set<Battery>> supportedBatteries = new StickyLiveData<>();

    private final StickyLiveData<Map<Battery, Integer>> batteryLevels = new StickyLiveData<>();

    @NonNull
    @Override
    public LiveData<Set<Battery>> getSupportedBatteriesLiveData() {
        return supportedBatteries;
    }

    @NonNull
    @Override
    public LiveData<Map<Battery, Integer>> getBatteryLevelsLiveData() {
        return batteryLevels;
    }

    @Override
    public void reset() {
        supportedBatteries.setValue(null);
        batteryLevels.setValue(null);
    }

    protected void updateSupportedBatteries(Set<Battery> supported) {
        supportedBatteries.postValue(supported);
    }

    protected void updateBatteryLevels(Set<BatteryLevel> update) {
        if (update == null || update.isEmpty()) {
            return;
        }

        final Map<Battery, Integer> levels = getBatteryLevels();
        update.forEach(batteryLevel -> {
            if (batteryLevel != null) {
                levels.put(batteryLevel.getBattery(), batteryLevel.getLevel());
            }
        });

        batteryLevels.postValue(levels);
    }

    private Map<Battery, Integer> getBatteryLevels() {
        Map<Battery, Integer> levels = batteryLevels.getValue();
        if (levels == null) {
            levels = new ConcurrentHashMap<>();
        }

        return levels;
    }

}
