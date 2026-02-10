/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.core.bluetooth.SendListener;
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator;
import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.data.SizeInfo;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.HoldData;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.PacketSentListener;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.PacketTimeOutManager;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.Parameters;
import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.PendingData;
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG;
import com.qualcomm.qti.gaiaclient.core.utils.Logger;

import java.util.concurrent.atomic.AtomicReference;

import static com.qualcomm.qti.gaiaclient.core.GaiaClientService.getTaskManager;
import static com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getHexadecimalStringFromInt;

/**
 * <p>A plugin handles received GAIA packets and manages the sending of GAIA packets.</p>
 * <p>Most packets that are sent to a device are expected to be acknowledged. The plugin keeps
 * track of packets that have been sent until they are either acknowledged or timed out.</p>
 * <p>Call {@link #start()} to start this plugin and allow it to send and receive packets. Call
 * {@link #stop()} to stop the plugin.</p>
 * <p>Call {@link #onReceiveGaiaPacket(GaiaPacket)} when a received GaiaPacket that must be managed
 * by this plugin.</p>
 */
public abstract class Plugin {

    /**
     * A tag to log information from this class.
     */
    private static final String TAG = "Plugin";
    /**
     * A constant to manage the logs from this class.
     */
    private static final boolean LOG_METHODS = DEBUG.Gaia.PLUGIN;
    /**
     * The object ot use to send packets to a device.
     */
    protected final GaiaSender mSender;
    /**
     * The vendor that corresponds to the plugin. This is used to set a vendor into packets that
     * are sent.
     */
    private final int mVendor;
    /**
     * Manages the packets that needs to be sent or have been given to the Sender to be sent.
     * Allows to hold data when it is required.
     */
    private final PendingData mPendingData = new PendingData();
    /**
     * The current status of the vendor: stopped or started.
     */
    private final AtomicReference<RunningStatus> mStatus = new AtomicReference<>(RunningStatus.STOPPED);
    /**
     * A manager to check if each sent packet receives a response within a certain timeout.
     */
    private final PacketTimeOutManager mTimeOutManager = new PacketTimeOutManager(this::onTimeOut);

    /**
     * To set this Plugin with a protocol version, a vendor and a GaiaSender.
     *
     * @param vendor
     *         The ID of the vendor this plugin is for. The vendor is used to add
     *         identification to the packets that are sent.
     * @param sender
     *         The sender this vendor should use to send packets.
     */
    protected Plugin(int vendor, @NonNull GaiaSender sender) {
        this.mVendor = vendor;
        this.mSender = sender;
    }

    /**
     * To get the vendor ID of this plugin.
     */
    public int getVendor() {
        return mVendor;
    }

    /**
     * To get the current running status of this vendor.
     */
    public RunningStatus getStatus() {
        return mStatus.get();
    }

    /**
     * This method calls {@link #onStarted()} if this Plugin was not started yet.
     * This method has no effect if the plugin is already in a running state.
     */
    public final void start() {
        RunningStatus previous = mStatus.getAndSet(RunningStatus.STARTED);
        if (previous != RunningStatus.STARTED) {
            onStarted();
        }
    }

    /**
     * Called when this plugin cannot send or receive data anymore. Most likely reason could be that
     * the device is disconnected.
     * This method calls {@link #onStopped()} that can be override when extending {@link Plugin}.
     * This method has no effect if the plugin was already stopped.
     */
    public final void stop() {
        RunningStatus previous = mStatus.getAndSet(RunningStatus.STOPPED);
        if (previous != RunningStatus.STOPPED) {
            cancelAll();
            mTimeOutManager.reset();
            onStopped();
        }
    }

    /**
     * <p>Called when a received GAIA packet must be managed by this plugin.</p>
     * <p>The plugin checks if the received packet has a matching packet that was waiting for an
     * acknowledgement. by calling {@link PacketTimeOutManager#cancelTimeOutRunnable(int)}. This automatically
     * cancels a corresponding time out in the case of the original packet not being
     * acknowledged.</p>
     * <p>This method calls {@link #onPacketReceived(GaiaPacket, GaiaPacket)} to indicate to its
     * children the packet that has been received and the matching - if any - sent packet.</p>
     *
     * @param received
     *         The GAIA packet that has been received and must be managed by this plugin.
     */
    public final void onReceiveGaiaPacket(@NonNull GaiaPacket received) {
        Logger.log(LOG_METHODS, TAG, "onReceiveGaiaPacket", new Pair<>("packet", received));

        if (mStatus.get() == RunningStatus.STOPPED) {
            Log.w(TAG, "[onReceiveGaiaPacket] ignored: plugin is not running.");
            return;
        }

        GaiaPacket sent = mTimeOutManager.cancelTimeOutRunnable(received.getKey());

        if (getVendor() != received.getVendorId()) {
            // unexpected as the vendor should have been checked when this is reached
            Log.w(TAG,
                  String.format("[onReceiveGaiaPacket] Unexpected vendor(%1$s) for plugin with " +
                                        "vendor=%2$s",
                                getHexadecimalStringFromInt(received.getVendorId()),
                                getHexadecimalStringFromInt(mVendor)));
            return;
        }

        onPacketReceived(received, sent);
    }

