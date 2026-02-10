/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.voiceprocessing;

import android.content.Context;

import androidx.core.util.Pair;

import com.qualcomm.qti.gaiaclient.R;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCOperationMode;
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.CVCBypassMode;

final class LabelProvider {

    static String getForOperationMode(Context context, CVCOperationMode mode) {
        if (context == null) {
            return null;
        }

        if (mode == null) {
            return context.getString(R.string.voice_processing_unsupported_value);
        }

        switch (mode) {
            case MODE_2MIC:
                return context.getString(R.string.voice_processing_operation_mode_2mic);
            case MODE_3MIC:
                return context.getString(R.string.voice_processing_operation_mode_3mic);
            default:
                return context.getString(R.string.voice_processing_unsupported_value);
        }
    }

    static String getForBypassMode(Context context, CVCBypassMode mode) {
        if (context == null) {
            return null;
        }

        if (mode == null) {
            return context.getString(R.string.voice_processing_unsupported_value);
        }

        switch (mode) {
            case BYPASS_VOICE:
                return context.getString(R.string.voice_processing_bypass_voice);
            case BYPASS_EXTERNAL:
                return context.getString(R.string.voice_processing_bypass_external);
            case BYPASS_INTERNAL:
                return context.getString(R.string.voice_processing_bypass_internal);
            default:
                return context.getString(R.string.voice_processing_unsupported_value);
        }
    }

    static String getForMicrophoneMode(Context context, int mode) {
        if (mode == 0) {
            return context == null ? null : context.getString(R.string.voice_processing_microphone_mode_bypass);
        }
        else if (mode > 0) {
            return context == null ? null : context.getResources()
                    .getQuantityString(R.plurals.voice_processing_microphones, mode, mode);
        }
        else {
            return context.getString(R.string.voice_processing_unsupported_value);
        }
    }

    public static Pair<String[], String[]> getEntriesForMicrophoneMode(Context context, int count) {
        String[] entries = new String[count];
        String[] values = new String[count];

        for (int i = 0; i < count; i++) {
            entries[i] = getForMicrophoneMode(context, i);
            values[i] = Integer.toString(i);
        }

        return new Pair<>(entries, values);
    }

    public static Pair<String[], String[]> getEntriesForBypassModes(Context context) {
        CVCBypassMode[] modes = CVCBypassMode.values();

        String[] entries = new String[modes.length];
        String[] values = new String[modes.length];

        for (int i = 0; i < modes.length; i++) {
            CVCBypassMode mode = modes[i];
            entries[i] = getForBypassMode(context, mode);
            values[i] = mode.toString();
        }

        return new Pair<>(entries, values);
    }

}
