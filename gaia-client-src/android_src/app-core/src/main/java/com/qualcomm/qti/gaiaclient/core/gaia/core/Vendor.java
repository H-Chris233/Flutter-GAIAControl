/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core;

import java.util.concurrent.atomic.AtomicReference;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.gaia.core.sending.GaiaSender;

/**
 * <p>Represents a vendor that can handle raw GAIA PDUs from a connected device.</p>
 * <p>When a vendor can communicate with a device, {@link #start(int)} must be called and the
 * version used by the device must be provided.</p>
 * <p>When a vendor cannot communicate with a device anymore, {@link #stop()} must be called.</p>
 * <p>Vendors are meant to manage one or multiple plugins.</p>
 */
public abstract class Vendor {

    /**
     * The id that corresponds to this vendor when sending and receiving GAIA packets.
     */
    private final int mVendorId;
    /**
     * The sender the vendor can use to send data.
     */
    private final GaiaSender mSender;
    /**
     * The current status of the vendor: stopped or started.
     */
    private final AtomicReference<RunningStatus> mStatus = new AtomicReference<>(RunningStatus.STOPPED);

    /**
     * @param id
     *         the id to identify the vendor, must be unique.
     * @param sender
     *         the sender this vendor should use to send packets.
     */
    public Vendor(int id, @NonNull GaiaSender sender) {
        mVendorId = id;
        mSender = sender;
    }

    /**
     * <p>To get the sender this vendor can use in order to send packets to a device.</p>
     */
    @NonNull
    protected final GaiaSender getSender() {
        return mSender;
    }

    /**
     * <p>To get the unique ID that corresponds to this vendor.</p>
     */
    public int getVendorId() {
        return mVendorId;
    }

    /**
     * To get the current running status of this vendor.
     */
    public RunningStatus getStatus() {
        return mStatus.get();
    }

    /**
     * This method calls {@link #onStarted(int)} if this Vendor was not started yet.
     * This method has no effect if the vendor was already started.
     *
     * @param gaiaVersion
     *         the GAIA version supported by the device.
     */
    public final void start(int gaiaVersion) {
        if (mStatus.compareAndSet(RunningStatus.STOPPED, RunningStatus.STARTED)) {
            onStarted(gaiaVersion);
        }
    }

    /**
     * Called by the application when this vendor cannot send or receive data anymore. Most
     * likely reason could be that the device is disconnected.
     * This method calls {@link #onStopped()} that can be override when extending
     * {@link Vendor}.
     * This method has no effect if the vendor was already stopped.
     */
    public final void stop() {
        if (mStatus.compareAndSet(RunningStatus.STARTED, RunningStatus.STOPPED)) {
            onStopped();
        }
    }

    /**
     * Called when the GAIA library receives a GAIA packet that has been identified as containing
     * the vendor ID of this vendor.
     *
     * @param data
     *         The raw GAIA PDU data of a GAIA packet: A 4 bytes header followed by an optional
     *         payload. The vendor ID is contained within the first two bytes.
     */
    public abstract void handleData(byte[] data);

    /**
     * Called when this vendor is started. The most likely reason being that a device is connected.
     *
     * @param gaiaVersion
     *         The GAIA version supported by the device when this is called.
     */
    protected abstract void onStarted(int gaiaVersion);

    /**
     * Called when this vendor is getting stopped.
     */
    protected abstract void onStopped();
}
