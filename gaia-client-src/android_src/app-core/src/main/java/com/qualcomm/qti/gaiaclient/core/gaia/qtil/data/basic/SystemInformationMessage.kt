/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.basic

import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils

/**
 * ```
 * bytes  0            1   2   3   4   5                     len+5
 *        +------------+---+---+---+---+--------- ... ---------+
 *   ...  |  MORE DATA |     TOKEN     |         DATA          |
 *        +------------+---+---+---+---+--------- ... ---------+
 * ```
 */
class SystemInformationMessage(bytes: ByteArray) {
    val hasMoreData: Boolean = BytesUtils.getUINT8(bytes, MORE_DATA_OFFSET) == HAS_MORE_DATA_VALUE
    val token: ByteArray = BytesUtils.getSubArray(bytes, TOKEN_OFFSET, TOKEN_LENGTH)
    val data: ByteArray = BytesUtils.getSubArray(bytes, DATA_OFFSET)

    companion object {
        private const val MORE_DATA_OFFSET = 0
        private const val TOKEN_OFFSET = 1
        private const val TOKEN_LENGTH = 4
        private const val DATA_OFFSET = 5
        private const val HAS_MORE_DATA_VALUE = 1
    }
}
