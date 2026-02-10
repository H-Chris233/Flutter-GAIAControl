/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.qualcomm.qti.gaiaclient.R;

public class SecondaryButtonPreference extends Preference {

    public SecondaryButtonPreference(Context context) {
        super(context);
        init();
    }

    public SecondaryButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SecondaryButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SecondaryButtonPreference(Context context, AttributeSet attrs, int defStyleAttr,
                                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View view = holder.itemView.findViewById(R.id.preference_secondary_button_id);
        Button button = (Button) view;
        button.setText(getTitle());
    }

    private void init() {
        setLayoutResource(R.layout.preference_secondary_button);
    }
}
