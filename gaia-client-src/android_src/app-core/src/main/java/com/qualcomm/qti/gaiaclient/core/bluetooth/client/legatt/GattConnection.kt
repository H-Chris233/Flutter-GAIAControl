/*
 * ************************************************************************************************
 * * © 2023 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.             *
 * ************************************************************************************************
 */
package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.IntentFilter
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.GaiaClientService
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.IdCreator
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.data.GattDescription
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.data.Notification
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations.GattOperation
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations.GattOperationManager
import com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.operations.GattResult
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.BluetoothStatus
import com.qualcomm.qti.gaiaclient.core.bluetooth.data.ConnectionState
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger
import java.lang.ref.WeakReference
import java.util.UUID

private const val TAG = "GattConnection"
private const val LOG_METHODS = DEBUG.Bluetooth.Gatt.CONNECTION

class GattConnection(
    context: Context,
    private val device: BluetoothDevice,
    private val description: GattDescription,
    private val listener: Listener
) {

    private val context: WeakReference<Context> = WeakReference(context.applicationContext)

    var logBytes = DEBUG.Bluetooth.DATA
        set(value) {
            field = value || DEBUG.Bluetooth.DATA
            gatt?.logBytes = field
        }

    var gatt: GattOperationManager? = null
        set(value) {
            field = value
            field?.logBytes = logBytes
        }

    private val bondReceiver = BondStateReceiver(device) { state, previous -> onBondState(state, previous) }

    var services: Map<UUID, List<BluetoothGattService>> = emptyMap()
        private set

    var mtu: Int = Gatt.Mtu.DEFAULT

    private var gattState: GattState = GattState.DISCONNECTED
        set(value) {
            Logger.log(LOG_METHODS, TAG, "gattState->set", Pair("from", field), Pair("to", value))
            field = value
            listener.onConnectionState(value.connectionState)
            if (gattState == GattState.DISCONNECTED || gattState == GattState.REGISTERING) {
                unregisterBondReceiver()
            }
        }

    private val gattListener: GattOperationManager.Listener = object : GattOperationManager.Listener {
        override fun onConnectionState(state: ConnectionState, status: GattStatus) {
            logState("onConnectionState", Pair("connectionState", state), Pair("status", status))
            if (status == ConnectionStatus.CONN_OK && state == ConnectionState.CONNECTED) {
                gatt?.operate(GattOperation.ConnectionPriority(Priority.HIGH)) // setting up high for speed
                discoverServices()
            } else if (state == ConnectionState.DISCONNECTED) {
                gattState = GattState.DISCONNECTED
                if (status != ConnectionStatus.CONN_OK) listener.onConnectionError(bluetoothStatus(status))
                clean()
            } else if (state != ConnectionState.CONNECTED) {
                gattState = GattState.valueOf(state)
            }
        }

        override fun onNotification(characteristic: BluetoothGattCharacteristic, value: ByteArray) {
            logState("onNotification", Pair("uuid", characteristic.uuid))
            description.getNotification(characteristic.service.uuid, characteristic.uuid)?.let { it.notify(value) }
        }

        override fun onMtu(mtu: Int) {
            logState("onMtu", Pair("mtu", mtu))
            this@GattConnection.onMtu(GattResult.Mtu(AttStatus.ANDROID_SUCCESS, mtu))
        }
    }

    fun connect(): BluetoothStatus {
        logState("connect")
        return connect(false)
    }

    fun reconnect(): BluetoothStatus {
        logState("reconnect")
        return connect(true)
    }

    fun disconnect() {
        logState("disconnect")
        if (gattState == GattState.DISCONNECTING) return // in progress
        if (gattState == GattState.DISCONNECTED) return // not connected
        gattState = GattState.DISCONNECTING

        gatt?.disconnect()

        clean()
    }

    fun operate(operation: GattOperation): Long {
        logState("operate", Pair("operation", operation))
        return gatt?.operate(operation) ?: IdCreator.NULL_ID
    }

    private fun connect(autoConnect: Boolean): BluetoothStatus {
        logState("connect", Pair("autoConnect", autoConnect))
        if (gattState == GattState.READY) return BluetoothStatus.ALREADY_CONNECTED
        if (gattState != GattState.DISCONNECTED && gattState != GattState.DISCONNECTING) return BluetoothStatus.IN_PROGRESS
        val context = context.get() ?: return BluetoothStatus.CONNECTION_FAILED

        gattState = GattState.CONNECTING
        val gatt = GattOperationManager(device, gattListener)
        this.gatt = gatt
        gatt.connect(context, autoConnect)
        return BluetoothStatus.IN_PROGRESS
    }

    private fun registerBondReceiver() {
        logState("registerBondReceiver")
        val context = context.get() ?: return
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        filter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST)
        context.registerReceiver(bondReceiver, filter)
    }

    private fun unregisterBondReceiver() {
        logState("unregisterBondReceiver")
        val context = context.get() ?: return
        try {
            context.unregisterReceiver(bondReceiver)
        } catch (_: Exception) {
        }
    }

    private fun discoverServices() {
        logState("discoverServices")
        if (gattState != GattState.CONNECTING) return // ignored

        gattState = GattState.DISCOVERING_SERVICE
        gatt?.operate(GattOperation.DiscoverServices { onServices(it) })
    }

    private fun onServices(result: GattResult) {
        logState("onServices", Pair("result", result))
        if (gattState != GattState.DISCOVERING_SERVICE) return // ignored

        if (result is GattResult.Error.Timeout) return onError(BluetoothStatus.TIME_OUT)
        if (result is GattResult.Error.NotExecuted) return onError(BluetoothStatus.OPERATION_FAILED)
        if (result !is GattResult.Services) return // unexpected
        if (result.status != AttStatus.ANDROID_SUCCESS) return onError(BluetoothStatus.DISCOVERY_FAILED)

        // it is safe to continue the connection process
        services = result.services
        inducePairing(0)
    }

    private fun inducePairing(attempts: Int) {
        logState("inducePairing", Pair("attempts", attempts))
        if (gattState != GattState.DISCOVERING_SERVICE
            || (gattState == GattState.INDUCING_PAIRING && attempts < 1)
        ) return // ignored

        gattState = GattState.INDUCING_PAIRING

        val inductionServices = services[description.pairingInduction.service]
        if (inductionServices == null || inductionServices.isEmpty()) return onError(BluetoothStatus.DEVICE_NOT_COMPATIBLE)

        val characteristic = inductionServices.first().getCharacteristic(description.pairingInduction.characteristic)
            ?: return onError(BluetoothStatus.DEVICE_NOT_COMPATIBLE)

        registerBondReceiver()
        gatt?.operate(GattOperation.Read(characteristic, 1, callback = { onInducingPairing(attempts + 1, it) }))
    }

    @SuppressLint("MissingPermission")
    private fun onInducingPairing(attempts: Int, result: GattResult) {
        logState("onInducingPairing", Pair("attempts=$attempts, result", result))
        if (gattState != GattState.INDUCING_PAIRING) return
        // If pairing was triggered by system: state should have changed to BONDING via onBondState()

        if (result is GattResult.Error.Timeout && device.bondState == BluetoothDevice.BOND_NONE) {
            // no result for reading a characteristic and device not paired -> creating bond manually
            gattState = GattState.PAIRING
            if (!device.createBond()) {
                unregisterBondReceiver()
                onError(BluetoothStatus.PAIRING_FAILED)
                return
            }
            // onBondState() to receive bond states asynchronously
            return
        }
        if (result is GattResult.Error.NotExecuted) {
            onError(BluetoothStatus.OPERATION_FAILED)
            return
        }

        if (result !is GattResult.Read) return // unexpected

        val MAX_ATTEMPTS = 3
        val ATTEMPT_DELAY = 500L
        if (result.status != AttStatus.ANDROID_SUCCESS) {
            // READ operation might have failed for encryption or pairing issue:
            // For either cases, the system should trigger the pairing or the encryption
            if (attempts < MAX_ATTEMPTS) {
                // trying again
                GaiaClientService.getTaskManager().schedule({ inducePairing(attempts) }, ATTEMPT_DELAY)
                return
            } else {
                unregisterBondReceiver()
                return onError(BluetoothStatus.CONNECTION_FAILED)
            }
        }

        // it is safe to continue the connection process
        unregisterBondReceiver()
        registerNotifications()
        return
    }

    private fun onBondState(state: Bond, previous: Bond) {
        logState("onBondState", Pair("from=$previous, to", state))
        if (gattState != GattState.INDUCING_PAIRING || gattState != GattState.PAIRING) return

        when (state) {
            Bond.BONDING -> gattState = GattState.PAIRING
            Bond.NONE -> if (previous != Bond.BONDING) onError(BluetoothStatus.PAIRING_FAILED)
            Bond.BONDED -> {
                unregisterBondReceiver()
                registerNotifications()
            }
        }
    }

    private fun registerNotifications() {
        logState("registerNotifications")
        if (gattState != GattState.INDUCING_PAIRING && gattState != GattState.PAIRING) return // ignored

        gattState = GattState.REGISTERING
        gatt?.operate(GattOperation.ConnectionPriority(Priority.HIGH)) // workaround: Android may 'forget' the priority
        registerNotifications(description.notifications.iterator(), services)
    }

    /**
     * Recursively look at each notification from notifications and
     * Once all notifications have been registered, sets the state to connected.
     */
    private fun registerNotifications(
        notifications: Iterator<Notification>,
        services: Map<UUID, List<BluetoothGattService>>
    ) {
        logState("registerNotifications(...)")
        if (gattState != GattState.REGISTERING) return // ignored

        if (!notifications.hasNext()) {
            setMtu() // moving to next connection step
            return
        }

        val notification = notifications.next()
        val notificationServices = services[notification.service]

        if (notificationServices == null || notificationServices.isEmpty()) {
            onRegisterNotificationFail(notification) { registerNotifications(notifications, services) }
            return
        }

        registerNotification(notification, notificationServices.iterator()) {
            registerNotifications(notifications, services)
        }
    }

    private fun registerNotification(
        notification: Notification,
        services: Iterator<BluetoothGattService>,
        next: () -> Unit
    ) {
        logState("registerNotification", Pair("notification", notification))
        val gatt = gatt ?: return // no connection: handled by connection state or connection error
        if (gattState != GattState.REGISTERING) return // ignored

        if (!services.hasNext()) {
            next()
            return
        }

        val service = services.next()
        val characteristic = service.getCharacteristic(notification.characteristic)
            ?: return onRegisterNotificationFail(notification) { registerNotification(notification, services, next) }

        gatt.operate(GattOperation.EnableNotifications(characteristic) {
            if (it is GattResult.GetNotifications && it.status == AttStatus.ANDROID_SUCCESS) {
                registerNotification(notification, services, next)
            } else {
                onRegisterNotificationFail(notification) { registerNotification(notification, services, next) }
            }
        })
    }

    private fun onRegisterNotificationFail(notification: Notification, next: () -> Unit) {
        logState("onRegisterNotificationFail")
        if (notification.mandatory) {
            // device does not provide the expected services for the app
            onError(BluetoothStatus.DEVICE_NOT_COMPATIBLE)
        } else {
            // notification non mandatory, continuing
            next()
        }
    }

    private fun setMtu() {
        logState("setMtu")
        if (gattState != GattState.REGISTERING) return // ignored

        gattState = GattState.MTU
        gatt?.operate(GattOperation.Mtu(Gatt.Mtu.MAX) { onMtu(it) })
    }

    private fun onMtu(result: GattResult) {
        logState("onMtu", Pair("result", result))
        if (gattState != GattState.MTU) return // ignored

        mtu =
            if (result is GattResult.Mtu && result.status == AttStatus.ANDROID_SUCCESS) result.mtu else Gatt.Mtu.DEFAULT

        gattState = GattState.READY // ready
    }

    private fun onError(error: BluetoothStatus) {
        logState("onError", Pair("error", error))
        listener.onConnectionError(error)
        disconnect()
    }

    private fun clean() {
        gatt = null
        services = emptyMap()
        mtu = Gatt.Mtu.DEFAULT
    }

    private fun logState(method: String) {
        Logger.log(LOG_METHODS, TAG, method, Pair("state", gattState))
    }

    private fun logState(method: String, data: Pair<String, Any>) {
        Logger.log(LOG_METHODS, TAG, method, Pair("state", gattState), data)
    }

    private fun logState(method: String, pair1: Pair<String, Any>, pair2: Pair<String, Any>) {
        Logger.log(LOG_METHODS, TAG, method, Pair("state", gattState), pair1, pair2)
    }

    interface Listener {
        val onConnectionState: (ConnectionState) -> Unit
        val onConnectionError: (BluetoothStatus) -> Unit
    }
}

