/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.content.Context;
import android.util.AttributeSet;

import androidx.databinding.DataBindingUtil;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.databinding.PreferenceGainBinding;

public class GainPreference extends Preference {

    PreferenceGainBinding binding;

    private GainPreferenceViewData data;

    public GainPreference(Context context) {
        super(context);
        init();
    }

    public GainPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GainPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public GainPreference(Context context, AttributeSet attrs, int defStyleAttr,
                          int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        binding = DataBindingUtil.bind(holder.itemView);
        binding.setData(data);
    }

    public void setData(GainPreferenceViewData data) {
        this.data = data;
        if (binding != null) {
            binding.setData(data);
        }
    }

    private void init() {
        setLayoutResource(R.layout.preference_gain);
    }

}
