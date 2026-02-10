/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.feedback

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.repository.service.feedback.FeedbackParameters
import com.qualcomm.qti.gaiaclient.utils.ifNullOrEmpty

data class FeedbackContent(val parameters: FeedbackParameters) {

    fun title(defaultValue: String): String = parameters.title.ifNullOrEmpty { defaultValue }
    fun description(defaultValue: String): String = parameters.description.ifNullOrEmpty { defaultValue }
    fun reporter(defaultValue: String): String = parameters.reporter.ifNullOrEmpty { defaultValue }

    fun clientAppVersion(defaultValue: String): String = parameters.client.appVersion.ifNullOrEmpty { defaultValue }
    fun clientSystemVersion(defaultValue: String): String =
        parameters.client.systemVersion.ifNullOrEmpty { defaultValue }

    fun clientDevice(defaultValue: String): String = parameters.client.device.ifNullOrEmpty { defaultValue }

    fun deviceHardwareVersion(defaultValue: String): String =
        parameters.device.hardwareVersion.ifNullOrEmpty { defaultValue }

    fun deviceApplicationVersion(defaultValue: String): String =
        parameters.device.applicationVersion.ifNullOrEmpty { defaultValue }

    fun deviceApplicationBuildId(defaultValue: String): String =
        parameters.device.applicationBuildId.ifNullOrEmpty { defaultValue }

    fun toClipboard(context: Context?): String? {
        return context?.getString(
            R.string.feedback_clipboard_content,
            title(""),
            description(""),
            reporter(""),
            deviceHardwareVersion(""),
            context.getString(
                R.string.feedback_client_content,
                clientAppVersion(""),
                clientSystemVersion(""),
                clientDevice("")
            ),
            context.getString(
                R.string.feedback_device_content,
                deviceApplicationVersion(""),
                deviceApplicationBuildId("")
            )
        )
    }
}
