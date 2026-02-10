/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui;

import static com.qualcomm.qti.gaiaclient.core.utils.LocationUtils.isLocationEnabled;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.google.android.material.snackbar.Snackbar;
import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType;
import com.qualcomm.qti.gaiaclient.repository.Resource;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.repository.ResultStatus;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.Device;
import com.qualcomm.qti.gaiaclient.repository.connection.BluetoothReason;
import com.qualcomm.qti.gaiaclient.repository.discovery.DiscoveryRepository;
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.common.SnackMessage;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class MainActivityViewModel extends AndroidViewModel {

    private final int[] NAVIGATION_WITHOUT_BOTTOM_MENU_IDS = {R.id.destination_discovery,
                                                              R.id.destination_connection,
                                                              R.id.destination_upgrade,
                                                              R.id.destination_download_logs};

    private final int[] NAVIGATION_WITHOUT_TITLE = {R.id.destination_discovery,
                                                    R.id.destination_connection};

    private final int[] NAVIGATION_WITHOUT_DISCONNECTED_MESSAGE = {R.id.destination_discovery,
                                                                   R.id.destination_connection,
                                                                   R.id.destination_upgrade};

    @IntDef(flag = true, value = {View.VISIBLE, View.GONE, View.INVISIBLE})
    private @interface Visibility {

    }

    private final StickyLiveData<Integer> destinationId = new StickyLiveData<>();

    private final ConnectionRepository connectionRepository;

    private final DiscoveryRepository discoveryRepository;

    private final SystemRepository systemRepository;

    private final StickyLiveData<Integer> mNavigationVisibility = new StickyLiveData<>(View.GONE);

    private final StickyLiveData<Boolean> mIconBarVisibility = new StickyLiveData<>();

    private final StickyLiveData<SnackMessage> mSnackMessage = new StickyLiveData<>();

    private final StickyLiveData<Boolean> mShouldRequestBluetooth = new StickyLiveData<>();

    private final DataObservers observers = new DataObservers();

    private boolean mIsBluetoothDialogActive = false;

    @Inject
    public MainActivityViewModel(@NonNull Application application, ConnectionRepository connectionRepository,
                                 DiscoveryRepository discoveryRepository, SystemRepository systemRepository) {
        super(application);
        this.connectionRepository = connectionRepository;
        this.discoveryRepository = discoveryRepository;
        this.systemRepository = systemRepository;
        this.mShouldRequestBluetooth.postValue(this.systemRepository.isBluetoothEnabled());

        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    public void onDestinationChanged(int destinationId) {
        Integer value = this.destinationId.getValue();
        int oldDestination = value == null ? 0 : value;

        this.destinationId.postValue(destinationId);

        updateIconBarVisibility(Arrays.stream(NAVIGATION_WITHOUT_TITLE).anyMatch(id -> id == destinationId));
        updateNavigationVisibility(Arrays.stream(NAVIGATION_WITHOUT_BOTTOM_MENU_IDS)
                                           .anyMatch(id -> id == destinationId) ? View.GONE : View.VISIBLE);

        if (Arrays.stream(NAVIGATION_WITHOUT_DISCONNECTED_MESSAGE).anyMatch(id -> id == destinationId)) {
            // removing any message
            mSnackMessage.postValue(null);
            return;
        }

        Device device = connectionRepository.getDevice();
        ConnectionState state = device == null ? null : device.getState();

        if (Arrays.stream(NAVIGATION_WITHOUT_DISCONNECTED_MESSAGE).anyMatch(id -> id == oldDestination)
                && state != ConnectionState.CONNECTED) {
            updateSnackMessage(destinationId, state); // to generate the reconnect message
        }
    }

    public void observeNavigationVisibility(@NonNull LifecycleOwner owner,
                                            @NonNull Observer<Integer> observer) {
        mNavigationVisibility.observe(owner, observer);
    }

    public void observeIconBarVisibility(@NonNull LifecycleOwner owner,
                                         @NonNull Observer<Boolean> observer) {
        mIconBarVisibility.observe(owner, observer);
    }

    public void observeBluetoothRequestState(@NonNull LifecycleOwner owner, @NonNull Observer<Boolean> observer) {
        mShouldRequestBluetooth.observe(owner, observer);
    }

    public void observeSnackMessage(@NonNull LifecycleOwner owner,
                                    @NonNull Observer<SnackMessage> observer) {
        mSnackMessage.observe(owner, observer);
    }

    @Nullable
    public SnackMessage getSnackMessage() {
        return mSnackMessage.getValue();
    }

    public void setSnackMessage(SnackMessage message) {
        if (message == null) {
            return;
        }
        mSnackMessage.postValue(message);
    }

    public void updateBluetoothState(@Nullable Context context) {
        systemRepository.updateBluetoothState(context);
    }

    public Boolean getBluetoothState() {
        return systemRepository.isBluetoothEnabled();
    }

    public boolean isNavigationVisible() {
        Integer visibility = mNavigationVisibility.getValue();
        return visibility != null && visibility == View.VISIBLE;
    }

    private void updateNavigationVisibility(@Visibility int visibility) {
        mNavigationVisibility.postValue(visibility);
    }

    private void updateIconBarVisibility(boolean show) {
        mIconBarVisibility.postValue(show);
    }

    private void updateBluetoothRequestState(boolean state) {
        mShouldRequestBluetooth.postValue(!(state || mIsBluetoothDialogActive));
    }

    private void onDiscoveryUpdate(BluetoothReason reason) {
        if (reason == null) {
            // no message to display
            return;
        }

        if (reason == BluetoothReason.NO_BLUETOOTH || reason == BluetoothReason.NO_PERMISSIONS) {
            // this is handled by the activity Bluetooth and permission checks
            return;
        }

        String message = getApplication().getString(reason.getMessageID());
        int buttonAction = reason.getActionID();
        View.OnClickListener listener = reason == BluetoothReason.NO_LOCATION ? v -> goToLocationSettings() : null;

        mSnackMessage.postValue(new SnackMessage(message, Snackbar.LENGTH_INDEFINITE, buttonAction, listener));
    }

    private void onConnectedDeviceUpdate(Resource<Device, BluetoothStatus> resource) {
        Device device = resource.getData();
        ConnectionState state = device == null ? null : device.getState();
        Integer value = this.destinationId.getValue();
        int destinationId = value == null ? -1 : value;
        updateSnackMessage(destinationId, state);
    }

    private void updateSnackMessage(int destinationId, ConnectionState state) {
        boolean canShow = Arrays.stream(NAVIGATION_WITHOUT_DISCONNECTED_MESSAGE).noneMatch(id -> id == destinationId);
        if (!canShow || state == null || state == ConnectionState.DISCONNECTING) {
            mSnackMessage.postValue(null);
            return;
        }

        int messageId, actionId, length;
        View.OnClickListener listener = null;

        switch (state) {
            case DISCONNECTED:
                messageId = R.string.device_disconnected;
                actionId = R.string.button_reconnect;
                length = Snackbar.LENGTH_INDEFINITE;
                listener = view -> reconnect();
                break;

            case CONNECTING:
                messageId = R.string.connecting;
                actionId = R.string.button_dismiss;
                length = Snackbar.LENGTH_INDEFINITE;
                break;

            case CONNECTED:
            default:
                messageId = R.string.device_reconnected;
                actionId = R.string.button_dismiss;
                length = Snackbar.LENGTH_SHORT;
                break;
        }

        mSnackMessage.postValue(new SnackMessage(getApplication().getString(messageId), length, actionId, listener));
    }


    public void setBluetoothDialogActive(boolean active) {
        this.mIsBluetoothDialogActive = active;
        mShouldRequestBluetooth.postValue(!(mIsBluetoothDialogActive || getBluetoothState()));
    }

    private void goToLocationSettings() {
        // check that the location has not been enabled since it was flagged as disabled
        if (!isLocationEnabled(getApplication().getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplication().startActivity(intent);
        }
    }

    private void reconnect() {
        connectionRepository.reconnect(getApplication().getApplicationContext());
    }

    private class DataObservers extends Observers {
        private final Observer<Boolean> bluetoothStateObserver =
                MainActivityViewModel.this::updateBluetoothRequestState;
        private final Observer<Resource<Device, BluetoothStatus>> connectedDeviceObserver =
                MainActivityViewModel.this::onConnectedDeviceUpdate;
        private final Map<DiscoveryType, Observer<Result<List<DiscoveredDevice>, BluetoothReason>>> devicesObservers =
                new ArrayMap<>();

        @Override
        protected void registerObservers() {
            systemRepository.getBluetoothStateLiveData().observeForever(bluetoothStateObserver);
            connectionRepository.getConnectedDeviceLiveData().observeForever(connectedDeviceObserver);
            for (DiscoveryType type : DiscoveryType.values()) {
                Observer<Result<List<DiscoveredDevice>, BluetoothReason>> observer = result -> {
                    BluetoothReason reason =
                            result == null || result.getStatus() != ResultStatus.FAIL ? null : result.getReason();
                    onDiscoveryUpdate(reason);
                };
                devicesObservers.put(type, observer);
                discoveryRepository.getDevices(type).observeForever(observer);
            }
        }

        @Override
        protected void unregisterObservers() {
            systemRepository.getBluetoothStateLiveData().removeObserver(bluetoothStateObserver);
            connectionRepository.getConnectedDeviceLiveData().removeObserver(connectedDeviceObserver);
            devicesObservers
                    .forEach((type, observer) -> discoveryRepository.getDevices(type).removeObserver(observer));
        }
    }

}
