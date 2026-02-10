/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common.progress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This represents the data to display in a dialog
 */
public class DialogViewData<D, O> {

    @NonNull
    private final D identifier;

    @NonNull
    private final String title;

    @NonNull
    private final String message;

    private boolean isCancelable = true;

    @Nullable
    private DialogOption<O> negativeOption = null;

    @Nullable
    private DialogOption<O> neutralOption = null;

    @Nullable
    private DialogOption<O> positiveOption = null;

    public DialogViewData(@NonNull D identifier, @NonNull String title, @NonNull String message) {
        this.title = title;
        this.message = message;
        this.identifier = identifier;
    }

    public void setCancelable(boolean cancelable) {
        isCancelable = cancelable;
    }

    public void setNegativeOption(@Nullable DialogOption<O> negativeOption) {
        this.negativeOption = negativeOption;
    }

    public void setNeutralOption(@Nullable DialogOption<O> neutralOption) {
        this.neutralOption = neutralOption;
    }

    public void setPositiveOption(@Nullable DialogOption<O> positiveOption) {
        this.positiveOption = positiveOption;
    }

    @NonNull
    public D getIdentifier() {
        return identifier;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    @NonNull
    public String getMessage() {
        return message;
    }

    public boolean isCancelable() {
        return isCancelable;
    }

    @Nullable
    public DialogOption<O>  getNegativeOption() {
        return negativeOption;
    }

    @Nullable
    public DialogOption<O>  getNeutralOption() {
        return neutralOption;
    }

    @Nullable
    public DialogOption<O>  getPositiveOption() {
        return positiveOption;
    }
}
