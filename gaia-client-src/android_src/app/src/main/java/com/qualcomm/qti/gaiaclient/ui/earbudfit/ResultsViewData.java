/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.earbudfit;

public class ResultsViewData {

    private final int leftResourceId;

    private final int rightResourceId;

    public ResultsViewData(int leftResourceId, int rightResourceId) {
        this.leftResourceId = leftResourceId;
        this.rightResourceId = rightResourceId;
    }

    public int getLeftResourceId() {
        return leftResourceId;
    }

    public int getRightResourceId() {
        return rightResourceId;
    }
}
