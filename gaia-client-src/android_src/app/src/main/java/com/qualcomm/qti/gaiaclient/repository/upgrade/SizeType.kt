/*
 * ************************************************************************************************
 * * © 2021, 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.       *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.upgrade

import com.qualcomm.qti.gaiaclient.core.gaia.core.GaiaPdu
import com.qualcomm.qti.libraries.upgrade.messages.OpCodes.UpgradeData
import com.qualcomm.qti.libraries.upgrade.messages.UpgradeMessage

enum class SizeType {
    EXPECTED,
    SET,
    MAX,
    DEFAULT;

    val default: Int
        get() = if (this@SizeType != EXPECTED) {
            GaiaPdu.Payload.minLength - UpgradeMessage.HEADER_LENGTH - UpgradeData.DATA_HEADER_LENGTH
        } else NULL_VALUE

    companion object {
        const val NULL_VALUE = -1
    }
}
