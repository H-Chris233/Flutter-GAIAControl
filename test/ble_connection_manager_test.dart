import 'dart:async';
import 'dart:typed_data';

import 'package:fake_async/fake_async.dart';
import 'package:flutter/services.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_reactive_ble/src/connected_device_operation.dart';
import 'package:flutter_reactive_ble/src/device_connector.dart';
import 'package:flutter_reactive_ble/src/device_scanner.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:reactive_ble_platform_interface/reactive_ble_platform_interface.dart'
    as reactive_ble;

const MethodChannel _permissionChannel =
    MethodChannel('flutter.baseflow.com/permissions/methods');

List<int> _extractPermissionCodes(dynamic arguments) {
  if (arguments is List) {
    return arguments.whereType<int>().toList(growable: false);
  }
  if (arguments is Map) {
    final fromList = arguments['permissions'];
    if (fromList is List) {
      return fromList.whereType<int>().toList(growable: false);
    }
    final single = arguments['permission'];
    if (single is int) {
      return <int>[single];
    }
  }
  return const <int>[];
}

class _NoopReactiveBlePlatform extends reactive_ble.ReactiveBlePlatform
    with MockPlatformInterfaceMixin {
  final StreamController<reactive_ble.BleStatus> _statusController =
      StreamController<reactive_ble.BleStatus>.broadcast();

  @override
  Stream<reactive_ble.BleStatus> get bleStatusStream =>
      _statusController.stream;

  @override
  Future<void> initialize() async {}

  @override
  Future<void> deinitialize() async {
    await _statusController.close();
  }
}

class _NoopDeviceScanner implements DeviceScanner {
  @override
  ScanSession? get currentScan => null;

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    return const Stream<DiscoveredDevice>.empty();
  }
}

class _NoopDeviceConnector implements DeviceConnector {
  @override
  Stream<ConnectionStateUpdate> get deviceConnectionStateUpdateStream =>
      const Stream<ConnectionStateUpdate>.empty();

