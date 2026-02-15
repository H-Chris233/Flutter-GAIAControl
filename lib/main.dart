import 'package:flutter/material.dart';
import 'package:get/get.dart';
import 'package:permission_handler/permission_handler.dart';

import 'controller/ota_server.dart';

void main() {
  runApp(const MyApp());
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
      ScaffoldMessenger.of(context)
        ..hideCurrentSnackBar()
        ..showSnackBar(SnackBar(
          content: Text(message),
          action: SnackBarAction(
            label: '去设置',
            onPressed: () {
              openAppSettings();
            },
          ),
        ));
      ota.consumeUserMessage();
    });
  }

  @override
  void dispose() {
    _messageWorker?.dispose();
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
                        color: Colors.white,
                        borderRadius:
                            const BorderRadius.all(Radius.circular(6)),
                        border: Border.all(color: const Color(0xffE4E7EE)),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              Expanded(
                                child: Text(
                                  device.name,
                                  style: const TextStyle(
                                      color: Color(0xff373F50),
                                      fontSize: 18,
                                      fontWeight: FontWeight.bold),
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
