import 'dart:async';
import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/ota_server.dart';
import 'package:gaia/test_ota_view.dart';
import 'package:get/get.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

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
    return mtu;
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

class _ViewBleConnectionManager extends BleConnectionManager {
  _ViewBleConnectionManager() : super(ble: _NoopBleClient());

  int disconnectCallCount = 0;

  @override
  void startBleStatusMonitor() {}

  @override
  void disconnect() {
    disconnectCallCount += 1;
    super.disconnect();
  }
}

class _SpyOtaServer extends OtaServer {
  _SpyOtaServer({required this.manager})
      : super(
          bleManagerOverride: manager,
          defaultFirmwarePathResolver: () async => '',
        );

  final _ViewBleConnectionManager manager;

  int startUpdateWithVersionCheckCallCount = 0;
  int stopUpgradeCallCount = 0;
  int quickRecoverNowCallCount = 0;
  int disconnectCallCount = 0;
  final List<String> loggedMessages = <String>[];
  final List<String> appliedFirmwarePaths = <String>[];

  @override
  void startUpdateWithVersionCheck() {
    startUpdateWithVersionCheckCallCount += 1;
  }

  @override
  Future<void> stopUpgrade({
    bool sendAbort = true,
    bool sendDisconnect = true,
  }) async {
    stopUpgradeCallCount += 1;
    isUpgrading.value = false;
  }

  @override
  void quickRecoverNow() {
    quickRecoverNowCallCount += 1;
  }

  @override
  void disconnect() {
    disconnectCallCount += 1;
    super.disconnect();
  }

  @override
  void setFirmwarePath(String path) {
    final value = path.trim();
    appliedFirmwarePaths.add(value);
    firmwarePath.value = value;
  }

  @override
  void addLog(String message) {
    loggedMessages.add(message);
    logText.value = '${logText.value}$message\n';
  }
}

class _FakeFilePicker extends FilePicker with MockPlatformInterfaceMixin {
  FilePickerResult? nextResult;

  @override
  Future<FilePickerResult?> pickFiles({
    String? dialogTitle,
    String? initialDirectory,
    FileType type = FileType.any,
    List<String>? allowedExtensions,
    Function(FilePickerStatus)? onFileLoading,
    bool allowCompression = false,
    int compressionQuality = 0,
    bool allowMultiple = false,
    bool withData = false,
    bool withReadStream = false,
    bool lockParentWindow = false,
    bool readSequential = false,
  }) async {
    return nextResult;
  }
}

FilePickerResult _singlePickResult({
  required String name,
  required String? path,
  int size = 1,
}) {
  return FilePickerResult(<PlatformFile>[
    PlatformFile(name: name, size: size, path: path),
  ]);
}

Future<void> _pumpOtaView(WidgetTester tester, _SpyOtaServer server) async {
  Get.put<OtaServer>(server);
  await tester.pumpWidget(
    const GetMaterialApp(
      home: TestOtaView(),
    ),
  );
  await tester.pump();
}

Finder _materialButtonWithTextPrefix(String prefix) {
  return find.byWidgetPredicate((widget) {
    if (widget is! MaterialButton) {
      return false;
    }
    final child = widget.child;
    if (child is! Text) {
      return false;
    }
    final data = child.data ?? '';
    return data.startsWith(prefix);
  });
}

