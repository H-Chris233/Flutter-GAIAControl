/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.content.Context
import androidx.annotation.StringRes
import com.qualcomm.qti.gaiaclient.R

sealed class Action(val context: Context, @StringRes val labelId: Int) {
    val label: String = context.getString(labelId)

    class CopyLink(context: Context) : Action(context, R.string.button_copy_link)
    class CopyFeedback(context: Context) : Action(context, R.string.button_copy_data)
    class Done(context: Context) : Action(context, R.string.button_done)
    class TryAgain(context: Context) : Action(context, R.string.button_try_again)
}

data class Actions(val primary: Action? = null, val secondary: Action? = null)
