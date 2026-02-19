import 'dart:async';

import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';

class _FakeBleClient implements BleClient {
  final StreamController<BleStatus> statusController =
      StreamController<BleStatus>.broadcast();
  final StreamController<ConnectionStateUpdate> connectionController =
      StreamController<ConnectionStateUpdate>.broadcast();
  final Map<String, StreamController<List<int>>> _characteristicControllers =
      <String, StreamController<List<int>>>{};
  final List<Stream<ConnectionStateUpdate>> queuedConnectionStreams =
      <Stream<ConnectionStateUpdate>>[];

  bool throwOnDiscoverAll = false;
  int requestMtuResult = 23;

  String? lastConnectedDeviceId;
  Duration? lastConnectionTimeout;
  QualifiedCharacteristic? lastWriteWithResponseCharacteristic;
  List<int>? lastWriteWithResponseValue;
  QualifiedCharacteristic? lastWriteWithoutResponseCharacteristic;
  List<int>? lastWriteWithoutResponseValue;
  String? lastRequestedMtuDeviceId;
  int? lastRequestedMtu;

  @override
  Stream<BleStatus> get statusStream => statusController.stream;

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    return const Stream<DiscoveredDevice>.empty();
  }

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    lastConnectedDeviceId = id;
    lastConnectionTimeout = connectionTimeout;
    if (queuedConnectionStreams.isNotEmpty) {
      return queuedConnectionStreams.removeAt(0);
    }
    return connectionController.stream;
  }

  @override
  Future<void> discoverAllServices(String deviceId) async {
    if (throwOnDiscoverAll) {
      throw Exception('discover failed');
    }
  }

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) async {
    return const <Service>[];
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    final key = characteristic.characteristicId.toString();
    return _characteristicControllers
        .putIfAbsent(key, () => StreamController<List<int>>.broadcast())
        .stream;
  }

  void emitCharacteristic(Uuid characteristicId, List<int> data) {
    final key = characteristicId.toString();
    _characteristicControllers[key]?.add(data);
  }

  @override
  Future<void> writeCharacteristicWithResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {
    lastWriteWithResponseCharacteristic = characteristic;
    lastWriteWithResponseValue = value;
  }

  @override
  Future<void> writeCharacteristicWithoutResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {
    lastWriteWithoutResponseCharacteristic = characteristic;
    lastWriteWithoutResponseValue = value;
  }

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) async {
    lastRequestedMtuDeviceId = deviceId;
    lastRequestedMtu = mtu;
    return requestMtuResult;
  }

  Future<void> dispose() async {
    await statusController.close();
    await connectionController.close();
    for (final controller in _characteristicControllers.values) {
      await controller.close();
    }
  }
}

class _TestableBleConnectionManager extends BleConnectionManager {
  final bool discoverResult;

  _TestableBleConnectionManager({
    required super.ble,
    required this.discoverResult,
  });

  @override
  Future<bool> discoverServicesIfNeeded(String deviceId) async {
    return discoverResult;
  }
}

