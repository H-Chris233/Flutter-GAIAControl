/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common.progress;

/**
 * This represents the data model of a Bluetooth device.
 */
public class ProgressViewData {

    private final String label;

    private final int progressForProgressBar;

    private final double progressInPercent;

    private final boolean isDeterminateProgress;

    public ProgressViewData(String label, boolean isDeterminateProgress, double progressInPercent) {
        this.label = label;
        this.progressInPercent = progressInPercent;
        this.progressForProgressBar = (int) progressInPercent;
        this.isDeterminateProgress = isDeterminateProgress;
    }

    public String getLabel() {
        return label;
    }

    public int getProgressForProgressBar() {
        return progressForProgressBar;
    }

    public double getProgressInPercent() {
        return progressInPercent;
    }

    public boolean isDeterminateProgress() {
        return isDeterminateProgress;
    }

}
