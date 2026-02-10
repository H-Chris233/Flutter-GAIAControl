/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade.data;

import com.qualcomm.qti.libraries.upgrade.messages.ErrorCodes;
import com.qualcomm.qti.libraries.upgrade.messages.OpCodes;

/**
 * <p>All the types of confirmation this manager could request from the listener during the
 * upgrade process.</p>
 */
public enum ConfirmationType {

    /**
     * <p>When the manager receives the
     * {@link OpCodes.Enum#UPGRADE_TRANSFER_COMPLETE_IND UPGRADE_TRANSFER_COMPLETE_IND} message,
     * the manager needs a confirmation to
     * {@link OpCodes.UpgradeTransferCompleteRES.Action#CONTINUE CONTINUE}
     * or {@link OpCodes.UpgradeTransferCompleteRES.Action#ABORT ABORT}  the process.</p>
     */
    TRANSFER_COMPLETE,
    /**
     * <p>When the manager receives the
     * {@link OpCodes.Enum#UPGRADE_COMMIT_REQ UPGRADE_COMMIT_REQ} message, the manager needs a
     * confirmation to {@link OpCodes.UpgradeCommitCFM.Action#CONTINUE CONTINUE}
     * or {@link OpCodes.UpgradeCommitCFM.Action#ABORT ABORT}  the process.</p>
     */
    COMMIT,
    /**
     * <p>When the resume point {@link ResumePoint#POST_REBOOT POST_REBOOT} is reached,
     * the manager needs a confirmation to
     * {@link OpCodes.UpgradeProceedToCommitRES.Action#CONTINUE CONTINUE}
     * or {@link OpCodes.UpgradeProceedToCommitRES.Action#ABORT ABORT} the process.</p>
     */
    IN_PROGRESS,
    /**
     * <p>When the manager receives
     * {@link ErrorCodes.Enum#WARN_SYNC_ID_IS_DIFFERENT
     * WARN_SYNC_ID_IS_DIFFERENT}, the listener has to ask if the upgrade should continue or
     * not.</p>
     */
    WARNING_FILE_IS_DIFFERENT,
    /**
     * <p>>When the Host receives {@link ErrorCodes.Enum#ERROR_BATTERY_LOW
     * ERROR_BATTERY_LOW},the listener has to ask if the upgrade should continue or not.</p>
     */
    BATTERY_LOW_ON_DEVICE
}
