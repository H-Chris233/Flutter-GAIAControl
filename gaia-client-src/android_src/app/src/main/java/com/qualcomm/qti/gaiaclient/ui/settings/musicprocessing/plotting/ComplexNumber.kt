/*
 * ************************************************************************************************
 * * © 2021 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.musicprocessing.plotting

import kotlin.math.atan2
import kotlin.math.sqrt

class ComplexNumber(val real: Double, val imaginary: Double = 0.0) {
    val radiusSquare: Double
        get() {
            return real * real + imaginary * imaginary
        }

    val radius: Double
        get() {
            return sqrt(radiusSquare)
        }

    val arg: Double
        get() {
            return atan2(imaginary, real)
        }

    fun conjugate(): ComplexNumber {
        return ComplexNumber(real, -imaginary)
    }

    operator fun plus(otherNumber: ComplexNumber): ComplexNumber {
        return ComplexNumber(real + otherNumber.real, imaginary + otherNumber.imaginary)
    }

    operator fun minus(otherNumber: ComplexNumber): ComplexNumber {
        return ComplexNumber(real - otherNumber.real, imaginary - otherNumber.imaginary)
    }

    operator fun times(otherNumber: ComplexNumber): ComplexNumber {
        return ComplexNumber(
            real * otherNumber.real - imaginary * otherNumber.imaginary,
            real * otherNumber.imaginary + imaginary * otherNumber.real
        )
    }

    operator fun div(otherNumber: ComplexNumber): ComplexNumber {
        return this * (otherNumber.conjugate() / otherNumber.radiusSquare)
    }

    operator fun div(otherNumber: Double): ComplexNumber {
        return ComplexNumber(real / otherNumber, imaginary / otherNumber)
    }
}