  @override
  Stream<ConnectionStateUpdate> connect({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    return const Stream<ConnectionStateUpdate>.empty();
  }

  @override
  Stream<ConnectionStateUpdate> connectToAdvertisingDevice({
    required String id,
    required List<Uuid> withServices,
    required Duration prescanDuration,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    return const Stream<ConnectionStateUpdate>.empty();
  }
}

class _NoopConnectedDeviceOperation implements ConnectedDeviceOperation {
  @override
  Stream<CharacteristicValue> get characteristicValueStream =>
      const Stream<CharacteristicValue>.empty();

  @override
  Future<List<DiscoveredService>> discoverServices(String deviceId) async {
    return const <DiscoveredService>[];
  }

  @override
  Future<List<DiscoveredService>> getDiscoverServices(String deviceId) async {
    return const <DiscoveredService>[];
  }

  @override
  Future<List<int>> readCharacteristic(
      reactive_ble.CharacteristicInstance characteristic) async {
    return const <int>[];
  }

  @override
  Future<int> requestMtu(String deviceId, int mtu) async {
    return mtu;
  }

  @override
  Future<void> requestConnectionPriority(
      String deviceId, ConnectionPriority priority) async {}

  @override
  Stream<List<int>> subscribeToCharacteristic(
    reactive_ble.CharacteristicInstance characteristic,
    Future<void> isDisconnected,
  ) {
    return const Stream<List<int>>.empty();
  }

  @override
  Future<void> writeCharacteristicWithResponse(
    reactive_ble.CharacteristicInstance characteristic, {
    required List<int> value,
  }) async {}

  @override
  Future<void> writeCharacteristicWithoutResponse(
    reactive_ble.CharacteristicInstance characteristic, {
    required List<int> value,
  }) async {}
}

class _NoopLogger implements reactive_ble.Logger {
  reactive_ble.LogLevel _logLevel = reactive_ble.LogLevel.none;

  @override
  reactive_ble.LogLevel get logLevel => _logLevel;

  @override
  set logLevel(reactive_ble.LogLevel level) {
    _logLevel = level;
  }

  @override
  void log(Object message) {}
}

class _FakeService implements Service {
  _FakeService({
    required this.id,
    this.deviceId = 'device-1',
  });

  @override
  final Uuid id;

  @override
  final String deviceId;

  @override
  List<Characteristic> get characteristics => const <Characteristic>[];
}

class _SpyFlutterReactiveBle extends FlutterReactiveBle {
  _SpyFlutterReactiveBle()
      : super.witDependencies(
          deviceScanner: _NoopDeviceScanner(),
          deviceConnector: _NoopDeviceConnector(),
          connectedDeviceOperation: _NoopConnectedDeviceOperation(),
          debugLogger: _NoopLogger(),
          initialization: Future<void>.value(),
          reactiveBlePlatform: _NoopReactiveBlePlatform(),
        );

  final StreamController<BleStatus> statusController =
      StreamController<BleStatus>.broadcast();
  final StreamController<DiscoveredDevice> scanController =
      StreamController<DiscoveredDevice>.broadcast();
  final StreamController<ConnectionStateUpdate> connectionController =
      StreamController<ConnectionStateUpdate>.broadcast();
  final StreamController<List<int>> notifyController =
      StreamController<List<int>>.broadcast();
  List<Service> discoveredServices = <Service>[];
  int mtuResult = 23;

  List<Uuid>? scanWithServices;
  ScanMode? scanMode;
  bool? requireLocationServicesEnabled;
  String? connectId;
  Duration? connectTimeout;
  String? discoverServicesDeviceId;
  String? getDiscoveredServicesDeviceId;
  QualifiedCharacteristic? subscribeCharacteristic;
  QualifiedCharacteristic? writeWithResponseCharacteristic;
  List<int>? writeWithResponseValue;
  QualifiedCharacteristic? writeWithoutResponseCharacteristic;
  List<int>? writeWithoutResponseValue;
  String? requestMtuDeviceId;
  int? requestMtuValue;

  @override
  Stream<BleStatus> get statusStream => statusController.stream;

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    scanWithServices = withServices;
    this.scanMode = scanMode;
    this.requireLocationServicesEnabled = requireLocationServicesEnabled;
    return scanController.stream;
  }

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    connectId = id;
    connectTimeout = connectionTimeout;
    return connectionController.stream;
  }

  @override
  Future<void> discoverAllServices(String deviceId) async {
    discoverServicesDeviceId = deviceId;
  }

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) async {
    getDiscoveredServicesDeviceId = deviceId;
    return discoveredServices;
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    subscribeCharacteristic = characteristic;
    return notifyController.stream;
  }

  @override
  Future<void> writeCharacteristicWithResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {
    writeWithResponseCharacteristic = characteristic;
    writeWithResponseValue = value;
  }

  @override
  Future<void> writeCharacteristicWithoutResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {
    writeWithoutResponseCharacteristic = characteristic;
    writeWithoutResponseValue = value;
  }

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) async {
    requestMtuDeviceId = deviceId;
    requestMtuValue = mtu;
    return mtuResult;
  }

  Future<void> dispose() async {
    await statusController.close();
    await scanController.close();
    await connectionController.close();
    await notifyController.close();
  }
}

class _FakeBleClient implements BleClient {
  final StreamController<BleStatus> statusController =
      StreamController<BleStatus>.broadcast();
  final StreamController<DiscoveredDevice> scanController =
      StreamController<DiscoveredDevice>.broadcast();
  final StreamController<ConnectionStateUpdate> connectionController =
      StreamController<ConnectionStateUpdate>.broadcast();
  final Map<String, StreamController<List<int>>> _characteristicControllers =
      <String, StreamController<List<int>>>{};
  final List<Stream<ConnectionStateUpdate>> queuedConnectionStreams =
      <Stream<ConnectionStateUpdate>>[];
  final List<List<Service>> discoveredServicesQueue = <List<Service>>[];
  final Set<String> subscribeThrowCharacteristicIds = <String>{};
  Stream<DiscoveredDevice>? queuedScanStream;

