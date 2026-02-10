/*
 * ************************************************************************************************
 * * © 2021-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.databinding.DataBindingUtil;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.slider.LabelFormatter;
import com.google.android.material.slider.Slider;
import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.databinding.PreferenceDiscreteSliderBinding;

public class DiscreteSliderPreference extends Preference {

    private static final String TAG = "DiscreteSliderPreference";

    PreferenceDiscreteSliderBinding binding;

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

    public DiscreteSliderPreference(Context context) {
        super(context);
        init();
    }

    public DiscreteSliderPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DiscreteSliderPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public DiscreteSliderPreference(Context context, AttributeSet attrs, int defStyleAttr,
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

        updateSlider();

        if (data.label != null) {
            binding.setLabelForValue(data.label);
        }
        if (data.formatter != null) {
            binding.slider.setLabelFormatter(data.formatter);
        }

        // removing listener if onBindViewHolder is called multiple times
        binding.slider.removeOnSliderTouchListener(onSliderTouchListener);
        // binding the listener
        binding.slider.addOnSliderTouchListener(onSliderTouchListener);
    }

    public void setMinValue(int minValue) {
        this.data.minValue = minValue;
        updateSlider();
    }

    public void setMaxValue(int maxValue) {
        this.data.maxValue = maxValue;
        updateSlider();
    }

    public void setValue(int value) {
        this.data.value = value;
        updateSlider();
    }

    public void setStepSize(int stepSize) {
        this.data.stepSize = stepSize;
        updateSlider();
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
        setLayoutResource(R.layout.preference_discrete_slider);
    }

    private void updateSlider() {
        if (binding == null) {
            return;
        }

        // getting data values
        int min = data.minValue == null ? 1 : data.minValue; // considered as starting point
        int stepData = data.stepSize == null ? 1 : data.stepSize;
        int maxData = data.maxValue == null ? 2 : data.maxValue;
        int valueData = data.value == null ? 1 : data.value;

        // slider values to be conformed with Slider expectations

        // step must be positive
        int step = Math.max(stepData, 1);

        // max must be higher than min and a multiple of step
        int max = maxData < min ? min + step : roundAsMultipleOfStep(maxData, min, step);


        // value must be between min and max and a multiple of step
        int value = Math.max(valueData, min);
        value = roundAsMultipleOfStep(value, min, step);
        value = Math.min(value, max);

        binding.setMinValue(min);
        binding.setStepSize(step);
        binding.setMaxValue(max);
        binding.setValue(value);
    }

    @VisibleForTesting
    protected static int roundAsMultipleOfStep(int value, int start, int step) {
        if (step <= 0 || value < start) {
            return start;
        }

        int remainder = (value - start) % step;
        int quotient = (value - start) / step;
        return remainder == 0 ? value : (quotient + 1) * step + start;
    }

    private static class ViewData {
        private Integer minValue = null;
        private Integer maxValue = null;
        private Integer stepSize = null;
        private Integer value = null;
        private String label = null;
        private LabelFormatter formatter = null;
    }

    public interface SliderListener {
        void onProgressChanged(float progress);
    }

}
