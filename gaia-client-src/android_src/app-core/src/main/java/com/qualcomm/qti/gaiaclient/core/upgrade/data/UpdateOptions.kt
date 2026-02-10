/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.upgrade.data

import android.net.Uri
import com.qualcomm.qti.gaiaclient.core.gaia.core.version.GaiaVersion

data class UpdateOptions(
    val fileLocation: Uri,
    val isLogged: Boolean = false,
    val isUploadFlushed: Boolean = false,
    val isUploadAcknowledged: Boolean = true,
    val expectedChunkSize: Int = 0,
    val useRwcp: Boolean = false
) {
    constructor(
        fileLocation: Uri, gaiaVersion: Int, isLogged: Boolean = false,
        isUploadAcknowledged: Boolean = true, expectedChunkSize: Int = 0, useRwcp: Boolean = false
    ) : this(
        fileLocation, isUploadFlushed = gaiaVersion <=
                GaiaVersion.V2, isLogged = isLogged, isUploadAcknowledged =
        isUploadAcknowledged, expectedChunkSize = expectedChunkSize, useRwcp = useRwcp
    )
}