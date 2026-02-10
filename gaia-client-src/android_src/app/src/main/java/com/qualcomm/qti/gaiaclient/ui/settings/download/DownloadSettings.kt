/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.download

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.settings.common.PersistentSettings

enum class DownloadSettings(override val resourceId: Int, override val isPersistent: Boolean) : PersistentSettings {
    APPLICATION_VERSION(R.string.settings_id_download_application_version, false),
    APPLICATION_BUILD_ID(R.string.settings_id_download_application_build_id, false),
    HARDWARE_VERSION(R.string.settings_id_download_hardware_version, true),
    INCLUDE_TAGS(R.string.settings_id_download_include_tags, true),
    EXCLUDE_TAGS(R.string.settings_id_download_exclude_tags, true),
    CREATED_AFTER(R.string.settings_id_download_created_after, true),
    CONTINUE(R.string.settings_id_download_continue, false);

    companion object {
        @JvmStatic
        val PERSISTENT = values().filter { it.isPersistent }.toTypedArray()

        @JvmStatic
        val NON_PERSISTENT = values().filter { !it.isPersistent }.toTypedArray()
    }
}
