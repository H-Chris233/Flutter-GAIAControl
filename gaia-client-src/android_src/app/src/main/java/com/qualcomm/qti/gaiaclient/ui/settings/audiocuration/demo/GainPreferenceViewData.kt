/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.audiocuration.demo

import android.content.Context
import com.qualcomm.qti.gaiaclient.R
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.EarbudPosition
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.FilterTopology
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.audiocuration.Gain
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.plugins.v3.V3AudioCurationPlugin

data class GainPreferenceViewData(val left: DualRings, val right: DualRings) {
    constructor(context: Context, version: Int, gain: Gain) : this(
        DualRings(context, version, gain.filterTopology, gain.gains[EarbudPosition.LEFT]),
        DualRings(context, version, gain.filterTopology, gain.gains[EarbudPosition.RIGHT])
    )
}

data class DualRings(val isDual: Boolean, val outer: GainRing, val inner: GainRing) {
    constructor(context: Context, version: Int, topology: FilterTopology, gains: List<Gain.Instance>?) : this(
        version >= V3AudioCurationPlugin.VERSIONS.V8 && topology is FilterTopology.DualMode,
        GainRing(context, version, gains?.get(0)),
        GainRing(context, version, gains?.get(1))
    )
}

data class GainRing(val progress: Int, val label: String) {
    constructor(context: Context, version: Int, instance: Gain.Instance?) : this(
        (instance?.value ?: 0) * 100 / Gain.MAX,
        if (version < V3AudioCurationPlugin.VERSIONS.V8) (instance?.value ?: 0).toString() else
            context.getString(R.string.audio_curation_gain_in_db_decimal, instance?.total ?: 0.0)
    )
}