  bool throwOnDiscoverAll = false;
  bool throwOnScan = false;
  int requestMtuResult = 23;
  int connectInvocationCount = 0;

  String? lastConnectedDeviceId;
  Duration? lastConnectionTimeout;
  List<Uuid>? lastScanServices;
  ScanMode? lastScanMode;
  bool? lastRequireLocationServicesEnabled;
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
    if (throwOnScan) {
      throw StateError('scan failed');
    }
    lastScanServices = withServices;
    lastScanMode = scanMode;
    lastRequireLocationServicesEnabled = requireLocationServicesEnabled;
    if (queuedScanStream != null) {
      final stream = queuedScanStream!;
      queuedScanStream = null;
      return stream;
    }
    return scanController.stream;
  }

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    connectInvocationCount += 1;
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
    if (discoveredServicesQueue.isNotEmpty) {
      return discoveredServicesQueue.removeAt(0);
    }
    return const <Service>[];
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    final key = characteristic.characteristicId.toString();
    if (subscribeThrowCharacteristicIds.contains(key)) {
      throw StateError('subscribe failed');
    }
    return _characteristicControllers
        .putIfAbsent(key, () => StreamController<List<int>>.broadcast())
        .stream;
  }

  void emitCharacteristic(Uuid characteristicId, List<int> data) {
    final key = characteristicId.toString();
    _characteristicControllers[key]?.add(data);
  }

  void emitCharacteristicError(Uuid characteristicId, Object error) {
    final key = characteristicId.toString();
    _characteristicControllers[key]?.addError(error);
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
    await scanController.close();
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
    super.onLog,
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
      TestWidgetsFlutterBinding.ensureInitialized();
      fakeBle = _FakeBleClient();
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(_permissionChannel, (call) async {
        switch (call.method) {
          case 'requestPermissions':
            final codes = _extractPermissionCodes(call.arguments);
            return <int, int>{
              for (final code in codes) code: PermissionStatus.granted.index
            };
          case 'checkPermissionStatus':
            return PermissionStatus.granted.index;
          case 'checkServiceStatus':
            return ServiceStatus.enabled.index;
          default:
            return null;
        }
      });
    });

    tearDown(() async {
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
          .setMockMethodCallHandler(_permissionChannel, null);
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

    test('startScan returns locationDenied on Android when location denied',
        () async {
      final logs = <String>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => true,
        requestAndroidPermissions: () async => <Permission, PermissionStatus>{
          Permission.location: PermissionStatus.denied,
          Permission.bluetoothScan: PermissionStatus.granted,
          Permission.bluetoothConnect: PermissionStatus.granted,
        },
      );

      final result = await manager.startScan();

      expect(result, BleScanStartResult.locationDenied);
      expect(logs, contains('location deny'));
      manager.dispose();
    });

    test('startScan returns bluetoothScanDenied on Android', () async {
      final logs = <String>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => true,
        requestAndroidPermissions: () async => <Permission, PermissionStatus>{
          Permission.location: PermissionStatus.granted,
          Permission.bluetoothScan: PermissionStatus.denied,
          Permission.bluetoothConnect: PermissionStatus.granted,
        },
      );

      final result = await manager.startScan();

      expect(result, BleScanStartResult.bluetoothScanDenied);
      expect(logs, contains('bluetoothScan deny'));
      manager.dispose();
    });

    test('startScan returns bluetoothConnectDenied on Android', () async {
      final logs = <String>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => true,
        requestAndroidPermissions: () async => <Permission, PermissionStatus>{
          Permission.location: PermissionStatus.granted,
          Permission.bluetoothScan: PermissionStatus.granted,
          Permission.bluetoothConnect: PermissionStatus.denied,
        },
      );

      final result = await manager.startScan();

      expect(result, BleScanStartResult.bluetoothConnectDenied);
      expect(logs, contains('bluetoothConnect deny'));
      manager.dispose();
    });

    test('startScan returns bluetoothDenied on non-Android', () async {
      final logs = <String>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => false,
        bluetoothPermissionStatus: () async => PermissionStatus.denied,
      );

      final result = await manager.startScan();

      expect(result, BleScanStartResult.bluetoothDenied);
      expect(logs, contains('bluetooth deny'));
      manager.dispose();
    });

    test('startScan uses default Android permission requester', () async {
      final manager = BleConnectionManager(
        ble: fakeBle,
        isAndroidPlatform: () => true,
      );

      final result = await manager.startScan();

      expect(result, BleScanStartResult.started);
      await manager.stopScan();
      manager.dispose();
    });

    test('startScan uses default platform and bluetooth status closures',
        () async {
      final manager = BleConnectionManager(ble: fakeBle);

      final result = await manager.startScan();

      expect(result, BleScanStartResult.started);
      await manager.stopScan();
      manager.dispose();
    });

    test('startScan started updates discovered devices and handles scan error',
        () async {
      final logs = <String>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => false,
        bluetoothPermissionStatus: () async => PermissionStatus.granted,
      );

      final result = await manager.startScan();
      expect(result, BleScanStartResult.started);
      expect(fakeBle.lastScanServices, equals(<Uuid>[manager.otaServiceUuid]));
      expect(fakeBle.lastScanMode, ScanMode.lowLatency);
      expect(fakeBle.lastRequireLocationServicesEnabled, isTrue);

      fakeBle.scanController.add(DiscoveredDevice(
        id: 'dev-empty',
        name: '',
        serviceData: const <Uuid, Uint8List>{},
        manufacturerData: Uint8List(0),
        rssi: -60,
        serviceUuids: const <Uuid>[],
      ));
      fakeBle.scanController.add(DiscoveredDevice(
        id: 'dev-1',
        name: 'GAIA',
        serviceData: const <Uuid, Uint8List>{},
        manufacturerData: Uint8List(0),
        rssi: -60,
        serviceUuids: const <Uuid>[],
      ));
      fakeBle.scanController.add(DiscoveredDevice(
        id: 'dev-1',
        name: 'GAIA',
        serviceData: const <Uuid, Uint8List>{},
        manufacturerData: Uint8List(0),
        rssi: -20,
        serviceUuids: const <Uuid>[],
      ));
      fakeBle.scanController.addError(StateError('scan stream error'));
      await Future<void>.delayed(Duration.zero);

      expect(manager.devices.length, 1);
      expect(manager.devices.first.id, 'dev-1');
      expect(manager.devices.first.rssi, -20);
      expect(logs.any((log) => log.contains('扫描失败')), isTrue);
      manager.dispose();
    });

    test('startScan returns failed when scan stream creation throws', () async {
      final logs = <String>[];
      fakeBle.throwOnScan = true;
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => false,
        bluetoothPermissionStatus: () async => PermissionStatus.granted,
      );

      final result = await manager.startScan();

      expect(result, BleScanStartResult.failed);
      expect(logs.any((log) => log.contains('扫描启动失败')), isTrue);
      manager.dispose();
    });

    test('stopScan cancels active scan subscription', () async {
      final manager = BleConnectionManager(
        ble: fakeBle,
        isAndroidPlatform: () => false,
        bluetoothPermissionStatus: () async => PermissionStatus.granted,
      );

      final result = await manager.startScan();
      expect(result, BleScanStartResult.started);

      await manager.stopScan();
      fakeBle.scanController.add(DiscoveredDevice(
        id: 'dev-after-stop',
        name: 'GAIA',
        serviceData: const <Uuid, Uint8List>{},
        manufacturerData: Uint8List(0),
        rssi: -30,
        serviceUuids: const <Uuid>[],
      ));
      await Future<void>.delayed(Duration.zero);

      expect(manager.devices, isEmpty);
      manager.dispose();
    });

    test('startScan logs when cleaning previous subscription fails', () async {
      final logs = <String>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        isAndroidPlatform: () => false,
        bluetoothPermissionStatus: () async => PermissionStatus.granted,
      );
      final failOnCancelController = StreamController<DiscoveredDevice>(
        onCancel: () async => throw StateError('cancel failed'),
      );
      fakeBle.queuedScanStream = failOnCancelController.stream;

      await manager.startScan();
      await manager.startScan();

      expect(logs.any((log) => log.contains('清理旧连接时出错')), isTrue);
      await failOnCancelController.close();
      manager.dispose();
    });

    test('connect schedules reconnect after disconnected', () async {
      final manager = BleConnectionManager(ble: fakeBle);
      await manager.connect('device-1');
      fakeBle.connectionController.add(const ConnectionStateUpdate(
        deviceId: 'device-1',
        connectionState: DeviceConnectionState.connected,
        failure: null,
      ));
      await Future<void>.delayed(Duration.zero);

      fakeBle.connectionController.add(const ConnectionStateUpdate(
        deviceId: 'device-1',
        connectionState: DeviceConnectionState.disconnected,
        failure: null,
      ));
      await Future<void>.delayed(const Duration(milliseconds: 50));

      expect(fakeBle.connectInvocationCount, 1);
      await Future<void>.delayed(const Duration(seconds: 6));
      expect(fakeBle.connectInvocationCount, greaterThanOrEqualTo(2));

      manager.dispose();
    });

    test('connect logs intermediate connection state changes', () async {
      final logs = <String>[];
      final states = <DeviceConnectionState>[];
      final manager = BleConnectionManager(
        ble: fakeBle,
        onLog: logs.add,
        onConnectionStateChanged: (state, _) => states.add(state),
      );

      await manager.connect('device-1');
      fakeBle.connectionController.add(const ConnectionStateUpdate(
        deviceId: 'device-1',
        connectionState: DeviceConnectionState.connecting,
        failure: null,
      ));
      await Future<void>.delayed(Duration.zero);

      expect(states, contains(DeviceConnectionState.connecting));
      expect(logs.any((log) => log.contains('连接状态变更')), isTrue);
      manager.dispose();
    });

    test('discoverServicesIfNeeded succeeds after retry discovers OTA service',
        () {
      final manager = BleConnectionManager(ble: fakeBle);
      fakeBle.discoveredServicesQueue
        ..add(<Service>[])
        ..add(<Service>[_FakeService(id: manager.otaServiceUuid)]);

      bool? result;
      fakeAsync((async) {
        manager.discoverServicesIfNeeded('device-1').then((value) {
          result = value;
        });
        async.flushMicrotasks();
        async.elapse(const Duration(milliseconds: 121));
        async.flushMicrotasks();
      });

      expect(result, isTrue);
      manager.dispose();
    });

    test('discoverServicesIfNeeded returns false when OTA service not found',
        () async {
      final manager = BleConnectionManager(ble: fakeBle);
      final result = await manager.discoverServicesIfNeeded('device-1');
      expect(result, isFalse);
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

    test('stale disconnected event from old generation is ignored', () async {
      var disconnectedCalled = 0;
      final manager = BleConnectionManager(ble: fakeBle);
      final oldController = StreamController<ConnectionStateUpdate>.broadcast();
      final newController = StreamController<ConnectionStateUpdate>.broadcast();
      fakeBle.queuedConnectionStreams.add(oldController.stream);
      fakeBle.queuedConnectionStreams.add(newController.stream);

      unawaited(manager.connect(
        'old-device',
        onDisconnected: () => disconnectedCalled += 1,
      ));
      await manager.connect(
        'new-device',
        onDisconnected: () => disconnectedCalled += 1,
      );

      oldController.add(const ConnectionStateUpdate(
        deviceId: 'old-device',
        connectionState: DeviceConnectionState.disconnected,
        failure: null,
      ));
      await Future<void>.delayed(Duration.zero);
      expect(disconnectedCalled, 0);

      newController.add(const ConnectionStateUpdate(
        deviceId: 'new-device',
        connectionState: DeviceConnectionState.disconnected,
        failure: null,
      ));
      await Future<void>.delayed(Duration.zero);
      expect(disconnectedCalled, 1);

      await oldController.close();
      await newController.close();
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

    test('registerNotifyChannel returns false when device is not connected',
        () async {
      final logs = <String>[];
      final manager = BleConnectionManager(ble: fakeBle, onLog: logs.add);

      final ready = await manager.registerNotifyChannel((_) {});

      expect(ready, isFalse);
      expect(logs, contains('通知注册失败：设备未连接'));
      manager.dispose();
    });

    test('registerNotifyChannel handles subscribe exception', () async {
      final logs = <String>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
        onLog: logs.add,
      );
      manager.connectedDeviceId = 'device-1';
      fakeBle.subscribeThrowCharacteristicIds
          .add(manager.notifyCharacteristicUuid.toString());

      final ready = await manager.registerNotifyChannel((_) {});

      expect(ready, isFalse);
      expect(logs.any((log) => log.contains('通知注册异常')), isTrue);
      manager.dispose();
    });

    test('registerNotifyChannel logs stream error from notify subscription',
        () async {
      final logs = <String>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
        onLog: logs.add,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerNotifyChannel((_) {});
      expect(ready, isTrue);

      fakeBle.emitCharacteristicError(
          manager.notifyCharacteristicUuid, StateError('notify error'));
      await Future<void>.delayed(Duration.zero);

      expect(logs.any((log) => log.contains('通知通道错误')), isTrue);
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

    test('registerRwcpChannel returns false when device is not connected',
        () async {
      final logs = <String>[];
      final manager = BleConnectionManager(ble: fakeBle, onLog: logs.add);

      final ready = await manager.registerRwcpChannel((_) {});

      expect(ready, isFalse);
      expect(logs, contains('RWCP注册失败：设备未连接'));
      manager.dispose();
    });

    test('registerRwcpChannel handles subscribe exception', () async {
      final logs = <String>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
        onLog: logs.add,
      );
      manager.connectedDeviceId = 'device-1';
      fakeBle.subscribeThrowCharacteristicIds
          .add(manager.writeNoResponseCharacteristicUuid.toString());

      final ready = await manager.registerRwcpChannel((_) {});

      expect(ready, isFalse);
      expect(logs.any((log) => log.contains('RWCP注册异常')), isTrue);
      manager.dispose();
    });

    test('registerRwcpChannel logs stream error from rwcp subscription',
        () async {
      final logs = <String>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
        onLog: logs.add,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerRwcpChannel((_) {});
      expect(ready, isTrue);

      fakeBle.emitCharacteristicError(
          manager.writeNoResponseCharacteristicUuid, StateError('rwcp error'));
      await Future<void>.delayed(Duration.zero);

      expect(logs.any((log) => log.contains('RWCP通道错误')), isTrue);
      manager.dispose();
    });

    test('cancelRwcpChannel cancels rwcp subscription', () async {
      final received = <List<int>>[];
      final manager = _TestableBleConnectionManager(
        ble: fakeBle,
        discoverResult: true,
      );
      manager.connectedDeviceId = 'device-1';

      final ready = await manager.registerRwcpChannel(received.add);
      expect(ready, isTrue);

      await manager.cancelRwcpChannel();
      fakeBle.emitCharacteristic(
          manager.writeNoResponseCharacteristicUuid, <int>[0x31]);
      await Future<void>.delayed(Duration.zero);

      expect(received, isEmpty);
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

    test('FlutterReactiveBleClient delegates all operations', () async {
      final ble = _SpyFlutterReactiveBle();
      final client = FlutterReactiveBleClient(ble);
      final qc = QualifiedCharacteristic(
        serviceId: Uuid.parse('00001100-d102-11e1-9b23-00025b00a5a5'),
        characteristicId: Uuid.parse('00001101-d102-11e1-9b23-00025b00a5a5'),
        deviceId: 'device-1',
      );
      ble.discoveredServices = <Service>[
        _FakeService(id: Uuid.parse('00001100-d102-11e1-9b23-00025b00a5a5')),
      ];
      ble.mtuResult = 247;

      final statusFuture = client.statusStream.first;
      ble.statusController.add(BleStatus.ready);
      expect(await statusFuture, BleStatus.ready);

      final scan = client.scanForDevices(
        withServices: <Uuid>[
          Uuid.parse('00001100-d102-11e1-9b23-00025b00a5a5')
        ],
        scanMode: ScanMode.lowLatency,
        requireLocationServicesEnabled: false,
      );
      final scanFuture = scan.first;
      ble.scanController.add(DiscoveredDevice(
        id: 'scan-dev',
        name: 'GAIA',
        serviceData: const <Uuid, Uint8List>{},
        manufacturerData: Uint8List(0),
        rssi: -44,
        serviceUuids: const <Uuid>[],
      ));
      expect((await scanFuture).id, 'scan-dev');
      expect(ble.scanMode, ScanMode.lowLatency);
      expect(ble.requireLocationServicesEnabled, isFalse);

      final connectFuture = client
          .connectToDevice(
              id: 'device-1', connectionTimeout: const Duration(seconds: 2))
          .first;
      ble.connectionController.add(const ConnectionStateUpdate(
        deviceId: 'device-1',
        connectionState: DeviceConnectionState.connected,
        failure: null,
      ));
      expect((await connectFuture).connectionState,
          DeviceConnectionState.connected);
      expect(ble.connectId, 'device-1');
      expect(ble.connectTimeout, const Duration(seconds: 2));

      await client.discoverAllServices('device-1');
      expect(ble.discoverServicesDeviceId, 'device-1');
      final services = await client.getDiscoveredServices('device-1');
      expect(ble.getDiscoveredServicesDeviceId, 'device-1');
      expect(services, hasLength(1));

      final notifyFuture = client.subscribeToCharacteristic(qc).first;
      ble.notifyController.add(<int>[0x10, 0x11]);
      expect(await notifyFuture, <int>[0x10, 0x11]);
      expect(
          ble.subscribeCharacteristic?.characteristicId, qc.characteristicId);

      await client.writeCharacteristicWithResponse(qc, value: <int>[0x01]);
      await client.writeCharacteristicWithoutResponse(qc, value: <int>[0x02]);
      expect(ble.writeWithResponseCharacteristic?.characteristicId,
          qc.characteristicId);
      expect(ble.writeWithResponseValue, <int>[0x01]);
      expect(ble.writeWithoutResponseCharacteristic?.characteristicId,
          qc.characteristicId);
      expect(ble.writeWithoutResponseValue, <int>[0x02]);

      final mtu = await client.requestMtu(deviceId: 'device-1', mtu: 300);
      expect(mtu, 247);
      expect(ble.requestMtuDeviceId, 'device-1');
      expect(ble.requestMtuValue, 300);

      await ble.dispose();
    });

    test('withFlutterReactiveBle factory uses wrapped client', () async {
      final ble = _SpyFlutterReactiveBle();
      final logs = <String>[];
      final manager = BleConnectionManager.withFlutterReactiveBle(
        ble: ble,
        onLog: logs.add,
      );

      manager.startBleStatusMonitor();
      ble.statusController.add(BleStatus.ready);
      await Future<void>.delayed(Duration.zero);

      expect(manager.ble, isA<FlutterReactiveBleClient>());
      expect(logs, contains('蓝牙打开'));

      manager.dispose();
      await ble.dispose();
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
