import 'dart:async';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

import 'package:gaia/utils/ble_constants.dart';

/// BLE 客户端抽象
///
/// 用于隔离具体 BLE 实现，便于单元测试。
abstract class BleClient {
  Stream<BleStatus> get statusStream;

  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  });

  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  });

  Future<void> discoverAllServices(String deviceId);

  Future<List<Service>> getDiscoveredServices(String deviceId);

  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic);

  Future<void> writeCharacteristicWithResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  });

  Future<void> writeCharacteristicWithoutResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  });

  Future<int> requestMtu({required String deviceId, required int mtu});
}

/// FlutterReactiveBle 适配器
class FlutterReactiveBleClient implements BleClient {
  final FlutterReactiveBle _ble;

  FlutterReactiveBleClient(this._ble);

  @override
  Stream<BleStatus> get statusStream => _ble.statusStream;

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    return _ble.scanForDevices(
      withServices: withServices,
      scanMode: scanMode,
      requireLocationServicesEnabled: requireLocationServicesEnabled,
    );
  }

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    return _ble.connectToDevice(
      id: id,
      servicesWithCharacteristicsToDiscover:
          servicesWithCharacteristicsToDiscover,
      connectionTimeout: connectionTimeout,
    );
  }

  @override
  Future<void> discoverAllServices(String deviceId) {
    return _ble.discoverAllServices(deviceId);
  }

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) {
    return _ble.getDiscoveredServices(deviceId);
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    return _ble.subscribeToCharacteristic(characteristic);
  }

  @override
  Future<void> writeCharacteristicWithResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) {
    return _ble.writeCharacteristicWithResponse(characteristic, value: value);
  }

  @override
  Future<void> writeCharacteristicWithoutResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) {
    return _ble.writeCharacteristicWithoutResponse(characteristic,
        value: value);
  }

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) {
    return _ble.requestMtu(deviceId: deviceId, mtu: mtu);
  }
}

/// BLE 数据接收回调
typedef OnDataReceived = void Function(List<int> data);

/// BLE 连接状态变更回调
typedef OnConnectionStateChanged = void Function(
    DeviceConnectionState state, String deviceId);

enum BleScanStartResult {
  started,
  bluetoothDenied,
  locationDenied,
  bluetoothScanDenied,
  bluetoothConnectDenied,
  failed,
}

/// BLE 连接管理器
///
/// 负责 BLE 设备扫描、连接管理、服务发现和特征值读写。
class BleConnectionManager {
  /// BLE 核心实例
  final BleClient ble;

  /// 扫描到的设备列表
  final RxList<DiscoveredDevice> devices = <DiscoveredDevice>[].obs;

  /// 当前连接的设备 ID
  String connectedDeviceId = "";

  /// 设备是否已连接
  bool isDeviceConnected = false;

  /// OTA 服务 UUID
  final Uuid otaServiceUuid = BleConstants.otaServiceUuid;

  /// 通知特征 UUID
  final Uuid notifyCharacteristicUuid = BleConstants.notifyCharacteristicUuid;

  /// 写入特征 UUID
  final Uuid writeCharacteristicUuid = BleConstants.writeCharacteristicUuid;

  /// 无响应写入特征 UUID
  final Uuid writeNoResponseCharacteristicUuid =
      BleConstants.writeNoResponseCharacteristicUuid;

  /// 扫描订阅
  StreamSubscription<DiscoveredDevice>? _scanSubscription;

  /// 连接订阅
  StreamSubscription<ConnectionStateUpdate>? _connectionSubscription;

  /// 通知通道订阅
  StreamSubscription<List<int>>? _notifySubscription;

  /// RWCP 通道订阅
  StreamSubscription<List<int>>? _rwcpSubscription;

  /// BLE 状态订阅
  StreamSubscription<BleStatus>? _bleStatusSubscription;

  /// 连接代数（用于取消过期的连接操作）
  int _connectionGeneration = 0;

  /// 自动重连定时器
  Timer? _reconnectTimer;

  /// 是否启用自动重连
  bool _autoReconnectEnabled = true;

  /// 日志输出回调
  final void Function(String message)? onLog;

  /// 连接状态变更回调
  OnConnectionStateChanged? onConnectionStateChanged;

  /// 构造函数
  BleConnectionManager({
    required this.ble,
    this.onLog,
    this.onConnectionStateChanged,
  });

  /// 便捷构造：使用 FlutterReactiveBle 实例
  factory BleConnectionManager.withFlutterReactiveBle({
    FlutterReactiveBle? ble,
    void Function(String message)? onLog,
    OnConnectionStateChanged? onConnectionStateChanged,
  }) {
    return BleConnectionManager(
      ble: FlutterReactiveBleClient(ble ?? FlutterReactiveBle()),
      onLog: onLog,
      onConnectionStateChanged: onConnectionStateChanged,
    );
  }

