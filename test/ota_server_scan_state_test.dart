import 'dart:async';

import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/ota_server.dart';
import 'package:get/get.dart';

class _FakeBleClient implements BleClient {
  final _status = StreamController<BleStatus>.broadcast();

  @override
  Stream<BleStatus> get statusStream => _status.stream;

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    return const Stream.empty();
  }

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    return const Stream.empty();
  }

  @override
  Future<void> discoverAllServices(String deviceId) async {}

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) async {
    return <Service>[];
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    return const Stream.empty();
  }

  @override
  Future<void> writeCharacteristicWithResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {}

  @override
  Future<void> writeCharacteristicWithoutResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {}

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) async {
    return mtu;
  }

  void dispose() {
    _status.close();
  }
}

class _FakeBleConnectionManager extends BleConnectionManager {
  _FakeBleConnectionManager({required this.scanResult})
      : super(ble: _FakeBleClient());

  BleScanStartResult scanResult;

  @override
  void startBleStatusMonitor() {}

  @override
  Future<BleScanStartResult> startScan() async {
    return scanResult;
  }

  @override
  Future<void> stopScan() async {}

  @override
  Future<int> requestMtu(int mtu) async => mtu;
}

void main() {
  setUp(() {
    Get.testMode = true;
  });

  tearDown(() {
    Get.reset();
  });

  test('startScan 拒绝权限时进入错误态并提示用户', () async {
    final fakeManager = _FakeBleConnectionManager(
        scanResult: BleScanStartResult.bluetoothConnectDenied);
    final server = OtaServer(
      bleManagerOverride: fakeManager,
      defaultFirmwarePathResolver: () async => '/tmp/test.bin',
    );
    Get.put<OtaServer>(server);

    await server.startScan();

    expect(server.deviceListUiState.value, DeviceListUiState.error);
    expect(server.isScanning.value, isFalse);
    expect(server.userMessage.value, contains('蓝牙连接权限'));
  });

  test('startScan 启动后超时无设备进入空状态', () async {
    final fakeManager =
        _FakeBleConnectionManager(scanResult: BleScanStartResult.started);
    final server = OtaServer(
      bleManagerOverride: fakeManager,
      defaultFirmwarePathResolver: () async => '/tmp/test.bin',
    );
    Get.put<OtaServer>(server);

    await server.startScan();
    expect(server.deviceListUiState.value, DeviceListUiState.scanning);

    await Future<void>.delayed(const Duration(seconds: 9));

    expect(server.deviceListUiState.value, DeviceListUiState.empty);
    expect(server.isScanning.value, isFalse);
  });

  test('stopScan 在无设备时保持空状态提示', () async {
    final fakeManager =
        _FakeBleConnectionManager(scanResult: BleScanStartResult.started);
    final server = OtaServer(
      bleManagerOverride: fakeManager,
      defaultFirmwarePathResolver: () async => '/tmp/test.bin',
    );
    Get.put<OtaServer>(server);

    await server.startScan();
    await server.stopScan();

    expect(server.deviceListUiState.value, DeviceListUiState.empty);
    expect(server.deviceListHint.value, contains('已停止扫描'));
  });
}
