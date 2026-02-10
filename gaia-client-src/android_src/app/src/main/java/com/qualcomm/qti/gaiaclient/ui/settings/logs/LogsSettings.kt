/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.logs

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.settings.common.Settings

enum class LogsSettings(override val resourceId: Int) : Settings {
    LOGS_SIZES(R.string.settings_id_logs_sizes),
    BUG_REPORT(R.string.settings_id_logs_bug_report),
    DEVICE_LOGS(R.string.settings_id_logs_device_logs);
}
