import 'dart:async';

import 'package:flutter/foundation.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/ota_server.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';
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
  int stopScanCalled = 0;
  int connectCalled = 0;
  int startBleStatusMonitorCalled = 0;
  final List<List<int>> writeWithResponsePayloads = <List<int>>[];

  @override
  void startBleStatusMonitor() {
    startBleStatusMonitorCalled += 1;
  }

  @override
  Future<BleScanStartResult> startScan() async {
    startScanCalled = true;
    return BleScanStartResult.started;
  }

  @override
  Future<void> stopScan() async {
    stopScanCalled += 1;
  }

  @override
  Future<void> connect(
    String deviceId, {
    VoidCallback? onConnected,
    VoidCallback? onDisconnected,
    void Function(Object error)? onError,
  }) async {
    connectCalled += 1;
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
      server = OtaServer(
        bleManagerOverride: fakeBleManager,
        defaultFirmwarePathResolver: () async => '/tmp/test_firmware.bin',
      );
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

    test('connectDevice stops scan before connecting', () async {
      await server.connectDevice('device-1');

      expect(fakeBleManager.stopScanCalled, 1);
      expect(fakeBleManager.connectCalled, 1);
    });

    test('receiveVMUPacket handles transfer complete and sends confirmation',
        () async {
      server.isUpgrading.value = true;
      final packet = VMUPacket.get(OpCodes.upgradeTransferCompleteInd);

      server.receiveVMUPacket(packet.getBytes());
      await Future<void>.delayed(Duration.zero);

      expect(server.transFerComplete, isTrue);
      expect(fakeBleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('onUpgradeComplete sends upgrade disconnect packet', () async {
      server.isUpgrading.value = true;

      server.onUpgradeComplete();
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
      expect(fakeBleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('onRequestNextDataPacket uses absolute file offset', () async {
      server.isUpgrading.value = true;
      server.mIsRWCPEnabled.value = false;
      server.mBytesFile = List<int>.generate(100, (index) => index);
      server.mStartOffset = 50;
      server.mMaxLengthForDataTransfer = 64;

      server.onRequestNextDataPacket(10, 20);
      await Future<void>.delayed(Duration.zero);

      expect(fakeBleManager.writeWithResponsePayloads, isNotEmpty);
      final sent = fakeBleManager.writeWithResponsePayloads.last;
      final gaia = GaiaPacketBLE.fromByte(sent);
      expect(gaia, isNotNull);
      final vmu = VMUPacket.getPackageFromByte(gaia!.mPayload ?? []);
      expect(vmu, isNotNull);
      expect(vmu!.mOpCode, OpCodes.upgradeData);
      expect(vmu.mData![0], 0x00);
      expect(vmu.mData!.sublist(1), List<int>.generate(10, (i) => 20 + i));
    });
  });
}
