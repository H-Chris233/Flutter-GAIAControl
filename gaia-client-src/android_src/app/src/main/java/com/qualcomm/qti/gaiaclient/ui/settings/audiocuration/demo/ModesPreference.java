/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.content.Context;
import android.util.AttributeSet;

import androidx.databinding.DataBindingUtil;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.GridLayoutManager;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.databinding.PreferenceModesBinding;
import com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.ModeViewData;

import java.util.List;

public class ModesPreference extends Preference {

    private static final int COLUMNS_COUNT = 3;

    private ModesListener listener;

    private final ModesAdapter adapter = new ModesAdapter((mode) -> {
        if (listener != null && isEnabled()) {
            listener.onSelected(mode);
        }
    });

    public ModesPreference(Context context) {
        super(context);
        init();
    }

    public ModesPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ModesPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ModesPreference(Context context, AttributeSet attrs, int defStyleAttr,
                           int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        PreferenceModesBinding binding = DataBindingUtil.bind(holder.itemView);

        if (binding != null) {
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), COLUMNS_COUNT);
            binding.modesList.setLayoutManager(layoutManager);
            binding.modesList.setAdapter(adapter);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        adapter.setEnabled(enabled);
    }

    public void setListener(ModesListener listener) {
        this.listener = listener;
    }

    /**
     * @param mode
     *         modes starts at 1.
     */
    public void setSelected(ModeViewData mode) {
        adapter.setSelectedMode(mode);

    }

    public void updateList(List<ModeViewData> modes) {
        adapter.updateList(modes);
    }

    private void init() {
        setLayoutResource(R.layout.preference_modes);
    }

    public interface ModesListener {
        void onSelected(ModeViewData mode);
    }

}
