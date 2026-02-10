/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration;

import android.content.Context;

import androidx.annotation.NonNull;

import com.qualcomm.qti.gaiaclient.R;

import java.util.Objects;

public class ModeViewData {

    private final int value;

    @NonNull
    private final String label;

    public ModeViewData(Context context, int value) {
        this(value, context.getString(R.string.audio_curation_mode_label, value));
    }

    public ModeViewData(int value, @NonNull String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    @NonNull
    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ModeViewData that = (ModeViewData) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, label);
    }
}
