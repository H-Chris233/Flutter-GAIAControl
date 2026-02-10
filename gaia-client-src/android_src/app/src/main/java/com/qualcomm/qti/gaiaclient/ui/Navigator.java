/*
 * ************************************************************************************************
 * * © 2020-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui;

import android.view.View;

import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

public final class Navigator {

    public static void navigate(View view, NavDirections action) {
        try {
            Navigation.findNavController(view).navigate(action);
        }
        catch (Exception ignored) {
            // catch NavController not found when the destination has already changed
            // this can happen when multiple dialogs are displayed during the upgrade process
        }
    }

    public static void navigateBack(View view) {
        try {
            Navigation.findNavController(view).popBackStack();
        }
        catch (Exception ignored) {
        }
    }

}
