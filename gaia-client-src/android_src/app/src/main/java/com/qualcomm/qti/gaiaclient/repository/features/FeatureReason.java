/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.features;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.R;

public enum FeatureReason {

    NOT_FOUND,
    NOT_AVAILABLE,
    FAILED,
    NOTIFICATION_NOT_SUPPORTED,
    UNKNOWN;

    public String getLabel(Context context) {
        return context == null ? null : context.getString(getResourceID());
    }

    private int getResourceID() {
        switch (this) {
            case NOT_FOUND:
                return R.string.features_not_found;
            case NOT_AVAILABLE:
                return R.string.features_not_available;
            case NOTIFICATION_NOT_SUPPORTED:
                return R.string.features_notification_not_supported;
            case FAILED:
            case UNKNOWN:
            default:
                return R.string.features_failed;
        }
    }
}
