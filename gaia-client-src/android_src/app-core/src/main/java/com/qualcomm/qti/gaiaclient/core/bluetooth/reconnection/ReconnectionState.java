/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.reconnection;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

class ReconnectionState {

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private final AtomicBoolean isHandover = new AtomicBoolean(false);

    private final AtomicBoolean isUpgrading = new AtomicBoolean(false);

    private final AtomicBoolean isBluetoothEnabled = new AtomicBoolean(false);

    private final AtomicBoolean isObserverEnabled = new AtomicBoolean(false);

    private final AtomicBoolean hasAssistantChanged = new AtomicBoolean(false);

    private final AtomicReference<ConnectionState> connectionState =
            new AtomicReference<>(ConnectionState.DISCONNECTED);

    public void setIsHandover(boolean isHandover) {
        this.isHandover.set(isHandover);
    }

    public void setHasAssistantChanged(boolean hasAssistantChanged) {
        this.hasAssistantChanged.set(hasAssistantChanged);
    }

    public boolean isUpgrading() {
        return isUpgrading.get();
    }

    public void setIsUpgrading(boolean isUpgrading) {
        this.isUpgrading.set(isUpgrading);
    }

    public void setConnectionState(ConnectionState state) {
        this.connectionState.set(state);
    }

    public ConnectionState getConnectionState() {
        return connectionState.get();
    }

    public void setIsBluetoothEnabled(boolean isBluetoothEnabled) {
        this.isBluetoothEnabled.set(isBluetoothEnabled);
    }

    public boolean setIsRunning(boolean isRunning) {
        return this.isRunning.getAndSet(isRunning);
    }

    public boolean isHandover() {
        return isHandover.get();
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public boolean isBluetoothEnabled() {
        return isBluetoothEnabled.get();
    }

    public boolean isObserverEnabled() {
        return isObserverEnabled.get();
    }

    public void setIsObserverEnabled(boolean isEnabled) {
        this.isObserverEnabled.getAndSet(isEnabled);
    }

    public boolean hasAssistantChanged() {
        return hasAssistantChanged.get();
    }

    @NonNull
    @Override
    public String toString() {
        return "ReconnectionState{" +
                "isObserverEnabled=" + isObserverEnabled.get() +
                ", running=" + isRunning.get() +
                ", handover=" + isHandover.get() +
                ", upgrading=" + isUpgrading.get() +
                ", bluetooth=" + isBluetoothEnabled.get() +
                ", assistantChanged=" + hasAssistantChanged.get() +
                ", state=" + connectionState.get() +
                '}';
    }
}
