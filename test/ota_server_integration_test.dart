import 'dart:async';

import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/ota_server.dart';
import 'package:gaia/utils/gaia/op_codes.dart';
import 'package:gaia/utils/gaia/vmu_packet.dart';

class _NoopBleClient implements BleClient {
  @override
  Stream<BleStatus> get statusStream => const Stream<BleStatus>.empty();

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    return const Stream<ConnectionStateUpdate>.empty();
  }

  @override
  Future<void> discoverAllServices(String deviceId) async {}

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) async {
    return const <Service>[];
  }

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) async {
    return 23;
  }

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    return const Stream<DiscoveredDevice>.empty();
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    return const Stream<List<int>>.empty();
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
}

class _FakeBleConnectionManager extends BleConnectionManager {
  _FakeBleConnectionManager() : super(ble: _NoopBleClient());

  bool startScanCalled = false;
  int startBleStatusMonitorCalled = 0;
  final List<List<int>> writeWithResponsePayloads = <List<int>>[];

  @override
  void startBleStatusMonitor() {
    startBleStatusMonitorCalled += 1;
  }

  @override
  Future<void> startScan() async {
    startScanCalled = true;
  }

  @override
  Future<void> writeWithResponse(List<int> data) async {
    writeWithResponsePayloads.add(List<int>.from(data));
  }
}

void main() {
  group('OtaServer integration', () {
    late OtaServer server;
    late _FakeBleConnectionManager fakeBleManager;

    setUp(() {
      fakeBleManager = _FakeBleConnectionManager();
      server = OtaServer(bleManagerOverride: fakeBleManager);
      server.onInit();
      server.connectDeviceId = 'device-1';
    });

    tearDown(() {
      server.onClose();
    });

    test('onInit starts BLE status monitor via BleConnectionManager', () {
      expect(fakeBleManager.startBleStatusMonitorCalled, 1);
    });

    test('startScan delegates to BleConnectionManager', () {
      server.startScan();
      expect(fakeBleManager.startScanCalled, isTrue);
    });

    test('receiveVMUPacket handles transfer complete and sends confirmation',
        () async {
      server.isUpgrading = true;
      final packet = VMUPacket.get(OpCodes.upgradeTransferCompleteInd);

      server.receiveVMUPacket(packet.getBytes());
      await Future<void>.delayed(Duration.zero);

      expect(server.transFerComplete, isTrue);
      expect(fakeBleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('onUpgradeComplete sends upgrade disconnect packet', () async {
      server.isUpgrading = true;

      server.onUpgradeComplete();
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading, isFalse);
      expect(fakeBleManager.writeWithResponsePayloads, isNotEmpty);
    });
  });
}
