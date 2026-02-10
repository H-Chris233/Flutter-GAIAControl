/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.system;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.core.GaiaClientService;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BluetoothSubscriber;
import com.qualcomm.qti.gaiaclient.core.utils.BluetoothUtils;
import com.qualcomm.qti.gaiaclient.utils.SystemUtils;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class SystemRepositoryImpl extends SystemRepositoryData {

    private final BluetoothSubscriber mBluetoothSubscriber = new BluetoothSubscriber() {
        @Override
        public void onEnabled() {
            updateBluetoothState(true);
        }

        @Override
        public void onDisabled() {
            updateBluetoothState(false);
        }
    };

    @Inject
    public SystemRepositoryImpl() {

    }

    @Override
    public void init(Context context) {
        PublicationManager publicationManager = GaiaClientService.getPublicationManager();
        publicationManager.subscribe(mBluetoothSubscriber);

        // initialising the bluetooth state
        updateBluetoothState(context);
    }

    @Override
    public void checkAndroidDeveloperModeState(Context context) {
        updateAndroidDeveloperModeState(SystemUtils.isAndroidDeveloperModeOn(context));
    }

    @Override
    public void updateBluetoothState(Context context) {
        updateBluetoothState(BluetoothUtils.isBluetoothEnabled(context));
    }
}
