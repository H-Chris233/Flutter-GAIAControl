/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.upgrade

import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.ui.settings.common.PersistentSettings

enum class UpgradeSettings(override val resourceId: Int, override val isPersistent: Boolean, val isTransfer: Boolean) :
    PersistentSettings {
    // non persistent
    APPLICATION_VERSION(R.string.settings_id_upgrade_application_version, false, false),
    APPLICATION_BUILD_ID(R.string.settings_id_upgrade_application_build_id, false, false),
    SELECT_FILE(R.string.settings_id_upgrade_select_file, false, false),
    CHECK_FOR_UPDATES(R.string.settings_id_upgrade_check_for_updates, false, false),
    DEVELOPER_OPTIONS(R.string.settings_id_upgrade_developer_options, false, false),
    GAIA_AND_PROTOCOL_VERSIONS(R.string.settings_id_upgrade_gaia_and_protocol_versions, false, false),
    CHUNK_SIZE(R.string.settings_id_upgrade_chunk_size, false, true),

    // persistent
    PERSISTENT_USE_DEFAULT(R.string.settings_id_upgrade_transfer_default, true, false),
    PERSISTENT_IS_UPLOAD_FLUSHED(R.string.settings_id_upgrade_flush_data, true, true),
    PERSISTENT_IS_UPLOAD_ACKNOWLEDGED(R.string.settings_id_upgrade_upload_acknowledged, true, true),
    PERSISTENT_IS_LOGGED(R.string.settings_id_upgrade_logs, true, false);

    companion object {
        @JvmField
        val PERSISTENT = values().filter { it.isPersistent }.toTypedArray()

        @JvmField
        val NON_PERSISTENT = values().filter { !it.isPersistent }.toTypedArray()

        @JvmField
        val TRANSFER_SETTINGS = values().filter { it.isTransfer }.toTypedArray()
    }
}