void main() {
  group('BleConnectionManager', () {
    late _FakeBleClient fakeBle;

    setUp(() {
      fakeBle = _FakeBleClient();
    });

    tearDown(() async {
      await fakeBle.dispose();
    });

    test('startBleStatusMonitor maps status to logs', () async {
      final logs = <String>[];
      final manager = BleConnectionManager(ble: fakeBle, onLog: logs.add);

      manager.startBleStatusMonitor();
      fakeBle.statusController.add(BleStatus.ready);
      fakeBle.statusController.add(BleStatus.poweredOff);
      fakeBle.statusController.add(BleStatus.unknown);
      fakeBle.statusController.add(BleStatus.unsupported);
      await Future<void>.delayed(Duration.zero);

      expect(logs, contains('蓝牙打开'));
      expect(logs, contains('蓝牙关闭'));
      expect(logs, contains('蓝牙状态未知'));
      expect(logs, contains('蓝牙不可用'));

      manager.dispose();
    });

    test('connect updates state and triggers callbacks', () async {
      final stateEvents = <DeviceConnectionState>[];
      var connectedCalled = false;
      var disconnectedCalled = false;
      final manager = BleConnectionManager(
        ble: fakeBle,
        onConnectionStateChanged: (state, _) => stateEvents.add(state),
      );

      await manager.connect(
        'device-1',
        onConnected: () => connectedCalled = true,
        onDisconnected: () => disconnectedCalled = true,
      );

      fakeBle.connectionController.add(const ConnectionStateUpdate(
        deviceId: 'device-1',
        connectionState: DeviceConnectionState.connected,
        failure: null,
      ));
      await Future<void>.delayed(Duration.zero);

      expect(manager.isDeviceConnected, isTrue);
      expect(manager.connectedDeviceId, 'device-1');
      expect(connectedCalled, isTrue);
      expect(stateEvents, contains(DeviceConnectionState.connected));
      expect(fakeBle.lastConnectionTimeout, const Duration(seconds: 5));

      manager.setAutoReconnectEnabled(false);
      fakeBle.connectionController.add(const ConnectionStateUpdate(
        deviceId: 'device-1',
        connectionState: DeviceConnectionState.disconnected,
        failure: null,
      ));
      await Future<void>.delayed(Duration.zero);

      expect(manager.isDeviceConnected, isFalse);
      expect(disconnectedCalled, isTrue);
      expect(stateEvents, contains(DeviceConnectionState.disconnected));

      manager.dispose();
    });

    test('stale onError from old generation is ignored', () async {
      var disconnectedCalled = 0;
      var onErrorCalled = 0;
      final manager = BleConnectionManager(ble: fakeBle);
      fakeBle.queuedConnectionStreams
          .add(Stream<ConnectionStateUpdate>.fromFuture(
        Future<ConnectionStateUpdate>.error(StateError('stale error')),
      ));
      fakeBle.queuedConnectionStreams
          .add(const Stream<ConnectionStateUpdate>.empty());

      unawaited(manager.connect(
        'old-device',
        onDisconnected: () => disconnectedCalled += 1,
        onError: (_) => onErrorCalled += 1,
      ));
      await manager.connect(
        'new-device',
        onDisconnected: () => disconnectedCalled += 1,
        onError: (_) => onErrorCalled += 1,
      );
      await Future<void>.delayed(const Duration(milliseconds: 10));

      expect(disconnectedCalled, 0);
      expect(onErrorCalled, 0);
      manager.dispose();
    });

    test('onError from current generation triggers callbacks', () async {
      var disconnectedCalled = 0;
      var onErrorCalled = 0;
      final manager = BleConnectionManager(ble: fakeBle);
      fakeBle.queuedConnectionStreams
          .add(Stream<ConnectionStateUpdate>.fromFuture(
        Future<ConnectionStateUpdate>.error(StateError('current error')),
      ));

      await manager.connect(
        'device-1',
        onDisconnected: () => disconnectedCalled += 1,
        onError: (_) => onErrorCalled += 1,
      );
      await Future<void>.delayed(const Duration(milliseconds: 10));

      expect(disconnectedCalled, 1);
      expect(onErrorCalled, 1);
      expect(manager.isDeviceConnected, isFalse);
      manager.dispose();
    });

    test('discoverServicesIfNeeded returns false and logs on exception',
        () async {
      final logs = <String>[];
      final manager = BleConnectionManager(ble: fakeBle, onLog: logs.add);
      fakeBle.throwOnDiscoverAll = true;

      final result = await manager.discoverServicesIfNeeded('device-1');

      expect(result, isFalse);
      expect(logs.any((x) => x.contains('服务发现异常')), isTrue);

      manager.dispose();
    });

    test('registerNotifyChannel forwards received data', () async {
      final received = <List<int>>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerNotifyChannel((data) {
        received.add(data);
      });

      fakeBle.emitCharacteristic(manager.notifyCharacteristicUuid, <int>[1, 2]);
      await Future<void>.delayed(Duration.zero);

      expect(ready, isTrue);
      expect(
          received,
          equals(<List<int>>[
            <int>[1, 2]
          ]));

      manager.dispose();
    });

    test('registerNotifyChannel returns false when service is not ready',
        () async {
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: false,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerNotifyChannel((_) {});

      expect(ready, isFalse);
      manager.dispose();
    });

    test('registerRwcpChannel forwards received data', () async {
      final received = <List<int>>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerRwcpChannel((data) {
        received.add(data);
      });

      fakeBle.emitCharacteristic(
          manager.writeNoResponseCharacteristicUuid, <int>[0x11, 0x22]);
      await Future<void>.delayed(Duration.zero);

      expect(ready, isTrue);
      expect(
          received,
          equals(<List<int>>[
            <int>[0x11, 0x22]
          ]));
      manager.dispose();
    });

    test('registerRwcpChannel returns false when service is not ready',
        () async {
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: false,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerRwcpChannel((_) {});

      expect(ready, isFalse);
      manager.dispose();
    });

    test('write and mtu methods call BleClient with expected arguments',
        () async {
      final manager = BleConnectionManager(ble: fakeBle);
      manager.connectedDeviceId = 'device-1';

      await manager.writeWithResponse(<int>[0x01]);
      await manager.writeWithoutResponse(<int>[0x02]);
      fakeBle.requestMtuResult = 185;
      final mtu = await manager.requestMtu(517);

      expect(
        fakeBle.lastWriteWithResponseCharacteristic?.characteristicId,
        manager.writeCharacteristicUuid,
      );
      expect(fakeBle.lastWriteWithResponseValue, equals(<int>[0x01]));
      expect(
        fakeBle.lastWriteWithoutResponseCharacteristic?.characteristicId,
        manager.writeNoResponseCharacteristicUuid,
      );
      expect(fakeBle.lastWriteWithoutResponseValue, equals(<int>[0x02]));
      expect(fakeBle.lastRequestedMtuDeviceId, 'device-1');
      expect(fakeBle.lastRequestedMtu, 517);
      expect(mtu, 185);

      manager.dispose();
    });

    test('disconnect marks device as disconnected', () {
      final manager = BleConnectionManager(ble: fakeBle);
      manager.isDeviceConnected = true;

      manager.disconnect();

      expect(manager.isDeviceConnected, isFalse);
      manager.dispose();
    });
  });
}
