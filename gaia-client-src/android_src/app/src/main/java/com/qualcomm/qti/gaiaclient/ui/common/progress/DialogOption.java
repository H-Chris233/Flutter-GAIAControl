/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common.progress;

import android.content.Context;

import androidx.annotation.NonNull;

public interface DialogOption<O> {

    O getIdentifier();

    String getLabel(@NonNull Context context);
}
