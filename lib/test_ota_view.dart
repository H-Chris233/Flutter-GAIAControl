import 'dart:io';

import 'package:file_picker/file_picker.dart';
import 'package:flutter/material.dart';
import 'package:get/get.dart';

import 'package:gaia/controller/ota_server.dart';

class TestOtaView extends StatefulWidget {
  const TestOtaView({super.key, this.otaServer});

  final OtaServer? otaServer;

  @override
  State<TestOtaView> createState() => _TestOtaState();
}

class _TestOtaState extends State<TestOtaView> {
  Worker? _upgradeSuccessWorker;
  late final OtaServer? _otaServer = _resolveOtaServer();

  OtaServer? _resolveOtaServer() {
    final provided = widget.otaServer;
    if (provided != null) {
      if (Get.isRegistered<OtaServer>()) {
        final registered = Get.find<OtaServer>();
        if (!identical(registered, provided)) {
          Get.replace<OtaServer>(provided);
        }
      } else {
        Get.put<OtaServer>(provided);
      }
      return provided;
    }
    if (Get.isRegistered<OtaServer>()) {
      return Get.find<OtaServer>();
    }
    return null;
  }

  @override
  void initState() {
    super.initState();
    final ota = _otaServer;
    if (ota == null) {
      return;
    }
    ota.startCurrentVersionPolling();
    _upgradeSuccessWorker = ever<int>(
      ota.upgradeSuccessCounter,
      (_) async {
        if (!mounted) return;
        await showDialog<void>(
          context: context,
          builder: (ctx) {
            return AlertDialog(
              title: const Text('升级成功'),
              content: Obx(() {
                final after = ota.versionAfterUpgrade.value;
                final display = (after == "UNKNOWN") ? "查询中..." : after;
                return Text('固件升级已完成。\n升级后版本：$display');
              }),
              actions: [
                TextButton(
                  onPressed: () => Navigator.of(ctx).pop(),
                  child: const Text('确定'),
                ),
              ],
            );
          },
        );
      },
    );
  }

