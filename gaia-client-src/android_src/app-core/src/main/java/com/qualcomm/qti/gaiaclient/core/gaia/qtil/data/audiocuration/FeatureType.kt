/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

sealed class FeatureType(open val value: Int) {
    object Static : FeatureType(0x01)
    object Leakthrough : FeatureType(0x02)
    object Adaptive : FeatureType(0x03)
    object AdaptiveLeakthrough : FeatureType(0x04)
    class Undefined(value: Int) : FeatureType(value)

    companion object {
        @JvmStatic
        fun valueOf(value: Int): FeatureType {
            return when (value) {
                Static.value -> Static
                Leakthrough.value -> Leakthrough
                Adaptive.value -> Adaptive
                AdaptiveLeakthrough.value -> AdaptiveLeakthrough
                else -> Undefined(value)
            }
        }
    }
}
