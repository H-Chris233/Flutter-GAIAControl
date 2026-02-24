package com.wen.gaia.gaia

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.os.Build
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.Locale

class MainActivity : FlutterActivity() {
    private val channelName = "gaia/system_bluetooth"
    private val methodGetConnectedDevices = "getConnectedDevices"

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
            .setMethodCallHandler { call: MethodCall, result: MethodChannel.Result ->
                when (call.method) {
                    methodGetConnectedDevices -> result.success(getConnectedDevices())
                    else -> result.notImplemented()
                }
            }
    }

    private fun getConnectedDevices(): List<Map<String, String>> {
        if (!hasBluetoothConnectPermission()) {
            return emptyList()
        }
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
            ?: return emptyList()
        val adapter = bluetoothManager.adapter ?: return emptyList()
        val connected = linkedMapOf<String, String>()

        // BLE 链接设备（GATT）
        addConnectedDevices(connected, bluetoothManager.getConnectedDevices(android.bluetooth.BluetoothProfile.GATT))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            addConnectedDevices(
                connected,
                bluetoothManager.getConnectedDevices(android.bluetooth.BluetoothProfile.GATT_SERVER)
            )
        }

        // 经典蓝牙（A2DP/HFP）通过反射读取 BluetoothDevice.isConnected
        val bondedDevices = adapter.bondedDevices ?: emptySet()
        for (device in bondedDevices) {
            if (isDeviceConnected(device)) {
                addConnectedDevice(connected, device)
            }
        }

        return connected.map { entry ->
            mapOf(
                "id" to entry.key,
                "name" to entry.value
            )
        }
    }

    private fun hasBluetoothConnectPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }
        return checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) ==
            android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private fun addConnectedDevices(
        out: MutableMap<String, String>,
        devices: List<BluetoothDevice>
    ) {
        for (device in devices) {
            addConnectedDevice(out, device)
        }
    }

    private fun addConnectedDevice(
        out: MutableMap<String, String>,
        device: BluetoothDevice
    ) {
        val address = device.address?.trim()?.uppercase(Locale.US) ?: return
        if (address.isEmpty()) {
            return
        }
        val name = device.name?.trim().orEmpty()
        out[address] = name
    }

    private fun isDeviceConnected(device: BluetoothDevice): Boolean {
        return try {
            val method = device.javaClass.getMethod("isConnected")
            (method.invoke(device) as? Boolean) == true
        } catch (_: Throwable) {
            false
        }
    }
}
