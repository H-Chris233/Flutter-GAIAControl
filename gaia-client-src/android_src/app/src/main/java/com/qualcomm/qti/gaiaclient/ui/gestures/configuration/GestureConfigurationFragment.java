/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.databinding.FragmentGestureConfigurationBinding;
import com.qualcomm.qti.gaiaclient.ui.MainActivity;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GestureConfigurationFragment extends Fragment {

    private GestureConfigurationViewModel viewModel;

    private TabLayout tabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GestureConfigurationViewModel.class);

        Bundle fragmentArgs = getArguments();
        if (fragmentArgs != null) {
            Gesture gesture = GestureConfigurationFragmentArgs.fromBundle(fragmentArgs).getGesture();
            viewModel.setGesture(gesture);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        com.qualcomm.qti.gaiaclient.databinding.FragmentGestureConfigurationBinding binding =
                FragmentGestureConfigurationBinding.inflate(inflater, container, false);

        initBindings(binding);
        initObservations();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initBindings(FragmentGestureConfigurationBinding binding) {
        GestureConfigurationPageViewerAdapter adapter =
                new GestureConfigurationPageViewerAdapter(this, viewModel.getGesture());
        binding.viewPager.setAdapter(adapter);
        new TabLayoutMediator(binding.tabLayout, binding.viewPager,
                              (tab, position) -> tab.setText(getTabTitle(position))).attach();
        tabLayout = binding.tabLayout;
    }

    private void initObservations() {
        viewModel.observeTitle(getViewLifecycleOwner(), this::updateTitle);
        viewModel.observeFeatureSupported(getViewLifecycleOwner(), this::updateVisibility);
    }

    private void updateVisibility(boolean visible) {
        if (tabLayout == null) {
            return;
        }
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.view.setEnabled(visible);
                if (visible) {
                    tab.view.setAlpha(1.0f);
                }
                else {
                    tab.view.setAlpha(0.5f);
                }
            }
        }
    }

    private void updateTitle(String title) {
        Activity activity = getActivity();

        if (activity instanceof MainActivity && title != null && !title.isEmpty()) {
            ((MainActivity) activity).updateTitle(title);
        }
    }

    private String getTabTitle(int position) {
        TabContexts context = TabContexts.valueOf(position);
        Context appContext = getContext();
        return context != null ? context.getLabel(appContext) : "";
    }

}
