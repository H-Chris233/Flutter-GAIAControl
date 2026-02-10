/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.upgrade;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.core.data.Reason;
import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaErrorStatus;
import com.qualcomm.qti.gaiaclient.core.publications.PublicationManager;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpdateOptions;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;

public class UpgradeHelperWrapper implements UpgradeHelper {

    private final UpgradeHelperImpl mHelper;

    public UpgradeHelperWrapper(@NonNull PublicationManager publicationManager) {
        this.mHelper = new UpgradeHelperImpl(publicationManager);
    }

    @Override
    public void startUpgrade(Context context, @NonNull UpdateOptions options) {
        mHelper.startUpgrade(context, options);
    }

    @Override
    public void abort() {
        mHelper.abort();
    }

    @Override
    public void confirm(UpgradeConfirmation confirmation, @NonNull ConfirmationOptions option) {
        mHelper.confirm(confirmation, option);
    }

    @Override
    public void onUpgradeMessage(byte[] data) {
        mHelper.onUpgradeMessage(data);
    }

    @Override
    public void onAcknowledged() {
        mHelper.onAcknowledged();
    }

    @Override
    public void onSendingFailed(Reason error) {
        mHelper.onSendingFailed(error);
    }

    @Override
    public void onErrorResponse(UpgradeGaiaCommand command, GaiaErrorStatus status) {
        mHelper.onErrorResponse(command, status);
    }

    @Override
    public void onUpgradeConnected() {
        mHelper.onUpgradeConnected();
    }

    @Override
    public void onUpgradeDisconnected() {
        mHelper.onUpgradeDisconnected();
    }

    @Override
    public void onPlugged(UpgradeHelperListener listener) {
        mHelper.onPlugged(listener);
    }

    @Override
    public void onUnplugged() {
        mHelper.onUnplugged();
    }

    @Override
    public boolean isFlushed() {
        return mHelper.isFlushed();
    }

    @Override
    public void release() {
        mHelper.release();
    }
}
