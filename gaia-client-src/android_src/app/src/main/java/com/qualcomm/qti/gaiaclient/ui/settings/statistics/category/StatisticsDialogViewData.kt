/*
 * ************************************************************************************************
 * * © 2022 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.settings.statistics.category

data class RefreshRateBounds(val minimum: Float, val maximum: Float)

data class RefreshRate(val rateInMilliseconds: Float) {
    val rateInSeconds: Float
        get() = rateInMilliseconds / 1000
}