  @override
  Widget build(BuildContext context) {
    final ota = _otaServer;
    if (ota == null) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('GAIA Control'),
        ),
        body: const Center(
          child: Text('OTA服务未初始化'),
        ),
      );
    }
    return Scaffold(
      appBar: AppBar(
        title: const Text("GAIA Control"),
      ),
      body: Column(
        children: [
          Obx(() {
            final connected = ota.isDeviceConnected.value;
            final rwcpEnabled = ota.mIsRWCPEnabled.value;
            final mode = ota.vendorMode.value.toUpperCase();
            final errors = ota.errorCount.value;
            final currentVersion = ota.currentVersion.value;
            return Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
              color:
                  connected ? const Color(0xffE8F5E9) : const Color(0xffFBE9E7),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("连接状态: ${connected ? "已连接" : "未连接"}"),
                  Text("RWCP模式: ${rwcpEnabled ? "已启用" : "未启用"}"),
                  Text("Vendor模式: $mode"),
                  Text("当前版本: $currentVersion",
                      maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text("错误计数: $errors"),
                ],
              ),
            );
          }),
          Obx(() {
            final currentPath = ota.firmwarePath.value;
            final fileName = _extractFileName(currentPath);
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Expanded(
                      child: MaterialButton(
                        color: Colors.blue,
                        onPressed: _chooseFirmwareFile,
                        child: const Text('选择本地固件(.bin)'),
                      ),
                    ),
                  ],
                ),
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: Text("当前固件: $fileName",
                      maxLines: 2, overflow: TextOverflow.ellipsis),
                ),
              ],
            );
          }),
          Obx(() {
            final per = ota.updatePer.value;
            return Row(
              children: [
                Expanded(
                  child: Slider(value: per, onChanged: null, max: 100, min: 0),
                ),
                SizedBox(width: 60, child: Text('${per.toStringAsFixed(2)}%')),
              ],
            );
          }),
          Obx(() {
            final time = ota.timeCount.value;
            final upgrading = ota.isUpgrading.value;
            return MaterialButton(
              color: Colors.blue,
              onPressed: upgrading
                  ? null
                  : () async {
                      if (!await _ensureFirmwareReady()) {
                        return;
                      }
                      ota.startUpdateWithVersionCheck();
                    },
              child: Text(upgrading ? '升级中... $time' : '开始升级 $time'),
            );
          }),
          Obx(() {
            final before = ota.versionBeforeUpgrade.value;
            final after = ota.versionAfterUpgrade.value;
            final rwcpStatus = ota.rwcpStatusText.value;
            final recoveryStatus = ota.recoveryStatusText.value;
            final compare = (before == "UNKNOWN" || after == "UNKNOWN")
                ? "信息不足"
                : (before == after ? "未变化（可能未升级成功）" : "已变化（升级成功）");
            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("升级前版本: $before",
                      maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text("升级后版本: $after",
                      maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text("版本对比: $compare"),
                  Text("RWCP状态: $rwcpStatus"),
                  Text("恢复状态: $recoveryStatus"),
                ],
              ),
            );
          }),
          Obx(() {
            final upgrading = ota.isUpgrading.value;
            return MaterialButton(
              color: Colors.blue,
              onPressed: upgrading
                  ? () {
                      ota.stopUpgrade();
                    }
                  : null,
              child: const Text('取消升级'),
            );
          }),
          Obx(() {
            final connected = ota.isDeviceConnected.value;
            final upgrading = ota.isUpgrading.value;
            final rwcpStatus = ota.rwcpStatusText.value;
            final recoveryStatus = ota.recoveryStatusText.value;
            final canRecover = connected ||
                upgrading ||
                rwcpStatus.contains("错误") ||
                recoveryStatus != "空闲";
            return MaterialButton(
              color: Colors.orange,
              onPressed: canRecover
                  ? () {
                      ota.quickRecoverNow();
                    }
                  : null,
              child: const Text('快速恢复'),
            );
          }),
          MaterialButton(
            color: Colors.blue,
            onPressed: () {
              ota.logText.value = "";
            },
            child: const Text('清空LOG'),
          ),
          Expanded(
            child: Obx(() {
              final log = ota.logText.value;
              return SingleChildScrollView(
                child: Text(
                  log,
                  style: const TextStyle(fontSize: 12),
                ),
              );
            }),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    _upgradeSuccessWorker?.dispose();
    final ota = _otaServer;
    if (ota != null) {
      ota.disconnect();
    }
    super.dispose();
  }

  Future<void> _chooseFirmwareFile() async {
    final ota = _otaServer;
    if (ota == null || !mounted) return;
    final initialDirectory = ota.lastFirmwareDirectory.value.trim();
    FilePickerResult? result;
    try {
      result = await FilePicker.platform.pickFiles(
        type: FileType.custom,
        allowedExtensions: ["bin"],
        allowMultiple: false,
        withData: false,
        initialDirectory: initialDirectory.isEmpty ? null : initialDirectory,
      );
    } catch (e) {
      ota.addLog("选择固件失败: $e");
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text("选择固件失败: $e")));
      return;
    }
    if (result == null || result.files.isEmpty) {
      return;
    }
    if (!mounted) return;
    final picked = result.files.single.path ?? "";
    if (picked.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text("未获取到文件路径，请重试")));
      return;
    }
    await _applyFirmwarePath(picked);
  }

  Future<bool> _ensureFirmwareReady() async {
    final ota = _otaServer;
    if (ota == null) {
      return false;
    }
    final usePath = ota.firmwarePath.value.trim();
    if (usePath.isEmpty) {
      ota.addLog("固件路径未设置");
      return false;
    }
    final error = await _validateFirmwareFile(usePath);
    if (error != null) {
      ota.addLog(error);
      if (!mounted) return false;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(error)));
      return false;
    }
    await _applyFirmwarePath(usePath);
    return true;
  }

  Future<void> _applyFirmwarePath(String rawPath) async {
    final ota = _otaServer;
    if (ota == null) {
      return;
    }
    final usePath = rawPath.trim();
    final error = await _validateFirmwareFile(usePath);
    if (error != null) {
      ota.addLog(error);
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(error)));
      return;
    }
    ota.setFirmwarePath(usePath);
  }

  String _extractFileName(String rawPath) {
    final normalizedPath = rawPath.trim().replaceAll("\\", "/");
    if (normalizedPath.isEmpty) {
      return "未设置";
    }
    final splitIndex = normalizedPath.lastIndexOf("/");
    if (splitIndex < 0 || splitIndex == normalizedPath.length - 1) {
      return normalizedPath;
    }
    return normalizedPath.substring(splitIndex + 1);
  }

  Future<String?> _validateFirmwareFile(String path) async {
    if (path.isEmpty) {
      return "固件路径不能为空";
    }
    if (!path.toLowerCase().endsWith(".bin")) {
      return "仅支持 .bin 固件文件";
    }
    try {
      final checkFile = File(path);
      if (!await checkFile.exists()) {
        return "固件文件不存在: $path";
      }
      final length = await checkFile.length();
      if (length <= 0) {
        return "固件文件为空: $path";
      }
    } catch (e) {
      return "读取固件文件失败: $e";
    }
    return null;
  }
}
