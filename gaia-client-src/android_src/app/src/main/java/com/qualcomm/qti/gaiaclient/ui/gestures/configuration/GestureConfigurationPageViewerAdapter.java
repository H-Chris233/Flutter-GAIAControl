/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.gestures.configuration;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.gestures.Gesture;

public class GestureConfigurationPageViewerAdapter extends FragmentStateAdapter {

    private final Gesture gesture;

    public GestureConfigurationPageViewerAdapter(@NonNull Fragment fragment, Gesture gesture) {
        super(fragment);
        this.gesture = gesture;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ConfigurationListFragment.newInstance(gesture, position);
    }

    @Override
    public int getItemCount() {
        return TabContexts.size();
    }
}
