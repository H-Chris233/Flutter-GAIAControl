import 'dart:async';
import 'dart:collection';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:get/get.dart';
import 'package:path_provider/path_provider.dart';

import 'package:gaia/test_ota_view.dart';
import 'package:gaia/utils/ble_constants.dart';
import 'package:gaia/utils/string_utils.dart';
import 'package:gaia/utils/gaia/confirmation_type.dart';
import 'package:gaia/utils/gaia/gaia.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';
import 'package:gaia/utils/gaia/op_codes.dart';
import 'package:gaia/utils/gaia/resume_points.dart';
import 'package:gaia/utils/gaia/vmu_packet.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_client.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_listener.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';
import 'package:gaia/utils/gaia/rwcp/segment.dart';

import 'package:gaia/controller/log_buffer.dart';
import 'package:gaia/controller/gaia_command_builder.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/upgrade_state_machine.dart';

typedef DefaultFirmwarePathResolver = Future<String> Function();

enum DeviceListUiState {
  idle,
  scanning,
  empty,
  ready,
  error,
}

class OtaServer extends GetxService
    implements RWCPListener, UpgradeStateMachineDelegate {
  // ============== 配置常量 ==============
  /// 升级看门狗超时时间（秒）
  static const int kUpgradeWatchdogTimeoutSeconds = 15;

  /// 升级后版本查询最大重试次数
  static const int kPostUpgradeVersionMaxRetries = 10;

  /// 升级后版本查询重试间隔（秒）
  static const int kPostUpgradeVersionRetryIntervalSeconds = 2;

  /// Vendor 探测超时时间（秒）
  static const int kVendorProbeTimeoutSeconds = 2;

  /// 版本查询超时时间（秒）
  static const int kVersionQueryTimeoutSeconds = 3;

  /// DFU 结果查询超时时间（秒）
  static const int kDfuResultQueryTimeoutSeconds = 3;

  /// 快速恢复前的延迟时间（秒）
  static const int kRecoveryDelaySeconds = 2;

  /// 错误累计触发恢复的阈值
  static const int kErrorBurstThreshold = 3;

  /// 错误累计时间窗口（秒）
  static const int kErrorBurstWindowSeconds = 10;

  /// 恢复时间窗口（分钟）
  static const int kRecoveryWindowMinutes = 1;

  /// 恢复窗口内最大恢复次数
  static const int kMaxRecoveryAttemptsPerWindow = 3;

  // 组件实例
  late final LogBuffer _logBuffer;
  late final GaiaCommandBuilder _cmdBuilder;
  late final BleConnectionManager _bleManager;
  late final UpgradeStateMachine _upgradeStateMachine;
  final BleConnectionManager? _bleManagerOverride;
  final UpgradeStateMachine? _upgradeStateMachineOverride;
  final DefaultFirmwarePathResolver _defaultFirmwarePathResolver;

  var logText = "".obs;
  final String tag = "OtaServer";
  late final RxList<DiscoveredDevice> devices;

  String connectDeviceId = "";
  final Uuid otaUUID = BleConstants.otaServiceUuid;
  final Uuid notifyUUID = BleConstants.notifyCharacteristicUuid;
  final Uuid writeUUID = BleConstants.writeCharacteristicUuid;
  final Uuid writeNoResUUID = BleConstants.writeNoResponseCharacteristicUuid;
  RxBool isDeviceConnected = false.obs;
  RxBool isScanning = false.obs;
  RxBool isConnecting = false.obs;
  RxString connectingDeviceId = "".obs;
  Rx<DeviceListUiState> deviceListUiState = DeviceListUiState.idle.obs;
  RxString deviceListHint = "点击“扫描蓝牙”开始搜索设备".obs;
  RxnString userMessage = RxnString();
  RxInt errorCount = 0.obs;

  /// To know if the upgrade process is currently running.
  RxBool isUpgrading = false.obs;

  bool transFerComplete = false;

  /// To know how many times we try to start the upgrade.

  /// The offset to use to upload data on the device.
  var mStartOffset = 0;

  /// The file to upload on the device.
  List<int>? mBytesFile;

  List<int> writeBytes = [];

  /// The maximum value for the data length of a VM upgrade packet for the data transfer step.
  var mMaxLengthForDataTransfer = 16;

  var mPayloadSizeMax = 16;

  /// To know if the packet with the operation code "upgradeData" which was sent was the last packet to send.

  int mBytesToSend = 0;

  var mIsRWCPEnabled = true.obs; // RWCP 始终启用，不可关闭
  int sendPkgCount = 0;

  RxDouble updatePer = RxDouble(0);
  var versionBeforeUpgrade = "UNKNOWN".obs;
  var versionAfterUpgrade = "UNKNOWN".obs;

  /// To know if we have to disconnect after any event which occurs as a fatal error from the board.

  String fileMd5 = "";
  var firmwarePath = "".obs;
  var rwcpStatusText = "未启用".obs;

  var percentage = 0.0.obs;

  Timer? _timer;
  static const bool _enableWriteTraceLog = false;
  static const int _dataPacketLogSampleInterval = 50;

  var timeCount = 0.obs;

  //RWCP
  ListQueue<double> mProgressQueue = ListQueue();

  late RWCPClient mRWCPClient;

  int mTransferStartTime = 0;

  int writeRTCPCount = 0;

  File? file;
  int _dfuPendingChunkSize = 0;
  bool _dfuWriteInFlight = false;
  Timer? _dfuResultTimer;
  bool _rwcpSetupInProgress = false;
  Timer? _upgradeWatchdogTimer;
  Timer? _reconnectTimer;
  bool _autoReconnectEnabled = true;
  String _fatalUpgradeReason = "";
  static const String vendorModeV3 = "v3";
  var vendorMode = vendorModeV3.obs;
  int _activeVendorId = 0x001D;
  var autoRecoveryEnabled = true.obs;
  var recoveryStatusText = "空闲".obs;
  int _errorBurstCount = 0;
  DateTime? _lastErrorTime;
  bool _isRecovering = false;
  int _recoveryAttempts = 0;
  DateTime? _recoveryWindowStart;
  bool _isVersionQueryInFlight = false;
  String _currentVersionQueryTag = "";
  Timer? _versionQueryTimer;
  void Function(String version)? _onVersionQuerySuccess;
  VoidCallback? _onVersionQueryFailed;
  bool _pendingStartAfterVersionQuery = false;
  Timer? _postUpgradeVersionRetryTimer;
  int _postUpgradeVersionRetryCount = 0;
  Timer? _scanWatchdogTimer;
  Worker? _deviceListWorker;

  OtaServer({
    BleConnectionManager? bleManagerOverride,
    UpgradeStateMachine? upgradeStateMachineOverride,
    DefaultFirmwarePathResolver? defaultFirmwarePathResolver,
  })  : _bleManagerOverride = bleManagerOverride,
        _upgradeStateMachineOverride = upgradeStateMachineOverride,
        _defaultFirmwarePathResolver =
            defaultFirmwarePathResolver ?? _resolveDefaultFirmwarePath;

  static Future<String> _resolveDefaultFirmwarePath() async {
    final filePath = await getApplicationDocumentsDirectory();
    return "${filePath.path}/1.bin";
  }

  static OtaServer get to => Get.find();

  @override
  void onInit() {
    super.onInit();
    // 初始化组件
    _logBuffer = LogBuffer(logText: logText);
    _cmdBuilder = GaiaCommandBuilder();
    _bleManager = _bleManagerOverride ??
        BleConnectionManager(
          ble: FlutterReactiveBleClient(FlutterReactiveBle()),
          onLog: addLog,
          onConnectionStateChanged: (state, deviceId) {
            if (state != DeviceConnectionState.connected) {
              return;
            }
            connectDeviceId = deviceId;
          },
        );
    devices = _bleManager.devices;
    _upgradeStateMachine =
        _upgradeStateMachineOverride ?? UpgradeStateMachine(delegate: this);
    mRWCPClient = RWCPClient(this);
    _initDefaultFirmwarePath();
    _bleManager.startBleStatusMonitor();
    _deviceListWorker = ever<List<DiscoveredDevice>>(devices, (current) {
      if (current.isNotEmpty) {
        _scanWatchdogTimer?.cancel();
        if (isScanning.value) {
          isScanning.value = false;
        }
        deviceListUiState.value = DeviceListUiState.ready;
        deviceListHint.value = "发现 ${current.length} 台设备";
      } else if (!isScanning.value &&
          deviceListUiState.value == DeviceListUiState.ready) {
        deviceListUiState.value = DeviceListUiState.empty;
        deviceListHint.value = "未发现设备，请确认设备已进入广播模式";
      }
    });
  }

  Future<void> _initDefaultFirmwarePath() async {
    try {
      firmwarePath.value = await _defaultFirmwarePathResolver();
    } catch (e) {
      addLog("初始化默认固件路径失败$e");
    }
  }

  void setFirmwarePath(String path) {
    final trimPath = path.trim();
    if (trimPath.isEmpty) {
      addLog("固件路径不能为空");
      return;
    }
    firmwarePath.value = trimPath;
    addLog("已设置固件路径$trimPath");
  }

  void consumeUserMessage() {
    userMessage.value = null;
  }

  void _notifyUser(String message) {
    userMessage.value = message;
    addLog(message);
  }

  Future<void> connectDevice(String id) async {
    try {
      isConnecting.value = true;
      connectingDeviceId.value = id;
      _scanWatchdogTimer?.cancel();
      isScanning.value = false;
      deviceListUiState.value = DeviceListUiState.idle;
      deviceListHint.value = "连接中...";
      _autoReconnectEnabled = true;
      _bleManager.setAutoReconnectEnabled(_autoReconnectEnabled);
      await _bleManager.connect(
        id,
        onConnected: () async {
          isConnecting.value = false;
          connectingDeviceId.value = "";
          isDeviceConnected.value = true;
          connectDeviceId = id;
          if (!isUpgrading.value) {
            rwcpStatusText.value = "待启用";
          }
          deviceListUiState.value = DeviceListUiState.ready;
          deviceListHint.value = "连接成功";
          addLog("Vendor模式固定为V3，使用${_vendorToHex(_activeVendorId)}");
          await registerNotice();
          await restPayloadSize();
          if (!isUpgrading.value) {
            Get.to(() => const TestOtaView());
          }
        },
        onDisconnected: () {
          isConnecting.value = false;
          connectingDeviceId.value = "";
          isDeviceConnected.value = false;
          rwcpStatusText.value = "连接断开";
          _notifyUser("设备已断开连接");
          deviceListUiState.value = DeviceListUiState.error;
          deviceListHint.value = "连接断开，请重试";
          if (isUpgrading.value) {
            _enterFatalUpgradeState("升级过程中蓝牙断链");
          }
        },
        onError: (error) {
          isConnecting.value = false;
          connectingDeviceId.value = "";
          isDeviceConnected.value = false;
          deviceListUiState.value = DeviceListUiState.error;
          deviceListHint.value = "连接失败，请重试";
          _notifyUser("连接失败: $error");
        },
      );
    } catch (e) {
      isConnecting.value = false;
      connectingDeviceId.value = "";
      isDeviceConnected.value = false;
      deviceListUiState.value = DeviceListUiState.error;
      deviceListHint.value = "连接失败，请重试";
      _notifyUser('开始连接失败: $e');
    }
  }

  void writeMsg(List<int> data) {
    scheduleMicrotask(() async {
      _touchUpgradeWatchdog();
      await writeData(data);
    });
  }

  GaiaPacketBLE _buildGaiaPacket(int command,
      {List<int>? payload, int? vendor}) {
    return GaiaPacketBLE(command,
        mPayload: payload, mVendorId: vendor ?? _activeVendorId);
  }

  String _vendorToHex(int vendor) {
    return "0x${vendor.toRadixString(16).padLeft(4, '0').toUpperCase()}";
  }

  // 命令构建（代理到 GaiaCommandBuilder）
  int _upgradeConnectCommand() => _cmdBuilder.upgradeConnectCommand();
  int _upgradeDisconnectCommand() => _cmdBuilder.upgradeDisconnectCommand();
  int _upgradeControlCommand() => _cmdBuilder.upgradeControlCommand();
  int _setDataEndpointModeCommand() => _cmdBuilder.setDataEndpointModeCommand();
  int _getApplicationVersionCommand() =>
      _cmdBuilder.getApplicationVersionCommand();
  int _registerNotificationCommand() =>
      _cmdBuilder.registerNotificationCommand();

  int _v3CommandFeature(int cmd) => _cmdBuilder.v3CommandFeature(cmd);
  int _v3CommandType(int cmd) => _cmdBuilder.v3CommandType(cmd);
  int _v3CommandId(int cmd) => _cmdBuilder.v3CommandId(cmd);

  void setVendorMode(String mode) {
    vendorMode.value = vendorModeV3;
    _activeVendorId = 0x001D;
    addLog("Vendor模式固定为V3，使用${_vendorToHex(_activeVendorId)}");
  }

  Future<void> registerRWCP() async {
    if (!mIsRWCPEnabled.value) {
      return;
    }
    rwcpStatusText.value = "建立通道中";
    await _bleManager.cancelRwcpChannel();
    await _bleManager.registerRwcpChannel((data) {
      //addLog("wenDataRec2>${StringUtils.byteToHexString(data)}");
      mRWCPClient.onReceiveRWCPSegment(data);
    });
    if (!_bleManager.isDeviceConnected) {
      rwcpStatusText.value = "服务未就绪";
      _rwcpSetupInProgress = false;
      return;
    }
    addLog(
        "isUpgrading${isUpgrading.value} transFerComplete $transFerComplete");
    if (isUpgrading.value) {
      rwcpStatusText.value = "已启用";
      _rwcpSetupInProgress = false;
      if (!transFerComplete) {
        sendUpgradeConnect();
      }
    } else {
      // 非升级状态下不自动启动升级，仅更新状态
      rwcpStatusText.value = "待启用";
      _rwcpSetupInProgress = false;
    }
  }

  //注册通知
  Future<void> registerNotice() async {
    await _bleManager.registerNotifyChannel((data) {
      addLog("收到通知>${StringUtils.byteToHexString(data)}");
      handleRecMsg(data);
    });
    final registerPayload = [0x06];
    GaiaPacketBLE registerPacket = _buildGaiaPacket(
      _registerNotificationCommand(),
      payload: registerPayload,
    );
    writeMsg(registerPacket.getBytes());
    //如果开启RWCP那么需要在重连之后启用RWCP
    if (isUpgrading.value && transFerComplete && mIsRWCPEnabled.value) {
      //开启RWCP
      writeMsg(_buildGaiaPacket(_setDataEndpointModeCommand(), payload: [0x01])
          .getBytes());
    }
  }

  Future<void> startUpdate() async {
    if (isUpgrading.value) {
      addLog("正在升级中，忽略重复开始请求");
      return;
    }
    logText.value = "";
    writeBytes.clear();
    writeRTCPCount = 0;
    mProgressQueue.clear();
    mTransferStartTime = 0;
    timeCount.value = 0;
    _timer?.cancel();
    _timer = Timer.periodic(const Duration(seconds: 1), (timer) {
      timeCount.value += 1;
    });
    sendPkgCount = 0;
    updatePer.value = 0;
    isUpgrading.value = true;
    _dfuResultTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _rwcpSetupInProgress = false;
    _fatalUpgradeReason = "";
    _autoReconnectEnabled = true;
    rwcpStatusText.value = "启用中";
    resetUpload();
    _armUpgradeWatchdog();
    // 当前仅支持 RWCP 模式，DFU 直传模式保留但未启用
    enableRwcpForUpgrade();
  }

  void startUpdateWithVersionCheck() {
    if (isUpgrading.value) {
      addLog("正在升级中，忽略重复开始请求");
      return;
    }
    _pendingStartAfterVersionQuery = true;
    versionAfterUpgrade.value = "UNKNOWN";
    queryApplicationVersion(
      tag: "升级前",
      onSuccess: (version) {
        versionBeforeUpgrade.value = version;
        if (_pendingStartAfterVersionQuery) {
          _pendingStartAfterVersionQuery = false;
          startUpdate();
        }
      },
      onFailed: () {
        versionBeforeUpgrade.value = "UNKNOWN";
        addLog("升级前版本查询失败，继续执行升级");
        if (_pendingStartAfterVersionQuery) {
          _pendingStartAfterVersionQuery = false;
          startUpdate();
        }
      },
    );
  }

  void handleRecMsg(List<int> data) async {
    _touchUpgradeWatchdog();
    final packet = GaiaPacketBLE.fromByte(data);
    if (packet == null) {
      addLog("数据包解析失败: 长度=${data.length}");
      return;
    }
    if (packet.mVendorId != 0x001D) {
      addLog("忽略非V3 Vendor包: ${_vendorToHex(packet.mVendorId)}");
      return;
    }
    _handleV3Packet(packet);
  }

  void _handleV3Packet(GaiaPacketBLE packet) {
    final cmd = packet.getCommand();
    final feature = _v3CommandFeature(cmd);
    final packetType = _v3CommandType(cmd);
    final commandId = _v3CommandId(cmd);
    final payload = packet.mPayload ?? [];

    if (packetType == GaiaCommandBuilder.v3PacketTypeResponse) {
      if (feature == GaiaCommandBuilder.v3FeatureFramework &&
          commandId == GaiaCommandBuilder.v3CmdAppVersion) {
        onApplicationVersionAckV3(payload);
        return;
      }
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3CmdSetDataEndpointMode) {
        if (mIsRWCPEnabled.value) {
          unawaited(registerRWCP());
        } else {
          _rwcpSetupInProgress = false;
        }
        return;
      }
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3CmdUpgradeConnect) {
        if (isUpgrading.value) {
          resetUpload();
          sendSyncReq();
        }
        return;
      }
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3CmdUpgradeControl) {
        onSuccessfulTransmission();
        return;
      }
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3CmdUpgradeDisconnect) {
        stopUpgrade(sendAbort: false, sendDisconnect: false);
        return;
      }
      return;
    }

    if (packetType == GaiaCommandBuilder.v3PacketTypeNotification) {
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3CmdUpgradeNotification) {
        receiveVMUPacket(payload);
      }
      return;
    }

    if (packetType == GaiaCommandBuilder.v3PacketTypeError) {
      final status = payload.isNotEmpty ? payload.first : -1;
      addLog(
          "V3错误响应 feature=$feature cmdId=$commandId status=0x${status.toRadixString(16)} ${_gaiaStatusText(status)}");
      _reportDeviceError(
          "V3错误 feature=$feature cmdId=$commandId status=0x${status.toRadixString(16)}",
          triggerRecovery: !isUpgrading.value);

      if (feature == GaiaCommandBuilder.v3FeatureFramework &&
          commandId == GaiaCommandBuilder.v3CmdAppVersion) {
        _finishVersionQueryFailed(
            "$_currentVersionQueryTag版本查询失败 status=0x${status.toRadixString(16)}");
        return;
      }

      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3CmdSetDataEndpointMode) {
        _rwcpSetupInProgress = false;
        rwcpStatusText.value = "RWCP错误";
        _enterFatalUpgradeState("RWCP数据通道启用失败");
        return;
      }

      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          (commandId == GaiaCommandBuilder.v3CmdUpgradeConnect ||
              commandId == GaiaCommandBuilder.v3CmdUpgradeControl ||
              commandId == GaiaCommandBuilder.v3CmdUpgradeDisconnect)) {
        _enterFatalUpgradeState(
            "V3升级命令失败 cmdId=$commandId status=0x${status.toRadixString(16)}");
      }
      return;
    }

    // 未识别的包类型，记录日志并报告错误
    addLog(
        "未知V3包类型 feature=$feature packetType=$packetType cmdId=$commandId payload=${payload.map((e) => e.toRadixString(16)).toList()}");
    if (isUpgrading.value) {
      _reportDeviceError(
          "收到未知V3响应 feature=$feature type=$packetType cmd=$commandId",
          triggerRecovery: false);
    }
  }

  void enableRwcpForUpgrade() {
    if (_rwcpSetupInProgress) {
      return;
    }
    _rwcpSetupInProgress = true;
    rwcpStatusText.value = "启用中";
    addLog("启用RWCP数据通道");
    final packet =
        _buildGaiaPacket(_setDataEndpointModeCommand(), payload: [0x01]);
    writeMsg(packet.getBytes());
  }

  void startUpgradeProcess() {
    if (!isUpgrading.value) {
      isUpgrading.value = true;
      resetUpload();
      sendSyncReq();
    } else {
      addLog("正在升级");
      return;
    }
  }

  /// <p>To reset the file transfer.</p>
  void resetUpload() {
    _upgradeStateMachine.reset();
    transFerComplete = false;
    mBytesToSend = 0;
    mStartOffset = 0;
  }

  Future<void> stopUpgrade(
      {bool sendAbort = true, bool sendDisconnect = true}) async {
    _clearUpgradeWatchdog();
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _pendingStartAfterVersionQuery = false;
    _rwcpSetupInProgress = false;
    rwcpStatusText.value = "待启用";
    timeCount.value = 0;
    if (sendAbort) {
      abortUpgrade();
    }
    resetUpload();
    writeRTCPCount = 0;
    updatePer.value = 0;
    isUpgrading.value = false;
    _dfuWriteInFlight = false;
    _dfuPendingChunkSize = 0;
    if (sendDisconnect &&
        isDeviceConnected.value &&
        connectDeviceId.isNotEmpty) {
      await Future.delayed(const Duration(milliseconds: 500));
      sendUpgradeDisconnect();
    }
  }

  Future<bool> loadFirmwareFile() async {
    String usePath = firmwarePath.value;
    if (usePath.isEmpty) {
      usePath = await _defaultFirmwarePathResolver();
      firmwarePath.value = usePath;
    }
    final selectedFile = File(usePath);
    file = selectedFile;
    if (!await selectedFile.exists()) {
      addLog("升级文件不存在：$usePath");
      return false;
    }
    mBytesFile = await selectedFile.readAsBytes();
    if ((mBytesFile ?? []).isEmpty) {
      addLog("升级文件为空：$usePath");
      return false;
    }
    fileMd5 = StringUtils.file2md5(mBytesFile ?? []).toUpperCase();
    addLog("读取到文件:$usePath");
    addLog("读取到文件MD5$fileMd5");
    return true;
  }

  Future<void> sendSyncReq() async {
    //A2305C3A9059C15171BD33F3BB08ADE4 MD5
    //000A0642130004BB08ADE4
    final loaded = await loadFirmwareFile();
    if (!loaded) {
      stopUpgrade();
      return;
    }
    final endMd5 = StringUtils.hexStringToBytes(fileMd5.substring(24));
    _upgradeStateMachine.startUpgrade();
    VMUPacket packet = VMUPacket.get(OpCodes.upgradeSyncReq, data: endMd5);
    sendVMUPacket(packet, false);
  }

  void sendDfuRequest() {
    addLog("发送DFU_REQUEST");
    _dfuWriteInFlight = false;
    _dfuPendingChunkSize = 0;
    mStartOffset = 0;
    mBytesToSend = mBytesFile?.length ?? 0;
    final packet = _buildGaiaPacket(GAIA.commandDfuRequest);
    writeMsg(packet.getBytes());
  }

  void sendDfuBegin() {
    if ((mBytesFile ?? []).isEmpty) {
      addLog("DFU_BEGIN失败：固件数据为空");
      stopUpgrade(sendAbort: false);
      return;
    }
    final fileLength = mBytesFile?.length ?? 0;
    final fileLengthBytes = [
      (fileLength >> 24) & 0xFF,
      (fileLength >> 16) & 0xFF,
      (fileLength >> 8) & 0xFF,
      fileLength & 0xFF
    ];
    final digest = StringUtils.hexStringToBytes(fileMd5.substring(24));
    final payload = [...fileLengthBytes, ...digest];
    addLog(
        "发送DFU_BEGIN length=$fileLength digest=${StringUtils.byteToHexString(digest)}");
    final packet = _buildGaiaPacket(GAIA.commandDfuBegin, payload: payload);
    writeMsg(packet.getBytes());
  }

  void sendNextDfuPacket() {
    if (!isUpgrading.value || _dfuWriteInFlight) {
      return;
    }
    final bytes = mBytesFile ?? [];
    if (mStartOffset >= bytes.length) {
      sendDfuCommit();
      return;
    }
    final chunkSize = (bytes.length - mStartOffset) < mPayloadSizeMax
        ? (bytes.length - mStartOffset)
        : mPayloadSizeMax;
    final payload = bytes.sublist(mStartOffset, mStartOffset + chunkSize);
    _dfuPendingChunkSize = chunkSize;
    _dfuWriteInFlight = true;
    final packet = _buildGaiaPacket(GAIA.commandDfuWrite, payload: payload);
    writeMsg(packet.getBytes());
  }

  void onDfuWriteAck() {
    if (!_dfuWriteInFlight) {
      return;
    }
    _dfuWriteInFlight = false;
    mStartOffset += _dfuPendingChunkSize;
    _dfuPendingChunkSize = 0;
    final total = (mBytesFile ?? []).length;
    if (total > 0) {
      updatePer.value = mStartOffset * 100.0 / total;
    }
    sendNextDfuPacket();
  }

  void sendDfuCommit() {
    addLog("发送DFU_COMMIT");
    final packet = _buildGaiaPacket(GAIA.commandDfuCommit);
    writeMsg(packet.getBytes());
  }

  void onDfuCommitAck() {
    updatePer.value = 100;
    sendDfuGetResult();
  }

  void sendDfuGetResult() {
    _dfuResultTimer?.cancel();
    addLog("发送DFU_GET_RESULT");
    final packet = _buildGaiaPacket(GAIA.commandDfuGetResult);
    writeMsg(packet.getBytes());
    _dfuResultTimer =
        Timer(Duration(seconds: kDfuResultQueryTimeoutSeconds), () {
      if (!isUpgrading.value) {
        return;
      }
      addLog("DFU_GET_RESULT超时，按提交成功处理");
      _finishDfuUpgrade("DFU提交完成（结果查询超时）", queryPostVersion: true);
    });
  }

  void onDfuGetResultAck(GaiaPacketBLE packet) {
    _dfuResultTimer?.cancel();
    final payload = packet.mPayload ?? [];
    if (payload.length < 2) {
      _finishDfuUpgrade("DFU提交完成（无结果码）", queryPostVersion: true);
      return;
    }
    final resultCode = payload[1];
    if (resultCode == 0x00) {
      _finishDfuUpgrade("DFU升级完成，设备返回成功", queryPostVersion: true);
      return;
    }
    _dfuWriteInFlight = false;
    isUpgrading.value = false;
    _timer?.cancel();
    addLog(
        "DFU升级失败，结果码=0x${resultCode.toRadixString(16).padLeft(2, '0')} ${_dfuResultText(resultCode)}");
  }

  void _finishDfuUpgrade(String message, {bool queryPostVersion = false}) {
    _dfuWriteInFlight = false;
    isUpgrading.value = false;
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    addLog(message);
    if (queryPostVersion) {
      _schedulePostUpgradeVersionQuery();
    }
  }

  // 状态/命令文本转换（代理到 GaiaCommandBuilder）
  String _gaiaStatusText(int status) => _cmdBuilder.gaiaStatusText(status);
  String _dfuResultText(int resultCode) =>
      _cmdBuilder.dfuResultText(resultCode);

  void queryApplicationVersion({
    required String tag,
    required void Function(String version) onSuccess,
    required VoidCallback onFailed,
  }) {
    if (_isVersionQueryInFlight) {
      addLog("版本查询进行中，忽略重复请求");
      return;
    }
    if (!isDeviceConnected.value) {
      addLog("$tag版本查询失败：设备未连接");
      onFailed();
      return;
    }
    _currentVersionQueryTag = tag;
    _onVersionQuerySuccess = onSuccess;
    _onVersionQueryFailed = onFailed;
    _isVersionQueryInFlight = true;
    _versionQueryTimer?.cancel();
    addLog("发送GET_APPLICATION_VERSION($tag)");
    final packet = _buildGaiaPacket(_getApplicationVersionCommand());
    writeMsg(packet.getBytes());
    _versionQueryTimer =
        Timer(Duration(seconds: kVersionQueryTimeoutSeconds), () {
      if (!_isVersionQueryInFlight) {
        return;
      }
      _finishVersionQueryFailed("$tag版本查询超时");
    });
  }

  void onApplicationVersionAckV3(List<int> payload) {
    if (!_isVersionQueryInFlight) {
      return;
    }
    _versionQueryTimer?.cancel();
    final version = _parseApplicationVersionV3(payload);
    final tag = _currentVersionQueryTag;
    _isVersionQueryInFlight = false;
    _currentVersionQueryTag = "";
    addLog("$tag版本号: $version");
    final successCallback = _onVersionQuerySuccess;
    _onVersionQuerySuccess = null;
    _onVersionQueryFailed = null;
    successCallback?.call(version);
  }

  void _finishVersionQueryFailed(String reason) {
    _versionQueryTimer?.cancel();
    final failedCallback = _onVersionQueryFailed;
    _isVersionQueryInFlight = false;
    _currentVersionQueryTag = "";
    _onVersionQuerySuccess = null;
    _onVersionQueryFailed = null;
    addLog(reason);
    failedCallback?.call();
  }

  /// 格式化字节数组为版本字符串
  /// 如果是可打印 ASCII，返回字符串格式；否则返回 HEX 格式
  String _formatVersionBytes(List<int> bytes) {
    if (bytes.isEmpty) return "UNKNOWN";
    final hex = StringUtils.byteToHexString(bytes).toUpperCase();
    final printable = bytes.every((b) => b >= 0x20 && b <= 0x7E);
    if (printable) {
      return "${String.fromCharCodes(bytes)} (HEX:$hex)";
    }
    return "HEX:$hex";
  }

  String _parseApplicationVersionV3(List<int> payload) {
    return _formatVersionBytes(payload);
  }

  void _schedulePostUpgradeVersionQuery() {
    _postUpgradeVersionRetryTimer?.cancel();
    _postUpgradeVersionRetryCount = 0;
    _postUpgradeVersionRetryTimer = Timer.periodic(
        Duration(seconds: kPostUpgradeVersionRetryIntervalSeconds), (timer) {
      if (_postUpgradeVersionRetryCount >= kPostUpgradeVersionMaxRetries) {
        timer.cancel();
        addLog("升级后版本查询超时，无法自动对比");
        return;
      }
      _postUpgradeVersionRetryCount++;
      if (_isVersionQueryInFlight || isUpgrading.value) {
        return;
      }
      if (!isDeviceConnected.value) {
        addLog(
            "等待设备重连后查询升级后版本($_postUpgradeVersionRetryCount/$kPostUpgradeVersionMaxRetries)");
        return;
      }
      queryApplicationVersion(
        tag: "升级后",
        onSuccess: (version) {
          versionAfterUpgrade.value = version;
          timer.cancel();
          _logVersionCompare();
        },
        onFailed: () {
          if (_postUpgradeVersionRetryCount >= kPostUpgradeVersionMaxRetries) {
            timer.cancel();
            addLog("升级后版本查询失败，无法自动对比");
          }
        },
      );
    });
  }

  void _logVersionCompare() {
    final before = versionBeforeUpgrade.value;
    final after = versionAfterUpgrade.value;
    if (before == "UNKNOWN" || after == "UNKNOWN") {
      addLog("版本对比结果：信息不足（before=$before, after=$after）");
      return;
    }
    if (before == after) {
      addLog("版本对比结果：未变化（升级可能未生效）");
      return;
    }
    addLog("版本对比结果：已变化（升级生效）");
  }

  /// <p>To send a VMUPacket over the defined protocol communication.</p>
  ///
  /// @param bytes
  ///              The packet to send.
  /// @param isTransferringData
  ///              True if the packet is about transferring the file data, false for any other packet.
  void sendVMUPacket(VMUPacket packet, bool isTransferringData) {
    List<int> bytes = packet.getBytes();
    if (isTransferringData && mIsRWCPEnabled.value) {
      final gaiaPacket =
          _buildGaiaPacket(_upgradeControlCommand(), payload: bytes);
      try {
        List<int> gaiaBytes = gaiaPacket.getBytes();
        if (mTransferStartTime <= 0) {
          mTransferStartTime = DateTime.now().millisecondsSinceEpoch;
        }
        bool success = mRWCPClient.sendData(gaiaBytes);
        if (!success) {
          addLog(
              "Fail to send GAIA packet for GAIA command: ${gaiaPacket.getCommandId()}");
        }
      } catch (e) {
        addLog("Exception when attempting to create GAIA packet: $e");
      }
    } else {
      final pkg = _buildGaiaPacket(_upgradeControlCommand(), payload: bytes);
      writeMsg(pkg.getBytes());
    }
  }

  @override
  void sendVmuPacket(VMUPacket packet, bool isTransferringData) {
    sendVMUPacket(packet, isTransferringData);
  }

  void receiveVMUPacket(List<int> data) {
    try {
      final packet = VMUPacket.getPackageFromByte(data);
      if (packet == null) {
        addLog(
            "receiveVMUPacket 无法解析VMU包: ${StringUtils.byteToHexString(data)}");
        return;
      }
      if (isUpgrading.value || packet.mOpCode == OpCodes.upgradeAbortCfm) {
        _upgradeStateMachine.handleVmuPacket(packet);
      } else {
        addLog(
            "receiveVMUPacket Received VMU packet while application is not upgrading anymore, opcode received");
      }
    } catch (e) {
      addLog("receiveVMUPacket $e");
    }
  }

  Future<void> sendUpgradeConnect() async {
    GaiaPacketBLE packet = _buildGaiaPacket(_upgradeConnectCommand());
    writeMsg(packet.getBytes());
  }

  void sendUpgradeDisconnect() {
    GaiaPacketBLE packet = _buildGaiaPacket(_upgradeDisconnectCommand());
    writeMsg(packet.getBytes());
  }

  void _handleDataBytesRequest(int bytesToSend, int fileOffset) {
    mBytesToSend = bytesToSend;
    addLog("本次发包: offset=$fileOffset bytesToSend=$mBytesToSend");
    mStartOffset += (fileOffset > 0 &&
            fileOffset + mStartOffset < (mBytesFile?.length ?? 0))
        ? fileOffset
        : 0;

    mBytesToSend = (mBytesToSend > 0) ? mBytesToSend : 0;
    final remainingLength = (mBytesFile?.length ?? 0) - mStartOffset;
    mBytesToSend =
        (mBytesToSend < remainingLength) ? mBytesToSend : remainingLength;
    if (mIsRWCPEnabled.value) {
      while (mBytesToSend > 0) {
        sendNextDataPacket();
      }
      return;
    }
    sendNextDataPacket();
  }

  void abortUpgrade() {
    if (mRWCPClient.isRunningASession()) {
      mRWCPClient.cancelTransfer();
    }
    mProgressQueue.clear();
    sendAbortReq();
    isUpgrading.value = false;
  }

  void sendAbortReq() {
    VMUPacket packet = VMUPacket.get(OpCodes.upgradeAbortReq);
    sendVMUPacket(packet, false);
  }

  //主要发包逻辑
  void sendNextDataPacket() {
    if (!isUpgrading.value) {
      stopUpgrade();
      return;
    }
    // inform listeners about evolution
    onFileUploadProgress();
    int bytesToSend = mBytesToSend < mMaxLengthForDataTransfer - 1
        ? mBytesToSend
        : mMaxLengthForDataTransfer - 1;
    // to know if we are sending the last data packet.
    bool lastPacket = (mBytesFile ?? []).length - mStartOffset <= bytesToSend;
    if (lastPacket) {
      addLog(
          "mMaxLengthForDataTransfer$mMaxLengthForDataTransfer bytesToSend$bytesToSend lastPacket$lastPacket");
    }
    List<int> dataToSend = [];
    for (int i = 0; i < bytesToSend; i++) {
      dataToSend.add((mBytesFile ?? [])[mStartOffset + i]);
    }

    if (lastPacket) {
      _upgradeStateMachine.setWasLastPacket(true);
      mBytesToSend = 0;
    } else {
      _upgradeStateMachine.setWasLastPacket(false);
      mStartOffset += bytesToSend;
      mBytesToSend -= bytesToSend;
    }

    sendData(lastPacket, dataToSend);
  }

  //计算进度
  void onFileUploadProgress() {
    final fileLength = (mBytesFile ?? []).length;
    if (fileLength <= 0) return;
    double percentage = (mStartOffset * 100.0 / fileLength);
    percentage = (percentage < 0)
        ? 0
        : (percentage > 100)
            ? 100
            : percentage;
    if (mIsRWCPEnabled.value) {
      mProgressQueue.add(percentage);
    } else {
      updatePer.value = percentage;
    }
  }

  void sendData(bool lastPacket, List<int> data) {
    List<int> dataToSend = [];
    dataToSend.add(lastPacket ? 0x01 : 0x00);
    dataToSend.addAll(data);
    sendPkgCount++;
    VMUPacket packet = VMUPacket.get(OpCodes.upgradeData, data: dataToSend);
    sendVMUPacket(packet, true);
  }

  void onSuccessfulTransmission() {
    _upgradeStateMachine.onSuccessfulTransmission();
    if (mBytesToSend > 0 &&
        _upgradeStateMachine.resumePoint == ResumePoints.dataTransfer &&
        !mIsRWCPEnabled.value) {
      sendNextDataPacket();
    }
  }

  void onRWCPNotSupported() {
    addLog("RWCP onRWCPNotSupported：设备不支持RWCP，终止升级");
    rwcpStatusText.value = "设备不支持";
    _enterFatalUpgradeState("设备不支持RWCP");
  }

  void askForConfirmation(int type) {
    int code = -1;
    switch (type) {
      case ConfirmationType.commit:
        {
          code = OpCodes.upgradeCommitCfm;
        }
        break;
      case ConfirmationType.inProgress:
        {
          code = OpCodes.upgradeInProgressRes;
        }
        break;
      case ConfirmationType.transferComplete:
        {
          code = OpCodes.upgradeTransferCompleteRes;
        }
        break;
      case ConfirmationType.batteryLowOnDevice:
        {
          addLog("设备电量过低，停止升级");
          stopUpgrade();
        }
        return;
      case ConfirmationType.warningFileIsDifferent:
        {
          stopUpgrade();
        }
        return;
    }
    addLog("askForConfirmation ConfirmationType type $type $code");
    VMUPacket packet = VMUPacket.get(code, data: [0]);
    sendVMUPacket(packet, false);
  }

  void sendErrorConfirmation(List<int> data) {
    VMUPacket packet = VMUPacket.get(OpCodes.upgradeErrorWarnRes, data: data);
    sendVMUPacket(packet, false);
  }

  void disconnectUpgrade() {
    sendUpgradeDisconnect();
  }

  @override
  void onTransferFailed() {
    _enterFatalUpgradeState("RWCP传输失败");
  }

  @override
  void onTransferFinished() {
    onSuccessfulTransmission();
    mProgressQueue.clear();
  }

  @override
  void onTransferProgress(int acknowledged) {
    if (acknowledged > 0) {
      double percentage = 0;
      while (acknowledged > 0 && mProgressQueue.isNotEmpty) {
        percentage = mProgressQueue.removeFirst();
        acknowledged--;
      }
      if (mIsRWCPEnabled.value) {
        updatePer.value = percentage;
      }
      // addLog("$mIsRWCPEnabled 升级进度$percentage");
    }
  }

  @override
  bool sendRWCPSegment(List<int> bytes) {
    writeMsgRWCP(bytes);
    return true;
  }

  @override
  void onUpgradeProgress(double percent) {
    updatePer.value = percent;
  }

  @override
  void onUpgradeComplete() {
    isUpgrading.value = false;
    _timer?.cancel();
    addLog("receiveCompleteIND 升级完成");
    _schedulePostUpgradeVersionQuery();
    disconnectUpgrade();
  }

  @override
  void onUpgradeError(String reason) {
    _enterFatalUpgradeState(reason);
  }

  @override
  void onRequestNextDataPacket(int bytesToSend, int startOffset) {
    _handleDataBytesRequest(bytesToSend, startOffset);
  }

  @override
  void onRequestConfirmation(int confirmationType) {
    if (confirmationType == ConfirmationType.transferComplete) {
      transFerComplete = true;
    }
    askForConfirmation(confirmationType);
  }

  //一般命令写入通道
  Future<void> writeData(List<int> data) async {
    try {
      _logGaiaWritePacket(data, channel: "WR");
      if (_enableWriteTraceLog) {
        addLog("writeData start>${StringUtils.byteToHexString(data)}");
      }
      await _bleManager.writeWithResponse(data);
      _touchUpgradeWatchdog();
      if (_enableWriteTraceLog) {
        addLog("writeData end>${StringUtils.byteToHexString(data)}");
      }
    } catch (e) {
      addLog("写入失败(writeWithResponse): $e");
      _reportDeviceError("写通道异常(writeWithResponse)");
      if (isUpgrading.value) {
        _enterFatalUpgradeState("写入通道异常");
      }
    }
  }

  //RWCP写入通道
  Future<void> writeMsgRWCP(List<int> data) async {
    try {
      _logRwcpWritePacket(data);
      await _bleManager.writeWithoutResponse(data);
      _touchUpgradeWatchdog();
    } catch (e) {
      addLog("写入失败(writeWithoutResponse): $e");
      _reportDeviceError("写通道异常(writeWithoutResponse)");
      if (isUpgrading.value) {
        _enterFatalUpgradeState("RWCP写入异常");
      }
    }
  }

  void disconnect() {
    _reconnectTimer?.cancel();
    _scanWatchdogTimer?.cancel();
    _bleManager.disconnect();
    isDeviceConnected.value = false;
    isConnecting.value = false;
    connectingDeviceId.value = "";
  }

  Future<void> restPayloadSize() async {
    int mtu = await _bleManager.requestMtu(256);
    if (!mIsRWCPEnabled.value) {
      mtu = 23;
    }
    int dataSize = mtu - 3;
    mPayloadSizeMax = dataSize - 4;
    addLog("协商mtu $mtu mPayloadSizeMax $mPayloadSizeMax");
  }

  /// 添加日志（代理到 LogBuffer）
  void addLog(String message) {
    _logBuffer.addLog(message);
  }

  void _logGaiaWritePacket(List<int> bytes, {required String channel}) {
    final gaia = GaiaPacketBLE.fromByte(bytes);
    if (gaia == null) {
      addLog("TX[$channel] RAW ${StringUtils.byteToHexString(bytes)}");
      return;
    }
    final cmd = gaia.getCommand();
    final cmdText = _cmdBuilder.gaiaCommandText(cmd);
    final payload = gaia.mPayload ?? [];
    final base =
        "TX[$channel] $cmdText(0x${cmd.toRadixString(16).padLeft(4, '0').toUpperCase()})";
    if (cmd == _upgradeControlCommand()) {
      final vmu = VMUPacket.getPackageFromByte(payload);
      if (vmu != null) {
        final vmuData = vmu.mData ?? [];
        final isUpgradeData = vmu.mOpCode == OpCodes.upgradeData;
        final isLastDataPacket = vmuData.isNotEmpty && vmuData.first == 0x01;
        final shouldLogUpgradeData = !isUpgradeData ||
            sendPkgCount <= 3 ||
            sendPkgCount % _dataPacketLogSampleInterval == 0 ||
            isLastDataPacket;
        if (!shouldLogUpgradeData) {
          return;
        }
        final chunkLength = isUpgradeData && vmuData.isNotEmpty
            ? vmuData.length - 1
            : vmuData.length;
        addLog(
            "$base VMU=${_vmuOpText(vmu.mOpCode)}(0x${vmu.mOpCode.toRadixString(16).padLeft(2, '0').toUpperCase()}) "
            "len=$chunkLength last=${isLastDataPacket ? 1 : 0} bytes=${StringUtils.byteToHexString(bytes)}");
        return;
      }
    }
    addLog("$base bytes=${StringUtils.byteToHexString(bytes)}");
  }

  void _logRwcpWritePacket(List<int> bytes) {
    final segment = Segment.parse(bytes);
    final opCode = segment.getOperationCode();
    final seq = segment.getSequenceNumber();
    final opText = _rwcpClientOpText(opCode);
    if (opCode != RWCPOpCodeClient.data) {
      addLog(
          "TX[RWCP] op=$opText seq=$seq bytes=${StringUtils.byteToHexString(bytes)}");
      return;
    }

    final payload = segment.getPayload();
    final gaia = GaiaPacketBLE.fromByte(payload);
    if (gaia == null) {
      addLog(
          "TX[RWCP] op=$opText seq=$seq bytes=${StringUtils.byteToHexString(bytes)}");
      return;
    }
    final cmd = gaia.getCommand();
    final gaiaName = _cmdBuilder.gaiaCommandText(cmd);
    final gaiaPayload = gaia.mPayload ?? [];
    if (cmd == _upgradeControlCommand()) {
      final vmu = VMUPacket.getPackageFromByte(gaiaPayload);
      if (vmu != null) {
        final vmuData = vmu.mData ?? [];
        final isUpgradeData = vmu.mOpCode == OpCodes.upgradeData;
        final isLastDataPacket = vmuData.isNotEmpty && vmuData.first == 0x01;
        final shouldLogUpgradeData = !isUpgradeData ||
            sendPkgCount <= 3 ||
            sendPkgCount % _dataPacketLogSampleInterval == 0 ||
            isLastDataPacket;
        if (!shouldLogUpgradeData) {
          return;
        }
        final chunkLength = isUpgradeData && vmuData.isNotEmpty
            ? vmuData.length - 1
            : vmuData.length;
        addLog("TX[RWCP] op=$opText seq=$seq gaia=$gaiaName "
            "vmu=${_vmuOpText(vmu.mOpCode)}(0x${vmu.mOpCode.toRadixString(16).padLeft(2, '0').toUpperCase()}) "
            "len=$chunkLength last=${isLastDataPacket ? 1 : 0} bytes=${StringUtils.byteToHexString(bytes)}");
        return;
      }
    }
    addLog("TX[RWCP] op=$opText seq=$seq gaia=$gaiaName "
        "bytes=${StringUtils.byteToHexString(bytes)}");
  }

  String _rwcpClientOpText(int opCode) {
    switch (opCode) {
      case RWCPOpCodeClient.data:
        return "DATA";
      case RWCPOpCodeClient.syn:
        return "SYN";
      case RWCPOpCodeClient.rst:
        return "RST";
      default:
        return "UNKNOWN";
    }
  }

  String _vmuOpText(int opCode) {
    switch (opCode) {
      case OpCodes.upgradeStartReq:
        return "upgradeStartReq";
      case OpCodes.upgradeData:
        return "upgradeData";
      case OpCodes.upgradeAbortReq:
        return "upgradeAbortReq";
      case OpCodes.upgradeTransferCompleteRes:
        return "upgradeTransferCompleteRes";
      case OpCodes.upgradeInProgressRes:
        return "upgradeInProgressRes";
      case OpCodes.upgradeCommitCfm:
        return "upgradeCommitCfm";
      case OpCodes.upgradeSyncReq:
        return "upgradeSyncReq";
      case OpCodes.upgradeStartDataReq:
        return "upgradeStartDataReq";
      case OpCodes.upgradeIsValidationDoneReq:
        return "upgradeIsValidationDoneReq";
      case OpCodes.upgradeErrorWarnRes:
        return "upgradeErrorWarnRes";
      default:
        return "unknownVMU";
    }
  }

  @override
  void onLog(String message) {
    addLog(message);
  }

  /// 清空日志
  void clearLog() {
    _logBuffer.clear();
  }

  void _armUpgradeWatchdog() {
    _clearUpgradeWatchdog();
    if (!isUpgrading.value) {
      return;
    }
    _upgradeWatchdogTimer =
        Timer(Duration(seconds: kUpgradeWatchdogTimeoutSeconds), () {
      if (!isUpgrading.value) {
        return;
      }
      _enterFatalUpgradeState('升级超时：$kUpgradeWatchdogTimeoutSeconds秒内未收到有效进展');
    });
  }

  void _touchUpgradeWatchdog() {
    if (!isUpgrading.value) {
      return;
    }
    _armUpgradeWatchdog();
  }

  void _clearUpgradeWatchdog() {
    _upgradeWatchdogTimer?.cancel();
    _upgradeWatchdogTimer = null;
  }

  void _enterFatalUpgradeState(String reason) {
    if (_fatalUpgradeReason == reason && !isUpgrading.value) {
      return;
    }
    _fatalUpgradeReason = reason;
    _autoReconnectEnabled = false;
    rwcpStatusText.value = "错误已退出";
    addLog("致命错误：$reason，已自动退出升级并关闭自动重连");
    final wasUpgrading = isUpgrading.value;
    if (isUpgrading.value) {
      unawaited(stopUpgrade(sendAbort: false));
    } else {
      _clearUpgradeWatchdog();
    }
    // stopUpgrade 已将 isUpgrading 置 false，恢复流程中无需再次 stopUpgrade
    _reportDeviceError(reason, triggerRecovery: wasUpgrading);
  }

  void _reportDeviceError(String reason, {bool triggerRecovery = false}) {
    if (!autoRecoveryEnabled.value) {
      return;
    }
    final now = DateTime.now();
    if (_lastErrorTime == null ||
        now.difference(_lastErrorTime!).inSeconds > kErrorBurstWindowSeconds) {
      _errorBurstCount = 0;
      errorCount.value = 0;
    }
    _lastErrorTime = now;
    _errorBurstCount += 1;
    errorCount.value = _errorBurstCount;
    addLog("错误累计($_errorBurstCount/$kErrorBurstThreshold): $reason");
    if (triggerRecovery || _errorBurstCount >= kErrorBurstThreshold) {
      unawaited(_quickRecoverFromDeviceError("自动恢复触发: $reason"));
    }
  }

  void quickRecoverNow() {
    unawaited(_quickRecoverFromDeviceError("手动快速恢复", forceAbort: true));
  }

  Future<void> _quickRecoverFromDeviceError(String reason,
      {bool forceAbort = false}) async {
    if (_isRecovering) {
      addLog("恢复进行中，忽略重复触发");
      return;
    }
    final now = DateTime.now();
    _recoveryWindowStart ??= now;
    if (now.difference(_recoveryWindowStart!).inMinutes >=
        kRecoveryWindowMinutes) {
      _recoveryWindowStart = now;
      _recoveryAttempts = 0;
    }
    if (_recoveryAttempts >= kMaxRecoveryAttemptsPerWindow) {
      recoveryStatusText.value = "恢复受限";
      addLog('$kRecoveryWindowMinutes分钟内恢复次数过多，暂停自动恢复');
      return;
    }
    _isRecovering = true;
    _recoveryAttempts += 1;
    _errorBurstCount = 0;
    errorCount.value = 0;
    recoveryStatusText.value = "恢复中";
    rwcpStatusText.value = "恢复中";
    // 手动恢复或第二次及以后的自动恢复，发送 Abort 强制重置设备状态
    final shouldSendAbort = forceAbort || _recoveryAttempts >= 2;
    addLog(
        "执行快速恢复($_recoveryAttempts/$kMaxRecoveryAttemptsPerWindow): $reason${shouldSendAbort ? ' [含Abort]' : ''}");
    try {
      if (isUpgrading.value) {
        await stopUpgrade(sendAbort: shouldSendAbort);
      } else if (shouldSendAbort && isDeviceConnected.value) {
        // 非升级状态但需要强制重置，直接发送 Abort
        sendAbortReq();
        await Future.delayed(const Duration(milliseconds: 300));
      }
      _bleManager.disconnect();
      isDeviceConnected.value = false;
      if (connectDeviceId.isNotEmpty) {
        recoveryStatusText.value = "重连中";
        rwcpStatusText.value = "重连中";
        await Future.delayed(Duration(seconds: kRecoveryDelaySeconds));
        await connectDevice(connectDeviceId);
      } else {
        addLog("无连接设备ID，无法自动重连");
        recoveryStatusText.value = "恢复失败";
        rwcpStatusText.value = "未连接";
      }
    } catch (e) {
      recoveryStatusText.value = "恢复失败";
      addLog("快速恢复失败: $e");
    } finally {
      _isRecovering = false;
    }
  }

  @override
  void onClose() {
    _deviceListWorker?.dispose();
    _logBuffer.dispose();
    _bleManager.dispose();
    mRWCPClient.dispose();
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    _upgradeWatchdogTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _reconnectTimer?.cancel();
    _scanWatchdogTimer?.cancel();
    super.onClose();
  }

  Future<void> startScan() async {
    isScanning.value = true;
    isConnecting.value = false;
    connectingDeviceId.value = "";
    deviceListUiState.value = DeviceListUiState.scanning;
    deviceListHint.value = "扫描中...";
    final result = await _bleManager.startScan();
    switch (result) {
      case BleScanStartResult.started:
        _scanWatchdogTimer?.cancel();
        _scanWatchdogTimer = Timer(const Duration(seconds: 8), () {
          if (devices.isEmpty && isScanning.value) {
            isScanning.value = false;
            deviceListUiState.value = DeviceListUiState.empty;
            deviceListHint.value = "未发现设备，请确认设备已开机并靠近手机";
          }
        });
        return;
      case BleScanStartResult.locationDenied:
        isScanning.value = false;
        deviceListUiState.value = DeviceListUiState.error;
        deviceListHint.value = "缺少定位权限";
        _notifyUser("请开启定位权限后重试");
        return;
      case BleScanStartResult.bluetoothScanDenied:
        isScanning.value = false;
        deviceListUiState.value = DeviceListUiState.error;
        deviceListHint.value = "缺少蓝牙扫描权限";
        _notifyUser("请开启蓝牙扫描权限后重试");
        return;
      case BleScanStartResult.bluetoothConnectDenied:
        isScanning.value = false;
        deviceListUiState.value = DeviceListUiState.error;
        deviceListHint.value = "缺少蓝牙连接权限";
        _notifyUser("请开启蓝牙连接权限后重试");
        return;
      case BleScanStartResult.bluetoothDenied:
        isScanning.value = false;
        deviceListUiState.value = DeviceListUiState.error;
        deviceListHint.value = "缺少蓝牙权限";
        _notifyUser("请开启蓝牙权限后重试");
        return;
      case BleScanStartResult.failed:
        isScanning.value = false;
        deviceListUiState.value = DeviceListUiState.error;
        deviceListHint.value = "扫描失败，请重试";
        _notifyUser("扫描失败，请稍后重试");
        return;
    }
  }

  Future<void> stopScan() async {
    _scanWatchdogTimer?.cancel();
    await _bleManager.stopScan();
    isScanning.value = false;
    if (devices.isEmpty) {
      deviceListUiState.value = DeviceListUiState.empty;
      deviceListHint.value = "已停止扫描，未发现设备";
      return;
    }
    deviceListUiState.value = DeviceListUiState.ready;
    deviceListHint.value = "已停止扫描";
  }
}
