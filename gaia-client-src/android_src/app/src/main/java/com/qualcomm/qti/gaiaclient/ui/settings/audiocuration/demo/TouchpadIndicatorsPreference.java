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

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.databinding.PreferenceTouchpadIndicatorsBinding;

public class TouchpadIndicatorsPreference extends Preference {

    private static final String TAG = "TouchpadIndicatorsPreference";

    PreferenceTouchpadIndicatorsBinding binding;

    private final ViewData data = new ViewData();

    public TouchpadIndicatorsPreference(Context context) {
        super(context);
        init();
    }

    public TouchpadIndicatorsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TouchpadIndicatorsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TouchpadIndicatorsPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                        int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        binding = DataBindingUtil.bind(holder.itemView);

        if (binding == null) {
            Log.w(TAG, "[onBindViewHolder] Unexpected: binding is null.");
            return;
        }

        updateLeftTouchpadView(data.leftTouchpadData);
        updateRightTouchpadView(data.rightTouchpadData);
    }

    public void setLeftTouchpadData(TouchpadViewData data) {
        this.data.leftTouchpadData = data;
        updateLeftTouchpadView(data);
    }

    public void setRightTouchpadData(TouchpadViewData data) {
        this.data.rightTouchpadData = data;
        updateRightTouchpadView(data);
    }

    private void updateLeftTouchpadView(TouchpadViewData data) {
        if (data != null && binding != null) {
            binding.leftTouchpad.setActivated(data.isActivated());
            binding.setLeftTouchpadData(data);
        }
    }

    private void updateRightTouchpadView(TouchpadViewData data) {
        if (data != null && binding != null) {
            binding.rightTouchpad.setActivated(data.isActivated());
            binding.setRightTouchpadData(data);
        }
    }

    private void init() {
        setLayoutResource(R.layout.preference_touchpad_indicators);
    }

    private static class ViewData {
        private TouchpadViewData leftTouchpadData = null;
        private TouchpadViewData rightTouchpadData = null;
    }

}
