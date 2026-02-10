/*
 * ************************************************************************************************
 * * © 2020, 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

import com.qualcomm.qti.gaiaclient.core.gaia.core.v1v2.packets.V1V2ErrorStatus;
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.packets.V3ErrorStatus;

/**
 * All the reasons for Requests failures. This enumeration allow a wrapping of the GAIA status for
 * the publication of the errors.
 */
public enum Reason {
    /**
     * Used when trying to fetch or start a process with the device but the device returns an error
     * for insufficient resources and/or incorrect state.
     */
    NOT_AVAILABLE,
    /**
     * Used when a feature or a command is not supported - either detected within this application
     * or sent by the device.
     */
    NOT_SUPPORTED,
    /**
     * Used when the device supports a feature but not the corresponding notifications.
     */
    NOTIFICATION_NOT_SUPPORTED,
    /**
     * Used when the parameters of a command do not match the expectations - either detected by the
     * core app and/or the device.
     */
    MALFORMED_REQUEST,
    /**
     * Used when the application times out on not receiving an answer for a command that requires
     * one. Here "response" means responding to a command, the answer can be an acknowledgement, a
     * response or an error.
     */
    NO_RESPONSE,
    /**
     * Used when sending a packet fails while the device is still connected.
     */
    SENDING_FAILED,
    /**
     * Used when using some commands that requires an authentication.
     */
    AUTHENTICATION,
    /**
     * Used when the app fails to create a file.
     */
    FILE_CREATION_FAILED,
    /**
     * Used when something failed to be performed due to the device being disconnected.
     */
    DISCONNECTED,
    /**
     * Used when the device sends an error that does not match anything known by the application.
     */
    UNKNOWN,
    /**
     * Used when the device informs the app that there is no data available when trying to get the
     * device logs.
     */
    LOGS_NO_DATA,
    /**
     * Used when the device informs the app that there is no debug partition available when trying
     * either to get debug information or the debug logs.
     */
    LOGS_NO_DEBUG_PARTITION,
    /**
     * Used when writing into a file fails while getting information from the device.
     */
    FILE_WRITING_FAILED,
    /**
     * Used when the response to a command do not match the expectations.
     */
    MALFORMED_RESPONSE;

    public static Reason valueOf(V1V2ErrorStatus status) {
        switch (status) {
            case NOT_AUTHENTICATED:
            case AUTHENTICATING:
                return AUTHENTICATION;

            case INSUFFICIENT_RESOURCES:
            case INCORRECT_STATE:
                return NOT_AVAILABLE;

            case COMMAND_NOT_SUPPORTED:
                return NOT_SUPPORTED;

            case INVALID_PARAMETER:
                return MALFORMED_REQUEST;

            case IN_PROGRESS: // unexpected
            case SUCCESS: // unexpected
            case NO_STATUS: // unexpected
            case UNKNOWN:
            default:
                return UNKNOWN;
        }
    }

    public static Reason valueOf(V3ErrorStatus status) {
        switch (status) {
            case INSUFFICIENT_RESOURCES:
            case INCORRECT_STATE:
            case FEATURE_SPECIFIC:
                return NOT_AVAILABLE;

            case FEATURE_NOT_SUPPORTED:
            case COMMAND_NOT_SUPPORTED:
                return NOT_SUPPORTED;

            case NOT_AUTHENTICATED:
            case AUTHENTICATING:
                return AUTHENTICATION;

            case INVALID_PARAMETER:
                return MALFORMED_REQUEST;

            case IN_PROGRESS:
            default:
                return UNKNOWN;
        }
    }
}
