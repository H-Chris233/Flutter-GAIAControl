/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SnackMessage {

    private static final int NULL_ACTION = -1;

    private final String message;
    private final int length;
    private final int action;
    @NonNull
    private final View.OnClickListener clickListener;

    public SnackMessage(String message, int length) {
        this(message, length, NULL_ACTION, null);
    }

    public SnackMessage(String message, int length, int action, @Nullable View.OnClickListener clickListener) {
        this.message = message;
        this.length = length;
        this.action = action;
        this.clickListener = clickListener != null ? clickListener : v -> {};
    }

    public String getMessage() {
        return message;
    }

    public int getLength() {
        return length;
    }

    public boolean hasAction() {
        return action != NULL_ACTION;
    }

    public int getAction() {
        return action;
    }

    @NonNull
    public View.OnClickListener getClickListener() {
        return clickListener;
    }
}
