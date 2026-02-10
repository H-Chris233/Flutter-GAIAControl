/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.features;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.R;

public enum InfoReason {

    NOT_FOUND,
    NOT_AVAILABLE,
    FAILED,
    UNKNOWN;

    public String getLabel(Context context) {
        return context == null ? null : context.getString(getResourceID());
    }

    private int getResourceID() {
        switch (this) {
            case NOT_FOUND:
                return R.string.info_not_found;
            case NOT_AVAILABLE:
                return R.string.info_not_available;
            case FAILED:
            case UNKNOWN:
            default:
                return R.string.info_failed;
        }
    }

}
