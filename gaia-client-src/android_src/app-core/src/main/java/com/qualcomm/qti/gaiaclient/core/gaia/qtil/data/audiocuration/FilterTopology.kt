/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

sealed class FilterTopology(open val value: Int) {
    object SingleMode : FilterTopology(0x00)
    object ParallelMode : FilterTopology(0x01)
    object DualMode : FilterTopology(0x02)
    class Undefined(value: Int) : FilterTopology(value)
    object Unsupported : FilterTopology(-1)

    companion object {
        @JvmStatic
        fun valueOf(value: Int): FilterTopology {
            return when (value) {
                SingleMode.value -> SingleMode
                ParallelMode.value -> ParallelMode
                DualMode.value -> DualMode
                else -> Undefined(value)
            }
        }
    }
}
