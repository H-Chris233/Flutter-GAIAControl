/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.earbudfit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qualcomm.qti.gaiaclient.databinding.FragmentEarbudFitBinding;
import com.qualcomm.qti.gaiaclient.ui.Navigator;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EarbudFitFragment extends Fragment {

    private FragmentEarbudFitBinding binding;

    private EarbudFitViewModel viewModel;


    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // this prevents the navigation to come back to another screen while it is upgrading
            viewModel.onAction(EarbudFitAction.NAVIGATE_BACK);
            navigateBack();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(EarbudFitViewModel.class);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, mBackPressedCallback);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEarbudFitBinding.inflate(inflater);
        subscribeUI();
        binding.buttonCancel.setOnClickListener((view) -> viewModel.onAction(EarbudFitAction.CANCEL));
        binding.buttonStart.setOnClickListener((view) -> viewModel.onAction(EarbudFitAction.START));
        return binding.getRoot();
    }

    private void subscribeUI() {
        viewModel.setObserver(getViewLifecycleOwner(), this::updateData);
    }

    private void updateData(EarbudFitViewData data) {
        binding.setData(data);
    }

    private void navigateBack() {
        Navigator.navigate(binding.getRoot(), EarbudFitFragmentDirections.back());
    }

}