    /**
     * To clear the data queue of the sender. This clears the whole queue of packets to be sent.
     */
    protected void cancelAll() {
        mSender.cancelData(
                mPendingData.getPendingIds()); // sender queue is cancelled directly => mSendingQueue can be cleared
        mPendingData.clear();
    }

    /**
     * <p>For this plugin to hold all the packets it is sending and any further requested.</p>
     * <p>To resume, call {@link #resumeAll()}.</p>
     */
    protected void holdAll() {
        if (mStatus.compareAndSet(RunningStatus.STARTED, RunningStatus.ON_HOLD)) {
            // only applies if this was started
            mSender.holdData(mPendingData.getPendingIds());
        }
    }

    /**
     * To resume the sending of packets for this plugin. Calling this releases any packets that was on hold to a "ready
     * to be sent" state.
     */
    protected void resumeAll() {
        if (mStatus.get() == RunningStatus.ON_HOLD) {
            mSender.resumeData(mPendingData.getPendingIds()); // resume sending of packets before to add more data
            mStatus.set(RunningStatus.STARTED);

            getTaskManager().runInBackground(() -> {
                while (mPendingData.hasHoldData() && mStatus.get() == RunningStatus.STARTED) {
                    HoldData data = mPendingData.pollHoldData();
                    if (data != null) {
                        processSending(data.getPacket(), data.getParameters());
                    }
                }
            });

        }
    }

    /**
     * <p>To get the optimum or maximum payload size that a device can handle for sending or receiving messages.</p>
     * <p>We recommend using the optimum sizes for better performance:
     * * <ul>
     * *     <li>{@link SizeInfo#OPTIMUM_RX_PAYLOAD}: to know the optimum size of a payload the connected device can receive.</li>
     * *     <li>{@link SizeInfo#OPTIMUM_TX_PAYLOAD}: to know the optimum size of a payload the connected device would send.</li>
     * * </ul></p>
     *
     * @param info
     *         The size to get the value of.
     */
    public int getMaxPayloadSize(SizeInfo info) {
        return mSender.getMaxPayloadSize(info);
    }

    /**
     * To get the available payload size based on the given transport size.
     * This is independent of any sizes set with the device.
     */
    public int getPayloadSize(int transportSize) {
        return mSender.getPayloadSize(transportSize);
    }

    /**
     * <p>To call when a packet has not received a response within an expected time.</p>
     *
     * @param packet
     *         The packet that wasn't acknowledged.
     */
    private void onTimeOut(GaiaPacket packet) {
        if (mStatus.get() != RunningStatus.STARTED) {
            Log.w(TAG, "[TimeOutListener->onTimeOut] ignored: plugin is not running.");
            return;
        }

        onFailed(packet, Reason.NO_RESPONSE);
    }

    /**
     * Called when this plugin starts.
     */
    protected abstract void onStarted();

    /**
     * Called when this plugin stops.
     */
    protected abstract void onStopped();

    /**
     * Called when this plugin has received a packet to manage and can manage it.
     *
     * @param received
     *         the received packet.
     * @param sent
     *         A packet that was waiting of an acknowledgement and matches the received packet.
     */
    protected abstract void onPacketReceived(GaiaPacket received, @Nullable GaiaPacket sent);

    /**
     * <p>Called to indicate that an error occurred for sending a packet.</p>
     *
     * @param packet
     *         The packet the error occurred for.
     * @param reason
     *         The error that happens.
     */
    protected abstract void onFailed(GaiaPacket packet, Reason reason);

    /**
     * The default time out to use for timing out acknowledgements.
     *
     * @return a value in milliseconds.
     */
    protected abstract long getDefaultTimeout();