void main() {
  FilePicker? originalFilePicker;
  late _FakeFilePicker fakeFilePicker;

  setUpAll(() {
    TestWidgetsFlutterBinding.ensureInitialized();
    try {
      originalFilePicker = FilePicker.platform;
    } catch (_) {
      originalFilePicker = null;
    }
  });

  setUp(() {
    Get.reset();
    Get.testMode = true;
    fakeFilePicker = _FakeFilePicker();
    FilePicker.platform = fakeFilePicker;
  });

  tearDown(() {
    if (originalFilePicker != null) {
      FilePicker.platform = originalFilePicker!;
    }
  });

  testWidgets('页面基础信息可渲染', (tester) async {
    final manager = _ViewBleConnectionManager();
    final server = _SpyOtaServer(manager: manager);
    server.isDeviceConnected.value = true;
    server.mIsRWCPEnabled.value = true;
    server.vendorMode.value = 'v3';
    server.errorCount.value = 2;
    server.updatePer.value = 12.34;
    server.timeCount.value = 9;
    server.versionBeforeUpgrade.value = '1.0.0';
    server.versionAfterUpgrade.value = '2.0.0';
    server.rwcpStatusText.value = '已启用';
    server.recoveryStatusText.value = '恢复中';
    server.logText.value = 'hello';

    await _pumpOtaView(tester, server);

    expect(find.textContaining('连接状态: 已连接'), findsOneWidget);
    expect(find.textContaining('RWCP模式: 已启用'), findsOneWidget);
    expect(find.textContaining('Vendor模式: V3'), findsOneWidget);
    expect(find.textContaining('错误计数: 2'), findsOneWidget);
    expect(find.text('12.34%'), findsOneWidget);
    expect(find.textContaining('升级前版本: 1.0.0'), findsOneWidget);
    expect(find.textContaining('升级后版本: 2.0.0'), findsOneWidget);
    expect(find.textContaining('版本对比: 已变化'), findsOneWidget);
    expect(find.text('hello'), findsOneWidget);
  });

  testWidgets('固件路径为空时开始升级会被拦截并记日志', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());

    await _pumpOtaView(tester, server);
    final startButton = tester.widget<MaterialButton>(
      _materialButtonWithTextPrefix('开始升级'),
    );
    expect(startButton.onPressed, isNotNull);
    startButton.onPressed?.call();
    await tester.pump();

    expect(server.startUpdateWithVersionCheckCallCount, 0);
    expect(server.loggedMessages, contains('固件路径未设置'));
  });

  testWidgets('固件路径非法时开始升级显示错误并阻止升级', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    final missingPath =
        '${Directory.systemTemp.path}/missing_${DateTime.now().millisecondsSinceEpoch}.bin';

    await _pumpOtaView(tester, server);
    server.firmwarePath.value = missingPath;
    await tester.pump();
    final startButton = tester.widget<MaterialButton>(
      _materialButtonWithTextPrefix('开始升级'),
    );
    expect(startButton.onPressed, isNotNull);

    await tester.runAsync(() async {
      startButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 120));
    });
    await tester.pump();

    expect(server.startUpdateWithVersionCheckCallCount, 0);
    expect(server.loggedMessages.any((m) => m.contains('固件文件不存在')), isTrue);
    expect(find.textContaining('固件文件不存在'), findsWidgets);
  });

  testWidgets('固件有效时点击开始升级触发版本检查流程', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    final tempDir = await tester.runAsync(
      () => Directory.systemTemp.createTemp('view_start_'),
    );
    final firmwareFile = File('${tempDir!.path}/good.bin');
    await tester.runAsync(
      () => firmwareFile.writeAsBytes(<int>[1, 2, 3, 4]),
    );
    await _pumpOtaView(tester, server);
    server.firmwarePath.value = firmwareFile.path;
    await tester.pump();
    final startButton = tester.widget<MaterialButton>(
      _materialButtonWithTextPrefix('开始升级'),
    );
    expect(startButton.onPressed, isNotNull);
    await tester.runAsync(() async {
      startButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.runAsync(
      () => Future<void>.delayed(const Duration(milliseconds: 500)),
    );
    await tester.pump();

    expect(server.startUpdateWithVersionCheckCallCount, 1);
    expect(server.appliedFirmwarePaths, contains(firmwareFile.path));

    await tester.runAsync(() => tempDir.delete(recursive: true));
  });

  testWidgets('升级中可点击取消并调用 stopUpgrade', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    server.isUpgrading.value = true;

    await _pumpOtaView(tester, server);
    final cancelButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '取消升级'),
    );
    cancelButton.onPressed?.call();
    await tester.pump();

    expect(server.stopUpgradeCallCount, 1);
  });

  testWidgets('快速恢复按钮按条件启用并触发 quickRecoverNow', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    server.isUpgrading.value = false;
    server.rwcpStatusText.value = '待启用';
    server.recoveryStatusText.value = '空闲';

    await _pumpOtaView(tester, server);

    MaterialButton button = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '快速恢复'),
    );
    expect(button.onPressed, isNull);

    server.rwcpStatusText.value = '错误已退出';
    await tester.pump();

    button = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '快速恢复'),
    );
    expect(button.onPressed, isNotNull);

    button.onPressed?.call();
    await tester.pump();
    expect(server.quickRecoverNowCallCount, 1);
  });

  testWidgets('点击清空LOG会清空日志内容', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    server.logText.value = 'line-a\nline-b';

    await _pumpOtaView(tester, server);
    final clearButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '清空LOG'),
    );
    clearButton.onPressed?.call();
    await tester.pump();

    expect(server.logText.value, isEmpty);
  });

  testWidgets('文件选择取消时不更新固件路径', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    fakeFilePicker.nextResult = null;

    await _pumpOtaView(tester, server);
    final chooseButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '选择本地固件(.bin)'),
    );
    await tester.runAsync(() async {
      chooseButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(server.appliedFirmwarePaths, isEmpty);
  });

  testWidgets('文件选择无路径时显示提示', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    fakeFilePicker.nextResult =
        _singlePickResult(name: 'no_path.bin', path: null);

    await _pumpOtaView(tester, server);
    final chooseButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '选择本地固件(.bin)'),
    );
    await tester.runAsync(() async {
      chooseButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(find.text('未获取到文件路径，请重试'), findsOneWidget);
    expect(server.appliedFirmwarePaths, isEmpty);
  });

  testWidgets('文件选择非 bin 时显示错误并记录日志', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    final tempDir = await tester.runAsync(
      () => Directory.systemTemp.createTemp('view_txt_'),
    );
    final txtFile = File('${tempDir!.path}/bad.txt');
    await tester.runAsync(() => txtFile.writeAsString('text'));
    fakeFilePicker.nextResult =
        _singlePickResult(name: 'bad.txt', path: txtFile.path);

    await _pumpOtaView(tester, server);
    final chooseButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '选择本地固件(.bin)'),
    );
    await tester.runAsync(() async {
      chooseButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(server.loggedMessages, contains('仅支持 .bin 固件文件'));
    expect(find.text('仅支持 .bin 固件文件'), findsOneWidget);

    await tester.runAsync(() => tempDir.delete(recursive: true));
  });

  testWidgets('文件选择不存在的 bin 时显示错误', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    final tempDir = await tester.runAsync(
      () => Directory.systemTemp.createTemp('view_missing_'),
    );
    final missingPath = '${tempDir!.path}/missing.bin';
    fakeFilePicker.nextResult =
        _singlePickResult(name: 'missing.bin', path: missingPath);

    await _pumpOtaView(tester, server);
    final chooseButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '选择本地固件(.bin)'),
    );
    await tester.runAsync(() async {
      chooseButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(server.loggedMessages.last, contains('固件文件不存在'));
    expect(find.textContaining('固件文件不存在'), findsWidgets);

    await tester.runAsync(() => tempDir.delete(recursive: true));
  });

  testWidgets('文件选择空 bin 时显示错误', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    final tempDir = await tester.runAsync(
      () => Directory.systemTemp.createTemp('view_empty_'),
    );
    final emptyBin = File('${tempDir!.path}/empty.bin');
    await tester.runAsync(() => emptyBin.writeAsBytes(<int>[]));
    fakeFilePicker.nextResult =
        _singlePickResult(name: 'empty.bin', path: emptyBin.path);

    await _pumpOtaView(tester, server);
    final chooseButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '选择本地固件(.bin)'),
    );
    await tester.runAsync(() async {
      chooseButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(server.loggedMessages.last, contains('固件文件为空'));
    expect(find.textContaining('固件文件为空'), findsWidgets);

    await tester.runAsync(() => tempDir.delete(recursive: true));
  });

  testWidgets('文件选择合法 bin 时更新固件路径', (tester) async {
    final server = _SpyOtaServer(manager: _ViewBleConnectionManager());
    final tempDir = await tester.runAsync(
      () => Directory.systemTemp.createTemp('view_good_'),
    );
    final goodBin = File('${tempDir!.path}/picked.bin');
    await tester.runAsync(() => goodBin.writeAsBytes(<int>[0x01]));
    fakeFilePicker.nextResult =
        _singlePickResult(name: 'picked.bin', path: goodBin.path);

    await _pumpOtaView(tester, server);
    final chooseButton = tester.widget<MaterialButton>(
      find.widgetWithText(MaterialButton, '选择本地固件(.bin)'),
    );
    await tester.runAsync(() async {
      chooseButton.onPressed?.call();
      await Future<void>.delayed(const Duration(milliseconds: 100));
    });
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(server.appliedFirmwarePaths, contains(goodBin.path));
    expect(server.firmwarePath.value, goodBin.path);

    await tester.runAsync(() => tempDir.delete(recursive: true));
  });

  testWidgets('页面销毁会调用 disconnect', (tester) async {
    final manager = _ViewBleConnectionManager();
    final server = _SpyOtaServer(manager: manager);

    await _pumpOtaView(tester, server);
    await tester.pumpWidget(const SizedBox.shrink());
    await tester.pump();
    await tester.pump(const Duration(milliseconds: 200));

    expect(server.disconnectCallCount, 1);
    expect(manager.disconnectCallCount, 1);
  });
}
