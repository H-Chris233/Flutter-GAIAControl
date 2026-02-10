/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade;

import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationType;

import androidx.annotation.NonNull;

/**
 * <p>A wrapper class to avoid casts that could break the upgrade process.</p>
 */
class UpgradeManagerWrapper implements UpgradeManager {

    private final UpgradeManagerImpl mManager;

    UpgradeManagerWrapper(@NonNull UpgradeListener listener,
                          @NonNull UpgradeTaskScheduler scheduler) {
        this.mManager = new UpgradeManagerImpl(listener, scheduler);
    }

    @Override
    public void showDebugLogs(boolean show) {
        mManager.showDebugLogs(show);
    }

    @Override
    public int setChunkSize(int size) {
        return mManager.setChunkSize(size);
    }

    @Override
    public void startUpgrade(byte[] data, byte[] dataID) {
        mManager.startUpgrade(data, dataID);
    }

    @Override
    public boolean start() {
        return mManager.start();
    }

    @Override
    public void pause() {
        mManager.pause();
    }

    @Override
    public boolean isUpgrading() {
        return mManager.isUpgrading();
    }

    @Override
    public void onUpgradeMessage(byte[] bytes) {
        mManager.onUpgradeMessage(bytes);
    }

    @Override
    public void abort() {
        mManager.abort();
    }

    @Override
    public void onConfirmation(ConfirmationType type, @NonNull ConfirmationOptions option) {
        mManager.onConfirmation(type, option);
    }

    @Override
    public void onUpgradeModeEnabled() {
        mManager.onUpgradeModeEnabled();
    }

    @Override
    public void onUpgradeModeDisabled() {
        mManager.onUpgradeModeDisabled();
    }

    @Override
    public void release() {
        mManager.release();
    }

    @Override
    public void onMessageTransferred() {
        mManager.onMessageTransferred();
    }

    @Override
    public void forceStop() {
        mManager.forceStop();
    }
}

