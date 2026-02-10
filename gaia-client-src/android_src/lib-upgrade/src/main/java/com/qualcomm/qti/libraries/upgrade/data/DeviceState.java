/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A class to store the different states of a device for an ongoing upgrade process.
 */
public final class DeviceState {

    /**
     * The current step the device is at.
     */
    private final AtomicReference<ResumePoint> resumePoint = new AtomicReference<>(ResumePoint.START);
    /**
     * To know if messages can be sent to the device.
     */
    private final AtomicBoolean isTransportConnected = new AtomicBoolean(false);
    /**
     * To know the upgrade protocol version supported by the device.
     */
    private final AtomicInteger protocolVersion = new AtomicInteger(0);
    /**
     * To know if the device supports silent commit.
     */
    private final AtomicBoolean silentCommitSupport = new AtomicBoolean(false);

    /**
     * Empty constructor to build a new instance of this class.
     */
    public DeviceState() {
    }

    /**
     * To get the current step the device is at for the upgrade.
     */
    public ResumePoint getResumePoint() {
        return resumePoint.get();
    }

    /**
     * To update the upgrade step the device is at.
     */
    public void setResumePoint(ResumePoint point) {
        this.resumePoint.set(point);
    }

    /**
     * To know if messages can be sent to a device.
     */
    public boolean isTransportConnected() {
        return isTransportConnected.get();
    }

    /**
     * To update the state of messages as processable.
     * This method compares the expected state to the current value prior to set it.
     * It returns True if the operation was successful: it means that the current value was the
     * expected value and the current value is now the new value.
     */
    public boolean setIsTransportConnected(boolean expected, boolean value) {
        return isTransportConnected.compareAndSet(expected, value);
    }

    /**
     * To know the upgrade protocol supported by the device.
     */
    public int getProtocolVersion() {
        return protocolVersion.get();
    }

    /**
     * To set the upgrade protocol version that is supported by the device.
     */
    public void setProtocolVersion(int version) {
        protocolVersion.set(version);
    }

    /**
     * To know if silent commit is supported by the device.
     */
    public boolean isSilentCommitSupported() {
        return silentCommitSupport.get();
    }

    /**
     * To set the support of silent commit by the device.
     */
    public void setSilentCommitSupport(boolean supported) {
        silentCommitSupport.set(supported);
    }

    /**
     * To reset all the states of a device.
     */
    public void reset() {
        setResumePoint(ResumePoint.START);
        setIsTransportConnected(isTransportConnected(), false);
        setProtocolVersion(0);
        setSilentCommitSupport(false);
    }
}
