/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3.V3AudioCurationPlugin
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getSINT16
import com.qualcomm.qti.gaiaclient.core.utils.BytesUtils.getUINT8

data class Gain(
    val mode: Int,
    val featureType: FeatureType,
    val filterTopology: FilterTopology,
    val gains: Map<EarbudPosition, List<Instance>>,
) {
    constructor(version: Int, data: ByteArray) : this(
        mode = getUINT8(data, Offsets.MODE),
        featureType = FeatureType.valueOf(getUINT8(data, Offsets.FEATURE_TYPE)),
        filterTopology = if (version < V3AudioCurationPlugin.VERSIONS.V8) FilterTopology.Unsupported else
            FilterTopology.valueOf(getUINT8(data, Offsets.FILTER_TOPOLOGY)),
        gains = mapOf<EarbudPosition, List<Instance>>(
            EarbudPosition.LEFT to listOf<Instance>(
                Instance(
                    value = getInstance0Gain(data, Offsets.LEFT_GAIN_INST0),
                    total = getTotal(version, data, Offsets.LEFT_GAIN_INST0_TOTAL)
                ),
                Instance(
                    value = getInstance1Gain(version, data, Offsets.LEFT_GAIN_INST1),
                    total = getTotal(version, data, Offsets.LEFT_GAIN_INST1_TOTAL)
                )
            ),
            EarbudPosition.RIGHT to listOf<Instance>(
                Instance(
                    value = getInstance0Gain(data, Offsets.RIGHT_GAIN_INST0),
                    total = getTotal(version, data, Offsets.RIGHT_GAIN_INST0_TOTAL)
                ),
                Instance(
                    value = getInstance1Gain(version, data, Offsets.RIGHT_GAIN_INST1),
                    total = getTotal(version, data, Offsets.RIGHT_GAIN_INST1_TOTAL)
                )
            )
        ),
    )

    fun getGain(position: EarbudPosition, instance: Int): Int = gains[position]?.get(instance)?.value ?: 0

    companion object {
        const val MIN = 0
        const val MAX = 255
        const val TOTAL_MIN = -2000
        const val TOTAL_MAX = 500
        private const val TOTAL_FACTOR = 10.0

        private fun getTotal(version: Int, data: ByteArray, offset: Int) : Double =
            if (version < V3AudioCurationPlugin.VERSIONS.V8) 0.0 else getSINT16(data, offset) / TOTAL_FACTOR

        private fun getInstance0Gain(data: ByteArray, offset: Int) : Int = getUINT8(data, offset)

        private fun getInstance1Gain(version: Int, data: ByteArray, offset: Int) : Int =
            if (version < V3AudioCurationPlugin.VERSIONS.V8) 0 else getUINT8(data, offset)
    }

    data class Instance(val value: Int, val total: Double)

    private object Offsets {
        const val MODE = 0
        const val FEATURE_TYPE = 1
        const val LEFT_GAIN_INST0 = 2
        const val RIGHT_GAIN_INST0 = 3
        const val FILTER_TOPOLOGY = 4
        const val LEFT_GAIN_INST1 = 5
        const val RIGHT_GAIN_INST1 = 6
        const val LEFT_GAIN_INST0_TOTAL = 7
        const val RIGHT_GAIN_INST0_TOTAL = 9
        const val LEFT_GAIN_INST1_TOTAL = 11
        const val RIGHT_GAIN_INST1_TOTAL = 13
    }
}
