/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.Device;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.settings.common.data.SettingsViewData;

import java.util.Map;

public abstract class SettingsViewModel<S extends Settings, VD extends SettingsViewData<S>> extends AndroidViewModel {

    @NonNull
    private final StickyLiveData<VD> mViewData;

    private final StickyLiveData<DialogData> mDialogData = new StickyLiveData<>();

    private final StickyLiveData<String> title = new StickyLiveData<>();

    private final ConnectionRepository connectionRepository;

    private final DataObservers observers = new DataObservers();

    public SettingsViewModel(@NonNull Application application, ConnectionRepository connectionRepository) {
        super(application);
        this.connectionRepository = connectionRepository;
        this.mViewData = new StickyLiveData<>(initViewData(application.getApplicationContext()));

        // simulate a late init to allow child classes to be initialised
        new Handler().post(observers::start);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    final void observeDialogData(LifecycleOwner owner, @NonNull Observer<DialogData> dialogObserver) {
        mDialogData.observe(owner, dialogObserver);
    }

    final void observeViewData(LifecycleOwner owner, @NonNull Observer<VD> observer) {
        mViewData.observe(owner, observer);
    }

    final void observeTitle(LifecycleOwner owner, @NonNull Observer<String> observer) {
        title.observe(owner, observer);
    }

    protected abstract VD initViewData(Context context);

    protected String getSettingValue(S setting) {
        VD viewData = mViewData.getValue();
        return viewData != null ? viewData.getSettingValue(setting) : null;
    }

    protected void setVisibilities(@NonNull ArrayMap<S, Boolean> visibilities) {
        for (Map.Entry<S, Boolean> entry : visibilities.entrySet()) {
            updateSettingData((viewData -> viewData.setSettingVisible(entry.getKey(), entry.getValue())));
        }
    }

    protected void setAlert(String title, String message, String positiveLabel,
                            DialogInterface.OnClickListener positiveListener) {
        DialogData data = new DialogData(title, message, positiveLabel, positiveListener);
        mDialogData.setValue(data);
    }

    public void setTitle(String update) {
        this.title.postValue(update);
    }

    protected void setSettingVisibility(@NonNull S setting, boolean visibility) {
        updateSettingData((viewData) -> viewData.setSettingVisible(setting, visibility));
    }

    protected void setSettingSummary(@NonNull S setting, String summary) {
        updateSettingData((viewData) -> viewData.setSettingSummary(setting, summary));
    }

    protected void setSettingEnable(@NonNull S setting, boolean enabled) {
        updateSettingData((viewData) -> viewData.setSettingEnable(setting, enabled));
    }

    protected void setSettingList(@NonNull S setting, Pair<String[], String[]> entries) {
        updateSettingData((viewData) -> viewData.setSettingEntries(setting, entries));
    }

    protected void setSettingValue(@NonNull S setting, String value) {
        updateSettingData((viewData) -> viewData.setSettingValue(setting, value));
    }

    protected void setSettingSwitch(@NonNull S setting, boolean checked) {
        updateSettingData((viewData) -> viewData.setSettingSwitch(setting, checked));
    }

    protected void setEnable(boolean enabled) {
        updateSettingData((viewData) -> viewData.setEnable(enabled));
    }

    private void onConnectionStateUpdated(Resource<Device, BluetoothStatus> resource) {
        Device device = resource == null ? null : resource.getData();
        ConnectionState state = device != null ? device.getState() : null;
        onConnectionStateUpdated(state);
    }

    protected void onConnectionStateUpdated(ConnectionState state) {
        setEnable(ConnectionState.CONNECTED.equals(state));
    }

    protected void updateSettingData(@NonNull Updater<S, VD> updater) {
        VD viewData = mViewData.getValue();
        if (viewData != null) {
            updater.update(viewData);
        }
    }

    protected interface Updater<S extends Settings, VD extends SettingsViewData<S>> {
        void update(@NonNull VD viewData);
    }

    private class DataObservers extends Observers {
        private final Observer<Resource<Device, BluetoothStatus>> connectedDeviceObserver =
                SettingsViewModel.this::onConnectionStateUpdated;

        @Override
        protected void registerObservers() {
            connectionRepository.getConnectedDeviceLiveData().observeForever(connectedDeviceObserver);
        }

        @Override
        protected void unregisterObservers() {
            connectionRepository.getConnectedDeviceLiveData().removeObserver(connectedDeviceObserver);
        }
    }

}
