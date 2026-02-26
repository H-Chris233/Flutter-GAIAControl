import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

import 'controller/ota_server.dart';
import 'utils/crash_reporter.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  try {
    await CrashReporter.init();
    CrashReporter.maybeInstance?.installGlobalHandlers();
  } catch (_) {}
  runZonedGuarded(
    () {
      runApp(const MyApp());
    },
    (error, stack) {
      CrashReporter.maybeInstance
          ?.recordError(error, stack, context: "runZonedGuarded");
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
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  Worker? _messageWorker;

  @override
  void initState() {
    super.initState();
    final ota = Get.put<OtaServer>(OtaServer());
    _messageWorker = ever<String?>(ota.userMessage, (message) {
      if (message == null || message.isEmpty || !mounted) {
        return;
      }
      final isCrashReport = message.contains("崩溃日志已保存:");
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(SnackBar(
          content: Text(message),
          action: SnackBarAction(
            label: isCrashReport ? '复制路径' : '去设置',
            onPressed: () {
              if (!isCrashReport) {
                openAppSettings();
                return;
              }
              final path = message.split("崩溃日志已保存:").last.trim();
              if (path.isEmpty) {
                return;
              }
              Clipboard.setData(ClipboardData(text: path));
            },
          ),
        ));
      ota.consumeUserMessage();
    });

    unawaited(() async {
      final last =
          await CrashReporter.maybeInstance?.consumePendingReportPath();
      if (!mounted || last == null) {
        return;
      }
      ota.userMessage.value = "检测到上次异常退出，崩溃日志已保存: $last";
    }());
  }

  @override
  void dispose() {
    _messageWorker?.dispose();
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
            final scanning = OtaServer.to.isScanning.value;
            return Padding(
              padding: const EdgeInsets.all(12),
              child: Row(
                children: [
                  ElevatedButton(
                    onPressed: scanning
                        ? () {
                            OtaServer.to.stopScan();
                          }
                        : () {
                            OtaServer.to.startScan();
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
                      OtaServer.to.deviceListHint.value,
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
              final state = OtaServer.to.deviceListUiState.value;
              final devices = OtaServer.to.devices;
              final isConnecting = OtaServer.to.isConnecting.value;
              final connectingId = OtaServer.to.connectingDeviceId.value;

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
                      Text(OtaServer.to.deviceListHint.value),
                      const SizedBox(height: 8),
                      OutlinedButton(
                        onPressed: () {
                          OtaServer.to.startScan();
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
                      OtaServer.to.isDeviceConnected.value &&
                          OtaServer.to.connectDeviceId == device.id;
                  final phoneConnectedThis = appConnectedThis ||
                      OtaServer.to.isSystemConnectedScanDevice(device);
                  return InkWell(
                    onTap: isConnecting
                        ? null
                        : () {
                            OtaServer.to.connectDevice(device.id);
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
                                : const Color(0xffE4E7EE)),
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
                                      fontWeight: FontWeight.bold),
                                ),
                              ),
                              if (phoneConnectedThis)
                                Container(
                                  padding: const EdgeInsets.symmetric(
                                      horizontal: 8, vertical: 2),
                                  decoration: BoxDecoration(
                                    color: appConnectedThis
                                        ? const Color(0xff2E7D32)
                                        : const Color(0xffF57C00),
                                    borderRadius: const BorderRadius.all(
                                        Radius.circular(10)),
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
                                color: Color(0xff373F50), fontSize: 12),
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
