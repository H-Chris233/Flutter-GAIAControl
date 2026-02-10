/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.download

import android.content.Context
import androidx.annotation.StringRes
import com.qualcomm.qti.gaiaclient.R

sealed class Action(context: Context, @StringRes val labelId: Int) {
    val label: String = context.getString(labelId)

    class StartUpgrade(context: Context) : Action(context, R.string.button_start_upgrade)
    class TryAgain(context: Context) : Action(context, R.string.button_try_again)
}

data class Actions(val primary: Action? = null, val secondary: Action? = null)
