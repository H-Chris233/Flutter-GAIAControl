/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing.plotting

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.Filter
import kotlin.math.*


class BiQuadFilter {

    private data class Coefficients(val first: Double, val second: Double, val third: Double) {
        constructor() : this(0.0, 0.0, 0.0)
    }

    private var coeffsA = Coefficients()
    private var coeffsB = Coefficients()

    private var m_fc: Double = 48000.0
    var fc: Double
        get() {
            return m_fc
        }
        set(value) {
            assert(value >= 0)        // barf if frequency isn't positive
            m_fc = min(value, m_fs / 2.0)
            calcCoeffs()
        }

    private var m_fs: Double = 48000.0
    var fs: Double
        get() {
            return m_fs
        }
        set(value) {
            assert(value >= 0)        // barf if frequency isn't positive
            m_fs = value
            m_fc = min(value, m_fs / 2.0)
            calcCoeffs()
        }

    var gain: Double = 1.0
        set(value) {
            field = value
            calcCoeffs()
        }

    var q: Double = 0.7
        set(value) {
            field = value
            calcCoeffs()
        }

    var filterType = Filter.BYPASS
        set(value) {
            field = value
            calcCoeffs()
        }


    init {
        calcBypassFilter()
    }

    fun calcComplexGain(freq: Double): ComplexNumber {
        val piFreqCalc = 2 * PI * freq / m_fs
        val num = ComplexNumber(
            coeffsB.first * cos(0.0) +
                    coeffsB.second * cos(piFreqCalc) +
                    coeffsB.third * cos(2 * piFreqCalc),
            -coeffsB.first * sin(0.0) -
                    coeffsB.second * sin(piFreqCalc) -
                    coeffsB.third * sin(2 * piFreqCalc)
        )

        val denom = ComplexNumber(
            1.0 * cos(0.0) +
                    coeffsA.second * cos(piFreqCalc) +
                    coeffsA.third * cos(2 * piFreqCalc),
            -1.0 * sin(0.0) -
                    coeffsA.second * sin(piFreqCalc) -
                    coeffsA.third * sin(2 * piFreqCalc)
        )

        val gainDiv = denom.radiusSquare

        return ComplexNumber(
            (num.real * denom.real + num.imaginary * denom.imaginary) / gainDiv,
            (num.imaginary * denom.real - num.real * denom.imaginary) / gainDiv
        )
    }

    fun calcDbGain(freq: Double): Double {
        return 20.0 * log10(calcComplexGain(freq).radius)
    }

    private fun calcCoeffs() {
        when (filterType) {
            Filter.BYPASS -> calcBypassFilter()
            Filter.LOW_PASS_1 -> calcLowPassBilinear()
            Filter.HIGH_PASS_1 -> calcHighPassBilinear()
            Filter.ALL_PASS_1 -> calcAllPassBilinear()
            Filter.LOW_SHELF_1 -> calcLowShelfBilinear()
            Filter.HIGH_SHELF_1 -> calcHighShelfBilinear()
            Filter.TILT_1 -> calcTiltBilinear()
            Filter.LOW_PASS_2 -> calcLowPassBiQuad()
            Filter.HIGH_PASS_2 -> calcHighPassBiQuad()
            Filter.ALL_PASS_2 -> calcAllPassBiQuad()
            Filter.LOW_SHELF_2 -> calcLowShelfBiQuad()
            Filter.HIGH_SHELF_2 -> calcHighShelfBiQuad()
            Filter.TILT_2 -> calcTiltBiQuad()
            Filter.PARAMETRIC_EQUALIZER -> calcParametric()
        }
    }

    private fun calcBypassFilter() {
        coeffsA = Coefficients(0.0, 0.0, 0.0)
        coeffsB = Coefficients(1.0, 0.0, 0.0)
    }

    // 1st order filter coefficient calculation routines

    private fun calcLowPassBilinear() {
        //--------------------------------------------------------------------------
        // create coefficients for first order low pass bilinear section
        // Filter only uses biquadratic sections so set unused coefficients to zero
        //
        // laplace transform w/(s+w)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val denom = wd + 1.0

        coeffsA = Coefficients(0.0, (wd - 1.0) / denom, 0.0)
        coeffsB = Coefficients(wd / denom, wd / denom, 0.0)
    }