  /// 启动 BLE 状态监听
  void startBleStatusMonitor() {
    _bleStatusSubscription?.cancel();
    _bleStatusSubscription = ble.statusStream.listen((event) {
      switch (event) {
        case BleStatus.ready:
          _log("蓝牙打开");
          break;
        case BleStatus.poweredOff:
          _log("蓝牙关闭");
          break;
        case BleStatus.unknown:
          _log("蓝牙状态未知");
          break;
        default:
          _log("蓝牙不可用");
          break;
      }
    });
  }

  /// 开始扫描设备
  Future<BleScanStartResult> startScan() async {
    devices.clear();
    if (Platform.isAndroid) {
      final statuses = await [
        Permission.location,
        Permission.bluetooth,
        Permission.bluetoothScan,
        Permission.bluetoothConnect,
      ].request();
      final location =
          statuses[Permission.location] ?? await Permission.location.status;
      final bluetoothScan = statuses[Permission.bluetoothScan] ??
          await Permission.bluetoothScan.status;
      final bluetoothConnect = statuses[Permission.bluetoothConnect] ??
          await Permission.bluetoothConnect.status;
      if (!location.isGranted) {
        _log("location deny");
        return BleScanStartResult.locationDenied;
      }
      if (!bluetoothScan.isGranted) {
        _log("bluetoothScan deny");
        return BleScanStartResult.bluetoothScanDenied;
      }
      if (!bluetoothConnect.isGranted) {
        _log("bluetoothConnect deny");
        return BleScanStartResult.bluetoothConnectDenied;
      }
    } else {
      var bluetooth = await Permission.bluetooth.status;
      if (!bluetooth.isGranted) {
        _log("bluetooth deny");
        return BleScanStartResult.bluetoothDenied;
      }
    }
    try {
      await _scanSubscription?.cancel();
      await _connectionSubscription?.cancel();
    } catch (e) {
      _log("清理旧连接时出错: $e");
    }
    try {
      _scanSubscription = ble.scanForDevices(
          withServices: [otaServiceUuid],
          scanMode: ScanMode.lowLatency,
          requireLocationServicesEnabled: true).listen((device) {
        if (device.name.isNotEmpty) {
          final knownDeviceIndex = devices.indexWhere((d) => d.id == device.id);
          if (knownDeviceIndex >= 0) {
            devices[knownDeviceIndex] = device;
          } else {
            devices.add(device);
          }
        }
      }, onError: (dynamic error) {
        _log("扫描失败: $error");
      });
      return BleScanStartResult.started;
    } catch (e) {
      _log("扫描启动失败: $e");
      return BleScanStartResult.failed;
    }
  }

  /// 停止扫描
  Future<void> stopScan() async {
    await _scanSubscription?.cancel();
    _scanSubscription = null;
  }

  /// 连接设备
  ///
  /// [deviceId] 设备 ID
  /// [onConnected] 连接成功回调
  /// [onDisconnected] 断开连接回调
  Future<void> connect(
    String deviceId, {
    VoidCallback? onConnected,
    VoidCallback? onDisconnected,
    void Function(Object error)? onError,
  }) async {
    final int generation = ++_connectionGeneration;
    _reconnectTimer?.cancel();
    await _scanSubscription?.cancel();
    await _connectionSubscription?.cancel();
    await _notifySubscription?.cancel();
    await _rwcpSubscription?.cancel();
    _scanSubscription = null;
    _connectionSubscription = null;
    _notifySubscription = null;
    _rwcpSubscription = null;
    _autoReconnectEnabled = true;

    _log('开始连接$deviceId');
    _connectionSubscription = ble
        .connectToDevice(
            id: deviceId, connectionTimeout: const Duration(seconds: 5))
        .listen((connectionState) async {
      if (generation != _connectionGeneration) {
        return;
      }
      final state = connectionState.connectionState;
      if (state == DeviceConnectionState.connected) {
        _reconnectTimer?.cancel();
        isDeviceConnected = true;
        connectedDeviceId = deviceId;
        _log("连接成功$connectedDeviceId");
        onConnectionStateChanged?.call(state, deviceId);
        onConnected?.call();
      } else if (state == DeviceConnectionState.disconnected) {
        isDeviceConnected = false;
        _log('断开连接');
        onConnectionStateChanged?.call(state, deviceId);
        onDisconnected?.call();
        if (_autoReconnectEnabled && connectedDeviceId.isNotEmpty) {
          _scheduleReconnect(generation, onConnected, onDisconnected);
        } else {
          _log("自动重连已关闭，等待手动重连");
        }
      } else {
        isDeviceConnected = false;
        _log('连接状态变更: $state');
        onConnectionStateChanged?.call(state, deviceId);
      }
    }, onError: (Object error) {
      isDeviceConnected = false;
      _log("连接异常: $error");
      onError?.call(error);
      onDisconnected?.call();
    });
  }

  /// 调度重连
  void _scheduleReconnect(
    int expectedGeneration,
    VoidCallback? onConnected,
    VoidCallback? onDisconnected,
  ) {
    _reconnectTimer?.cancel();
    _reconnectTimer = Timer(const Duration(seconds: 5), () {
      if (expectedGeneration != _connectionGeneration) {
        return;
      }
      if (!_autoReconnectEnabled ||
          isDeviceConnected ||
          connectedDeviceId.isEmpty) {
        return;
      }
      connect(connectedDeviceId,
          onConnected: onConnected, onDisconnected: onDisconnected);
    });
  }

