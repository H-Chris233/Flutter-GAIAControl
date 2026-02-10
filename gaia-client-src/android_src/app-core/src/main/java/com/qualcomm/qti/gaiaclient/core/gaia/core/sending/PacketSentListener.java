/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.sending;

/**
 * To be notified when a packet is sent.
 */
public interface PacketSentListener {

    /**
     * This is called once the core library could send a packet to a device. If the data is
     * flushed this is called after. If the data is not flushed this is called once the data is
     * given to the Android system.
     */
    void onSent();

}
