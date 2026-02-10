/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.databinding.PreferenceLeftRightBalanceBinding;

public class LeftRightBalancePreference extends Preference {

    private static final String TAG = "LeftRightBalancePreference";

    PreferenceLeftRightBalanceBinding binding;

    private final ViewData data = new ViewData();

    private SliderListener listener;

    private final Slider.OnSliderTouchListener onSliderTouchListener = new Slider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(@NonNull Slider slider) {
            // nothing to do
        }

        @Override
        public void onStopTrackingTouch(@NonNull Slider slider) {
            if (listener != null) {
                listener.onProgressChanged(slider.getValue());
            }
        }
    };

    public LeftRightBalancePreference(Context context) {
        super(context);
        init();
    }

    public LeftRightBalancePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LeftRightBalancePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public LeftRightBalancePreference(Context context, AttributeSet attrs, int defStyleAttr,
                                      int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        binding = DataBindingUtil.bind(holder.itemView);

        if (binding == null) {
            Log.w(TAG, "[onBindViewHolder] Unexpected: binding is null.");
            return;
        }

        if (data.value != null) {
            binding.setValue(data.value);
        }
        if (data.label != null) {
            binding.setLabelForValue(data.label);
        }
        if (data.formatter != null) {
            binding.slider.setLabelFormatter(data.formatter);
        }

        binding.slider.removeOnSliderTouchListener(onSliderTouchListener); // onBindViewHolder can be called multiple times
        binding.slider.addOnSliderTouchListener(onSliderTouchListener);
    }

    public void setValue(int value) {
        this.data.value = value;
        if (binding != null) {
            binding.setValue(value);
        }
    }

    public void setLabel(String label) {
        this.data.label = label;
        if (binding != null) {
            binding.setLabelForValue(label);
        }
    }

    public void setLabelFormatter(LabelFormatter formatter) {
        this.data.formatter = formatter;
        if (binding != null && formatter != null) {
            binding.slider.setLabelFormatter(formatter);
        }
    }

    public void setListener(SliderListener listener) {
        this.listener = listener;
    }

    private void init() {
        setLayoutResource(R.layout.preference_left_right_balance);
    }

    private static class ViewData {
        private Integer value = null;
        private String label = null;
        private LabelFormatter formatter = null;
    }

    public interface SliderListener {
        void onProgressChanged(float progress);
    }

}
