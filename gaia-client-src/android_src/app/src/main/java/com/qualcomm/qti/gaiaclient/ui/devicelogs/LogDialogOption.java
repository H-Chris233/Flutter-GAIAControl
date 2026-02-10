/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.devicelogs;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.common.progress.DialogOption;

public enum LogDialogOption implements DialogOption<Void> {

    OK(R.string.button_ok);

    private static final LogDialogOption[] VALUES = values();

    private final int labelId;

    LogDialogOption(int labelId) {
        this.labelId = labelId;
    }

    public String getLabel(@NonNull Context context) {
        return context.getString(labelId);
    }

    public Void getIdentifier() {
        return null;
    }

}
