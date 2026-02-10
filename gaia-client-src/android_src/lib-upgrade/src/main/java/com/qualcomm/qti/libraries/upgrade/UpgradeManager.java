/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.libraries.upgrade;

import androidx.annotation.NonNull;

import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationType;
import com.qualcomm.qti.libraries.upgrade.data.UpgradeError;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage;

/**
 * <p>This class manages the process of the Upgrade Protocol. It defines and builds
 * the upgrade messages and follows the protocol.</p>
 */
public interface UpgradeManager {

    /**
     * <p>To enable the display of debug logs for this manager.</p>
     * <p>They give complementary information on any call of a method.
     * They can indicate that a method is reached but also the action the method does.</p>
     *
     * @param show
     *         True to show the debug logs, false otherwise.
     */
    void showDebugLogs(boolean show);

    /**
     * <p>To set the maximum size for transferring data chunks.</p>
     *
     * @param size
     *         the maximum for the data chunks.
     *
     * @return the value that has been set.
     */
    int setChunkSize(int size);

    /**
     * <p>This method is the entry point to start the upgrade process.</p>
     * <p>This method can dispatch a {@link UpgradeError} object if the manager has not been able to
     * start the upgrade process.</p>
     * <p>The possible {@link UpgradeError.ErrorTypes ErrorTypes} are the following:
     * <ul>
     * <li>{@link UpgradeError.ErrorTypes#AN_UPGRADE_IS_ALREADY_PROCESSING
     * AN_UPGRADE_IS_ALREADY_PROCESSING}</li>
     * <li>{@link UpgradeError.ErrorTypes#NO_DATA NO_DATA}</li>
     * </ul></p>
     *
     * @param data
     *         The data to upload during an upgrade to upgrade the receiver.
     * @param dataID
     *         The ID to identify the given data on the receiver.
     */
    void startUpgrade(byte[] data, byte[] dataID);

    /**
     * <p>If an upgrade is processing, to resume it after a disconnection of the process, this
     * method should be called to restart the existing running process.</p>
     *
     * @return true if the manager has been able to resume an upgrade.
     */
    boolean start();

    /**
     * <p>To pause the process of the manager. This should be called if the upload has to stop
     * or packets cannot be sent anymore. For instance in the case of a disconnection.</p>
     * <p>It is expected to call {@link #start()} after.</p>
     */
    void pause();

    /**
     * <p>To know if there is an existing upgrade process running.</p>
     *
     * @return true if an upgrade has been started and not ended - successfully or not.
     */
    boolean isUpgrading();

    /**
     * <p>This method allows to manage an upgrade message which has been received.</p>
     * <p>If a received message does not correspond to an {@link UpgradeMessage UpgradeMessage} the
     * upgrade process is aborted.</p>
     *
     * @param bytes
     *         The received byte array.
     */
    void onUpgradeMessage(byte[] bytes);

    /**
     * <p>To abort the upgrade.</p>
     */
    void abort();

    /**
     * <p>This method is called by the listener after
     * {@link UpgradeListener#onConfirmationRequired(ConfirmationType, ConfirmationOptions[])
     * onConfirmationRequired} has
     * been called . It allows continuation of the upgrade process depending on the confirmation
     * choice.</p>
     *
     * @param type
     *         The type of confirmation which has been requested.
     * @param option
     *         The option that has been selected for the confirmation.
     */
    void onConfirmation(ConfirmationType type, @NonNull ConfirmationOptions option);

    /**
     * This method is called when the Upgrade mode has been enabled.
     */

    void onUpgradeModeEnabled();

    /**
     * This method is called when the Upgrade mode has been disabled.
     */
    void onUpgradeModeDisabled();

    /**
     * This is to release any resources hold by this manager.
     */
    void release();

    /**
     * This method is called when a message is successfully transmitted.
     * This is mainly used to know when the device is ready to abort after the user has not
     * confirmed a request.
     * Depending on the communication protocol, a message could be successfully transferred only if
     * it has been acknowledged.
     */
    void onMessageTransferred();

    /**
     * This method forces the UpgradeManager to stop any upgrade that is in progress.
     * This should be used only if a transport layers fails and cannot be recovered.
     */
    void forceStop();
}

