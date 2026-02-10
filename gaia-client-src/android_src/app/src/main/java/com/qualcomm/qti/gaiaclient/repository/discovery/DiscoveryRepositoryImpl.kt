/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.repository.discovery

import android.bluetooth.BluetoothProfile
import android.content.Context
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveredDevice
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.DiscoveryType
import com.qualcomm.qti.gaiaclient.core.requests.core.RequestListener
import com.qualcomm.qti.gaiaclient.core.requests.qtil.ClassicBluetoothDiscoveryRequest
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetConnectedDevicesRequest
import com.qualcomm.qti.gaiaclient.core.requests.qtil.GetPairedDevicesRequest
import com.qualcomm.qti.gaiaclient.repository.connection.BluetoothReason
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscoveryRepositoryImpl @Inject constructor() : DiscoveryRepositoryData() {

    private val pairedListener = DiscoveryListener<List<DiscoveredDevice>, Void>(
        DiscoveryType.PAIRED,
        complete = { this@DiscoveryRepositoryImpl.onSuccess(DiscoveryType.PAIRED, it) }
    )

    private val discoveredListener = DiscoveryListener<Void, DiscoveredDevice>(
        DiscoveryType.DISCOVERED,
        progress = { this@DiscoveryRepositoryImpl.onProgress(DiscoveryType.DISCOVERED, it) }
    )

    private val connectedListener = DiscoveryListener<List<DiscoveredDevice>, Void>(
        DiscoveryType.CONNECTED,
        complete = { this@DiscoveryRepositoryImpl.onSuccess(DiscoveryType.CONNECTED, it) }
    )

    override fun fetchDevices(context: Context, type: DiscoveryType) {
        val requestManager = GaiaClientService.getRequestManager()
        when (type) {
            DiscoveryType.CONNECTED -> {
                requestManager.execute(
                    context,
                    GetConnectedDevicesRequest(BluetoothProfile.A2DP, connectedListener)
                )
                requestManager.execute(
                    context,
                    GetConnectedDevicesRequest(BluetoothProfile.HEADSET, connectedListener)
                )
            }
            DiscoveryType.PAIRED -> requestManager.execute(context, GetPairedDevicesRequest(pairedListener))
            DiscoveryType.DISCOVERED ->  // only classic discovery as compatible devices have classic and might have LE
                requestManager.execute(context, ClassicBluetoothDiscoveryRequest(discoveredListener))
        }
    }

    private fun matchStatusToReason(status: BluetoothStatus): BluetoothReason {
        return when (status) {
            BluetoothStatus.NO_BLUETOOTH -> BluetoothReason.NO_BLUETOOTH

            BluetoothStatus.NO_LOCATION -> BluetoothReason.NO_LOCATION

            BluetoothStatus.DISCOVERY_FAILED -> BluetoothReason.DISCOVERY_FAILED

            BluetoothStatus.NO_PERMISSIONS -> BluetoothReason.NO_PERMISSIONS

            BluetoothStatus.DEVICE_NOT_FOUND, BluetoothStatus.DEVICE_NOT_COMPATIBLE,
            BluetoothStatus.NO_TRANSPORT_FOUND, BluetoothStatus.CONNECTION_LOST,
            BluetoothStatus.TIME_OUT, BluetoothStatus.ALREADY_CONNECTED,
            BluetoothStatus.IN_PROGRESS, BluetoothStatus.CONNECTION_FAILED,
            BluetoothStatus.RECONNECTION_TIME_OUT -> BluetoothReason.UNKNOWN

            else -> BluetoothReason.UNKNOWN
        }
    }

    private inner class DiscoveryListener<Result, Progress>(
        private val discoveryType: DiscoveryType,
        private val progress: (Progress) -> Unit = { this@DiscoveryRepositoryImpl.onProgress(discoveryType) },
        private val complete: (Result) -> Unit = { this@DiscoveryRepositoryImpl.onSuccess(discoveryType) }
    ) : RequestListener<Result, Progress, BluetoothStatus> {
        override fun onProgress(progress: Progress) = progress(progress)
        override fun onComplete(result: Result) = complete(result)
        override fun onError(error: BluetoothStatus) {
            this@DiscoveryRepositoryImpl.onError(discoveryType, matchStatusToReason(error))
        }
    }
}
