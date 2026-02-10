/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.data;

public enum GestureConfigurationInfo {

    /**
     * <ul>
     *     <li>Fetch: no parameters.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    TOUCHPAD_CONFIGURATION,
    /**
     * <ul>
     *     <li>Fetch: no parameters.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    SUPPORTED_GESTURES,
    /**
     * <ul>
     *     <li>Fetch: no parameters.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    SUPPORTED_CONTEXTS,
    /**
     * <ul>
     *     <li>Fetch: no parameters.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    SUPPORTED_ACTIONS,
    /**
     * <ul>
     *     <li>Fetch: cannot be fetched.</li>
     *     <li>Set: parameter of type {@link com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.SetGestureConfiguration}.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    SET_GESTURE_CONFIGURATION,
    /**
     * <ul>
     *     <li>Fetch: parameter of type <code>int</code> that represents a gesture ID.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when fetching this information.</li>
     * </ul>>
     */
    GET_GESTURE_CONFIGURATION,
    /**
     * <ul>
     *     <li>Fetch: cannot be fetched.</li>
     *     <li>Set: cannot be set.</li>
     *     <li>Error: if an error occurs when resetting the entire configuration to default.</li>
     * </ul>>
     */
    RESET
}
