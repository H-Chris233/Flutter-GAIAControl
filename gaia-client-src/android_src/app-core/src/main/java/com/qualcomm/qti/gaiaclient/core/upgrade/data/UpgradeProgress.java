/*
 * ************************************************************************************************
 * * © 2020-2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.upgrade.data;

import androidx.annotation.NonNull;

import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.data.EndType;

public class UpgradeProgress {

    private final UpgradeState state;

    private final double uploadProgress;

    private final UpgradeInfoType type;

    private final UpgradeConfirmation confirmation;

    private final ConfirmationOptions[] options;

    private final EndType endType;

    public static UpgradeProgress upload(UpgradeState upgradeState, double progress) {
        return new UpgradeProgress(UpgradeInfoType.UPLOAD_PROGRESS, upgradeState, progress, null, null, null);
    }

    public static UpgradeProgress state(UpgradeState state) {
        double progress = (state == UpgradeState.UPLOAD || state == UpgradeState.INITIALISATION
                || state == UpgradeState.REBOOT || state == UpgradeState.RECONNECTING) ? 0 : 100;
        return new UpgradeProgress(UpgradeInfoType.STATE, state, progress, null, null, null);
    }

    public static UpgradeProgress confirmation(UpgradeState state, UpgradeConfirmation confirmation,
                                               @NonNull ConfirmationOptions[] options) {
        double progress = (state == UpgradeState.UPLOAD || state == UpgradeState.INITIALISATION
                || state == UpgradeState.REBOOT || state == UpgradeState.RECONNECTING) ? 0 : 100;
        return new UpgradeProgress(UpgradeInfoType.CONFIRMATION, state, progress, confirmation, options, null);
    }

    public static UpgradeProgress end(UpgradeState state, EndType endType) {
        return new UpgradeProgress(UpgradeInfoType.END, state, 100, null, null, endType);
    }

    private UpgradeProgress(UpgradeInfoType type, UpgradeState state, double uploadProgress,
                            UpgradeConfirmation confirmation, ConfirmationOptions[] options,
                            EndType endType) {
        this.state = state;
        this.uploadProgress = uploadProgress;
        this.type = type;
        this.confirmation = confirmation;
        this.options = options;
        this.endType = endType;
    }

    public UpgradeState getState() {
        return state;
    }

    public double getUploadProgress() {
        return uploadProgress;
    }

    public UpgradeInfoType getType() {
        return type;
    }

    public UpgradeConfirmation getConfirmation() {
        return confirmation;
    }

    public ConfirmationOptions[] getOptions() {
        return options;
    }

    public EndType getEndType() {
        return endType;
    }

    @NonNull
    @Override
    public String toString() {
        return "UpgradeProgress{" +
                "type=" + type +
                ", state=" + state +
                ", endType=" + endType +
                ", upload=" + uploadProgress + "%" +
                '}';
    }
}
