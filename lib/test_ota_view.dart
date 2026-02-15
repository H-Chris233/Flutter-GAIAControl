import 'dart:io';

import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'package:get/get.dart';

import 'package:gaia/controller/ota_server.dart';

class TestOtaView extends StatefulWidget {
  const TestOtaView({super.key});

  @override
  State<TestOtaView> createState() => _TestOtaState();
}

class _TestOtaState extends State<TestOtaView> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("GAIA Control"),
      ),
      body: Column(
        children: [
          Obx(() {
            final connected = OtaServer.to.isDeviceConnected.value;
            final rwcpEnabled = OtaServer.to.mIsRWCPEnabled.value;
            final mode = OtaServer.to.vendorMode.value.toUpperCase();
            final errors = OtaServer.to.errorCount.value;
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
                  Text("错误计数: $errors"),
                ],
              ),
            );
          }),
          Obx(() {
            final currentPath = OtaServer.to.firmwarePath.value;
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
                  child: Text(
                      "当前固件: ${currentPath.isEmpty ? '未设置' : currentPath}",
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis),
                ),
              ],
            );
          }),
          Obx(() {
            final per = OtaServer.to.updatePer.value;
            return Row(
              children: [
                Expanded(
                    child:
                        Slider(value: per, onChanged: null, max: 100, min: 0)),
                SizedBox(width: 60, child: Text('${per.toStringAsFixed(2)}%'))
              ],
            );
          }),
          Obx(() {
            final time = OtaServer.to.timeCount.value;
            final upgrading = OtaServer.to.isUpgrading.value;
            return MaterialButton(
              color: Colors.blue,
              onPressed: upgrading
                  ? null
                  : () async {
                      if (!await _ensureFirmwareReady()) {
                        return;
                      }
                      OtaServer.to.startUpdateWithVersionCheck();
                    },
              child: Text(upgrading ? '升级中... $time' : '开始升级 $time'),
            );
          }),
          Obx(() {
            final before = OtaServer.to.versionBeforeUpgrade.value;
            final after = OtaServer.to.versionAfterUpgrade.value;
            final rwcpStatus = OtaServer.to.rwcpStatusText.value;
            final recoveryStatus = OtaServer.to.recoveryStatusText.value;
            final compare = (before == "UNKNOWN" || after == "UNKNOWN")
                ? "信息不足"
                : (before == after ? "未变化（可能未升级成功）" : "已变化（升级成功）");
            return Padding(
              padding: const EdgeInsets.symmetric(horizontal: 12),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text("RWCP状态: $rwcpStatus"),
                  Text("恢复状态: $recoveryStatus"),
                  Text("升级前版本: $before",
                      maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text("升级后版本: $after",
                      maxLines: 1, overflow: TextOverflow.ellipsis),
                  Text("版本对比: $compare"),
                ],
              ),
            );
          }),
          Obx(() {
            final upgrading = OtaServer.to.isUpgrading.value;
            return MaterialButton(
              color: Colors.blue,
              onPressed: upgrading
                  ? () {
                      OtaServer.to.stopUpgrade();
                    }
                  : null,
              child: const Text('取消升级'),
            );
          }),
          Obx(() {
            final upgrading = OtaServer.to.isUpgrading.value;
            final rwcpStatus = OtaServer.to.rwcpStatusText.value;
            final recoveryStatus = OtaServer.to.recoveryStatusText.value;
            final canRecover = upgrading ||
                rwcpStatus.contains("错误") ||
                recoveryStatus != "空闲";
            return MaterialButton(
              color: Colors.orange,
              onPressed: canRecover
                  ? () {
                      OtaServer.to.quickRecoverNow();
                    }
                  : null,
              child: const Text('快速恢复'),
            );
          }),
          MaterialButton(
              color: Colors.blue,
              onPressed: () {
                OtaServer.to.logText.value = "";
              },
              child: const Text('清空LOG')),
          Expanded(child: Obx(() {
            final log = OtaServer.to.logText.value;
            return SingleChildScrollView(
                child: Text(
              log,
              style: const TextStyle(fontSize: 12),
            ));
          }))
        ],
      ),
    );
  }

  @override
  void dispose() {
    OtaServer.to.disconnect();
    super.dispose();
  }

  Future<void> _chooseFirmwareFile() async {
    if (!mounted) return;
    final result = await FilePicker.platform.pickFiles(
      type: FileType.custom,
      allowedExtensions: ["bin"],
      allowMultiple: false,
      withData: false,
    );
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
    String usePath = OtaServer.to.firmwarePath.value.trim();
    if (usePath.isEmpty) {
      OtaServer.to.addLog("固件路径未设置");
      return false;
    }
    final error = await _validateFirmwareFile(usePath);
    if (error != null) {
      OtaServer.to.addLog(error);
      if (!mounted) return false;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(error)));
      return false;
    }
    await _applyFirmwarePath(usePath);
    return true;
  }

  Future<void> _applyFirmwarePath(String rawPath) async {
    final usePath = rawPath.trim();
    final error = await _validateFirmwareFile(usePath);
    if (error != null) {
      OtaServer.to.addLog(error);
      if (!mounted) return;
      ScaffoldMessenger.of(context)
          .showSnackBar(SnackBar(content: Text(error)));
      return;
    }
    OtaServer.to.setFirmwarePath(usePath);
  }

  Future<String?> _validateFirmwareFile(String path) async {
    if (path.isEmpty) {
      return "固件路径不能为空";
    }
    if (!path.toLowerCase().endsWith(".bin")) {
      return "仅支持 .bin 固件文件";
    }
    final checkFile = File(path);
    if (!await checkFile.exists()) {
      return "固件文件不存在: $path";
    }
    final length = await checkFile.length();
    if (length <= 0) {
      return "固件文件为空: $path";
    }
    return null;
  }
}
