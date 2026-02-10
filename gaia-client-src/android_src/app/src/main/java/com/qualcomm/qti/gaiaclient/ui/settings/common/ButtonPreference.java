/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.common;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.qualcomm.qti.gaiaclient.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

public class ButtonPreference extends Preference {

    public ButtonPreference(Context context) {
        super(context);
        init();
    }

    public ButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ButtonPreference(Context context, AttributeSet attrs, int defStyleAttr,
                            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View view = holder.itemView.findViewById(R.id.preference_button_id);
        Button button = (Button) view;
        button.setText(getTitle());
    }

    private void init() {
        setLayoutResource(R.layout.preference_button);
    }

}
