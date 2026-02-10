/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common.progress;

/**
 * This represents the data model of a Bluetooth device.
 */
public class ProgressFragmentViewData {

    private final String cancelButton;

    private final String doneButton;

    public ProgressFragmentViewData(String cancelButton, String doneButton) {
        this.cancelButton = cancelButton;
        this.doneButton = doneButton;
    }

    public String getCancelButton() {
        return cancelButton;
    }

    public String getDoneButton() {
        return doneButton;
    }
}
