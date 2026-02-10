/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.gestures;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.databinding.FragmentGesturesBinding;
import com.qualcomm.qti.gaiaclient.ui.Navigator;
import com.qualcomm.qti.gaiaclient.utils.Event;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GesturesFragment extends Fragment {

    private GesturesViewModel viewModel;

    private FragmentGesturesBinding binding;

    private boolean isFeatureSupported = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(GesturesViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentGesturesBinding.inflate(inflater, container, false);

        GesturesAdapter adapter = new GesturesAdapter(this::onGestureSelected);
        adapter.setIsClickable(isFeatureSupported);
        binding.gesturesList.setAdapter(adapter);
        subscribeUI(adapter);

        binding.resetButton.setOnClickListener((view) -> viewModel.resetToDefaultRequest());

        return binding.getRoot();
    }

    private void subscribeUI(GesturesAdapter adapter) {
        LifecycleOwner owner = getViewLifecycleOwner();
        viewModel.observeGestures(owner, (list) -> updateList(adapter, list));
        viewModel.observeFeatureSupported(owner, this::updateListVisibility);
        viewModel.getResetToDefaultWarningLiveData().observe(owner, this::showResetToDefaultWarning);
    }

    private void updateListVisibility(boolean supported) {
        // Save the value to also update the list items when the list changes (e.g. updateList()).
        isFeatureSupported = supported;
        if (binding.gesturesList.getAdapter() != null) {
            ((GesturesAdapter) binding.gesturesList.getAdapter()).setIsClickable(supported);
        }

        for (int i = 0; i < binding.gesturesList.getChildCount(); i++) {
            binding.gesturesList.getChildAt(i).setClickable(supported);
        }

        if (supported) {
            binding.gesturesList.setAlpha(1.0f);
        }
        else {
            binding.gesturesList.setAlpha(0.5f);
        }
    }

    private void updateList(GesturesAdapter adapter, List<GestureViewData> list) {
        adapter.submitList(list);
        binding.resetButton.setVisibility(list == null || list.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onGestureSelected(Gesture gesture) {
        Navigator.navigate(binding.getRoot(), GesturesFragmentDirections.toGestureConfiguration(gesture));
    }

    private void showResetToDefaultWarning(Event<Void> event) {
        if (event == null || event.isConsumed()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.gesture_override_warning_title);
        builder.setMessage(R.string.gesture_override_other_context_warning_message);
        builder.setCancelable(true); // the user can cancel the dialog with native back button
        builder.setPositiveButton(R.string.button_reset,
                                  (dialogInterface, i) -> {
                                      viewModel.resetToDefault();
                                  });
        builder.setNegativeButton(R.string.button_cancel, (dialog, which) -> {});
        builder.setOnDismissListener(dialog -> event.consume());
        builder.show();
    }
}
