/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth;

/**
 * A listener to be notify on the success of the failure of sending some bytes to a device.
 */
public interface SendListener {

    /**
     * This is called when the core library is about to send the bytes that were provided with this listener.
     */
    void onSending();

    /**
     * This is called once the core library could send some bytes to a device. If the data is
     * flushed this is called after. If the data is not flushed this is called once the data is
     * provided to the Android system.
     */
    void onSent();

    /**
     * This is called if sending some bytes to a device as failed.
     */
    void onFailed();

}
