/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.repository.service.feedback.Issue

sealed class FeedbackStatus(
    val title: String,
    val content: String,
    val progress: Boolean,
    val success: Boolean,
    val error: Boolean,
    val actions: Actions,
    val refs: String? = null
) {
    class Progress(title: String) :
        FeedbackStatus(title, "", true, false, false, Actions())

    class Complete(context: Context, issue: Issue) :
        FeedbackStatus(
            context.getString(R.string.feedback_sent),
            context.getString(R.string.feedback_issue_created, issue.id, issue.title),
            false,
            true,
            false,
            Actions(Action.Done(context), Action.CopyLink(context))
        )

    class Error(context: Context, title: String, content: String, refs: String) :
        FeedbackStatus(
            title,
            content,
            false,
            false,
            true,
            Actions(Action.TryAgain(context), Action.CopyFeedback(context)),
            refs
        )
}
