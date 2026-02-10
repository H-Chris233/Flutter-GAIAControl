/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo;

import android.content.Context;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.LeftRightBalance;

final class LabelProvider {

    public static String getForLeakthroughGainStepper(Context context, int minValue, int stepSizeInDb, int stepValue) {
        int currentValueInDb = minValue + (stepValue - 1) * stepSizeInDb;
        return context == null ? "" : context.getString(R.string.audio_curation_gain_in_db, currentValueInDb);
    }

    public static String getForLeftRightBalance(Context context, LeftRightBalance balance) {
        int resourceId = balance.getPosition() == EarbudPosition.LEFT ? R.string.audio_curation_balance_left
                                                                      : R.string.audio_curation_balance_right;
        return context == null ? "" : context.getString(resourceId, balance.getGain());
    }

    public static String getAlteredLabelForLeftRightBalance(Context context, int value) {
        int resourceId = value < 0 ? R.string.audio_curation_balance_left : R.string.audio_curation_balance_right;
        int displayValue = Math.abs(value);
        displayValue =
                displayValue <= 2 ? 0 : displayValue; // ignoring values of 1 and 2 to help setting the value to 0
        return context == null ? "" : context.getString(resourceId, displayValue);
    }
}
