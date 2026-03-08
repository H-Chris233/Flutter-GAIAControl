import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

import 'controller/ota_server.dart';
import 'test_ota_view.dart';
import 'utils/crash_reporter.dart';
import 'utils/log.dart';

typedef PendingCrashPathReader = Future<String?> Function();
typedef SettingsOpener = Future<void> Function();
typedef ClipboardWriter = Future<void> Function(String text);

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await CrashReporter.init();
    CrashReporter.maybeInstance?.installGlobalHandlers();
  } catch (e, s) {
    // 关键初始化失败不能静默吞掉：至少在 debug 下输出，便于排障。
    Log.e("main", "CrashReporter init/install failed: $e\n$s");
  }
  runZonedGuarded(
    () {
      runApp(const MyApp());
    },
    (error, stack) {
      final reporter = CrashReporter.maybeInstance;
      if (reporter != null) {
        unawaited(
          reporter.recordError(error, stack, context: "runZonedGuarded"),
        );
      }
    },
  );
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return GetMaterialApp(
      title: 'GAIA Control',
      theme: ThemeData(
        primarySwatch: Colors.blue,
      ),
      home: const MyHomePage(title: 'GAIA Control'),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({
    super.key,
    required this.title,
    this.otaServer,
    this.pendingCrashPathReader,
    this.openSettingsAction,
    this.copyToClipboard,
    this.onNavigateToOta,
  });

  final String title;
  final OtaServer? otaServer;
  final PendingCrashPathReader? pendingCrashPathReader;
  final SettingsOpener? openSettingsAction;
  final ClipboardWriter? copyToClipboard;
  final VoidCallback? onNavigateToOta;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Worker? _messageWorker;
  Worker? _connectionWorker;
  bool _navigatedToOta = false;
  late final OtaServer _ota;

  @override
  void initState() {
    super.initState();
    _ota = _resolveOtaServer();
    _messageWorker = ever<String?>(_ota.userMessage, (message) {
      if (message == null || message.isEmpty || !mounted) {
        return;
      }
      final isCrashReport = message.contains("崩溃日志已保存:");
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(
          SnackBar(
            content: Text(message),
            action: SnackBarAction(
              label: isCrashReport ? '复制路径' : '去设置',
              onPressed: () {
                if (!isCrashReport) {
                  unawaited(_openSettings());
                  return;
                }
                final path = message.split("崩溃日志已保存:").last.trim();
                if (path.isEmpty) {
                  return;
                }
                unawaited(_writeClipboard(path));
              },
            ),
          ),
        );
      _ota.consumeUserMessage();
    });

    unawaited(() async {
      final last = await _readPendingCrashPath();
      if (!mounted || last == null) {
        return;
      }
      _ota.userMessage.value = "检测到上次异常退出，崩溃日志已保存: $last";
    }());

    _connectionWorker = ever<bool>(
      _ota.isDeviceConnected,
      (connected) {
        if (!mounted) return;
        if (!connected) {
          _navigatedToOta = false;
          return;
        }
        if (_navigatedToOta) return;
        _navigatedToOta = true;
        _navigateToOtaPage();
      },
    );
  }

  OtaServer _resolveOtaServer() {
    final injected = widget.otaServer;
    if (injected != null) {
      if (Get.isRegistered<OtaServer>()) {
        final registered = Get.find<OtaServer>();
        if (!identical(registered, injected)) {
          Get.replace<OtaServer>(injected);
        }
      } else {
        Get.put<OtaServer>(injected);
      }
      return injected;
    }
    if (Get.isRegistered<OtaServer>()) {
      return Get.find<OtaServer>();
    }
    return Get.put<OtaServer>(OtaServer());
  }

  Future<String?> _readPendingCrashPath() async {
    final reader = widget.pendingCrashPathReader;
    if (reader != null) {
      return reader();
    }
    return CrashReporter.maybeInstance?.consumePendingReportPath();
  }

  Future<void> _openSettings() async {
    final action = widget.openSettingsAction;
    if (action != null) {
      await action();
      return;
    }
    await openAppSettings();
  }

  Future<void> _writeClipboard(String text) async {
    final writer = widget.copyToClipboard;
    if (writer != null) {
      await writer(text);
      return;
    }
    await Clipboard.setData(ClipboardData(text: text));
  }

  void _navigateToOtaPage() {
    final callback = widget.onNavigateToOta;
    if (callback != null) {
      callback();
      return;
    }
    Get.to(() => TestOtaView(otaServer: _ota));
  }

  @override
  void dispose() {
    _messageWorker?.dispose();
    _connectionWorker?.dispose();
    CrashReporter.maybeInstance?.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.title),
      ),
      body: Column(
        children: [
          Obx(() {
            final scanning = _ota.isScanning.value;
            return Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  ElevatedButton(
                    onPressed: scanning
                        ? () {
                            _ota.stopScan();
                          }
                        : () {
                            _ota.startScan();
                          },
                    child: Text(scanning ? '停止扫描' : '扫描蓝牙'),
                  ),
                  const SizedBox(width: 12),
                  if (scanning)
                    const SizedBox(
                      height: 16,
                      width: 16,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    ),
                  if (scanning) const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      _ota.deviceListHint.value,
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                ],
              ),
            );
          }),
          Expanded(
            child: Obx(() {
              final state = _ota.deviceListUiState.value;
              final devices = _ota.devices;
              final isConnecting = _ota.isConnecting.value;
              final connectingId = _ota.connectingDeviceId.value;

              if ((state == DeviceListUiState.scanning && devices.isEmpty) ||
                  (isConnecting && devices.isEmpty)) {
                return const Center(child: Text('正在搜索设备...'));
              }
              if (state == DeviceListUiState.empty && devices.isEmpty) {
                return const Center(child: Text('未发现设备，请确认设备已开机并靠近手机'));
              }
              if (state == DeviceListUiState.error && devices.isEmpty) {
                return Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(_ota.deviceListHint.value),
                      const SizedBox(height: 8),
                      OutlinedButton(
                        onPressed: () {
                          _ota.startScan();
                        },
                        child: const Text('重试扫描'),
                      ),
                    ],
                  ),
                );
              }

              return ListView.builder(
                itemCount: devices.length,
                itemBuilder: (context, index) {
                  final device = devices[index];
                  final connectingThis =
                      isConnecting && connectingId == device.id;
                  final appConnectedThis =
                      _ota.isDeviceConnected.value &&
                          _ota.currentConnectedDeviceId == device.id;
                  final phoneConnectedThis =
                      appConnectedThis || _ota.isSystemConnectedScanDevice(device);
                  return InkWell(
                    onTap: isConnecting
                        ? null
                        : () {
                            _ota.connectDevice(device.id);
                          },
                    child: Container(
                      margin:
                          const EdgeInsets.only(left: 10, right: 10, bottom: 8),
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: phoneConnectedThis
                            ? const Color(0xffE8F5E9)
                            : Colors.white,
                        borderRadius:
                            const BorderRadius.all(Radius.circular(6)),
                        border: Border.all(
                          color: phoneConnectedThis
                              ? const Color(0xff81C784)
                              : const Color(0xffE4E7EE),
                        ),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: Text(
                                  device.name.isNotEmpty
                                      ? device.name
                                      : '未命名设备',
                                  style: const TextStyle(
                                    color: Color(0xff373F50),
                                    fontSize: 18,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                              ),
                              if (phoneConnectedThis)
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                    horizontal: 8,
                                    vertical: 2,
                                  ),
                                  decoration: BoxDecoration(
                                    color: appConnectedThis
                                        ? const Color(0xff2E7D32)
                                        : const Color(0xffF57C00),
                                    borderRadius: const BorderRadius.all(
                                      Radius.circular(10),
                                    ),
                                  ),
                                  child: Text(
                                    appConnectedThis ? '本机已连接' : '手机已连接',
                                    style: const TextStyle(
                                      color: Colors.white,
                                      fontSize: 10,
                                      fontWeight: FontWeight.w600,
                                    ),
                                  ),
                                ),
                              if (connectingThis)
                                const SizedBox(
                                  width: 16,
                                  height: 16,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                  ),
                                ),
                            ],
                          ),
                          const SizedBox(height: 6),
                          Text(
                            device.id,
                            style: const TextStyle(
                              color: Color(0xff373F50),
                              fontSize: 12,
                            ),
                          ),
                          if (connectingThis)
                            const Padding(
                              padding: EdgeInsets.only(top: 6),
                              child: Text('连接中...'),
                            ),
                        ],
                      ),
                    ),
                  );
                },
              );
            }),
          ),
        ],
      ),
    );
  }
}
