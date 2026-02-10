/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.reconnection;

import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.TransportManager;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState;
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.Device;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverInformation;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.HandoverType;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.VoiceAssistant;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.publications.core.ExecutionType;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.BluetoothSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.ConnectionSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.HandoverSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.UpgradeSubscriber;
import com.qualcomm.qti.gaiaclient.core.publications.qtil.subscribers.VoiceUISubscriber;
import com.qualcomm.qti.gaiaclient.core.tasks.RunnableState;
import com.qualcomm.qti.gaiaclient.core.tasks.TaskManager;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.ChunkSizeType;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeInfoType;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeState;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;

import java.util.List;

/**
 * <p>This class observes the different states and events a reconnection delegate needs.<p>
 * <p>{@link #onDisconnected()} is always going to be called, this delegate running or not.</p>
 * <p>For a reconnection to succeed, this must be enabled and in reconnection mode. Use {@link #enable()} and
 * * {@link #disable()} to respectively enter and leave the enable mode. Use {@link #start()} and
 * {@link #stop()} to respectively enter and leave the reconnecting (running) mode.</p>
 * <p>On successful reconnection, {@link #stop()} is automatically called by this observer.</p>
 * <p>Prior to destroy this object call {@link #release()} to ensure the
 * object releases its resources. This class listens for Bluetooth, connection and handover states
 * until it would be released.</p>
 */
public abstract class ReconnectionObserver {

    private static final String TAG = "ReconnectionObserver";

    private static final boolean LOG_METHODS = DEBUG.Bluetooth.RECONNECTION_OBSERVER;

    @VisibleForTesting
    public static final long ASSISTANT_TIME_OUT_MS = 5000;

    private final TaskManager mTaskManager;

    private final TransportManager mTransportManager;

    private final ReconnectionState mState = new ReconnectionState();

    private final RunnableState mHandoverRunnableState = new RunnableState();
    private final Runnable mHandoverRunnable = () -> {
        Logger.log(LOG_METHODS, TAG, "HandoverRunnable->run", new Pair<>("state", mState));
        mState.setIsHandover(false);
        mHandoverRunnableState.setIsRunning(false);
        onHandoverEnd();
    };

