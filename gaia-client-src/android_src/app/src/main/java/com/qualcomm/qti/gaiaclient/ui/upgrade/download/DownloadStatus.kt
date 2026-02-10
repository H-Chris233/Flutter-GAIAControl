/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.upgrade.download

import android.content.Context

sealed class DownloadStatus(
    val title: String,
    val content: String,
    val success: Boolean,
    val error: Boolean,
    val actions: Actions,
    val progress: Int? = null,
    val refs: String? = null
) {
    class Progress(title: String, progress: Int) :
        DownloadStatus(title, "", false, false, Actions(), progress = progress)

    class Complete(context: Context, title: String) :
        DownloadStatus(
            title,
            "",
            true,
            false,
            Actions(Action.StartUpgrade(context))
        )

    class Error(context: Context, title: String, content: String, refs: String) :
        DownloadStatus(
            title,
            content,
            false,
            true,
            Actions(secondary = Action.TryAgain(context)),
            refs = refs
        )
}
