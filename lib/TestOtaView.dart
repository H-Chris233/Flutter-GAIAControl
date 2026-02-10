import 'dart:io';

import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'package:get/get.dart';

import 'package:gaia/controlller/OtaServer.dart';

class TestOtaView extends StatefulWidget {
  const TestOtaView({Key? key}) : super(key: key);

  @override
  State<TestOtaView> createState() => _TestOtaState();
}

class _TestOtaState extends State<TestOtaView> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("GAIA Control Demo"),
      ),
      body: Column(
        children: [
          Obx(() {
            final currentPath = OtaServer.to.firmwarePath.value;
            final currentMode = OtaServer.to.vendorMode.value;
            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 12),
                  child: Row(
                    children: [
                      const Text("Vendor模式: "),
                      DropdownButton<String>(
                        value: currentMode,
                        items: const [
                          DropdownMenuItem(
                              value: OtaServer.vendorModeV3,
                              child: Text("V3 (001D)")),
                          DropdownMenuItem(
                              value: OtaServer.vendorModeV1V2,
                              child: Text("V1/V2 (000A)")),
                          DropdownMenuItem(
                              value: OtaServer.vendorModeAuto,
                              child: Text("Auto")),
                        ],
                        onChanged: (mode) {
                          if (mode != null) {
                            OtaServer.to.setVendorMode(mode);
                          }
                        },
                      )
                    ],
                  ),
                ),
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
                    child: Slider(
                        value: per, onChanged: (data) {}, max: 100, min: 0)),
                SizedBox(width: 60, child: Text('${per.toStringAsFixed(2)}%'))
              ],
            );
          }),
          Obx(() {
            final time = OtaServer.to.timeCount.value;
            return MaterialButton(
              color: Colors.blue,
              onPressed: () async {
                if (!await _ensureFirmwareReady()) {
                  return;
                }
                OtaServer.to.startUpdateWithVersionCheck();
              },
              child: Text('开始升级 $time'),
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
          MaterialButton(
            color: Colors.blue,
            onPressed: () {
              OtaServer.to.stopUpgrade();
            },
            child: const Text('取消升级'),
          ),
          MaterialButton(
            color: Colors.orange,
            onPressed: () {
              OtaServer.to.quickRecoverNow();
            },
            child: const Text('快速恢复'),
          ),
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
              style: const TextStyle(fontSize: 10),
            ));
          }))
        ],
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    OtaServer.to.disconnect();
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
      usePath = OtaServer.to.firmwarePath.value.trim();
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
