/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Configuration;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.databinding.FragmentConfigurationListBinding;
import com.qualcomm.qti.gaiaclient.utils.Event;

import java.util.Set;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ConfigurationListFragment extends Fragment {

    private static final String TAB_CONTEXT_ORDINAL_KEY = "TAB_CONTEXT_ORDINAL_KEY";

    private static final String GESTURE_KEY = "GESTURE_KEY";

    private ConfigurationListViewModel viewModel;

    private FragmentConfigurationListBinding binding;

    public static ConfigurationListFragment newInstance(Gesture gesture, int index) {
        ConfigurationListFragment fragment = new ConfigurationListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(TAB_CONTEXT_ORDINAL_KEY, index);
        bundle.putParcelable(GESTURE_KEY, gesture);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ConfigurationListViewModel.class);
        if (getArguments() != null) {
            int ordinal = getArguments().getInt(TAB_CONTEXT_ORDINAL_KEY);
            viewModel.setTabContext(TabContexts.valueOf(ordinal));
            viewModel.setGesture(getArguments().getParcelable(GESTURE_KEY));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConfigurationListBinding.inflate(inflater, container, false);

        ContextsAdapter adapter = new ContextsAdapter(new ContextsAdapter.ContextSelectionCallback() {
            @Override
            public void onAction(GestureContext context, Action action, TouchpadViewType touchpadType) {
                viewModel.onSelection(context, action, touchpadType);
            }

            @Override
            public void onSwitch(GestureContext context, boolean enabled) {
                viewModel.onSwitchChecked(context, enabled);
            }
        });
        binding.configurationList.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(binding.configurationList.getContext());
        binding.configurationList.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(binding.configurationList.getContext(),
                                                                                layoutManager.getOrientation());
        binding.configurationList.addItemDecoration(dividerItemDecoration);

        subscribeUI(adapter);

        ActionBar appBar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        if (appBar != null) {
            // removing the elevation for the tab layout
            appBar.setElevation(0);
        }

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void subscribeUI(ContextsAdapter adapter) {
        LifecycleOwner owner = getViewLifecycleOwner();
        viewModel.observeContextViewDataList(owner, adapter::submitList);
        viewModel.getShowOverrideOtherDialogEvent().observe(owner, this::showOverrideOtherWarningDialog);
        viewModel.getShowOverrideGlobalDialogEvent().observe(owner, this::showOverrideGlobalWarningDialog);
    }

    /**
     * Dialog for overriding existing context specific action with a global action.
     */
    private void showOverrideOtherWarningDialog(Event<Set<Configuration>> event) {
        if (event == null || event.isConsumed()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.gesture_override_warning_title);
        builder.setMessage(R.string.gesture_override_other_context_warning_message);
        builder.setCancelable(false); // the user cannot cancel the dialog with native back button
        builder.setPositiveButton(R.string.button_override,
                                  (dialogInterface, i) -> {
                                      // when the user taps "OK", request the permissions
                                      viewModel.overrideDialogAccepted(event.getData());
                                  });
        builder.setNegativeButton(R.string.button_cancel, ((dialog, which) -> {}));
        builder.setOnDismissListener(dialog -> event.consume());
        builder.show();
    }

    /**
     * Dialog for overriding existing global action with a context specific action.
     */
    private void showOverrideGlobalWarningDialog(Event<Set<Configuration>> event) {
        if (event == null || event.isConsumed()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.gesture_override_warning_title);
        builder.setMessage(R.string.gesture_override_global_warning_message);
        builder.setCancelable(false); // the user cannot cancel the dialog with native back button
        builder.setPositiveButton(R.string.button_override,
                                  (dialogInterface, i) -> {
                                      // when the user taps "OK", request the permissions
                                      viewModel.overrideDialogAccepted(event.getData());
                                  });
        builder.setNegativeButton(R.string.button_cancel, ((dialog, which) -> {}));
        builder.setOnDismissListener(dialog -> event.consume());
        builder.show();
    }
}
