/*
 * ************************************************************************************************
 * * © 2021-2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.utils;

import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

public final class SystemUtils {

    /**
     * <p>This method checks if all the permissions are granted.</p>
     *
     * @return true if all permissions are granted, false if at least one of them is not.
     */
    public static boolean arePermissionsGranted(@NonNull Context context, @NonNull String[] permissions) {
        for (String wanted : permissions) {
            if (ActivityCompat.checkSelfPermission(context, wanted) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    /**
     * <p>This method checks if the given permission is granted for the application.</p>
     *
     * @return true if the permissions is granted, false otherwise.
     */
    public static boolean isPermissionsGranted(@NonNull Context context, @NonNull String permission) {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
