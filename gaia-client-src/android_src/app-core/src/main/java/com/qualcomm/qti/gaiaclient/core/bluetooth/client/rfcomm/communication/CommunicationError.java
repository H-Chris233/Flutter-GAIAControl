/*
 * ************************************************************************************************
 * * © 2020, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.rfcomm.communication;

public enum CommunicationError {
    /**
     * Occurs when the communication is initialised. This can failed if the communication cannot
     * be established with the device.
     */
    INITIALISATION_FAILED,
    /**
     * Occurs when the connection with the device is lost.
     */
    CONNECTION_LOST
}
