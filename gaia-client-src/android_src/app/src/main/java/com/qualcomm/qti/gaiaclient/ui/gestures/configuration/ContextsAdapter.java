/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Action;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.GestureContext;
import com.qualcomm.qti.gaiaclient.databinding.GestureContextItemBinding;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterDataItemCallback;

public class ContextsAdapter extends ListAdapter<ContextViewData, ContextsAdapter.ContextViewHolder> {

    private static final int DEFAULT_ITEM_TYPE = 0;

    @NonNull
    private final ContextSelectionCallback callback;

    public ContextsAdapter(@NonNull ContextSelectionCallback callback) {
        super(new ListAdapterDataItemCallback<>());
        setHasStableIds(true);
        this.callback = callback;
    }

    @NonNull
    @Override
    public ContextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        GestureContextItemBinding binding = GestureContextItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ContextViewHolder(binding);
    }

    @Override
    public int getItemViewType(int position) {
        return DEFAULT_ITEM_TYPE; // all items have the same view
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull ContextViewHolder holder, int position) {
        ContextViewData item = getItem(position);
        holder.bind(callback, item);
    }

    public static class ContextViewHolder extends RecyclerView.ViewHolder {

        private final GestureContextItemBinding binding;

        ContextViewHolder(GestureContextItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(@NonNull ContextSelectionCallback callback, ContextViewData viewData) {
            binding.setViewData(viewData);

            GestureContext context = viewData.getContext();
            ActionsAdapter adapter = new ActionsAdapter(
                    (action, touchpadType) -> callback.onAction(context, action, touchpadType));
            adapter.submitList(viewData.getActionViewDataList());
            binding.actionsList.setAdapter(adapter);
            binding.selectionTypeSwitch.setOnCheckedChangeListener(
                    (buttonView, isChecked) -> callback.onSwitch(context, isChecked));
        }
    }

    public interface ContextSelectionCallback {

        void onAction(GestureContext context, Action action, TouchpadViewType touchpadType);

        void onSwitch(GestureContext context, boolean enabled);
    }
}
