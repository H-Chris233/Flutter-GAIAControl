/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.reconnection;

import android.bluetooth.BluetoothAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.TransportManager;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.publishers.ConnectionPublisher;
import com.qualcomm.qti.gaiaclient.core.tasks.RunnableState;
import com.qualcomm.qti.gaiaclient.core.tasks.TaskManager;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

public class ReconnectionDelegate extends ReconnectionObserver {

    private static final String TAG = "ReconnectionDelegate";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.RECONNECTION_DELEGATE;

    @VisibleForTesting
    public static final long RECONNECTION_DURING_UPGRADE_INITIAL_DELAY_MS = 5000;

    @VisibleForTesting
    public static final long RECONNECTION_DURING_UPGRADE_DELAY_BETWEEN_ATTEMPTS_MS = 100;

    @VisibleForTesting
    public static final long UPGRADE_TIME_OUT_MS = 40000;

    @VisibleForTesting
    public static final long DEFAULT_TIME_OUT_MS = 4000;

    @VisibleForTesting
    public static final long RECONNECTION_DEFAULT_DELAY_BETWEEN_ATTEMPTS_MS = 100;

    private final Runnable mUpgradeTimeOutRunnable = this::onUpgradeTimeOut;
    private final RunnableState mUpgradeTimeOutRunnableState = new RunnableState();

    private final Runnable mReconnectionRunnable = this::onReconnect;
    private final RunnableState mReconnectionRunnableState = new RunnableState();

    private final Runnable mDefaultTimeOutRunnable = this::onDefaultTimeOut;
    private final RunnableState mDefaultTimeOutRunnableState = new RunnableState();

    private final ConnectionPublisher mConnectionPublisher = new ConnectionPublisher();

    public ReconnectionDelegate(@NonNull TaskManager taskManager,
                                @NonNull PublicationManager publicationManager,
                                @NonNull TransportManager transportManager,
                                BluetoothAdapter adapter) {
        super(taskManager, publicationManager, transportManager, adapter);
        publicationManager.register(mConnectionPublisher);
    }

    @Override
    protected void onStarted(boolean wasRunning) {
        Logger.log(LOG_METHODS, TAG, "onStarted", new Pair<>("wasRunning", wasRunning));
        attemptToReconnect();
    }

    @Override
    protected void onStopped(boolean wasRunning) {
        Logger.log(LOG_METHODS, TAG, "onStopped", new Pair<>("wasRunning", wasRunning));
        cancelAllRunnables();
    }

    @Override
    protected void onEnabled() {
        Logger.log(LOG_METHODS, TAG, "enable");
        if (isUpgrading()) {
            start();
        }
    }

    @Override
    protected void onDisabled() {
        Logger.log(LOG_METHODS, TAG, "disable");
        cancelAllRunnables();
        stop();
    }

    @Override
    protected void onConnected() {
        Logger.log(LOG_METHODS, TAG, "onConnected");
        if (isRunning() && !isUpgrading() && !hasAssistantChanged()) {
            stop();
        }
        else if (isUpgrading()) {
            stopRunnable(mUpgradeTimeOutRunnableState, false);
        }
    }

    @Override
    protected void onDisconnected() {
        Logger.log(LOG_METHODS, TAG, "onDisconnected");
        attemptToReconnect();
    }

    @Override
    protected void onBluetoothEnabled() {
        Logger.log(LOG_METHODS, TAG, "onBluetoothEnabled");
        attemptToReconnect();
    }

    @Override
    protected void onBluetoothDisabled() {
        Logger.log(LOG_METHODS, TAG, "onBluetoothDisabled");
        cancelAllRunnables();

        // forces a disconnection if the link has not been dropped.
        // this is a **WORKAROUND** for Android devices that have the following issue: the RFCOMM
        // link is maintained when the user turns the BT off.
        disconnect(false);

        // wait for BT to come back
    }

