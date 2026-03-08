import 'dart:async';
import 'dart:collection';
import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter/services.dart';
import 'package:get/get.dart';
import 'package:path_provider/path_provider.dart';

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
import 'package:gaia/utils/crash_reporter.dart';
import 'package:gaia/utils/app_settings.dart';

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
  static const MethodChannel _systemBluetoothChannel =
      MethodChannel('gaia/system_bluetooth');

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

  /// Abort 确认等待超时（秒）
  static const int kAbortConfirmTimeoutSeconds = 2;

  /// 重连后恢复校验延迟（秒）
  static const int kRecoveryReconnectCheckSeconds = 7;

  /// 错误累计触发恢复的阈值
  static const int kErrorBurstThreshold = 3;

  /// 错误累计时间窗口（秒）
  static const int kErrorBurstWindowSeconds = 10;

  /// 恢复时间窗口（分钟）
  static const int kRecoveryWindowMinutes = 1;

  /// 恢复窗口内最大恢复次数
  static const int kMaxRecoveryAttemptsPerWindow = 3;

  /// RWCP 发送节流：每次泵最多入队的 UpgradeData 包数。
  ///
  /// 背景：原实现会在收到 DATA_BYTES_REQ 后用 while 一次性把大量数据切片入队到 RWCP 的 pending 队列，
  /// 在设备端进入校验/写闪存导致 ACK 变慢时，pending 会快速膨胀，引发内存峰值甚至 OOM 闪退。
  static const int _kRwcpPumpMaxPacketsPerTick = 24;

  // 组件实例
  late final LogBuffer _logBuffer;
  late final GaiaCommandBuilder _cmdBuilder;
  late final BleConnectionManager _bleManager;
  late final UpgradeStateMachine _upgradeStateMachine;
  final BleConnectionManager? _bleManagerOverride;
  final UpgradeStateMachine? _upgradeStateMachineOverride;
  final DefaultFirmwarePathResolver _defaultFirmwarePathResolver;
  final DateTime Function() _nowProvider;

  var logText = "".obs;
  final String tag = "OtaServer";
  late final RxList<DiscoveredDevice> devices;

  /// 最近一次成功/尝试连接的设备 ID（用于自动恢复时的重连目标）。
  String connectDeviceId = "";

  /// 当前已连接设备 ID 以 BleConnectionManager 为单一真相。
  String get currentConnectedDeviceId => _bleManager.connectedDeviceId;
  final Uuid otaUUID = BleConstants.otaServiceUuid;
  final Uuid notifyUUID = BleConstants.notifyCharacteristicUuid;
  final Uuid writeUUID = BleConstants.writeCharacteristicUuid;
  final Uuid writeNoResUUID = BleConstants.writeNoResponseCharacteristicUuid;
  RxBool isDeviceConnected = false.obs;
  RxBool isScanning = false.obs;
  RxBool isConnecting = false.obs;
  RxList<String> systemConnectedDeviceIds = <String>[].obs;
  RxString connectingDeviceId = "".obs;
  Rx<DeviceListUiState> deviceListUiState = DeviceListUiState.idle.obs;
  RxString deviceListHint = "点击“扫描蓝牙”开始搜索设备".obs;
  RxnString userMessage = RxnString();
  RxInt errorCount = 0.obs;

  /// To know if the upgrade process is currently running.
  RxBool isUpgrading = false.obs;

  /// 升级成功次数计数（用于 UI 触发一次性弹窗/提示）。
  ///
  /// 设计：用“自增计数”比用 bool 更易表达事件语义，避免在 UI 层处理重复/重置。
  final RxInt upgradeSuccessCounter = 0.obs;

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
  var currentVersion = "UNKNOWN".obs;

  /// To know if we have to disconnect after any event which occurs as a fatal error from the board.

  String fileMd5 = "";
  var firmwarePath = "".obs;
  RxString lastFirmwareDirectory = "".obs;
  var rwcpStatusText = "未启用".obs;

  var percentage = 0.0.obs;

  Timer? _timer;
  static final bool _enableWriteTraceLog = kDebugMode;
  static const int _dataPacketLogSampleInterval = 50;
  static const int _validationPollLogSampleInterval = 10;

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
  bool _upgradePaused = false;
  bool _deviceRequestedUpgradeDisconnect = false;
  Timer? _upgradeWatchdogTimer;
  Timer? _reconnectTimer;
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
  bool _suppressVersionQueryLog = false;
  Timer? _currentVersionPollTimer;
  bool _pendingStartAfterVersionQuery = false;
  Timer? _postUpgradeVersionRetryTimer;
  int _postUpgradeVersionRetryCount = 0;
  bool _suppressCurrentVersionPolling = false;
  Timer? _scanWatchdogTimer;
  Timer? _recoveryRetryTimer;
  Timer? _abortConfirmTimer;
  DateTime? _abortSentAt;
  String _abortReason = "";
  bool _waitingAbortConfirm = false;
  Worker? _deviceListWorker;
  DateTime? _expectedRebootDisconnectUntil;
  bool _upgradeModeEnabled = false;
  bool _isClosed = false;
  int _validationPollRawNotifyCounter = 0;
  int _validationPollTxCounter = 0;
  int _validationPollRxCounter = 0;

  OtaServer({
    BleConnectionManager? bleManagerOverride,
    UpgradeStateMachine? upgradeStateMachineOverride,
    DefaultFirmwarePathResolver? defaultFirmwarePathResolver,
    DateTime Function()? nowProvider,
  })  : _bleManagerOverride = bleManagerOverride,
        _upgradeStateMachineOverride = upgradeStateMachineOverride,
        _defaultFirmwarePathResolver =
            defaultFirmwarePathResolver ?? _resolveDefaultFirmwarePath,
        _nowProvider = nowProvider ?? DateTime.now;

  static Future<String> _resolveDefaultFirmwarePath() async {
    final filePath = await getApplicationDocumentsDirectory();
    return "${filePath.path}/1.bin";
  }

  static OtaServer get to => Get.find();

  @visibleForTesting
  BleConnectionManager get bleManager => _bleManager;

  @override
  void onInit() {
    super.onInit();
    // 初始化组件
    _logBuffer = LogBuffer(logText: logText);
    final crash = CrashReporter.maybeInstance;
    crash?.setLogSnapshotProvider(
        () => _logBuffer.snapshotText(maxChars: 60000));
    crash?.setContextProvider(() {
      return <String, Object?>{
        "isUpgrading": isUpgrading.value,
        "isDeviceConnected": isDeviceConnected.value,
        "rwcpStatusText": rwcpStatusText.value,
        "recoveryStatusText": recoveryStatusText.value,
        "vendorMode": vendorMode.value,
        "upgradeState": _upgradeStateMachine.state.name,
        "resumePoint": _upgradeStateMachine.resumePoint,
        "transferComplete": transFerComplete,
        "upgradePaused": _upgradePaused,
        "offset": mStartOffset,
        "bytesToSend": mBytesToSend,
        "fileLength": mBytesFile?.length ?? 0,
        "progressQueueSize": mProgressQueue.length,
        "payloadSizeMax": mPayloadSizeMax,
        "maxLengthForDataTransfer": mMaxLengthForDataTransfer,
      };
    });
    _cmdBuilder = GaiaCommandBuilder();
    _bleManager = _bleManagerOverride ??
        BleConnectionManager(
          ble: FlutterReactiveBleClient(FlutterReactiveBle()),
          onLog: addLog,
          onConnectionStateChanged: (state, deviceId) {
            if (state == DeviceConnectionState.connected) {
              connectDeviceId = deviceId;
            }
          },
        );
    devices = _bleManager.devices;
    _upgradeStateMachine =
        _upgradeStateMachineOverride ?? UpgradeStateMachine(delegate: this);
    mRWCPClient = RWCPClient(this);
    _initFirmwarePathOnStartup();
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
      lastFirmwareDirectory.value = _extractDirectoryPath(firmwarePath.value);
    } catch (e) {
      addLog("初始化默认固件路径失败$e");
    }
  }

  void _initFirmwarePathOnStartup() {
    unawaited(() async {
      if (Get.testMode) {
        await _initDefaultFirmwarePath();
        return;
      }

      if (firmwarePath.value.trim().isNotEmpty) {
        return;
      }
      final lastPath = await AppSettings.readLastFirmwarePath();

      if (firmwarePath.value.trim().isNotEmpty) {
        return;
      }

      final trimmedLastPath = lastPath?.trim() ?? '';
      if (trimmedLastPath.isNotEmpty) {
        firmwarePath.value = trimmedLastPath;
        lastFirmwareDirectory.value = _extractDirectoryPath(trimmedLastPath);
        addLog("已恢复上次固件路径$trimmedLastPath");
        return;
      }

      if (firmwarePath.value.trim().isNotEmpty) {
        return;
      }
      await _initDefaultFirmwarePath();
    }());
  }

  void setFirmwarePath(String path) {
    final trimPath = path.trim();
    if (trimPath.isEmpty) {
      addLog("固件路径不能为空");
      return;
    }
    firmwarePath.value = trimPath;
    lastFirmwareDirectory.value = _extractDirectoryPath(trimPath);
    if (!Get.testMode) {
      unawaited(AppSettings.writeLastFirmwarePath(trimPath));
    }
    addLog("已设置固件路径$trimPath");
  }

  String _extractDirectoryPath(String fullPath) {
    final normalizedPath = fullPath.trim().replaceAll("\\", "/");
    if (normalizedPath.isEmpty) {
      return "";
    }
    final splitIndex = normalizedPath.lastIndexOf("/");
    if (splitIndex <= 0) {
      return "";
    }
    return normalizedPath.substring(0, splitIndex);
  }

  String normalizeBluetoothId(String rawId) {
    final trimmed = rawId.trim();
    if (trimmed.isEmpty) {
      return "";
    }
    final upper = trimmed.toUpperCase();
    final compact = upper.replaceAll(RegExp(r'[^0-9A-F]'), '');
    if (compact.length == 12) {
      return compact;
    }
    return upper;
  }

  bool isSystemConnectedScanDevice(DiscoveredDevice device) {
    final normalizedId = normalizeBluetoothId(device.id);
    if (normalizedId.isEmpty) {
      return false;
    }
    return systemConnectedDeviceIds.contains(normalizedId);
  }

  Future<void> refreshSystemConnectedDevices() async {
    if (!Platform.isAndroid) {
      systemConnectedDeviceIds.clear();
      return;
    }
    try {
      final rawDevices = await _systemBluetoothChannel
          .invokeMethod<List<dynamic>>('getConnectedDevices');
      final ids = <String>{};
      for (final item in rawDevices ?? const <dynamic>[]) {
        if (item is Map) {
          final rawId = item['id'];
          if (rawId is String) {
            final normalizedId = normalizeBluetoothId(rawId);
            if (normalizedId.isNotEmpty) {
              ids.add(normalizedId);
            }
          }
          continue;
        }
        if (item is String) {
          final normalizedId = normalizeBluetoothId(item);
          if (normalizedId.isNotEmpty) {
            ids.add(normalizedId);
          }
        }
      }
      systemConnectedDeviceIds
        ..clear()
        ..addAll(ids);
    } catch (e) {
      systemConnectedDeviceIds.clear();
      addLog("读取系统已连接设备失败: $e");
    }
  }

  void consumeUserMessage() {
    userMessage.value = null;
  }

  void _notifyUser(String message) {
    userMessage.value = message;
    addLog(message);
  }

  void _setAutoReconnectEnabled(bool enabled) {
    _bleManager.setAutoReconnectEnabled(enabled);
  }

  Future<void> connectDevice(String id) async {
    try {
      connectDeviceId = id;
      isConnecting.value = true;
      connectingDeviceId.value = id;
      _scanWatchdogTimer?.cancel();
      isScanning.value = false;
      deviceListUiState.value = DeviceListUiState.idle;
      deviceListHint.value = "连接中...";
      await _bleManager.stopScan();
      _setAutoReconnectEnabled(true);
      await _bleManager.connect(
        id,
        onConnected: () async {
          isConnecting.value = false;
          connectingDeviceId.value = "";
          isDeviceConnected.value = true;
          _cancelRecoveryRetryTimer();
          recoveryStatusText.value = "空闲";
          if (!isUpgrading.value) {
            rwcpStatusText.value = "待启用";
          }
          deviceListUiState.value = DeviceListUiState.ready;
          deviceListHint.value = "连接成功";
          addLog("Vendor模式固定为V3，使用${_vendorToHex(_activeVendorId)}");
          await registerNotice();
          await restPayloadSize();
        },
        onDisconnected: () {
          final wasUpgrading = isUpgrading.value;
          isConnecting.value = false;
          connectingDeviceId.value = "";
          isDeviceConnected.value = false;
          _upgradeModeEnabled = false;
          rwcpStatusText.value = "连接断开";
          _notifyUser("设备已断开连接");
          deviceListUiState.value = DeviceListUiState.error;
          deviceListHint.value = "连接断开，请重试";
          if (wasUpgrading && _shouldTreatDisconnectAsReboot()) {
            _clearUpgradeWatchdog();
            // 末期断开通常是设备重启/切换状态；清理 RWCP 会话避免超时重传在断链期继续写导致异常。
            mRWCPClient.reset(true);
            mProgressQueue.clear();
            rwcpStatusText.value = "重启中";
            recoveryStatusText.value = "等待重连";
            addLog("升级末期设备可能重启导致断开，等待自动重连");
            return;
          }
          if (wasUpgrading) {
            _enterFatalUpgradeState("升级过程中蓝牙断链");
          }
        },
        onError: (error) {
          isConnecting.value = false;
          connectingDeviceId.value = "";
          isDeviceConnected.value = false;
          if (_isRecovering || recoveryStatusText.value == "重连中") {
            recoveryStatusText.value = "恢复失败";
            rwcpStatusText.value = "未连接";
          }
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

  bool _shouldTreatDisconnectAsReboot() {
    final until = _expectedRebootDisconnectUntil;
    if (until != null && _nowProvider().isBefore(until)) {
      return true;
    }
    // 末期阶段（TransferComplete/校验/提交/完成）出现断开，通常是设备重启或切换升级态导致。
    return _upgradeStateMachine.transferComplete ||
        _upgradeStateMachine.state == UpgradeState.validating ||
        _upgradeStateMachine.state == UpgradeState.committing ||
        _upgradeStateMachine.state == UpgradeState.complete;
  }

  void _markExpectedRebootDisconnect({int seconds = 20}) {
    _expectedRebootDisconnectUntil =
        _nowProvider().add(Duration(seconds: seconds));
  }

  void writeMsg(List<int> data) {
    if (_isClosed) {
      return;
    }
    // 仅做“触发写入”而不在这里 await，避免多次调用时形成隐式并发写入。
    // 实际写入会在 BleConnectionManager 内部做串行化。
    _touchUpgradeWatchdog();
    unawaited(writeData(data));
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
    final channelReady = await _bleManager.registerRwcpChannel((data) {
      _logRwcpRxPacket(data);
      mRWCPClient.onReceiveRWCPSegment(data);
    });
    // 当前连接设备 ID 与底层连接状态以 BleConnectionManager 为单一真相，
    // OtaServer 只维护面向 UI 的响应式镜像，避免多处写入造成竞态。
    if (!channelReady || !isDeviceConnected.value) {
      rwcpStatusText.value = "服务未就绪";
      _rwcpSetupInProgress = false;
      return;
    }
    addLog(
        "isUpgrading${isUpgrading.value} transFerComplete $transFerComplete");
    if (isUpgrading.value) {
      rwcpStatusText.value = "已启用";
      _rwcpSetupInProgress = false;
      if (!_upgradeModeEnabled) {
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
    final channelReady = await _bleManager.registerNotifyChannel((data) {
      handleRecMsg(data);
    });
    if (!channelReady) {
      addLog("通知通道未就绪，跳过通知注册命令");
      if (isUpgrading.value) {
        _enterFatalUpgradeState("通知通道未就绪");
      }
      return;
    }
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
    // 每次开始升级都把 RWCP client 重置到 listen，保证会话从 RST->SYN 重新建链。
    mRWCPClient.reset(true);
    mRWCPClient.setCloseSessionWhenIdle(true);
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
    _setAutoReconnectEnabled(true);
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
    // 高频通知在“校验轮询阶段”会刷屏，且内容与后续结构化日志重复。
    // 这里保留“收到通知>”关键字，但在 validating 阶段做采样输出，避免影响阅读。
    if (_shouldSampleValidationPollLog()) {
      _validationPollRawNotifyCounter += 1;
      if (_validationPollRawNotifyCounter <= 3 ||
          _validationPollRawNotifyCounter % _validationPollLogSampleInterval ==
              0) {
        addLog("收到通知>${StringUtils.byteToHexString(data)}");
      }
    } else {
      _validationPollRawNotifyCounter = 0;
      _validationPollTxCounter = 0;
      _validationPollRxCounter = 0;
      addLog("收到通知>${StringUtils.byteToHexString(data)}");
    }
    final packet = GaiaPacketBLE.fromByte(data);
    if (packet == null) {
      // 保持历史日志关键字，避免测试/外部解析依赖被破坏。
      addLog("数据包解析失败");
      addLog(
          "RX[GAIA] 解析失败 len=${data.length} bytes=${StringUtils.byteToHexString(data)}");
      return;
    }
    _logGaiaRxPacket(packet);
    if (packet.mVendorId != 0x001D) {
      addLog("忽略非V3 Vendor包: ${_vendorToHex(packet.mVendorId)}");
      return;
    }
    _handleV3Packet(packet);
  }

  void _logGaiaRxPacket(GaiaPacketBLE packet) {
    final cmd = packet.getCommandId();
    final feature = _v3CommandFeature(cmd);
    final packetType = _v3CommandType(cmd);
    final commandId = _v3CommandId(cmd);
    final payload = packet.mPayload ?? [];
    final cmdText =
        _cmdBuilder.gaiaCommandText(cmd, vendorId: packet.mVendorId);
    final base =
        "RX[GAIA] $cmdText(cmd=0x${cmd.toRadixString(16).padLeft(4, '0').toUpperCase()} "
        "feature=0x${feature.toRadixString(16).padLeft(2, '0').toUpperCase()} "
        "type=$packetType id=0x${commandId.toRadixString(16).padLeft(2, '0').toUpperCase()})";

    if (cmd == _upgradeControlCommand()) {
      final vmu = VMUPacket.getPackageFromByte(payload);
      if (vmu != null) {
        final vmuData = vmu.mData ?? [];
        if (_shouldSampleValidationPollLog() &&
            vmu.mOpCode == OpCodes.upgradeIsValidationDoneCfm) {
          // 校验轮询：只保留关键信息，避免每次都输出完整 hex。
          _validationPollRxCounter += 1;
          if (!(_validationPollRxCounter <= 3 ||
              _validationPollRxCounter % _validationPollLogSampleInterval ==
                  0)) {
            return;
          }
          addLog(
              "$base VMU=${_vmuOpText(vmu.mOpCode)}(0x${vmu.mOpCode.toRadixString(16).padLeft(2, '0').toUpperCase()}) "
              "len=${vmuData.length}");
          return;
        }
        addLog(
            "$base VMU=${_vmuOpText(vmu.mOpCode)}(0x${vmu.mOpCode.toRadixString(16).padLeft(2, '0').toUpperCase()}) "
            "len=${vmuData.length} bytes=${StringUtils.byteToHexString(packet.mBytes ?? [])}");
        return;
      }
    }

    addLog(
        "$base len=${payload.length} bytes=${StringUtils.byteToHexString(packet.mBytes ?? [])}");
  }

  void _logRwcpRxPacket(List<int> bytes) {
    if (bytes.isEmpty) {
      addLog("RX[RWCP] empty");
      return;
    }
    final segment = Segment.parse(bytes);
    final opCode = segment.getOperationCode();
    final seq = segment.getSequenceNumber();
    final opText = _rwcpServerOpText(opCode);
    final payload = segment.getPayload();
    if (payload.isEmpty) {
      addLog(
          "RX[RWCP] op=$opText seq=$seq bytes=${StringUtils.byteToHexString(bytes)}");
      return;
    }
    final gaia = GaiaPacketBLE.fromByte(payload);
    if (gaia == null) {
      addLog(
          "RX[RWCP] op=$opText seq=$seq payloadLen=${payload.length} bytes=${StringUtils.byteToHexString(bytes)}");
      return;
    }
    final cmd = gaia.getCommandId();
    final cmdText = _cmdBuilder.gaiaCommandText(cmd, vendorId: gaia.mVendorId);
    addLog(
        "RX[RWCP] op=$opText seq=$seq gaia=$cmdText bytes=${StringUtils.byteToHexString(bytes)}");
  }

  String _rwcpServerOpText(int opCode) {
    switch (opCode) {
      case RWCPOpCodeServer.dataAck:
        return "DATA_ACK";
      case RWCPOpCodeServer.synAck:
        return "SYN_ACK";
      case RWCPOpCodeServer.rstAck:
        return "RST_ACK";
      case RWCPOpCodeServer.gap:
        return "GAP";
      default:
        return "UNKNOWN";
    }
  }

  void _handleV3Packet(GaiaPacketBLE packet) {
    final cmd = packet.getCommandId();
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
          _upgradeModeEnabled = true;
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
        _upgradeModeEnabled = false;
        // 对齐 gaia-client-src: 设备通过 STOP_REQUEST(action=DISCONNECT_UPGRADE) 要求断开升级通道时，
        // Host 仅需断开 GAIA transport 与 upgrade library 的绑定，不应结束升级进程。
        if (isUpgrading.value && _deviceRequestedUpgradeDisconnect) {
          addLog("UpgradeDisconnect响应：设备请求断开升级通道，保持升级状态等待恢复");
          return;
        }
        // 升级完成后我们也会主动发 UpgradeDisconnect 以关闭升级通道：
        // 此时 isUpgrading 已为 false，但仍需要继续执行“升级后版本查询”定时器，
        // 因此不要在这里调用 stopUpgrade（其会取消 _postUpgradeVersionRetryTimer）。
        if (!isUpgrading.value) {
          addLog("UpgradeDisconnect响应：已退出升级模式");
          return;
        }
        stopUpgrade(sendAbort: false, sendDisconnect: false);
        return;
      }
      // 兜底：未知/未处理的 V3 Response 不能静默忽略，否则升级现场容易“卡住但无日志”。
      addLog(
          "未处理V3响应 feature=$feature cmdId=$commandId payloadLen=${payload.length}");
      if (isUpgrading.value) {
        _reportDeviceError(
          "未处理V3响应 feature=$feature cmdId=$commandId",
          triggerRecovery: false,
        );
      }
      return;
    }

    if (packetType == GaiaCommandBuilder.v3PacketTypeNotification) {
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3NtfUpgradeDataIndication) {
        receiveVMUPacket(payload);
        return;
      }
      // GAIA STOP/START notifications (feature=0x06, type=Notification, id=0x01/0x02).
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3NtfUpgradeStopRequest) {
        final action = payload.isNotEmpty ? payload.first : 0x01;
        _upgradePaused = true;
        _clearUpgradeWatchdog();
        if (action == 0x00) {
          _deviceRequestedUpgradeDisconnect = true;
          addLog("收到GAIA STOP通知(action=0x00)，断开升级通道");
          disconnectUpgrade();
        } else if (action == 0x01) {
          _deviceRequestedUpgradeDisconnect = false;
          addLog("收到GAIA STOP通知(action=0x01)，暂停发送升级数据");
        } else {
          _deviceRequestedUpgradeDisconnect = false;
          addLog(
              "收到GAIA STOP通知(action=0x${action.toRadixString(16).padLeft(2, '0')})，按暂停处理");
        }
        return;
      }
      if (feature == GaiaCommandBuilder.v3FeatureUpgrade &&
          commandId == GaiaCommandBuilder.v3NtfUpgradeStartRequest) {
        final action = payload.isNotEmpty ? payload.first : 0x01;
        if (action == 0x00) {
          _upgradePaused = false;
          _deviceRequestedUpgradeDisconnect = false;
          _armUpgradeWatchdog();
          addLog("收到GAIA START通知(action=0x00)，重新连接升级通道");
          sendUpgradeConnect();
          return;
        }
        if (action == 0x01) {
          _upgradePaused = false;
          _deviceRequestedUpgradeDisconnect = false;
          _armUpgradeWatchdog();
          addLog("收到GAIA START通知(action=0x01)，恢复发送升级数据");
          _resumeUpgradeDataIfPossible();
          return;
        }
        _upgradePaused = false;
        _deviceRequestedUpgradeDisconnect = false;
        _armUpgradeWatchdog();
        addLog(
            "收到GAIA START通知(action=0x${action.toRadixString(16).padLeft(2, '0')})，按恢复处理");
        _resumeUpgradeDataIfPossible();
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
    _upgradePaused = false;
    _deviceRequestedUpgradeDisconnect = false;
  }

  Future<void> stopUpgrade(
      {bool sendAbort = true, bool sendDisconnect = true}) async {
    _clearUpgradeWatchdog();
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _pendingStartAfterVersionQuery = false;
    _suppressCurrentVersionPolling = false;
    _rwcpSetupInProgress = false;
    rwcpStatusText.value = "待启用";
    timeCount.value = 0;
    if (sendAbort) {
      abortUpgrade();
    }
    // 停止升级时也重置 RWCP，避免后台残留会话继续触发回调。
    mRWCPClient.reset(true);
    mRWCPClient.setCloseSessionWhenIdle(true);
    resetUpload();
    writeRTCPCount = 0;
    updatePer.value = 0;
    isUpgrading.value = false;
    _dfuWriteInFlight = false;
    _dfuPendingChunkSize = 0;
    // 连接态以 BleConnectionManager 为主；但在少数状态切换窗口（尤其测试桩）下，
    // 可能出现“已连接标志为 true，connectedDeviceId 尚未同步”的短暂不一致。
    // 这里允许退化到 connectDeviceId，避免漏发 UpgradeDisconnect。
    final hasKnownDeviceId =
        currentConnectedDeviceId.isNotEmpty || connectDeviceId.isNotEmpty;
    if (sendDisconnect && isDeviceConnected.value && hasKnownDeviceId) {
      await Future.delayed(const Duration(milliseconds: 500));
      sendUpgradeDisconnect();
    }
  }

  Future<bool> loadFirmwareFile() async {
    try {
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
    } catch (e) {
      addLog("读取升级文件失败: error=$e");
      mBytesFile = null;
      fileMd5 = "";
      return false;
    }
  }

  List<int>? _getMd5TailBytes() {
    final md5Text = fileMd5.trim();
    if (md5Text.length < 8) {
      return null;
    }
    final tailHex = md5Text.substring(md5Text.length - 8);
    final bytes = StringUtils.hexStringToBytes(tailHex);
    if (bytes.length != 4) {
      return null;
    }
    return bytes;
  }

  Future<void> sendSyncReq() async {
    //A2305C3A9059C15171BD33F3BB08ADE4 MD5
    //000A0642130004BB08ADE4
    final loaded = await loadFirmwareFile();
    if (!loaded) {
      await stopUpgrade();
      return;
    }
    final endMd5 = _getMd5TailBytes();
    if (endMd5 == null) {
      addLog("固件MD5异常，无法构建SYNC_REQ: md5=$fileMd5");
      await stopUpgrade();
      return;
    }
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
    final digest = _getMd5TailBytes();
    if (digest == null) {
      addLog("DFU_BEGIN失败：固件MD5异常 md5=$fileMd5");
      stopUpgrade(sendAbort: false);
      return;
    }
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
    bool suppressLog = false,
  }) {
    if (_isVersionQueryInFlight) {
      if (!suppressLog) {
        addLog("版本查询进行中，忽略重复请求");
      }
      return;
    }
    if (!isDeviceConnected.value) {
      if (!suppressLog) {
        addLog("$tag版本查询失败：设备未连接");
      }
      onFailed();
      return;
    }
    _currentVersionQueryTag = tag;
    _onVersionQuerySuccess = onSuccess;
    _onVersionQueryFailed = onFailed;
    _isVersionQueryInFlight = true;
    _suppressVersionQueryLog = suppressLog;
    _versionQueryTimer?.cancel();
    if (!suppressLog) {
      addLog("发送GET_APPLICATION_VERSION($tag)");
    }
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
    final suppressLog = _suppressVersionQueryLog;
    _isVersionQueryInFlight = false;
    _currentVersionQueryTag = "";
    _suppressVersionQueryLog = false;
    if (!suppressLog) {
      addLog("$tag版本号: $version");
    }
    final successCallback = _onVersionQuerySuccess;
    _onVersionQuerySuccess = null;
    _onVersionQueryFailed = null;
    successCallback?.call(version);
  }

  void _finishVersionQueryFailed(String reason) {
    _versionQueryTimer?.cancel();
    final failedCallback = _onVersionQueryFailed;
    final suppressLog = _suppressVersionQueryLog;
    _isVersionQueryInFlight = false;
    _currentVersionQueryTag = "";
    _onVersionQuerySuccess = null;
    _onVersionQueryFailed = null;
    _suppressVersionQueryLog = false;
    if (!suppressLog) {
      addLog(reason);
    }
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
    _suppressCurrentVersionPolling = true;
    _postUpgradeVersionRetryTimer = Timer.periodic(
        Duration(seconds: kPostUpgradeVersionRetryIntervalSeconds), (timer) {
      if (_postUpgradeVersionRetryCount >= kPostUpgradeVersionMaxRetries) {
        timer.cancel();
        _postUpgradeVersionRetryTimer = null;
        _suppressCurrentVersionPolling = false;
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
          if (currentVersion.value != version) {
            currentVersion.value = version;
          }
          timer.cancel();
          _postUpgradeVersionRetryTimer = null;
          _suppressCurrentVersionPolling = false;
          _logVersionCompare();
        },
        onFailed: () {
          if (_postUpgradeVersionRetryCount >= kPostUpgradeVersionMaxRetries) {
            timer.cancel();
            _postUpgradeVersionRetryTimer = null;
            _suppressCurrentVersionPolling = false;
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

  void startCurrentVersionPolling(
      {Duration interval = const Duration(seconds: 1)}) {
    if (Get.testMode) {
      return;
    }
    _currentVersionPollTimer?.cancel();
    _pollCurrentVersionOnce();
    _currentVersionPollTimer = Timer.periodic(interval, (_) {
      _pollCurrentVersionOnce();
    });
  }

  void stopCurrentVersionPolling() {
    _currentVersionPollTimer?.cancel();
    _currentVersionPollTimer = null;
  }

  void _pollCurrentVersionOnce() {
    if (_isClosed) {
      stopCurrentVersionPolling();
      return;
    }
    if (!isDeviceConnected.value) {
      if (currentVersion.value != "UNKNOWN") {
        currentVersion.value = "UNKNOWN";
      }
      return;
    }
    if (_suppressCurrentVersionPolling) {
      return;
    }
    if (isUpgrading.value || _isVersionQueryInFlight) {
      return;
    }
    queryApplicationVersion(
      tag: "当前",
      suppressLog: true,
      onSuccess: (version) {
        if (currentVersion.value != version) {
          currentVersion.value = version;
        }
      },
      onFailed: () {},
    );
  }

  /// <p>To send a VMUPacket over the defined protocol communication.</p>
  ///
  /// @param bytes
  ///              The packet to send.
  /// @param isTransferringData
  ///              True if the packet is about transferring the file data, false for any other packet.
  void sendVMUPacket(VMUPacket packet, bool isTransferringData) {
    List<int> bytes = packet.getBytes();
    final gaiaPacket =
        _buildGaiaPacket(_upgradeControlCommand(), payload: bytes);
    List<int> gaiaBytes;
    try {
      gaiaBytes = gaiaPacket.getBytes();
    } catch (e) {
      addLog("Exception when attempting to create GAIA packet: $e");
      return;
    }

    // 对齐手册(80-CH482-1 Rev.AH, 3.6.5)：Data Endpoint Mode 用于“发送升级数据”。
    // 因此仅将固件数据包（upgradeData）走 RWCP；其余控制/确认类消息仍走常规写通道，
    // 以降低 RWCP 抖动与末期阶段误断链风险。
    if (isTransferringData && _shouldSendUpgradeControlOverRwcp()) {
      if (mTransferStartTime <= 0) {
        mTransferStartTime = DateTime.now().millisecondsSinceEpoch;
      }
      try {
        final success = mRWCPClient.sendData(gaiaBytes);
        if (!success) {
          addLog(
              "Fail to send GAIA packet for GAIA command: ${gaiaPacket.getCommandId()}");
        }
      } catch (e) {
        addLog("Exception when attempting to send GAIA packet: $e");
      }
      return;
    }

    // RWCP 未就绪时（Stage A），按文档写 Command Char（纯 GAIA PDU）。
    writeMsg(gaiaBytes);
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
      if (packet.mOpCode == OpCodes.upgradeAbortCfm) {
        _markAbortConfirmed();
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

  void _handleDataBytesRequest(int bytesToSend, int moveBy) {
    mBytesToSend = bytesToSend;
    final fileLength = mBytesFile?.length ?? 0;

    // 对齐 gaia-client-src/lib-upgrade DataReader#set(move, requested):
    // - moveBy 为有符号 int32（Java int 会溢出为负数），且仅在 move>0 时前移 offset。
    // - requested bytes 若越界则截断为 remaining length。
    if (moveBy > 0 && (mStartOffset + moveBy) < fileLength) {
      mStartOffset += moveBy;
    }
    addLog(
        "本次发包: moveBy=$moveBy offset=$mStartOffset bytesToSend=$mBytesToSend");

    mBytesToSend = (mBytesToSend > 0) ? mBytesToSend : 0;
    final remainingLength = fileLength - mStartOffset;
    mBytesToSend =
        (mBytesToSend < remainingLength) ? mBytesToSend : remainingLength;
    if (mIsRWCPEnabled.value) {
      _pumpRwcpData(force: true);
      return;
    }
    if (!_upgradePaused) {
      sendNextDataPacket();
    }
  }

  void abortUpgrade() {
    if (mRWCPClient.isRunningASession()) {
      mRWCPClient.cancelTransfer();
    }
    mProgressQueue.clear();
    sendAbortReq(reason: "停止升级");
    isUpgrading.value = false;
  }

  void _startAbortConfirmWatch(String reason) {
    _abortConfirmTimer?.cancel();
    _abortSentAt = _nowProvider();
    _abortReason = reason;
    _waitingAbortConfirm = true;
    addLog("已发送Abort，等待设备确认($reason)");
    _abortConfirmTimer =
        Timer(Duration(seconds: kAbortConfirmTimeoutSeconds), () {
      if (!_waitingAbortConfirm) {
        return;
      }
      _waitingAbortConfirm = false;
      addLog("Abort确认超时(${kAbortConfirmTimeoutSeconds}s): $reason");
    });
  }

  void _markAbortConfirmed() {
    if (!_waitingAbortConfirm) {
      return;
    }
    _waitingAbortConfirm = false;
    _abortConfirmTimer?.cancel();
    _abortConfirmTimer = null;
    final sentAt = _abortSentAt;
    final reason = _abortReason;
    _abortSentAt = null;
    _abortReason = "";
    if (sentAt == null) {
      addLog("收到Abort确认");
      return;
    }
    final elapsedMs = _nowProvider().difference(sentAt).inMilliseconds;
    addLog("收到Abort确认，耗时${elapsedMs}ms($reason)");
  }

  void _clearAbortConfirmWatch() {
    _abortConfirmTimer?.cancel();
    _abortConfirmTimer = null;
    _abortSentAt = null;
    _abortReason = "";
    _waitingAbortConfirm = false;
  }

  void sendAbortReq({String reason = "通用"}) {
    VMUPacket packet = VMUPacket.get(OpCodes.upgradeAbortReq);
    _startAbortConfirmWatch(reason);
    sendVMUPacket(packet, false);
  }

  //主要发包逻辑
  void sendNextDataPacket() {
    if (!isUpgrading.value) {
      stopUpgrade();
      return;
    }
    if (_upgradePaused) {
      return;
    }
    final bytes = mBytesFile;
    if (bytes == null || bytes.isEmpty) {
      _enterFatalUpgradeState("固件数据为空，无法继续发包");
      return;
    }
    if (mStartOffset < 0 || mStartOffset > bytes.length) {
      _enterFatalUpgradeState("发包offset异常: $mStartOffset/${bytes.length}");
      return;
    }
    // inform listeners about evolution
    onFileUploadProgress();
    int bytesToSend = mBytesToSend < mMaxLengthForDataTransfer - 1
        ? mBytesToSend
        : mMaxLengthForDataTransfer - 1;
    // to know if we are sending the last data packet.
    final available = bytes.length - mStartOffset;
    if (bytesToSend > available) {
      bytesToSend = available;
    }
    if (bytesToSend <= 0) {
      return;
    }
    bool lastPacket = available <= bytesToSend;
    if (lastPacket) {
      addLog(
          "mMaxLengthForDataTransfer$mMaxLengthForDataTransfer bytesToSend$bytesToSend lastPacket$lastPacket");
    }
    final end = mStartOffset + bytesToSend;
    final dataToSend = bytes.sublist(mStartOffset, end);

    if (lastPacket) {
      _upgradeStateMachine.setWasLastPacket(true);
      mBytesToSend = 0;
    } else {
      _upgradeStateMachine.setWasLastPacket(false);
      mStartOffset = end;
      mBytesToSend = mBytesToSend - bytesToSend;
    }

    sendData(lastPacket, dataToSend);
  }

  void _pumpRwcpData({bool force = false}) {
    if (!mIsRWCPEnabled.value || _upgradePaused) {
      return;
    }
    if (!isUpgrading.value) {
      return;
    }
    if (!force &&
        _upgradeStateMachine.resumePoint != ResumePoints.dataTransfer) {
      return;
    }
    if (mBytesToSend <= 0) {
      return;
    }

    // RWCP 节流：
    // - GAP/超时重传时，继续预灌会扩大 pending/unacked 堆积，使“卡在 GAP 重传”的现象更难恢复
    // - 同时避免 OtaServer 过度前移 offset，减少内存与队列压力
    if (mRWCPClient.isResendingSegments) {
      return;
    }

    final buffered =
        mRWCPClient.pendingDataLength + mRWCPClient.unacknowledgedLength;
    // 经验值：最多缓存 2 个窗口的数据（pending+unacked），上限不超过 64（序号空间）。
    final maxBuffered = ((mRWCPClient.window * 2).clamp(2, 64)).toInt();
    if (buffered >= maxBuffered) {
      return;
    }
    final budget = maxBuffered - buffered;

    var pumped = 0;
    final pumpLimit =
        (budget < _kRwcpPumpMaxPacketsPerTick) ? budget : _kRwcpPumpMaxPacketsPerTick;
    while (pumped < pumpLimit &&
        mBytesToSend > 0 &&
        !_upgradePaused &&
        isUpgrading.value) {
      sendNextDataPacket();
      pumped += 1;
    }
  }

  void _resumeUpgradeDataIfPossible() {
    if (_upgradePaused || !isUpgrading.value) {
      return;
    }
    if (_upgradeStateMachine.resumePoint != ResumePoints.dataTransfer) {
      return;
    }
    if (mBytesToSend <= 0) {
      return;
    }

    if (mIsRWCPEnabled.value) {
      _pumpRwcpData();
      return;
    }
    sendNextDataPacket();
  }

  bool _shouldSendUpgradeControlOverRwcp() {
    return mIsRWCPEnabled.value &&
        isDeviceConnected.value &&
        rwcpStatusText.value == "已启用";
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
    if (mIsRWCPEnabled.value) {
      _pumpRwcpData();
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
          _markExpectedRebootDisconnect();
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
      default:
        addLog("askForConfirmation 未知类型: $type，忽略");
        return;
    }
    if (code < 0) {
      addLog("askForConfirmation 无效确认码: type=$type code=$code，忽略");
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
    if (acknowledged <= 0 || !mIsRWCPEnabled.value) {
      return;
    }

    // RWCP 的 ACK 可能在窗口/重传/会话切换时出现“没有对应进度占位”的情况。
    // 若此时把默认值 0 写回 UI，会造成进度条从 100% 回跳到 0% 的假象。
    if (mProgressQueue.isEmpty) {
      return;
    }

    var percentage = updatePer.value;
    var consumed = false;
    while (acknowledged > 0 && mProgressQueue.isNotEmpty) {
      percentage = mProgressQueue.removeFirst();
      acknowledged--;
      consumed = true;
    }
    if (consumed) {
      updatePer.value = percentage;
    }
    _pumpRwcpData();
  }

  @override
  bool sendRWCPSegment(List<int> bytes) {
    if (_isClosed) {
      return false;
    }

    // 兼容测试与上层调用语义：
    // - 测试覆盖会在非升级状态下直接调用 sendRWCPSegment 并期望返回 true。
    // - 真正升级过程中，若处于暂停/断链/RWCP未启用，则返回 false 以触发 RWCPClient 的退避重试。
    if (isUpgrading.value) {
      if (_upgradePaused || !isDeviceConnected.value || !mIsRWCPEnabled.value) {
        return false;
      }
    }

    // 这里返回 true 的语义是“已成功提交给 BLE 写入链路”（不是“对端已收到”）。
    // 底层异常会在 writeMsgRWCP 内部进入 fatal state/断链恢复流程。
    unawaited(writeMsgRWCP(bytes));
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
    upgradeSuccessCounter.value += 1;
    // 升级结束后清空 RWCP 会话状态，避免后续流程复用旧 session。
    mRWCPClient.reset(true);
    mRWCPClient.setCloseSessionWhenIdle(true);
    _schedulePostUpgradeVersionQuery();
    disconnectUpgrade();
  }

  @override
  void onUpgradeError(String reason) {
    _enterFatalUpgradeState(reason);
  }

  @override
  void onRequestNextDataPacket(int bytesToSend, int moveBy) {
    _handleDataBytesRequest(bytesToSend, moveBy);
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
    // 避免断开后 UpgradeStateMachine 的轮询 Timer 继续触发发包。
    _upgradeStateMachine.dispose();
    _reconnectTimer?.cancel();
    _scanWatchdogTimer?.cancel();
    _cancelRecoveryRetryTimer();
    _clearAbortConfirmWatch();
    stopCurrentVersionPolling();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _isVersionQueryInFlight = false;
    _currentVersionQueryTag = "";
    _onVersionQuerySuccess = null;
    _onVersionQueryFailed = null;
    _suppressVersionQueryLog = false;
    _pendingStartAfterVersionQuery = false;
    _suppressCurrentVersionPolling = false;
    _bleManager.disconnect();
    isDeviceConnected.value = false;
    isConnecting.value = false;
    connectingDeviceId.value = "";
  }

  Future<void> restPayloadSize() async {
    int mtu = 23;
    try {
      mtu = await _bleManager.requestMtu(256);
    } catch (e) {
      addLog("请求MTU失败，使用默认MTU=23: $e");
    }
    final rwcpEnabled = mIsRWCPEnabled.value;
    // 非 RWCP 模式下保持历史行为：按 23 计算，避免设备未协商 MTU 时超限。
    final effectiveMtu = rwcpEnabled ? mtu : 23;
    final maxAttrLen = effectiveMtu - 3;

    // 纯 GAIA PDU（vendor+cmd 4字节开销）可用 payload 上限。
    mPayloadSizeMax = maxAttrLen - 4;

    // UPGRADE_DATA 的 GAIA PDU 固定开销：
    // vendor+cmd(4) + opcode(1) + len(2) + is_end(1) = 8 bytes
    // RWCP 额外 header(1) 会占用 attribute 空间。
    final maxGaiaPduLenForUpgrade = maxAttrLen - (rwcpEnabled ? 1 : 0);
    final maxUpgradeDataLen = maxGaiaPduLenForUpgrade - 8; // 即 MTU - 12
    final clampedUpgradeDataLen = maxUpgradeDataLen < 0 ? 0 : maxUpgradeDataLen;
    // mMaxLengthForDataTransfer 表示 VMU data 长度上限（含 is_end 1字节）。
    mMaxLengthForDataTransfer = clampedUpgradeDataLen + 1;
    if (mMaxLengthForDataTransfer < 2) {
      mMaxLengthForDataTransfer = 2;
    }

    addLog("协商mtu $effectiveMtu mPayloadSizeMax $mPayloadSizeMax "
        "mMaxLengthForDataTransfer $mMaxLengthForDataTransfer");
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
    final cmd = gaia.getCommandId();
    final cmdText = _cmdBuilder.gaiaCommandText(cmd, vendorId: gaia.mVendorId);
    final payload = gaia.mPayload ?? [];
    final base =
        "TX[$channel] $cmdText(0x${cmd.toRadixString(16).padLeft(4, '0').toUpperCase()})";
    if (cmd == _upgradeControlCommand()) {
      final vmu = VMUPacket.getPackageFromByte(payload);
      if (vmu != null) {
        final vmuData = vmu.mData ?? [];
        final isUpgradeData = vmu.mOpCode == OpCodes.upgradeData;
        final isLastDataPacket = vmuData.isNotEmpty && vmuData.first == 0x01;
        if (_shouldSampleValidationPollLog() &&
            vmu.mOpCode == OpCodes.upgradeIsValidationDoneReq) {
          // 校验轮询请求：只做采样，且不输出 bytes，避免刷屏。
          _validationPollTxCounter += 1;
          if (!(_validationPollTxCounter <= 3 ||
              _validationPollTxCounter % _validationPollLogSampleInterval ==
                  0)) {
            return;
          }
          addLog(
              "$base VMU=${_vmuOpText(vmu.mOpCode)}(0x${vmu.mOpCode.toRadixString(16).padLeft(2, '0').toUpperCase()})");
          return;
        }
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
    final cmd = gaia.getCommandId();
    final gaiaName = _cmdBuilder.gaiaCommandText(cmd, vendorId: gaia.mVendorId);
    final gaiaPayload = gaia.mPayload ?? [];
    if (cmd == _upgradeControlCommand()) {
      final vmu = VMUPacket.getPackageFromByte(gaiaPayload);
      if (vmu != null) {
        final vmuData = vmu.mData ?? [];
        final isUpgradeData = vmu.mOpCode == OpCodes.upgradeData;
        final isLastDataPacket = vmuData.isNotEmpty && vmuData.first == 0x01;
        if (_shouldSampleValidationPollLog() &&
            vmu.mOpCode == OpCodes.upgradeIsValidationDoneReq) {
          _validationPollTxCounter += 1;
          if (!(_validationPollTxCounter <= 3 ||
              _validationPollTxCounter % _validationPollLogSampleInterval ==
                  0)) {
            return;
          }
          addLog("TX[RWCP] op=$opText seq=$seq gaia=$gaiaName "
              "vmu=${_vmuOpText(vmu.mOpCode)}(0x${vmu.mOpCode.toRadixString(16).padLeft(2, '0').toUpperCase()})");
          return;
        }
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

  bool _shouldSampleValidationPollLog() {
    // 只在“固件数据已传完后的校验轮询阶段”做日志压缩。
    // 这段期间：设备会以 0x17 / WAITING_TIME 驱动 host 继续轮询，日志极高频且重复度高。
    return isUpgrading.value &&
        _upgradeStateMachine.state == UpgradeState.validating;
  }

  String _vmuOpText(int opCode) {
    switch (opCode) {
      case OpCodes.upgradeStartReq:
        return "upgradeStartReq";
      case OpCodes.upgradeData:
        return "upgradeData";
      case OpCodes.upgradeAbortReq:
        return "upgradeAbortReq";
      case OpCodes.upgradeAbortCfm:
        return "upgradeAbortCfm";
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
      case OpCodes.upgradeSilentCommitSupportedReq:
        return "upgradeSilentCommitSupportedReq";
      case OpCodes.upgradeSilentCommitSupportedCfm:
        return "upgradeSilentCommitSupportedCfm";
      case OpCodes.upgradeSilentCommitCfm:
        return "upgradeSilentCommitCfm";
      case OpCodes.upgradePutEarbudsInCaseReq:
        return "upgradePutEarbudsInCaseReq";
      case OpCodes.upgradeEarbudsInCaseCfm:
        return "upgradeEarbudsInCaseCfm";
      case OpCodes.upgradeCompleteIndWithStatus:
        return "upgradeCompleteIndWithStatus";
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
    if (!isUpgrading.value || _upgradePaused) {
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
    _setAutoReconnectEnabled(false);
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
    final now = _nowProvider();
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

  void _cancelRecoveryRetryTimer() {
    _recoveryRetryTimer?.cancel();
    _recoveryRetryTimer = null;
  }

  void _scheduleRecoveryRetryCheck(String reason) {
    _cancelRecoveryRetryTimer();
    _recoveryRetryTimer =
        Timer(Duration(seconds: kRecoveryReconnectCheckSeconds), () {
      _recoveryRetryTimer = null;
      if (_isRecovering || isDeviceConnected.value || connectDeviceId.isEmpty) {
        return;
      }
      if (_recoveryAttempts >= kMaxRecoveryAttemptsPerWindow) {
        recoveryStatusText.value = "恢复受限";
        addLog('$kRecoveryWindowMinutes分钟内恢复次数过多，暂停自动恢复');
        return;
      }
      addLog("重连仍未恢复，继续执行快速恢复");
      unawaited(_quickRecoverFromDeviceError(
        "重连校验失败: $reason",
        forceAbort: true,
      ));
    });
  }

  Future<void> _quickRecoverFromDeviceError(String reason,
      {bool forceAbort = false}) async {
    if (_isRecovering) {
      addLog("恢复进行中，忽略重复触发");
      return;
    }
    final now = _nowProvider();
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
    _cancelRecoveryRetryTimer();
    // 设备已连接或升级进行中时，首次恢复也强制发送 Abort 清设备异常态
    final shouldSendAbort = forceAbort ||
        isUpgrading.value ||
        isDeviceConnected.value ||
        _recoveryAttempts >= 2;
    addLog(
        "执行快速恢复($_recoveryAttempts/$kMaxRecoveryAttemptsPerWindow): $reason${shouldSendAbort ? ' [含Abort]' : ''}");
    try {
      if (isUpgrading.value) {
        await stopUpgrade(sendAbort: shouldSendAbort);
      } else if (shouldSendAbort && isDeviceConnected.value) {
        // 非升级状态但需要强制重置，直接发送 Abort
        sendAbortReq(reason: "快速恢复");
        await Future.delayed(const Duration(milliseconds: 300));
      }
      _bleManager.disconnect();
      isDeviceConnected.value = false;
      if (connectDeviceId.isNotEmpty) {
        recoveryStatusText.value = "重连中";
        rwcpStatusText.value = "重连中";
        await Future.delayed(Duration(seconds: kRecoveryDelaySeconds));
        await connectDevice(connectDeviceId);
        _scheduleRecoveryRetryCheck(reason);
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
    _isClosed = true;
    // 避免 onClose 后仍有异步写入触发看门狗/升级状态变更（单测与真实场景均可能出现）。
    isUpgrading.value = false;
    _upgradeStateMachine.dispose();
    _deviceListWorker?.dispose();
    _logBuffer.dispose();
    _bleManager.dispose();
    mRWCPClient.dispose();
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    _upgradeWatchdogTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _currentVersionPollTimer?.cancel();
    _reconnectTimer?.cancel();
    _scanWatchdogTimer?.cancel();
    _clearAbortConfirmWatch();
    _recoveryRetryTimer?.cancel();
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
        unawaited(refreshSystemConnectedDevices());
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
