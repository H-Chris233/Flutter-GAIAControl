/*
 * ************************************************************************************************
 * * © 2020 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.utils;

import android.content.Context;

public final class SystemUtils {

    public static boolean isAndroidDeveloperModeOn(Context context) {
        String DEVELOPMENT_SETTINGS_ENABLED =
                android.provider.Settings.Global.DEVELOPMENT_SETTINGS_ENABLED;
        return context != null
                && android.provider.Settings.Global.getInt(context.getContentResolver(),
                                                           DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
    }
}
