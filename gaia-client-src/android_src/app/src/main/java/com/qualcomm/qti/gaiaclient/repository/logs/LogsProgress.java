/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.logs;

import com.qualcomm.qti.gaiaclient.core.data.DownloadLogsState;

public class LogsProgress {

    private final DownloadLogsState state;

    private final double progress;

    public LogsProgress(DownloadLogsState state, double progress) {
        this.state = state;
        this.progress = progress;
    }

    public DownloadLogsState getState() {
        return state;
    }

    public double getProgress() {
        return progress;
    }
}