  /// 设置是否启用自动重连
  void setAutoReconnectEnabled(bool enabled) {
    _autoReconnectEnabled = enabled;
    if (!enabled) {
      _reconnectTimer?.cancel();
    }
  }

  /// 发现服务（如果需要）
  Future<bool> discoverServicesIfNeeded(String deviceId) async {
    try {
      await ble.discoverAllServices(deviceId);
      final services = await ble.getDiscoveredServices(deviceId);
      if (_hasRequiredOtaService(services)) {
        return true;
      }
      final stopwatch = Stopwatch()..start();
      while (stopwatch.elapsed < const Duration(seconds: 3)) {
        await Future<void>.delayed(const Duration(milliseconds: 120));
        final retryServices = await ble.getDiscoveredServices(deviceId);
        if (_hasRequiredOtaService(retryServices)) {
          return true;
        }
      }
      return false;
    } catch (e) {
      _log("服务发现异常: $e");
      return false;
    }
  }

  /// 检查是否有 OTA 服务
  bool _hasRequiredOtaService(List<Service> services) {
    for (final service in services) {
      if (service.id == otaServiceUuid) {
        return true;
      }
    }
    return false;
  }

  /// 注册通知通道
  Future<bool> registerNotifyChannel(OnDataReceived onDataReceived) async {
    await _notifySubscription?.cancel();
    if (connectedDeviceId.isEmpty) {
      _log("通知注册失败：设备未连接");
      return false;
    }
    if (!await discoverServicesIfNeeded(connectedDeviceId)) {
      _log("通知注册失败：服务未就绪");
      return false;
    }
    final characteristic = QualifiedCharacteristic(
        serviceId: otaServiceUuid,
        characteristicId: notifyCharacteristicUuid,
        deviceId: connectedDeviceId);
    try {
      _notifySubscription =
          ble.subscribeToCharacteristic(characteristic).listen((data) {
        onDataReceived(data);
      }, onError: (dynamic error) {
        _log("通知通道错误: $error");
      });
      return true;
    } catch (e) {
      _log("通知注册异常: $e");
      return false;
    }
  }

  /// 注册 RWCP 通道
  Future<bool> registerRwcpChannel(OnDataReceived onDataReceived) async {
    await _rwcpSubscription?.cancel();
    if (connectedDeviceId.isEmpty) {
      _log("RWCP注册失败：设备未连接");
      return false;
    }
    if (!await discoverServicesIfNeeded(connectedDeviceId)) {
      _log("RWCP注册失败：服务未就绪");
      return false;
    }
    final characteristic = QualifiedCharacteristic(
        serviceId: otaServiceUuid,
        characteristicId: writeNoResponseCharacteristicUuid,
        deviceId: connectedDeviceId);
    try {
      _rwcpSubscription =
          ble.subscribeToCharacteristic(characteristic).listen((data) {
        onDataReceived(data);
      }, onError: (dynamic error) {
        _log("RWCP通道错误: $error");
      });
      return true;
    } catch (e) {
      _log("RWCP注册异常: $e");
      return false;
    }
  }

  /// 取消 RWCP 通道
  Future<void> cancelRwcpChannel() async {
    await _rwcpSubscription?.cancel();
    _rwcpSubscription = null;
  }

  /// 写入数据（带响应）
  Future<void> writeWithResponse(List<int> data) async {
    final characteristic = QualifiedCharacteristic(
        serviceId: otaServiceUuid,
        characteristicId: writeCharacteristicUuid,
        deviceId: connectedDeviceId);
    await ble.writeCharacteristicWithResponse(characteristic, value: data);
  }

  /// 写入数据（无响应）
  Future<void> writeWithoutResponse(List<int> data) async {
    final characteristic = QualifiedCharacteristic(
        serviceId: otaServiceUuid,
        characteristicId: writeNoResponseCharacteristicUuid,
        deviceId: connectedDeviceId);
    await ble.writeCharacteristicWithoutResponse(characteristic, value: data);
  }

  /// 请求 MTU
  Future<int> requestMtu(int mtu) async {
    return await ble.requestMtu(deviceId: connectedDeviceId, mtu: mtu);
  }

  /// 断开连接
  void disconnect() {
    _reconnectTimer?.cancel();
    _connectionSubscription?.cancel();
    _notifySubscription?.cancel();
    _rwcpSubscription?.cancel();
    _connectionSubscription = null;
    _notifySubscription = null;
    _rwcpSubscription = null;
    isDeviceConnected = false;
  }

  /// 释放资源
  void dispose() {
    _bleStatusSubscription?.cancel();
    _scanSubscription?.cancel();
    _connectionSubscription?.cancel();
    _notifySubscription?.cancel();
    _rwcpSubscription?.cancel();
    _reconnectTimer?.cancel();
  }

  /// 输出日志
  void _log(String message) {
    onLog?.call(message);
  }
}
