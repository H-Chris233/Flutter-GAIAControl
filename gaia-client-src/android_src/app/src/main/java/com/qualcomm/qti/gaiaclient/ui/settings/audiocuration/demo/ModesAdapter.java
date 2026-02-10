/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.qualcomm.qti.gaiaclient.databinding.ModeItemBinding;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData;

import java.util.ArrayList;
import java.util.List;

public class ModesAdapter extends RecyclerView.Adapter<ModesAdapter.ViewHolder> {

    private final ClickCallback callback;

    private ModeViewData selectedMode = null;

    @NonNull
    private List<ModeViewData> modes = new ArrayList<>();

    private boolean enabled = true;

    protected ModesAdapter(ClickCallback callback) {
        super();
        this.callback = callback;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        notifyDataSetChanged();
    }


    public void updateList(List<ModeViewData> update) {
        modes = update == null ? new ArrayList<>() : update;
        notifyDataSetChanged();
    }

    public void setSelectedMode(ModeViewData mode) {
        // getting indexes that have changed
        int oldIndex = getIndex(selectedMode);
        int newIndex = getIndex(mode);

        // updating value
        this.selectedMode = mode;

        // notify changes
        notifyChange(oldIndex);
        notifyChange(newIndex);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ModeItemBinding itemBinding = ModeItemBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        itemBinding.getRoot().setOnClickListener((view) -> {
            ModeItemViewData data = itemBinding.getData();
            if (enabled && data != null && callback != null) {
                callback.onSelected(data.getMode());
            }
        });
        return new ViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModeViewData mode = modes.get(position);
        if (mode != null) {
            ModeItemViewData data = new ModeItemViewData(mode, mode.equals(selectedMode), enabled);
            holder.bind(data);
        }
    }

    @Override
    public int getItemCount() {
        return modes.size();
    }

    private void notifyChange(int item) {
        int count = modes != null ? modes.size() : 0;
        if (0 <= item && item < count) {
            notifyItemChanged(item);
        }
    }

    private int getIndex(ModeViewData mode) {
        return mode == null ? -1 : mode.getValue() - 1; // lists start at 0, modes start at 1
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ModeItemBinding mmBinding;

        public ViewHolder(ModeItemBinding binding) {
            super(binding.getRoot());
            mmBinding = binding;
        }

        public void bind(ModeItemViewData data) {
            mmBinding.setData(data);
            mmBinding.getRoot().setActivated(data.isSelected()); // applies to children views
            mmBinding.getRoot().setEnabled(data.isEnabled()); // do not apply to children views
        }
    }

    public interface ClickCallback {
        void onSelected(ModeViewData mode);
    }

}
