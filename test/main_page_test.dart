import 'dart:async';

import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/ota_server.dart';
import 'package:gaia/main.dart' as app;
import 'package:get/get.dart';

class _NoopBleClient implements BleClient {
  @override
  Stream<BleStatus> get statusStream => const Stream<BleStatus>.empty();

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
    return const Stream<ConnectionStateUpdate>.empty();
  }

  @override
  Future<void> discoverAllServices(String deviceId) async {}

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) async {
    return const <Service>[];
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
    QualifiedCharacteristic characteristic,
  ) {
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

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) async {
    return mtu;
  }
}

class _HomeBleConnectionManager extends BleConnectionManager {
  _HomeBleConnectionManager() : super(ble: _NoopBleClient());

  @override
  void startBleStatusMonitor() {}
}

class _SpyHomeOtaServer extends OtaServer {
  _SpyHomeOtaServer()
      : super(
          bleManagerOverride: _HomeBleConnectionManager(),
          defaultFirmwarePathResolver: () async => '',
        );

  int startScanCallCount = 0;
  int stopScanCallCount = 0;
  int consumeUserMessageCallCount = 0;
  final List<String> connectedDeviceIds = <String>[];

  @override
  Future<void> startScan() async {
    startScanCallCount += 1;
  }

  @override
  Future<void> stopScan() async {
    stopScanCallCount += 1;
  }

  @override
  Future<void> connectDevice(String id) async {
    connectedDeviceIds.add(id);
  }

  @override
  void consumeUserMessage() {
    consumeUserMessageCallCount += 1;
    super.consumeUserMessage();
  }
}

void main() {
  setUp(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    Get.reset();
    Get.testMode = true;
  });

  tearDown(() {
    Get.reset();
  });

  testWidgets('普通消息会显示去设置并执行回调', (tester) async {
    final server = _SpyHomeOtaServer();
    var openSettingsCallCount = 0;

    await tester.pumpWidget(
      GetMaterialApp(
        home: app.MyHomePage(
          title: 'GAIA Control',
          otaServer: server,
          pendingCrashPathReader: () async => null,
          openSettingsAction: () async {
            openSettingsCallCount += 1;
          },
        ),
      ),
    );
    await tester.pump();

    server.userMessage.value = '需要去设置';
    await tester.pump();

    expect(find.text('需要去设置'), findsOneWidget);
    expect(find.text('去设置'), findsOneWidget);

    await tester.tap(find.text('去设置'));
    await tester.pump();

    expect(openSettingsCallCount, 1);
    expect(server.consumeUserMessageCallCount, 1);
  });

  testWidgets('崩溃日志消息会复制路径', (tester) async {
    final server = _SpyHomeOtaServer();
    String? copiedPath;

    await tester.pumpWidget(
      GetMaterialApp(
        home: app.MyHomePage(
          title: 'GAIA Control',
          otaServer: server,
          pendingCrashPathReader: () async => null,
          copyToClipboard: (text) async {
            copiedPath = text;
          },
        ),
      ),
    );
    await tester.pump();

    server.userMessage.value = '检测到上次异常退出，崩溃日志已保存: /tmp/crash.txt';
    await tester.pump();

    expect(find.text('复制路径'), findsOneWidget);

    await tester.tap(find.text('复制路径'));
    await tester.pump();

    expect(copiedPath, '/tmp/crash.txt');
    expect(server.consumeUserMessageCallCount, 1);
  });

  testWidgets('启动时会读取待处理崩溃路径并显示提示', (tester) async {
    final server = _SpyHomeOtaServer();

    await tester.pumpWidget(
      GetMaterialApp(
        home: app.MyHomePage(
          title: 'GAIA Control',
          otaServer: server,
          pendingCrashPathReader: () async => '/tmp/pending.txt',
        ),
      ),
    );
    await tester.pump();
    await tester.pump();

    expect(find.textContaining('/tmp/pending.txt'), findsOneWidget);
    expect(server.consumeUserMessageCallCount, 1);
  });

  testWidgets('连接状态会触发一次导航，断开后再次连接可重新导航', (tester) async {
    final server = _SpyHomeOtaServer();
    var navigateCallCount = 0;

    await tester.pumpWidget(
      GetMaterialApp(
        home: app.MyHomePage(
          title: 'GAIA Control',
          otaServer: server,
          pendingCrashPathReader: () async => null,
          onNavigateToOta: () {
            navigateCallCount += 1;
          },
        ),
      ),
    );
    await tester.pump();

    server.isDeviceConnected.value = true;
    await tester.pump();
    expect(navigateCallCount, 1);

    server.isDeviceConnected.value = true;
    await tester.pump();
    expect(navigateCallCount, 1);

    server.isDeviceConnected.value = false;
    await tester.pump();
    server.isDeviceConnected.value = true;
    await tester.pump();
    expect(navigateCallCount, 2);
  });
}
