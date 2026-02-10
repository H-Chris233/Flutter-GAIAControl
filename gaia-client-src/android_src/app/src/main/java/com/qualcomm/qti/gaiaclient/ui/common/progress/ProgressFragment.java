/*
 * ************************************************************************************************
 * * © 2020, 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.  *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.common.progress;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;

import com.qualcomm.qti.gaiaclient.databinding.FragmentProgressBinding;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;

public abstract class ProgressFragment<D, O, VM extends ProgressViewModel<D, O>> extends Fragment {

    private FragmentProgressBinding mBinding;

    private VM mViewModel;

    private AlertDialog mDialog;

    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // this prevents the navigation to come back to another screen while in progress
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewModel = initViewModel(this);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, mBackPressedCallback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        mBinding = FragmentProgressBinding.inflate(inflater);
        mBinding.setIsInProgress(true);
        mBinding.setStaticData(getProgressFragmentViewData());
        mBinding.cancel.setOnClickListener(view -> mViewModel.onAction(ProgressAction.CANCEL));
        mBinding.done.setOnClickListener(view -> mViewModel.onAction(ProgressAction.DONE));

        subscribeUI();
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mViewModel.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismissDialog();
    }

    protected View getRoot() {
        return mBinding.getRoot();
    }

    protected abstract VM initViewModel(Fragment fragment);

    protected abstract ProgressFragmentViewData getProgressFragmentViewData();

    protected abstract void onEvent(NavigationEvent event);

    private void subscribeUI() {
        LifecycleOwner owner = getViewLifecycleOwner();
        mViewModel.setProgressObserver(owner, this::onProgress);
        mViewModel.setDialogObserver(owner, this::onDialog);
        mViewModel.setDoneObserver(owner, this::onDone);
        mViewModel.setEventObserver(owner, this::onEvent);
    }

    private void onProgress(ProgressViewData data) {
        mBinding.setProgress(data);
    }

    private void onDialog(DialogViewData<D, O> data) {
        dismissDialog(); // only one dialog at a time

        if (data != null) {
            displayDialog(data);
        }
    }

    private void onDone(String message) {
        boolean isInProgress = message == null;
        mBinding.setIsInProgress(isInProgress);
        mBinding.setMessage(message);
        mBackPressedCallback.setEnabled(isInProgress);
    }

    private void displayDialog(DialogViewData<D, O> data) {
        Context context = getContext();

        if (context == null) {
            return;
        }

        D identifier = data.getIdentifier();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(data.getMessage())
                .setTitle(data.getTitle())
                .setCancelable(data.isCancelable())
                .setOnCancelListener(dialog -> onDialogAction(identifier, null));

        DialogOption<O> negativeOption = data.getNegativeOption();
        if (negativeOption != null) {
            builder.setNegativeButton(negativeOption.getLabel(context),
                                      (dialog, which) -> onDialogAction(identifier, negativeOption.getIdentifier()));
        }

        DialogOption<O> neutralOption = data.getNeutralOption();
        if (neutralOption != null) {
            builder.setNeutralButton(neutralOption.getLabel(context),
                                     (dialog, which) -> onDialogAction(identifier, neutralOption.getIdentifier()));
        }

        DialogOption<O> positiveOption = data.getPositiveOption();
        if (positiveOption != null) {
            builder.setPositiveButton(positiveOption.getLabel(context),
                                      (dialog, which) -> onDialogAction(identifier, positiveOption.getIdentifier()));
        }

        mDialog = builder.create();
        mDialog.show();
    }

    private void onDialogAction(D identifier, @Nullable O option) {
        mDialog = null;
        mViewModel.onDialogAction(identifier, option);
    }

    private void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }

}
