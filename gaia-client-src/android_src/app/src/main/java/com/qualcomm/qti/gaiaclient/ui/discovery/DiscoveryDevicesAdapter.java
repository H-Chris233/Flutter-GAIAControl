/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.discovery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.qualcomm.qti.gaiaclient.databinding.DeviceItemBinding;
import com.qualcomm.qti.gaiaclient.databinding.TitleItemBinding;
import com.qualcomm.qti.gaiaclient.ui.common.ListAdapterDataItemCallback;
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.DeviceViewData;
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.ItemViewData;
import com.qualcomm.qti.gaiaclient.ui.discovery.viewdata.TitleViewData;

class DiscoveryDevicesAdapter extends ListAdapter<ItemViewData, RecyclerView.ViewHolder> {

    private final DeviceClickCallback mDeviceClickCallback;

    public DiscoveryDevicesAdapter(DeviceClickCallback clickCallback) {
        super(new ListAdapterDataItemCallback<>());
        mDeviceClickCallback = clickCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                      @ItemViewData.ItemType int viewType) {
        switch (viewType) {
            case ItemViewData.ItemType.DEVICE:
                DeviceItemBinding deviceItemBinding = DeviceItemBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new DeviceViewHolder(deviceItemBinding, mDeviceClickCallback);

            case ItemViewData.ItemType.TITLE:
            default:
                TitleItemBinding titleItemBinding = TitleItemBinding
                        .inflate(LayoutInflater.from(parent.getContext()), parent, false);
                return new TitleViewHolder(titleItemBinding);
        }
    }

    @Override
    @ItemViewData.ItemType
    public int getItemViewType(int position) {
        ItemViewData item = getItem(position);
        return item.getType();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ItemViewData item = getItem(position);

        switch (item.getType()) {
            case ItemViewData.ItemType.TITLE:
                ((TitleViewHolder) holder).bind((TitleViewData) item);
                break;

            case ItemViewData.ItemType.DEVICE:
                ((DeviceViewHolder) holder).bind((DeviceViewData) item);
                break;
        }
    }

    private static class TitleViewHolder extends RecyclerView.ViewHolder {

        private final TitleItemBinding mmBinding;

        public TitleViewHolder(@NonNull TitleItemBinding binding) {
            super(binding.getRoot());
            mmBinding = binding;
        }

        void bind(TitleViewData data) {
            mmBinding.setData(data);
        }
    }

    private static class DeviceViewHolder extends RecyclerView.ViewHolder {

        private final DeviceItemBinding mmBinding;

        DeviceViewHolder(DeviceItemBinding binding, DeviceClickCallback callback) {
            super(binding.getRoot());
            mmBinding = binding;
            mmBinding.getRoot().setOnClickListener(v -> {
                DeviceViewData deviceData = binding.getDevice();
                if (deviceData != null && callback != null) {
                    callback.onClick(deviceData);
                }
            });
        }

        void bind(DeviceViewData device) {
            mmBinding.setDevice(device);
        }
    }

    public interface DeviceClickCallback {

        void onClick(DeviceViewData device);
    }
}