    @Override
    protected void onHandoverStart() {
        Logger.log(LOG_METHODS, TAG, "onHandoverStart");
        stop();
        // nothing to do
    }

    @Override
    protected void onHandoverEnd() {
        Logger.log(LOG_METHODS, TAG, "onHandoverEnd");
        start();
    }

    @Override
    protected void onUpgradeStart() {
        Logger.log(LOG_METHODS, TAG, "onUpgradeStart");
        start();
    }

    @Override
    protected void onUpgradeEnd() {
        Logger.log(LOG_METHODS, TAG, "onUpgradeEnd");
        if (!mDefaultTimeOutRunnableState.isRunning()) {
            stop();
        }
    }

    @Override
    protected void onAssistantStart() {
        Logger.log(LOG_METHODS, TAG, "onAssistantStart");
        // nothing to do
    }

    @Override
    protected void onAssistantEnd() {
        Logger.log(LOG_METHODS, TAG, "onAssistantEnd");
        start();
    }

    private void attemptToReconnect() {
        Logger.log(LOG_METHODS, TAG, "attemptToReconnect");
        if (getConnectionState() == ConnectionState.CONNECTED) {
            onConnected();
            return;
        }

        if (!isObserverEnabled() || !isRunning() || !isBluetoothEnabled() || isHandover()) {
            // is not running or must wait for bluetooth to be back or handover to end
            return;
        }

        long reconnectionDelay = RECONNECTION_DEFAULT_DELAY_BETWEEN_ATTEMPTS_MS;

        if (isUpgrading()) {
            // always stop the default time out runnable before to start the upgrade time out runnable
            stopRunnable(mDefaultTimeOutRunnableState, false);
            reconnectionDelay = mUpgradeTimeOutRunnableState.isRunning() ?
                                RECONNECTION_DURING_UPGRADE_DELAY_BETWEEN_ATTEMPTS_MS :
                                RECONNECTION_DURING_UPGRADE_INITIAL_DELAY_MS;
            startRunnable(mUpgradeTimeOutRunnable, mUpgradeTimeOutRunnableState, UPGRADE_TIME_OUT_MS);
        }
        else /* if (isRunning()) */ {
            // soft attempts after handover or change of voice assistant
            startRunnable(mDefaultTimeOutRunnable, mDefaultTimeOutRunnableState, DEFAULT_TIME_OUT_MS);
        }

        // schedule reconnection
        startRunnable(mReconnectionRunnable, mReconnectionRunnableState, reconnectionDelay);
    }

    private void onUpgradeTimeOut() {
        Logger.log(LOG_METHODS, TAG, "onUpgradeTimeOut");
        mUpgradeTimeOutRunnableState.setIsRunning(false);
        onFailed();
    }

    private void onDefaultTimeOut() {
        Logger.log(LOG_METHODS, TAG, "onDefaultTimeOut");
        mDefaultTimeOutRunnableState.setIsRunning(false);
        onFailed();
    }

    private void onReconnect() {
        Logger.log(LOG_METHODS, TAG, "onReconnect");
        mReconnectionRunnableState.setIsRunning(false);

        if (getConnectionState() == ConnectionState.CONNECTED) {
            onConnected();
            return;
        }

        if (!isObserverEnabled() || !isRunning() || !isBluetoothEnabled() || isHandover()) {
            // is not running or must wait for bluetooth to be back or handover to end
            return;
        }

        reconnect();
    }

    private void onFailed() {
        Logger.log(LOG_METHODS, TAG, "onFailed");
        stop();
        mConnectionPublisher.publishConnectionError(getTransportManager().getDevice(),
                                                    BluetoothStatus.RECONNECTION_TIME_OUT);
    }

    private void cancelAllRunnables() {
        Logger.log(LOG_METHODS, TAG, "cancelAllRunnables");
        stopRunnable(mUpgradeTimeOutRunnableState, true);
        stopRunnable(mDefaultTimeOutRunnableState, true);
        stopRunnable(mReconnectionRunnableState, true);
    }

}
