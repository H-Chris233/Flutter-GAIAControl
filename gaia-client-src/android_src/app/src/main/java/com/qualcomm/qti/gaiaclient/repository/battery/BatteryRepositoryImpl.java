/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.battery;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.BatteryLevel;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BatterySubscriber;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetBatteryLevelsRequest;
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetSupportedBatteriesRequest;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class BatteryRepositoryImpl extends BatteryRepositoryData {

    private final BatterySubscriber mBatterySubscriber = new BatterySubscriber() {
        @Override
        public void onSupportedBatteries(Set<Battery> supported) {
            updateSupportedBatteries(supported);
        }

        @Override
        public void onBatteryLevels(Set<BatteryLevel> levels) {
            updateBatteryLevels(levels);
        }
    };

    @Inject
    public BatteryRepositoryImpl() {
    }

    @Override
    public void init() {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mBatterySubscriber);
    }

    @Override
    public void fetchSupportedBatteries(Context context) {
        GetSupportedBatteriesRequest request = new GetSupportedBatteriesRequest();
        GaiaClientService.getRequestManager().execute(context, request);
    }

    @Override
    public void fetchBatteryLevels(Context context, Set<Battery> batteries) {
        GetBatteryLevelsRequest request = new GetBatteryLevelsRequest(batteries);
        GaiaClientService.getRequestManager().execute(context, request);
    }
}
