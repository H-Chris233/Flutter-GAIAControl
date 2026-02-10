/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.connection;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.qualcomm.qti.gaiaclient.databinding.FragmentConnectionBinding;
import com.qualcomm.qti.gaiaclient.repository.connection.Device;
import com.qualcomm.qti.gaiaclient.ui.Navigator;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEvent;
import com.qualcomm.qti.gaiaclient.ui.common.events.NavigationEventType;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ConnectionFragment extends Fragment {

    private ConnectionViewModel viewModel;

    private FragmentConnectionBinding binding;

    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            // this prevents the navigation to come back to a previous screen while waiting for the connection result
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConnectionViewModel.class);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, mBackPressedCallback);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentConnectionBinding.inflate(inflater);
        subscribeUI();
        initConnection();
        binding.back.setOnClickListener(view -> navigateBack());
        return binding.getRoot();
    }

    private void subscribeUI() {
        viewModel.setObservers(getViewLifecycleOwner(), this::updateData, this::onEvent);
    }

    private void updateData(ConnectionViewData data) {
        binding.setConnection(data);
    }

    private void onEvent(NavigationEvent event) {
        if (event.getType() == NavigationEventType.NAVIGATE_TO_INFO) {
            Navigator.navigate(binding.getRoot(), ConnectionFragmentDirections.toInformation());
        }
    }

    private void initConnection() {
        Bundle fragmentArgs = getArguments();
        Device device = fragmentArgs == null ? null : ConnectionFragmentArgs.fromBundle(getArguments()).getDevice();
        viewModel.connect(device);
    }

    private void navigateBack() {
        Navigator.navigate(binding.getRoot(), ConnectionFragmentDirections.back());
    }

}