    private final RunnableState mAssistantRunnableState = new RunnableState();
    private final Runnable mAssistantRunnable = () -> {
        Logger.log(LOG_METHODS, TAG, "AssistantRunnable->run", new Pair<>("state", mState));
        mState.setHasAssistantChanged(false);
        mAssistantRunnableState.setIsRunning(false);
        onAssistantEnd();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final VoiceUISubscriber mVoiceUISubscriber = new VoiceUISubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onSelectedAssistant(VoiceAssistant assistant) {
            Logger.log(LOG_METHODS, TAG, "VoiceUISubscriber->onSelectedAssistant",
                       new Pair<>("state", mState));
            mState.setHasAssistantChanged(true);

            stopRunnable(mAssistantRunnableState, false);

            startRunnable(mAssistantRunnable, mAssistantRunnableState, ASSISTANT_TIME_OUT_MS);
            onAssistantStart();
        }

        @Override
        public void onSupportedAssistants(List<VoiceAssistant> assistants) {

        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final UpgradeSubscriber mUpgradeSubscriber = new UpgradeSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onProgress(UpgradeProgress progress) {
            Logger.log(LOG_METHODS, TAG, "UpgradeSubscriber->onProgress", new Pair<>("state", mState),
                       new Pair<>("\nprogress", progress));
            if (!mState.isUpgrading() && progress.getType() == UpgradeInfoType.STATE
                    && progress.getState() == UpgradeState.INITIALISATION) {
                // if it is not the upgrade initialisation
                //                  => is already upgrading or is receiving a progress updated after "end" state
                mState.setIsUpgrading(true);
                ReconnectionObserver.this.onUpgradeStart();
            }
            else if (mState.isUpgrading() && progress.getState().isEnd()) {
                // no need to reconnect anymore
                mState.setIsUpgrading(false);
                ReconnectionObserver.this.onUpgradeEnd();
            }
        }

        @Override
        public void onError(UpgradeFail error) {
            Logger.log(LOG_METHODS, TAG, "UpgradeSubscriber->onError", new Pair<>("state", mState));
            if (mState.isUpgrading()) {
                // no need to reconnect
                mState.setIsUpgrading(false);
                ReconnectionObserver.this.onUpgradeEnd();
            }
        }

        @Override
        public void onChunkSize(ChunkSizeType type, int size) {
            // nothing to do
        }

        @Override
        public void onAlert(UpgradeAlert alert, boolean raised) {
            // nothing to do
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final ConnectionSubscriber mConnectionSubscriber = new ConnectionSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onConnectionStateChanged(Device device, ConnectionState state) {
            Logger.log(LOG_METHODS, TAG, "ConnectionSubscriber->onConnectionStateChanged", new Pair<>("state", mState),
                       new Pair<>("connectionState", state));
            mState.setConnectionState(state);
            ReconnectionObserver.this.onConnectionStateChanged(state);
        }

        @Override
        public void onConnectionError(Device device, BluetoothStatus reason) {
            Logger.log(LOG_METHODS, TAG, "ConnectionSubscriber->onConnectionError", new Pair<>("state", mState),
                       new Pair<>("reason", reason));
            ReconnectionObserver.this.onBluetoothStatus(reason);
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final BluetoothSubscriber mBluetoothSubscriber = new BluetoothSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onEnabled() {
            Logger.log(LOG_METHODS, TAG, "BluetoothSubscriber->onEnabled", new Pair<>("state", mState));
            mState.setIsBluetoothEnabled(true);
            ReconnectionObserver.this.onBluetoothEnabled();
        }

        @Override
        public void onDisabled() {
            Logger.log(LOG_METHODS, TAG, "BluetoothSubscriber->onDisabled", new Pair<>("state", mState));
            mState.setIsBluetoothEnabled(false);
            ReconnectionObserver.this.onBluetoothDisabled();
        }
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final HandoverSubscriber mHandoverSubscriber = new HandoverSubscriber() {
        @NonNull
        @Override
        public ExecutionType getExecutionType() {
            return ExecutionType.BACKGROUND;
        }

        @Override
        public void onHandoverStart(HandoverInformation info) {
            Logger.log(LOG_METHODS, TAG, "HandoverSubscriber->onStart", new Pair<>("state", mState),
                       new Pair<>("info", info));
            if (info.getType() == HandoverType.STATIC) {
                // cancel any ongoing waiting runnable
                stopRunnable(mHandoverRunnableState, false);

                // start new runnable from latest information
                long delayMs = info.getDelayInSeconds() * 1000;
                mState.setIsHandover(true);
                startRunnable(mHandoverRunnable, mHandoverRunnableState, delayMs);
                ReconnectionObserver.this.onHandoverStart();
            }
        }
    };

    protected ReconnectionObserver(TaskManager taskManager,
                                   @NonNull PublicationManager publicationManager,
                                   @NonNull TransportManager transportManager,
                                   BluetoothAdapter adapter) {
        this.mTaskManager = taskManager;
        this.mTransportManager = transportManager;

        publicationManager.subscribe(mHandoverSubscriber);
        publicationManager.subscribe(mBluetoothSubscriber);
        publicationManager.subscribe(mConnectionSubscriber);
        publicationManager.subscribe(mUpgradeSubscriber);
        publicationManager.subscribe(mVoiceUISubscriber);

        // initialising the bluetooth state
        mState.setIsBluetoothEnabled(adapter != null && adapter.isEnabled());
    }

    public final void start() {
        Logger.log(LOG_METHODS, TAG, "start", new Pair<>("state", mState));
        if (!mState.isObserverEnabled()) {
            // cannot be started as this object is not enabled
            return;
        }
        boolean wasRunning = mState.setIsRunning(true);
        onStarted(wasRunning);
    }

    public void stop() {
        Logger.log(LOG_METHODS, TAG, "stop", new Pair<>("state", mState));
        boolean wasRunning = mState.setIsRunning(false);
        onStopped(wasRunning);
    }

    /**
     * Enables the observer when attempts to reconnect should be made depending on events that occurs during a
     * connection.
     */
    public final void enable() {
        Logger.log(LOG_METHODS, TAG, "enable");
        this.mState.setIsObserverEnabled(true);
        onEnabled();
    }

    /**
     * Disables the observer when no attempt to reconnect should be made regardless of the different states.
     */
    public final void disable() {
        Logger.log(LOG_METHODS, TAG, "disable");
        this.mState.setIsObserverEnabled(false);
        onDisabled();
    }

    public void release() {
        Logger.log(LOG_METHODS, TAG, "release", new Pair<>("state", mState));
        stop();
        stopRunnable(mHandoverRunnableState, true);
        stopRunnable(mAssistantRunnableState, true);
    }

    protected ConnectionState getConnectionState() {
        return mState.getConnectionState();
    }

    protected boolean isHandover() {
        return mState.isHandover();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isBluetoothEnabled() {
        return mState.isBluetoothEnabled();
    }

    protected boolean isRunning() {
        return mState.isRunning();
    }

    protected boolean isUpgrading() {
        return mState.isUpgrading();
    }

    protected boolean isObserverEnabled() {
        return mState.isObserverEnabled();
    }

    public boolean hasAssistantChanged() {
        return mState.hasAssistantChanged();
    }

    protected TaskManager getTaskManager() {
        return mTaskManager;
    }

    protected TransportManager getTransportManager() {
        return mTransportManager;
    }

    protected void reconnect() {
        Logger.log(LOG_METHODS, TAG, "reconnect", new Pair<>("state", mState));
        BluetoothStatus status = mTransportManager.reconnect();
        onBluetoothStatus(status);
    }

    protected void disconnect(boolean hard) {
        Logger.log(LOG_METHODS, TAG, "disconnect", new Pair<>("state", mState));
        mTransportManager.disconnect(hard);
    }

    private void onBluetoothStatus(@NonNull BluetoothStatus status) {
        Logger.log(LOG_METHODS, TAG, "onBluetoothStatus", new Pair<>("state", mState), new Pair<>("status", status));
        switch (status) {
            case ALREADY_CONNECTED:
                onConnected();
                break;

            case IN_PROGRESS:
                // nothing to do
                break;

            case CONNECTION_FAILED:
            case CONNECTION_LOST:
                // could not establish the connection
                // disconnected status to be triggered
                break;

            case NO_BLUETOOTH:
                // flow is managed by #onBluetoothEnabled & #onBluetoothDisabled
                break;

            case RECONNECTION_TIME_OUT:
                // ignored: sent by this observer or a child class.
                break;

            case DEVICE_NOT_FOUND:
            case DEVICE_NOT_COMPATIBLE:
            case NO_TRANSPORT_FOUND:
                // unexpected: device was previously connected
            case TIME_OUT:
            case DISCOVERY_FAILED:
                // unexpected this is not a discovery
            default:
                Log.w(TAG, "[onBluetoothStatus] unexpected status: " + status);
        }
    }

    private void onConnectionStateChanged(ConnectionState state) {
        Logger.log(LOG_METHODS, TAG, "onConnectionStateChanged", new Pair<>("state", mState),
                   new Pair<>("connectionState", state));

        switch (state) {
            case CONNECTING:
                // reconnection in progress
                break;

            case CONNECTED:
                onConnected();
                break;

            case DISCONNECTING:
                // should be called with DISCONNECTED soon after
                break;

            case DISCONNECTED:
                onDisconnected();
                break;
        }
    }

    protected boolean startRunnable(Runnable runnable, RunnableState state, long delay) {
        boolean started = state.compareSetIsRunning(false, true);
        if (started) {
            long id = mTaskManager.schedule(runnable, delay);
            state.setId(id);
        }
        return started;
    }

    protected boolean stopRunnable(RunnableState state, boolean force) {
        if (state.compareSetIsRunning(true, false) || force) {
            mTaskManager.cancelScheduled(state.getId());
            return true;
        }
        return false;
    }

    protected abstract void onStarted(boolean wasRunning);

    protected abstract void onStopped(boolean wasRunning);

    protected abstract void onEnabled();

    protected abstract void onDisabled();

    protected abstract void onConnected();

    protected abstract void onDisconnected();

    protected abstract void onBluetoothEnabled();

    protected abstract void onBluetoothDisabled();

    protected abstract void onHandoverStart();

    protected abstract void onHandoverEnd();

    protected abstract void onUpgradeStart();

    protected abstract void onUpgradeEnd();

    protected abstract void onAssistantStart();

    protected abstract void onAssistantEnd();
}