    private fun calcHighPassBilinear() {
        //--------------------------------------------------------------------------
        // create coefficients for first order low pass bilinear section
        // Filter only uses biquadratic sections so set unused coefficients to zero
        //
        // laplace transform s/(s+w)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val denom = wd + 1.0

        coeffsA = Coefficients(0.0, (wd - 1.0) / denom, 0.0)
        coeffsB = Coefficients(1.0 / denom, -1.0 / denom, 0.0)
    }

    private fun calcAllPassBilinear() {
        //--------------------------------------------------------------------------
        // create coefficients for first order all pass bilinear section
        // Filter only uses biquadratic sections so set unused coefficients to zero
        //
        // laplace transform (s-w)/(s+w)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val denom = wd + 1.0

        coeffsA = Coefficients(0.0, (wd - 1.0) / denom, 0.0)
        coeffsB = Coefficients((1.0 - wd) / denom, -1.0, 0.0)
    }

    private fun calcLowShelfBilinear() {
        //--------------------------------------------------------------------------
        // create coefficients for first order low frequency shelf bilinear section
        // Filter only uses biquadratic sections so set unused coefficients to zero
        //
        // laplace transform (s-w1)/(s+w2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val wd1 = wd * (10.0.pow(gain / 40.0))
        val wd2 = wd / (10.0.pow(gain / 40.0))
        val denom = wd2 + 1.0

        coeffsA = Coefficients(0.0, (wd2 - 1.0) / denom, 0.0)
        coeffsB = Coefficients((wd1 + 1.0) / denom, (wd1 - 1.0) / denom, 0.0)
    }

    private fun calcHighShelfBilinear() {
        //--------------------------------------------------------------------------
        // create coefficients for first order high frequency shelf bilinear section
        // Filter only uses biquadratic sections so set unused coefficients to zero
        //
        // laplace transform g*(s-w1)/(s+w2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val wd1 = wd / (10.0.pow(gain / 40.0))
        val wd2 = wd * (10.0.pow(gain / 40.0))
        val denom = wd2 + 1.0

        coeffsA = Coefficients(
            0.0,
            (wd2 - 1.0) / denom,
            0.0
        )
        coeffsB = Coefficients(
            10.0.pow(gain / 20) * (wd1 + 1.0) / denom,
            10.0.pow(gain / 20) * (wd1 - 1.0) / denom,
            0.0
        )
    }

    private fun calcTiltBilinear() {
        //--------------------------------------------------------------------------
        // create coefficients for first order tilt bilinear section
        // Filter only uses biquadratic sections so set unused coefficients to zero
        //
        // laplace transform g*(s-w1)/(s+w2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val wd1 = wd / (10.0.pow(gain / 40.0))
        val wd2 = wd * (10.0.pow(gain / 40.0))
        val denom = wd2 + 1.0

        coeffsA = Coefficients(0.0, (wd2 - 1.0) / denom, 0.0)
        coeffsB = Coefficients(
            10.0.pow(gain / 40) * (wd1 + 1.0) / denom,
            10.0.pow(gain / 40) * (wd1 - 1.0) / denom,
            0.0
        )
    }

    // 2nd order filter coefficient calculation routines

    private fun calcLowPassBiQuad() {
        //--------------------------------------------------------------------------
        // create coefficients for second order low pass biquadratic section
        //
        // laplace transform w^2/(s^2+sw/q+w^2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val denom = wd * wd + wd / q + 1.0

        coeffsA = Coefficients(
            0.0,
            (2 * wd * wd - 2.0) / denom,
            (wd * wd - wd / q + 1.0) / denom
        )

        coeffsB = Coefficients(
            wd * wd / denom,
            2.0 * wd * wd / denom,
            wd * wd / denom
        )
    }

    private fun calcHighPassBiQuad() {
        //--------------------------------------------------------------------------
        // create coefficients for second order high pass biquadratic section
        //
        // laplace transform s^2/(s^2+sw/q+w^2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val denom = wd * wd + wd / q + 1.0

        coeffsA = Coefficients(
            0.0,
            (2 * wd * wd - 2.0) / denom,
            (wd * wd - wd / q + 1.0) / denom
        )

        coeffsB = Coefficients(
            1.0 / denom,
            -2.0 / denom,
            1.0 / denom
        )
    }

