/*
 * ************************************************************************************************
 * * © 2020-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.connection;

public class ConnectionViewData {

    private final String state;
    private final String reason;
    private final boolean inProgress;
    private final boolean isConnected;
    private final boolean isError;

    ConnectionViewData(String state, String reason, boolean inProgress, boolean isConnected, boolean isError) {
        this.state = state;
        this.reason = reason;
        this.inProgress = inProgress;
        this.isConnected = isConnected;
        this.isError = isError;
    }

    public String getState() {
        return state;
    }

    public String getReason() {
        return reason;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isError() {
        return isError;
    }
}
