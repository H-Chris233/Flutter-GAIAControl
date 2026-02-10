/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.upgrade;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeConfirmation;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeFail;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeProgress;
import com.qualcomm.qti.gaiaclient.core.upgrade.data.UpgradeState;
import com.qualcomm.qti.gaiaclient.repository.Result;
import com.qualcomm.qti.gaiaclient.repository.ResultStatus;
import com.qualcomm.qti.gaiaclient.repository.system.SystemRepository;
import com.qualcomm.qti.gaiaclient.repository.upgrade.SizeType;
import com.qualcomm.qti.gaiaclient.repository.upgrade.UpgradeRepository;
import com.qualcomm.qti.gaiaclient.ui.common.Observers;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEventType;
import com.qualcomm.qti.gaiaclient.ui.common.progress.DialogViewData;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressAction;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressViewData;
import com.qualcomm.qti.gaiaclient.ui.common.progress.ProgressViewModel;
import com.qualcomm.qti.libraries.upgrade.data.ConfirmationOptions;
import com.qualcomm.qti.libraries.upgrade.data.EndType;
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeAlert;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class UpgradeProgressViewModel extends ProgressViewModel<UpgradeDialog, ConfirmationOptions> {

    private static final String TAG = "UpgradeProgressViewModel";

    private final UpgradeRepository upgradeRepository;

    private final SystemRepository systemRepository;

    private final DataObservers observers = new DataObservers();

    @Inject
    public UpgradeProgressViewModel(@NonNull Application application, UpgradeRepository upgradeRepository,
                                    SystemRepository systemRepository) {
        super(application);
        this.upgradeRepository = upgradeRepository;
        this.systemRepository = systemRepository;

        observers.start();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        observers.stop();
    }

    @Override
    protected void onDialogCancelled(UpgradeDialog identifier) {
        if (identifier == UpgradeDialog.ERROR) {
            navigateBack();
        }
    }

    @Override
    protected void onDialogOption(UpgradeDialog identifier, @NonNull ConfirmationOptions option) {
        if (identifier != null && identifier.isConfirmation()) {
            upgradeRepository.confirmUpgrade(getApplication(), identifier.getUpgradeConfirmation(), option);
        }
        else if (identifier == UpgradeDialog.ALERT_PUT_EARBUD_IN_CASE && option == ConfirmationOptions.ABORT) {
            upgradeRepository.abortUpgrade(getApplication());
        }
        else if (identifier == UpgradeDialog.ERROR) {
            navigateBack();
        }
    }

    @Override
    protected void onAction(ProgressAction action) {
        if (action == null) {
            return;
        }

        switch (action) {
            case CANCEL:
                upgradeRepository.abortUpgrade(getApplication());
                break;

            case DONE:
                navigateBack();
                break;
        }
    }

    private void onResult(Result<UpgradeProgress, UpgradeFail> result) {
        if (result == null) {
            // this can happen after a reset of the values
            return;
        }

        // get the data from the result
        ResultStatus status = result.getStatus();
        UpgradeProgress info = result.getData();
        UpgradeFail error = result.getReason();

        switch (status) {
            case FAIL:
                if (error != null) {
                    showUpgradeFail(error);
                }
                break;

            case SUCCESS:
                if (info != null) {
                    onProgressDone(updateProgressMessage(info.getEndType()));
                }
                // no break as the data needs to also be updated
            case IN_PROGRESS:
                if (info != null) {
                    updateData(info);
                }
                break;
        }
    }

    private void onSetChunkSize(Integer value) {
        int expected = upgradeRepository.getSize(SizeType.EXPECTED);

        if (value == null || expected == SizeType.NULL_VALUE || value == expected
                || !systemRepository.isAndroidDeveloperModeOn()) {
            // nothing to do
            return;
        }

        String toast = getApplication().getString(R.string.toast_upgrade_set_size,
                                                  String.valueOf(value));
        Toast.makeText(getApplication(), toast, Toast.LENGTH_SHORT).show();
    }

    private void onAlerts(Pair<UpgradeAlert, Boolean> pair) {
        UpgradeAlert alert = pair.first;

        if (alert == null) {
            return;
        }

        if (getDialogId() != null && getDialogId() != UpgradeDialog.ALERT_PUT_EARBUD_IN_CASE) {
            // other dialogs (error or process confirmation) have priority
            return;
        }

        boolean raised = pair.second != null && pair.second;

        if (raised) {
            Context context = getApplication().getBaseContext();
            UpgradeDialog dialogId = UpgradeDialog.ALERT_PUT_EARBUD_IN_CASE;
            DialogViewData<UpgradeDialog, ConfirmationOptions> data =
                    new DialogViewData<>(dialogId, dialogId.getTitle(context), dialogId.getMessage(context));
            data.setCancelable(false);
            data.setNegativeOption(UpgradeDialogOption.ABORT);
            showDialog(data);
        }
        else {
            hideDialog();
        }
    }

    private void onBluetoothStateChanged(Boolean state) {
        if (!state) {
            // Check if there are open dialogs.
            // If not error dialogs, close them.
            // If it's not confirmation it means it's an error
            DialogViewData<UpgradeDialog, ConfirmationOptions> dialogViewData = getDialogViewData();
            UpgradeDialog dialog = dialogViewData != null ? dialogViewData.getIdentifier() : null;
            if (dialog != null && dialog.isConfirmation()) {
                hideDialog();
            }
        }
    }

    private void updateData(@NonNull UpgradeProgress info) {
        switch (info.getType()) {
            case UPLOAD_PROGRESS:
            case STATE:
                updateProgress(info.getState(), info.getUploadProgress());
                break;

            case CONFIRMATION:
                showConfirmation(info.getConfirmation(), info.getOptions());
                break;

            case END:
                updateProgressMessage(info.getEndType());
                break;
        }
    }

    private void updateProgress(UpgradeState state, double uploadProgress) {
        ProgressViewData viewData = getViewData();

        double progress = state == UpgradeState.UPLOAD ? uploadProgress :
                          state == UpgradeState.PAUSED && viewData != null ? viewData.getProgressInPercent() :
                          (state == UpgradeState.INITIALISATION || state == UpgradeState.REBOOT
                                  || state == UpgradeState.RECONNECTING) ? 0 : 100;

        boolean isDeterminate = state == UpgradeState.UPLOAD
                || (state == UpgradeState.PAUSED && viewData != null && viewData.isDeterminateProgress());

        String stateLabel = state == null ? null : state.toString();
        updateProgress(stateLabel, isDeterminate, progress);

        if (state == UpgradeState.INITIALISATION) {
            // a reconnection has happened -> hide any previous dialogs
            hideDialog();
        }
    }

    private void showConfirmation(UpgradeConfirmation confirmation, @NonNull ConfirmationOptions[] options) {
        if (confirmation == UpgradeConfirmation.IN_PROGRESS) {
            // this stage is bypassed because it is followed by the commit confirmation
            upgradeRepository
                    .confirmUpgrade(getApplication(), UpgradeConfirmation.IN_PROGRESS, ConfirmationOptions.CONFIRM);
            return;
        }

        Context context = getApplication().getBaseContext();
        UpgradeDialog dialogId = UpgradeDialog.valueOf(confirmation);

        DialogViewData<UpgradeDialog, ConfirmationOptions> data =
                new DialogViewData<>(dialogId, dialogId.getTitle(context), dialogId.getMessage(context));
        data.setCancelable(false);
        setDialogOptions(data, options);

        showDialog(data);
    }

    private void navigateBack() {
        sendEvent(new NavigationEvent(NavigationEventType.NAVIGATE_BACK));
    }

    /**
     * @param options
     *         maximum 3 options ordered as follows:
     *         <ul>
     *             <li>1 option: POSITIVE.</li>
     *             <li>2 options: NEGATIVE, POSITIVE.</li>
     *             <li>3 options: NEUTRAL, NEGATIVE, POSITIVE.</li>
     *         </ul>
     */
    private void setDialogOptions(DialogViewData<UpgradeDialog, ConfirmationOptions> data,
                                  ConfirmationOptions[] options) {
        int positiveOffset = 0;
        int negativeOffset = 0;
        switch (options.length) {
            case 3:
                // has 3 options
                data.setNeutralOption(UpgradeDialogOption.valueOf(options[0]));
                negativeOffset++;
                positiveOffset++;
            case 2:
                // has 2 options
                data.setNegativeOption(UpgradeDialogOption.valueOf(options[negativeOffset]));
                positiveOffset++;
            case 1:
                // has 1 option
                data.setPositiveOption(UpgradeDialogOption.valueOf(options[positiveOffset]));
                break;

            default:
                Log.i(TAG, "[setDialogOptions] Unexpected number of options for: dialog=" + data.getIdentifier() +
                        ", options=" + options.length);
        }
    }

    private String updateProgressMessage(EndType endType) {
        if (endType == null) {
            return "";
        }

        switch (endType) {
            case SILENT_COMMIT:
                return getApplication().getString(R.string.upgrade_finish_silent_commit);
            case COMPLETE:
                return getApplication().getString(R.string.upgrade_finish_complete);
            case UPGRADE_IN_PROGRESS_WITH_DIFFERENT_ID:
                return getApplication().getString(R.string.upgrade_finish_different_upgrade_in_progress);
            case ABORTED:
                return getApplication().getString(R.string.upgrade_end_aborted);
            case COMPLETE_WITH_SECURITY_UPDATE_FAILED:
                return getApplication().getString(R.string.upgrade_finish_complete_with_security_update_failed);
            default:
                return null;
        }
    }

    private void showUpgradeFail(@NonNull UpgradeFail fail) {
        Context context = getApplication().getApplicationContext();
        String title = UpgradeDialog.ERROR.getTitle(context);
        String content = new UpgradeErrorFormatter(context, fail).format();
        DialogViewData<UpgradeDialog, ConfirmationOptions> data =
                new DialogViewData<>(UpgradeDialog.ERROR, title, content);
        data.setPositiveOption(UpgradeDialogOption.OK);
        showDialog(data);
    }

    private class DataObservers extends Observers {
        private final Observer<Boolean> bluetoothStateObserver = UpgradeProgressViewModel.this::onBluetoothStateChanged;
        private final Observer<Result<UpgradeProgress, UpgradeFail>> upgradeResultObserver =
                UpgradeProgressViewModel.this::onResult;
        private final Observer<Integer> setSizeObserver = UpgradeProgressViewModel.this::onSetChunkSize;
        private final Observer<Pair<UpgradeAlert, Boolean>> alertsObserver = UpgradeProgressViewModel.this::onAlerts;

        @Override
        protected void registerObservers() {
            systemRepository.getBluetoothStateLiveData().observeForever(bluetoothStateObserver);
            upgradeRepository.getUpgradeLiveData().observeForever(upgradeResultObserver);
            upgradeRepository.getSizeLiveData(SizeType.SET).observeForever(setSizeObserver);
            upgradeRepository.getAlertsLiveData().observeForever(alertsObserver);
        }

        @Override
        protected void unregisterObservers() {
            systemRepository.getBluetoothStateLiveData().removeObserver(bluetoothStateObserver);
            upgradeRepository.getUpgradeLiveData().removeObserver(upgradeResultObserver);
            upgradeRepository.getSizeLiveData(SizeType.SET).removeObserver(setSizeObserver);
            upgradeRepository.getAlertsLiveData().removeObserver(alertsObserver);
        }
    }
}
