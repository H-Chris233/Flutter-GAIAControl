/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.handsetservice;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.MultipointType;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.handsetservice.HandsetServiceRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.settings.common.SettingsViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class HandsetServiceViewModel extends SettingsViewModel<HandsetServiceSettings, HandsetServiceViewData> {

    private final HandsetServiceRepository handsetServiceRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public HandsetServiceViewModel(@NonNull Application application, ConnectionRepository connectionRepository,
                                   HandsetServiceRepository handsetServiceRepository) {
        super(application, connectionRepository);
        this.handsetServiceRepository = handsetServiceRepository;
        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    @Override
    protected HandsetServiceViewData initViewData(Context context) {
        return new HandsetServiceViewData(context);
    }

    protected void setMultipointEnable(boolean enable) {
        MultipointType type = enable ? MultipointType.MULTI_POINT : MultipointType.SINGLE_POINT;
        handsetServiceRepository.setMultipointType(getApplication().getApplicationContext(), type);
    }

    private void onMultipointType(MultipointType type) {
        if (type != null) {
            boolean enabled = type == MultipointType.MULTI_POINT;
            setSettingSwitch(HandsetServiceSettings.MULTIPOINT_ENABLE, enabled);
        }
    }

    private class DataObservers extends Observers {
        private final Observer<MultipointType> multipointTypeObserver = HandsetServiceViewModel.this::onMultipointType;

        @Override
        protected void registerObservers() {
            handsetServiceRepository.getMultipointTypeLiveData().observeForever(multipointTypeObserver);
        }

        @Override
        protected void unregisterObservers() {
            handsetServiceRepository.getMultipointTypeLiveData().removeObserver(multipointTypeObserver);
        }
    }

}
