import 'dart:io';

import 'package:flutter/material.dart';
import 'package:file_picker/file_picker.dart';
import 'package:get/get.dart';
import 'package:path_provider/path_provider.dart';

import 'package:gaia/controlller/OtaServer.dart';
import 'package:gaia/utils/StringUtils.dart';
import 'package:gaia/utils/http.dart';

class TestOtaView extends StatefulWidget {
  const TestOtaView({Key? key}) : super(key: key);

  @override
  State<TestOtaView> createState() => _TestOtaState();
}

class _TestOtaState extends State<TestOtaView> {
  var isDownloading = false;
  var progress = 0;
  var savePath = "";
  final TextEditingController _firmwarePathController = TextEditingController();

  @override
  void initState() {
    super.initState();
    _firmwarePathController.text = OtaServer.to.firmwarePath.value;
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text("GAIA Control Demo"),
      ),
      body: Column(
        children: [
          MaterialButton(
            color: Colors.blue,
            onPressed: () {
              _download();
            },
            child: Text(
                "下载bin\n${!isDownloading ? "路径：$savePath" : '下载中($progress)\n路径：$savePath'}"),
          ),
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
                  child: TextField(
                    controller: _firmwarePathController,
                    decoration: const InputDecoration(
                      hintText:
                          "输入固件绝对路径，例如 /storage/emulated/0/Download/firmware.bin",
                    ),
                  ),
                ),
                Row(
                  children: [
                    Expanded(
                      child: MaterialButton(
                        color: Colors.blue,
                        onPressed: () async {
                          await _applyFirmwarePath(
                              _firmwarePathController.text);
                        },
                        child: const Text('应用路径'),
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
          Row(
            children: [
              Text('RWCP'),
              Obx(() {
                bool rwcp = OtaServer.to.mIsRWCPEnabled.value;
                return Checkbox(
                    value: rwcp,
                    onChanged: (on) async {
                      OtaServer.to.mIsRWCPEnabled.value = on ?? false;
                      await OtaServer.to.restPayloadSize();
                      await Future.delayed(const Duration(seconds: 1));
                      if (OtaServer.to.mIsRWCPEnabled.value) {
                        OtaServer.to.writeMsg(
                            StringUtils.hexStringToBytes("000A022E01"));
                      } else {
                        OtaServer.to.writeMsg(
                            StringUtils.hexStringToBytes("000A022E00"));
                      }
                    });
              }),
              Expanded(
                child: MaterialButton(
                    color: Colors.blue,
                    onPressed: () {
                      OtaServer.to.logText.value = "";
                    },
                    child: const Text('清空LOG')),
              ),
            ],
          ),
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
                OtaServer.to.startUpdate();
              },
              child: Text('开始升级 $time'),
            );
          }),
          MaterialButton(
            color: Colors.blue,
            onPressed: () {
              OtaServer.to.stopUpgrade();
            },
            child: const Text('取消升级'),
          ),
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
    _firmwarePathController.dispose();
    super.dispose();
    OtaServer.to.disconnect();
  }

  void _download() async {
    if (isDownloading) return;
    var url = "https://file.mymei.tv/test/1.bin";
    //url = "https://file.mymei.tv/test/M2_20221230_DEMO.bin";
    final filePath = await getApplicationDocumentsDirectory();
    final saveBinPath = filePath.path + "/1.bin";
    setState(() {
      savePath = saveBinPath;
    });
    await HttpUtil().download(url, savePath: saveBinPath,
        onReceiveProgress: (int count, int total) {
      setState(() {
        isDownloading = true;
        progress = count * 100.0 ~/ total;
      });
    });
    setState(() {
      isDownloading = false;
    });
    OtaServer.to.setFirmwarePath(saveBinPath);
    _firmwarePathController.text = saveBinPath;
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
    final picked = result.files.single.path ?? "";
    if (picked.isEmpty) {
      ScaffoldMessenger.of(context)
          .showSnackBar(const SnackBar(content: Text("未获取到文件路径，请重试")));
      return;
    }
    await _applyFirmwarePath(picked);
  }

  Future<bool> _ensureFirmwareReady() async {
    String usePath = _firmwarePathController.text.trim();
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
    _firmwarePathController.text = usePath;
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
