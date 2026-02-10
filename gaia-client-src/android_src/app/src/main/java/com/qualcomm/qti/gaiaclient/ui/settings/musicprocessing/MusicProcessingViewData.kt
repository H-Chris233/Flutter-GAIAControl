/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo
import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.PreSet

class MusicProcessingViewData(
    val deviceConnected: Boolean,
    val eqEnabled: Boolean,
    val presets: List<PreSet>,
    val selectedPreset: PreSet?,
    val userBands: Set<BandInfo>
) {

    constructor() : this(
        true,
        false,
        emptyList<PreSet>(),
        null,
        emptySet<BandInfo>()
    )

    val orderedUserBands: List<BandInfo>
        get() {
            return userBands.toList().sortedBy { it.id }
        }
}