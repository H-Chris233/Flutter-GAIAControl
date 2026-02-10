/*
 * ************************************************************************************************
 * * © 2022-2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.ui.information

import com.qualcomm.qti.gaiaclient.core.gaia.qtil.data.battery.Battery
import com.qualcomm.qti.gaiaclient.ui.common.ImageViewData

class InformationViewData {

    var deviceImage: ImageViewData? = null
    var name: String? = null
    var variantName: String? = null
    var bluetoothAddress: String? = null
    var state: String? = null
    var serialNumber: String? = null
    var serialNumberLeft: String? = null
    var serialNumberRight: String? = null
    var applicationVersion: String? = null
    var gaiaVersion: Int? = null
    var batteryLevels: MutableMap<Battery, String> = mutableMapOf()
    var isCharging: Boolean? = null
        get() {
            val value: Boolean? = field
            return value != null && value
        }
    var applicationBuildId: String? = null

    fun hasStates(): Boolean = state != null || isCharging != null

    fun hasVersions() = applicationVersion != null || gaiaVersion != null || applicationBuildId != null

    fun hasSerialNumbers() = serialNumber != null || serialNumberLeft != null || serialNumberRight != null

    fun showSingleSerialNumber() = serialNumber != null && serialNumberLeft == null && serialNumberRight == null

    fun hasBatteryLevels() = batteryLevels.isNotEmpty()

    fun getBatteryLevel(battery: Battery): String? = batteryLevels[battery]

}
