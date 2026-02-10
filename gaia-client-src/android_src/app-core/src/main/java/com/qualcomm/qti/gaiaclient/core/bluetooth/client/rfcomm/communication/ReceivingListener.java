/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

/**
 * <p>This is to get updates during a RFCOMM connection when using {@link Communicator
 * Communicator}.</p>
 * <p>The {@link Communicator Communicator} takes care of calling this listener
 * over the thread it was created from.</p>
 */
public interface ReceivingListener {

    /**
     * <p>This method is called to inform that it is ready to send and receive messages through an
     * ongoing connection.</p>
     */
    void onCommunicationReady();

    /**
     * Indicates that the connection has failed and provides the reason.
     */
    void onCommunicationFailed(CommunicationError reason);

    /**
     * Indicates that the communication channel with the device has ended. This is always sent
     * even if an error occurred leading the communication to end.
     */
    void onCommunicationEnded();

}
