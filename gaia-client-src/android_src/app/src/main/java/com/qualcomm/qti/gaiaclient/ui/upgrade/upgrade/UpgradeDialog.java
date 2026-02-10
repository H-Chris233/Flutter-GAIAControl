/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.upgrade;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;

public enum UpgradeDialog {

    ERROR(R.string.upgrade_error_title, UpgradeDialog.NO_ID),
    CONFIRMATION_BATTERY_LOW_ON_DEVICE(R.string.alert_upgrade_low_battery_title,
                                       R.string.alert_upgrade_low_battery_message,
                                       UpgradeConfirmation.BATTERY_LOW_ON_DEVICE),
    CONFIRMATION_COMMIT(R.string.alert_upgrade_commit_title, R.string.alert_upgrade_commit_message,
                        UpgradeConfirmation.COMMIT),
    CONFIRMATION_TRANSFER_COMPLETE(R.string.alert_upgrade_transfer_complete_title,
                                   R.string.alert_upgrade_transfer_complete_message,
                                   UpgradeConfirmation.TRANSFER_COMPLETE),
    CONFIRMATION_WARNING_FILE_IS_DIFFERENT(R.string.alert_upgrade_sync_id_different_title,
                                           R.string.alert_upgrade_sync_id_different_message,
                                           UpgradeConfirmation.WARNING_FILE_IS_DIFFERENT),
    ALERT_PUT_EARBUD_IN_CASE(R.string.alert_upgrade_earbud_in_case_title,
                             R.string.alert_upgrade_earbud_in_case_message);

    private static final int NO_ID = 0;

    private static final UpgradeDialog[] VALUES = values();

    private final UpgradeConfirmation confirmation;

    private final int titleId;

    private final int messageId;

    UpgradeDialog(int titleId, int messageId) {
        this.titleId = titleId;
        this.messageId = messageId;
        this.confirmation = null;
    }

    UpgradeDialog(int titleId, int messageId, UpgradeConfirmation confirmation) {
        this.titleId = titleId;
        this.messageId = messageId;
        this.confirmation = confirmation;
    }

    public static UpgradeDialog valueOf(UpgradeConfirmation confirmation) {
        for (UpgradeDialog id : VALUES) {
            if (id.confirmation == confirmation) {
                return id;
            }
        }

        return ERROR;
    }

    public String getTitle(@NonNull Context context) {
        return titleId == NO_ID ? "" : context.getString(titleId);
    }

    public String getMessage(@NonNull Context context) {
        return messageId == NO_ID ? "" : context.getString(messageId);
    }

    public UpgradeConfirmation getUpgradeConfirmation() {
        return confirmation;
    }

    public boolean isConfirmation() {
        return confirmation != null;
    }

}
