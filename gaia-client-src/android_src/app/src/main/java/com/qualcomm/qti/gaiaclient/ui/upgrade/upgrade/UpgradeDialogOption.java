/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.upgrade;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.ui.common.progress.DialogOption;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;

public enum UpgradeDialogOption implements DialogOption<ConfirmationOptions> {

    OK(R.string.button_ok, null),
    ABORT(R.string.button_abort, ConfirmationOptions.ABORT),
    CANCEL(R.string.button_cancel, ConfirmationOptions.CANCEL),
    CONFIRM(R.string.button_confirm, ConfirmationOptions.CONFIRM),
    INTERACTIVE_COMMIT(R.string.button_now, ConfirmationOptions.INTERACTIVE_COMMIT),
    SILENT_COMMIT(R.string.button_later, ConfirmationOptions.SILENT_COMMIT);

    private static final UpgradeDialogOption[] VALUES = values();

    private final ConfirmationOptions option;

    private final int labelId;

    UpgradeDialogOption(int labelId, ConfirmationOptions option) {
        this.labelId = labelId;
        this.option = option;
    }

    public static UpgradeDialogOption valueOf(ConfirmationOptions option) {
        for (UpgradeDialogOption value : VALUES) {
            if (value.option == option) {
                return value;
            }
        }

        return null;
    }

    public String getLabel(@NonNull Context context) {
        return context.getString(labelId);
    }

    public ConfirmationOptions getIdentifier() {
        return option;
    }

}