    private fun calcAllPassBiQuad() {
        //--------------------------------------------------------------------------
        // create coefficients for second order all pass biquadratic section
        //
        // laplace transform (s^2-sw/q+w^2)/(s^2+sw/q+w^2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val denom = wd * wd + wd / q + 1.0

        coeffsA = Coefficients(
            0.0,
            (2 * wd * wd - 2.0) / denom,
            (wd * wd - wd / q + 1.0) / denom
        )

        coeffsB = Coefficients(
            (wd * wd - wd / q + 1.0) / denom,
            (2.0 * wd * wd - 2.0) / denom,
            1.0
        )
    }

    private fun calcLowShelfBiQuad() {
        //--------------------------------------------------------------------------
        // create coefficients for second order low shelf biquadratic section
        //
        // laplace transform (s^2+sw1/q+w1^2)/(s^2+sw2/q+w2^2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val wd1 = wd * 10.0.pow(gain / 80)
        val wd2 = wd / 10.0.pow(gain / 80)
        val denom = wd2 * wd2 + wd2 / q + 1.0

        coeffsA = Coefficients(
            0.0,
            (2.0 * wd2 * wd2 - 2.0) / denom,
            (wd2 * wd2 - wd2 / q + 1.0) / denom
        )

        coeffsB = Coefficients(
            (wd1 * wd1 + wd1 / q + 1.0) / denom,
            (2.0 * wd1 * wd1 - 2.0) / denom,
            (wd1 * wd1 - wd1 / q + 1.0) / denom
        )
    }

    private fun calcHighShelfBiQuad() {
        //--------------------------------------------------------------------------
        // create coefficients for second order low shelf biquadratic section
        //
        // laplace transform g*(s^2+sw1/q+w1^2)/(s^2+sw2/q+w2^2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val wd1 = wd / 10.0.pow(gain / 80)
        val wd2 = wd * 10.0.pow(gain / 80)
        val denom = wd2 * wd2 + wd2 / q + 1.0

        coeffsA = Coefficients(
            0.0,
            (2.0 * wd2 * wd2 - 2.0) / denom,
            (wd2 * wd2 - wd2 / q + 1.0) / denom
        )

        coeffsB = Coefficients(
            10.0.pow(gain / 20) * (wd1 * wd1 + wd1 / q + 1.0) / denom,
            10.0.pow(gain / 20) * (2.0 * wd1 * wd1 - 2.0) / denom,
            10.0.pow(gain / 20) * (wd1 * wd1 - wd1 / q + 1.0) / denom
        )
    }

    private fun calcTiltBiQuad() {
        //--------------------------------------------------------------------------
        // create coefficients for second order low shelf biquadratic section
        //
        // laplace transform g*(s^2+sw1/q+w1^2)/(s^2+sw2/q+w2^2)
        //--------------------------------------------------------------------------
        val wd = tan(PI * m_fc / m_fs)
        val wd1 = wd / 10.0.pow(gain / 80)
        val wd2 = wd * 10.0.pow(gain / 80)
        val denom = wd2 * wd2 + wd2 / q + 1.0

        coeffsA = Coefficients(
            0.0,
            (2.0 * wd2 * wd2 - 2.0) / denom,
            (wd2 * wd2 - wd2 / q + 1.0) / denom
        )

        coeffsB = Coefficients(
            10.0.pow(gain / 40) * (wd1 * wd1 + wd1 / q + 1.0) / denom,
            10.0.pow(gain / 40) * (2.0 * wd1 * wd1 - 2.0) / denom,
            10.0.pow(gain / 40) * (wd1 * wd1 - wd1 / q + 1.0) / denom
        )
    }

    private fun calcParametric() {
        val wd = tan(PI * m_fc / m_fs)
        val a = -1.0 / (2.0 * q) + sqrt((1.0 / (2.0 * q)).pow(2.0) + 1.0)
        val aTerm = tan(PI * a * m_fc / m_fs)
        val qdTop = aTerm * wd
        val qdBottom = wd.pow(2.0) - aTerm.pow(2.0)
        val qd = qdTop / qdBottom
        val q1 = qd / 10.0.pow(gain / 40.0)
        val q2 = qd * 10.0.pow(gain / 40.0)
        val denom = wd * wd + wd / q2 + 1.0

        coeffsA = Coefficients(
            0.0,
            (2 * wd * wd - 2.0) / denom,
            (wd * wd - wd / q2 + 1.0) / denom
        )

        coeffsB = Coefficients(
            (wd * wd + wd / q1 + 1.0) / denom,
            (2.0 * wd * wd - 2.0) / denom,
            (wd * wd - wd / q1 + 1.0) / denom
        )
    }
}
