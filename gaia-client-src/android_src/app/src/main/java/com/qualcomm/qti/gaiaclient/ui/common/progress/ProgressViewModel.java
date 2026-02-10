/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common.progress;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;

import com.qualcomm.qti.gaiaclient.ui.common.StickyLiveData;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;

public abstract class ProgressViewModel<D, O> extends AndroidViewModel {

    private final StickyLiveData<NavigationEvent> mEventData = new StickyLiveData<>();

    private final StickyLiveData<ProgressViewData> mViewData = new StickyLiveData<>();

    private final StickyLiveData<DialogViewData<D, O>> mDialogData = new StickyLiveData<>();

    private final StickyLiveData<String> mProgressDoneData = new StickyLiveData<>();

    public ProgressViewModel(@NonNull Application application) {
        super(application);
        mViewData.setValue(new ProgressViewData("", false, 0));
    }

    final void onResume() {
        // forces dialogs to be added to the UI if data was generated while the activity was paused.
        mDialogData.setValue(mDialogData.getValue()); // must be consumed
    }

    final void setProgressObserver(@NonNull LifecycleOwner owner,
                                   @NonNull Observer<ProgressViewData> observer) {
        mViewData.observe(owner, observer);
    }

    final void setDialogObserver(@NonNull LifecycleOwner owner,
                                 @NonNull Observer<DialogViewData<D, O>> observer) {
        mDialogData.observe(owner, observer);
    }

    final void setDoneObserver(@NonNull LifecycleOwner owner, @NonNull Observer<String> observer) {
        mProgressDoneData.observe(owner, observer);
    }

    final void setEventObserver(@NonNull LifecycleOwner owner, @NonNull Observer<NavigationEvent> observer) {
        mEventData.observe(owner, observer);
    }

    final void onDialogAction(D identifier, @Nullable O option) {
        mDialogData.setValue(null); // must be consumed
        if (option == null) {
            onDialogCancelled(identifier);
        }
        else {
            onDialogOption(identifier, option);
        }
    }

    protected abstract void onDialogCancelled(D identifier);

    protected abstract void onDialogOption(D identifier, @NonNull O option);

    protected abstract void onAction(ProgressAction action);

    protected final void onProgressDone(String message) {
        mProgressDoneData.setValue(message);
    }

    protected final void updateProgress(String message, boolean isDeterminateProgress, double progress) {
        mViewData.setValue(new ProgressViewData(message, isDeterminateProgress, progress));
    }

    protected final void showDialog(@NonNull DialogViewData<D, O> data) {
        mDialogData.setValue(data);
    }

    protected final void hideDialog() {
        mDialogData.setValue(null); // must be consumed
    }

    protected final void sendEvent(NavigationEvent event) {
        mEventData.setValue(event); // must be consumed
    }

    public ProgressViewData getViewData() {
        return mViewData.getValue();
    }

    public DialogViewData<D, O> getDialogViewData() {
        return mDialogData.getValue();
    }

    public D getDialogId() {
        DialogViewData<D, O> dialogViewData = getDialogViewData();
        return dialogViewData != null ? dialogViewData.getIdentifier() : null;
    }
}
