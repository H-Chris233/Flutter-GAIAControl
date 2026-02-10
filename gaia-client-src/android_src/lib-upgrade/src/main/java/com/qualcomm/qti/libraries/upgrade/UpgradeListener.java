/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade;

import com.qualcomm.qti.libraries.upgrade.data.EndType;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeError;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationType;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessageListener;
import com.qualcomm.qti.libraries.upgrade.data.ResumePoint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * <p>This interface allows the UpgradeManager to dispatch messages or events.</p>
 */
public interface UpgradeListener {

    /**
     * <p>To send an {@link UpgradeMessage UpgradeMessage} over the transport managed by the
     * UpgradeListener owner.</p>
     *
     * @param bytes
     *         The bytes of the message to send.
     */
    void sendUpgradeMessage(byte[] bytes, @Nullable UpgradeMessageListener listener);

    /**
     * <p>Called when an error occurs during the upgrade process. When this method is called the
     * process is aborting.</p>
     *
     * @param error
     *         The error that occurs during the upgrade process.
     */
    void onUpgradeError(UpgradeError error);

    /**
     * <p>This method is called when the upgrade process has successfully ended - either the upgrade has completed or
     * the process has nicely ended - for silent commit for instance.</p>
     * <p>This is an event only, the ending of the Upgrade process is managed by the UpgradeManager.</p>
     */
    void onUpgradeEnd(EndType type);

    /**
     * <p>To inform the listener about any progress on the steps of the upgrade process.</p>
     *
     * @param point
     *         The step that the manager is now at.
     */
    void onResumePointChanged(ResumePoint point);

    /**
     * <p>This method is called during the upload of the data to provide the progress of the
     * upload in percent.</p>
     *
     * @param resumePoint the current resume point the device is at.
     * @param percentage the percentage of bytes that have been uploaded onto the device.
     */
    void onUploadProgress(ResumePoint resumePoint, double percentage);

    /**
     * <p>This method is called when the manager needs the listener to confirm any
     * action before continuing the process.</p>
     * <p>To answer to the manager, the listener must call
     * {@link UpgradeManager#onConfirmation(ConfirmationType, ConfirmationOptions) onConfirmation}.</p>
     */
    void onConfirmationRequired(@NonNull ConfirmationType type, @NonNull ConfirmationOptions[] options);

    /**
     * <p>This method is called when the {@link UpgradeManager UpgradeManager} is about to
     * start an upgrade.</p>
     * <p>It allows the owner of {@link UpgradeManager UpgradeManager} to resolve any state or
     * requirements before the process starts.</p>
     * <p>Once done, the listener must call {@link UpgradeManager#onUpgradeModeEnabled()}.</p>
     */
    void enableUpgradeMode();

    /**
     * p>This method is called when the {@link UpgradeManager UpgradeManager} has finished the
     * process, either after an error, an abort or a successful completion.</p>
     * <p>It allows the owner of {@link UpgradeManager UpgradeManager} to resolve any state or
     * requirements after the process has ended.</p>
     * <p>Once done, the listener must call {@link UpgradeManager#onUpgradeModeDisabled()}.</p>
     */
    void disableUpgradeMode();

    /**
     * <p>This is used by the manager to let the listener knows when it is aborting an upgrade to
     * end it.</p>
     * <p>This can either be called when the upgrade manager detects it has to abort the upgrade
     * or when {@link UpgradeManager#abort()} is called.</p>
     */
    void isAborting();

    /**
     * <p>This is used by the manager to let the listener knows when the device has raised a warning
     * for the user to interact directly with the device.</p>
     */
    void onAlert(UpgradeAlert alert, boolean raised);

}
