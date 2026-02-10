/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.gestures;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;
import com.qualcomm.qti.gaiaclient.databinding.GestureItemBinding;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterDataItemCallback;
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.ItemViewData;

class GesturesAdapter extends ListAdapter<GestureViewData, GesturesAdapter.GestureViewHolder> {

    private final GestureClickCallback clickCallback;

    private boolean isClickable;

    public GesturesAdapter(GestureClickCallback clickCallback) {
        super(new ListAdapterDataItemCallback<>());
        this.clickCallback = clickCallback;
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public GestureViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                @ItemViewData.ItemType int viewType) {
        GestureItemBinding binding = GestureItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new GestureViewHolder(binding, clickCallback, isClickable);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).hashCode();
    }

    @Override
    public void onBindViewHolder(@NonNull GestureViewHolder holder, int position) {
        GestureViewData item = getItem(position);
        holder.bind(item);
    }

    protected static class GestureViewHolder extends RecyclerView.ViewHolder {

        private final GestureItemBinding binding;

        GestureViewHolder(GestureItemBinding binding, GestureClickCallback callback, boolean clickable) {
            super(binding.getRoot());
            this.binding = binding;
            this.binding.getRoot().setOnClickListener(v -> {
                GestureViewData gesture = binding.getGesture();
                if (gesture != null && callback != null) {
                    callback.onClick(gesture.getGesture());
                }
            });
            this.binding.getRoot().setClickable(clickable);
        }

        void bind(GestureViewData gesture) {
            binding.setGesture(gesture);
        }
    }

    public void setIsClickable(boolean clickable) {
        this.isClickable = clickable;
    }

    public interface GestureClickCallback {

        void onClick(Gesture gesture);
    }
}
