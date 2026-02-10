/*
 * ************************************************************************************************
 * * © 2020-2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.repository.anclegacy;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.R;

public enum ANCModeLegacy {

    MODE_1(0x00),
    MODE_2(0x01),
    MODE_3(0x02),
    MODE_4(0x03),
    MODE_5(0x04),
    MODE_6(0x05),
    MODE_7(0x06),
    MODE_8(0x07),
    MODE_9(0x08),
    MODE_10(0x09);

    private static final String TAG = "ANCMode";

    private final int value;

    private static final ANCModeLegacy[] VALUES = values();

    ANCModeLegacy(int value) {
        this.value = value;
    }

    public static Pair<String[], String[]> getANCModesEntries(Context context, ANCModeLegacy[] modes) {
        if (modes == null || modes.length == 0) {
            return new Pair<>(new String[0], new String[0]);
        }

        String[] entries = new String[modes.length];
        String[] values = new String[modes.length];

        for (int i = 0; i < modes.length; i++) {
            ANCModeLegacy mode = modes[i];
            entries[i] = mode.getLabel(context);
            values[i] = mode.toString();
        }

        return new Pair<>(entries, values);
    }

    public int getValue() {
        return value;
    }

    public String getLabel(Context context) {
        return context.getString(R.string.settings_legacy_anc_mode_label, (value + 1));
    }

    public static ANCModeLegacy valueOf(int value) {
        for (ANCModeLegacy mode : VALUES) {
            if (mode.value == value) {
                return mode;
            }
        }

        Log.w(TAG, "[valueOf] unknown value for mode, value=" + value);

        return null;
    }

    public static ANCModeLegacy[] getModes(int count) {
        count = count < 0 ? 0 : count > VALUES.length ? VALUES.length : count;
        ANCModeLegacy[] modes = new ANCModeLegacy[count];
        System.arraycopy(VALUES, 0, modes, 0, count);
        return modes;
    }

}