    /**
     * The default method to send a packet.
     */
    protected void send(@NonNull GaiaPacket packet) {
        Parameters parameters = new Parameters(
                true,
                true,
                false,
                false,
                null,
                getDefaultTimeout()
        );
        send(packet, parameters);
    }

    /**
     * A default method to send packets with a listener to be notified when the packet is sent.
     */
    protected void send(@NonNull GaiaPacket packet, @NonNull PacketSentListener listener) {
        Parameters parameters = new Parameters(
                true,
                true,
                false,
                false,
                listener,
                getDefaultTimeout()
        );
        send(packet, parameters);
    }

    /**
     * <p>A method to define detailed parameters for sending a packet.</p>
     *
     * @param packet
     *         The packet to send.
     * @param parameters
     *         To specify any parameters for sending the packet such as the timeout within which to expect a response
     *         from the device.
     */
    protected void send(@NonNull GaiaPacket packet, @NonNull Parameters parameters) {
        RunningStatus status = mStatus.get();

        if (status == RunningStatus.STOPPED) {
            Log.w(TAG, "[send] ignored: plugin is not running.");
            return;
        }

        if ((parameters.isHoldable() && status == RunningStatus.ON_HOLD)
                || (!parameters.isHoldable() && status == RunningStatus.STARTED && mPendingData.hasHoldData())) {
            mPendingData.holdData(new HoldData(packet, parameters));
        }
        else {
            processSending(packet, parameters);
        }
    }

    /**
     * <p>This method processes the sending of a packet with the given parameters.</p>
     * <p>If the packet is acknowledged it starts a runnable to time out if no corresponding
     * acknowledgement were received. Then it builds a listener for sending the packet using the
     * {@link GaiaSender} and sends the packet.</p>
     * <p>If the sending failed, this method would called {@link #onFailed(GaiaPacket, Reason)}
     * giving the packet that was supposed to be sent.</p>
     */
    private void processSending(@NonNull GaiaPacket packet, @NonNull Parameters parameters) {
        Logger.log(LOG_METHODS, TAG, "processSending", new Pair<>("isAcknowledged", parameters.isAcknowledged()),
                   new Pair<>("timeout", parameters.getTimeout()), new Pair<>("packet", packet));

        SendListenerImplementation listener = new SendListenerImplementation(packet, parameters);
        long id = mSender.sendData(packet.getBytes(), parameters.isFlushed(), parameters.getUseRwcp(), listener);

        onProcessed(id, packet, parameters.isAcknowledged(), listener);
    }

    private void onProcessed(long id, @NonNull GaiaPacket packet, boolean acknowledged,
                             @NonNull SendListenerImplementation listener) {
        if (id == IdCreator.NULL_ID) {
            onSendingFailed(packet, acknowledged);
        }
        else {
            listener.setId(id);
            mPendingData.addSendingId(id);
        }
    }

    /**
     * Called when sending a packet fails. This method cancels any corresponding time out and
     * calls {@link #onFailed(GaiaPacket, Reason)}.
     */
    private void onSendingFailed(@NonNull GaiaPacket packet, boolean acknowledged) {
        if (acknowledged) {
            mTimeOutManager.cancelTimeOutRunnable(packet.getKey());
        }
        if (mSender.isConnected()) {
            // if sender is disconnected, no point on dispatching a failure
            onFailed(packet, Reason.SENDING_FAILED);
        }
    }

    /**
     * To build a {@link SendListener} to provide to the {@link GaiaSender} that updates the {@link PacketSentListener}
     * once the packet has been sent.
     */
    private class SendListenerImplementation implements SendListener {

        private long id = IdCreator.NULL_ID;

        private final GaiaPacket gaiaPacket;

        private final Parameters parameters;

        public SendListenerImplementation(GaiaPacket gaiaPacket, Parameters parameters) {
            this.gaiaPacket = gaiaPacket;
            this.parameters = parameters;
        }

        public void setId(long id) {
            this.id = id;
        }

        @Override // SendListener
        public void onSending() {
            mPendingData.removeSendingId(id);
            if (parameters.isAcknowledged()) {
                mTimeOutManager.startTimeOutRunnable(gaiaPacket, parameters.getTimeout());
            }
        }

        @Override // SendListener
        public void onSent() {
            PacketSentListener listener = parameters.getListener();
            if (listener != null) {
                listener.onSent();
            }
        }

        @Override // SendListener
        public void onFailed() {
            mPendingData.removeSendingId(id);
            onSendingFailed(gaiaPacket, parameters.isAcknowledged());
        }
    }

}
