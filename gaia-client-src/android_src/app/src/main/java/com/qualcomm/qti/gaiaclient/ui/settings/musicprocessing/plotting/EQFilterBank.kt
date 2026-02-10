/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing.plotting

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.BandInfo

data class GraphPoint(val result: Double, val frequency: Double)

typealias GraphCurve = List<GraphPoint>
typealias MutableGraphCurve = MutableList<GraphPoint>

class GraphData(val filterCurves: List<GraphCurve>, val totalCurve: GraphCurve)

class EQFilterBank(val bands: List<BandInfo>) {
    constructor() : this(emptyList())

    private var filters: List<BiQuadFilter>
    var frequency: Double = 48000.0
        set(newValue) {
            field = newValue
            for (filter in filters) {
                filter.fs = frequency
            }
        }

    var pregain: Double = 0.0

    var count: Int = 0
        get() {
            return filters.count()
        }

    init {
        var newFilters = mutableListOf<BiQuadFilter>()
        for (band in bands) {
            val filter = BiQuadFilter()
            filter.filterType = band.filter
            filter.fc = band.frequency.toDouble()
            filter.gain = band.gain
            filter.q = band.q
            newFilters.add(filter)
        }
        filters = newFilters
    }

    operator fun get(idx: Int): BiQuadFilter {
        assert(idx < filters.count())
        return filters[idx]
    }

    fun generateDbGainGraph(frequencies: List<Double>): GraphData {
        var totalGainCurve: MutableGraphCurve = mutableListOf<GraphPoint>()
        val filterGainCurves: List<MutableGraphCurve> = filters.map { mutableListOf<GraphPoint>() }

        for (frequency in frequencies) {
            var totalGain = 0.0
            for (filterIndex in 0 until filters.count()) {
                val filter = filters[filterIndex]
                val gain = filter.calcDbGain(frequency)
                totalGain += gain
                filterGainCurves[filterIndex].add(GraphPoint(gain, frequency))
            }
            totalGainCurve.add(GraphPoint(totalGain + pregain, frequency))
        }

        return GraphData(filterGainCurves, totalGainCurve)
    }
}
