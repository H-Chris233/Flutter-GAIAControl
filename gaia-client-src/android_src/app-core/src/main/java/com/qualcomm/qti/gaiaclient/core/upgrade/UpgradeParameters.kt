/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.upgrade

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu
import com.qualcomm.qti.libraries.upgrade.messages.OpCodes
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage

internal class UpgradeParameters {
    var expectedChunkSize: Int
        private set
    var isLogged: Boolean
        private set
    var isFlushed: Boolean
        private set
    var isUploadAcknowledged: Boolean
        private set
    var useRwcp: Boolean = false
        private set

    init {
        expectedChunkSize = DEFAULT_CHUNK_SIZE
        isLogged = false
        isFlushed = false
        isUploadAcknowledged = true
    }

    fun reset(expectedChunkSize: Int) {
        this.expectedChunkSize = expectedChunkSize
        isLogged = false
        isFlushed = false
        isUploadAcknowledged = true
    }

    fun set(
        expectedChunkSize: Int,
        isLogged: Boolean,
        isUploadFlushed: Boolean,
        isUploadAcknowledged: Boolean,
        useRwcp: Boolean

    ) {
        this.expectedChunkSize = expectedChunkSize
        this.isLogged = isLogged
        this.isFlushed = isUploadFlushed
        this.isUploadAcknowledged = isUploadAcknowledged
        this.useRwcp = useRwcp
    }

    companion object {
        private const val DEFAULT_CHUNK_SIZE = (GaiaPdu.Payload.minLength
                - UpgradeMessage.HEADER_LENGTH
                - OpCodes.UpgradeData.DATA_HEADER_LENGTH) // 16
    }
}