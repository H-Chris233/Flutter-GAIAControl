/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.connection;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.repository.ResultStatus;
import com.qualcomm.qti.gaiaclient.repository.connection.ConnectionRepository;
import com.qualcomm.qti.gaiaclient.repository.connection.Device;
import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEventType;
import com.qualcomm.qti.gaiaclient.utils.ConnectionUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ConnectionViewModel extends AndroidViewModel {

    private final StickyLiveData<ConnectionViewData> mViewData = new StickyLiveData<>();

    private final StickyLiveData<NavigationEvent> mEventData = new StickyLiveData<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final ConnectionRepository connectionRepository;

    private final Observer<Result<Device, BluetoothStatus>> connectingObserver = this::updateData;

    private LiveData<Result<Device, BluetoothStatus>> connectingLiveData = null;

    @Inject
    public ConnectionViewModel(@NonNull Application application, ConnectionRepository connectionRepository) {
        super(application);
        this.connectionRepository = connectionRepository;
    }

    void setObservers(@NonNull LifecycleOwner owner,
                      @NonNull Observer<ConnectionViewData> dataObserver,
                      @NonNull Observer<NavigationEvent> eventObserver) {
        mViewData.observe(owner, dataObserver);
        mEventData.observe(owner, eventObserver);
    }

    void connect( Device device) {
        connectingLiveData = connectionRepository.connect(getApplication().getApplicationContext(), device);
        connectingLiveData.observeForever(connectingObserver);
    }

    private void updateData(Result<Device, BluetoothStatus> result) {
        // get the data from the result
        Device device = result == null ? null : result.getData();
        ConnectionState state = device != null ? device.getState() : null;
        BluetoothStatus reason = result == null ? null : result.getReason();
        ResultStatus status = result == null ? null : result.getStatus();

        // build data object
        boolean inProgress = status == ResultStatus.IN_PROGRESS;
        String stateLabel = ConnectionUtils.getLabel(getApplication(), state);
        String reasonLabel = ConnectionUtils.getLabel(getApplication(), reason);
        boolean isConnected = state == ConnectionState.CONNECTED;
        boolean isError = status == ResultStatus.FAIL;
        ConnectionViewData data = new ConnectionViewData(stateLabel, reasonLabel, inProgress, isConnected, isError);
        // notify data observer
        mViewData.postValue(data);

        // notify the device is connected
        if (isConnected) {
            // delay the event for the user to see an update of the information first
            long DELAY_MS = 750;
            mHandler.postDelayed(() -> mEventData.postValue(new NavigationEvent(NavigationEventType.NAVIGATE_TO_INFO)),
                                 DELAY_MS);
        }

        if (!inProgress && connectingLiveData != null) {
            connectingLiveData.removeObserver(connectingObserver);
            connectingLiveData = null;
        }
    }

}
