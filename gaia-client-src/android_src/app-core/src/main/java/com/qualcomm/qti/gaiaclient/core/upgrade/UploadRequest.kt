/*
 * ************************************************************************************************
 * * © 2020-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.upgrade

import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessageListener

data class UploadRequest(
    val data: ByteArray,
    val isFlushed: Boolean,
    val isAcknowledged: Boolean,
    val useRwcp: Boolean,
    val upgradeMessageListener: UpgradeMessageListener
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadRequest

        if (!data.contentEquals(other.data)) return false
        if (isFlushed != other.isFlushed) return false
        if (isAcknowledged != other.isAcknowledged) return false
        if (useRwcp != other.useRwcp) return false
        if (upgradeMessageListener != other.upgradeMessageListener) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + isFlushed.hashCode()
        result = 31 * result + isAcknowledged.hashCode()
        result = 31 * result + useRwcp.hashCode()
        result = 31 * result + upgradeMessageListener.hashCode()
        return result
    }
}