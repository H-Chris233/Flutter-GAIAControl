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
import com.qualcomm.qti.gaiaclient.databinding.ActionItemBinding;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterDataItemCallback;

public class ActionsAdapter extends ListAdapter<ActionViewData, ActionsAdapter.ActionViewHolder> {

    private final ActionClickCallback itemClickCallback;

    public ActionsAdapter(ActionClickCallback clickCallback) {
        super(new ListAdapterDataItemCallback<>());
        itemClickCallback = clickCallback;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ActionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ActionItemBinding binding = ActionItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ActionViewHolder(binding, itemClickCallback);
    }

    @Override
    public int getItemViewType(int position) {
        return 0; // all the same - or should we have a "nothing" type?
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull ActionViewHolder holder, int position) {
        ActionViewData item = getItem(position);
        holder.bind(item);
    }

    public static class ActionViewHolder extends RecyclerView.ViewHolder {

        private final ActionItemBinding binding;

        ActionViewHolder(ActionItemBinding binding, ActionClickCallback callback) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.leftTouchpadTouchArea.setOnClickListener(view -> dispatchEvent(callback, TouchpadViewType.LEFT));
            this.binding.rightTouchpadTouchArea.setOnClickListener(view -> dispatchEvent(callback, TouchpadViewType.RIGHT));
            this.binding.tickTouchpadTouchArea.setOnClickListener(view -> dispatchEvent(callback, TouchpadViewType.TICK));
            this.binding.getRoot().setOnClickListener(view -> {
                ActionViewData data = binding.getData();
                if (data != null && data.isRowSelectable()) {
                    dispatchEvent(callback, TouchpadViewType.TICK);
                }
            });
        }

        void bind(ActionViewData viewData) {
            binding.setData(viewData);
            binding.leftTouchpad.setActivated(viewData.getLeftTouchpad().isSelected());
            binding.rightTouchpad.setActivated(viewData.getRightTouchpad().isSelected());
        }

        private void dispatchEvent(ActionClickCallback callback, TouchpadViewType touchpadType) {
            ActionViewData data = binding.getData();
            if (data != null && callback != null) {
                callback.onClick(data.getAction(), touchpadType);
            }
        }
    }

    public interface ActionClickCallback {

        void onClick(Action action, TouchpadViewType touchpadType);
    }
}