private fun bluetoothStatus(status: GattStatus): BluetoothStatus {
    return when (status) {
        ConnectionStatus.CONN_FAILED_ESTABLISHMENT -> BluetoothStatus.CONNECTION_FAILED
        ConnectionStatus.CONN_L2C_FAILURE -> BluetoothStatus.CONNECTION_FAILED
        ConnectionStatus.CONN_LMP_TIMEOUT -> BluetoothStatus.TIME_OUT
        ConnectionStatus.CONN_NONE -> BluetoothStatus.CONNECTION_FAILED
        ConnectionStatus.CONN_TERMINATED_POWER_OFF -> BluetoothStatus.CONNECTION_FAILED
        ConnectionStatus.CONN_TERMINATE_LOCAL_HOST -> BluetoothStatus.CONNECTION_FAILED
        ConnectionStatus.CONN_TERMINATE_PEER_USER -> BluetoothStatus.CONNECTION_FAILED
        ConnectionStatus.CONN_TIMEOUT -> BluetoothStatus.TIME_OUT
        else -> BluetoothStatus.CONNECTION_FAILED
    }
}

private enum class GattState(val connectionState: ConnectionState) {
    CONNECTING(ConnectionState.CONNECTING),
    DISCOVERING_SERVICE(ConnectionState.CONNECTING),
    INDUCING_PAIRING(ConnectionState.CONNECTING),
    PAIRING(ConnectionState.CONNECTING),
    REGISTERING(ConnectionState.CONNECTING),
    MTU(ConnectionState.CONNECTING),
    READY(ConnectionState.CONNECTED),
    DISCONNECTING(ConnectionState.DISCONNECTING),
    DISCONNECTED(ConnectionState.DISCONNECTED);

    companion object {
        fun valueOf(state: ConnectionState): GattState = when (state) {
            ConnectionState.DISCONNECTED -> DISCONNECTED
            ConnectionState.CONNECTING -> CONNECTING
            ConnectionState.CONNECTED -> DISCOVERING_SERVICE
            ConnectionState.DISCONNECTING -> DISCONNECTING
        }
    }
}
