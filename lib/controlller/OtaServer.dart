import 'dart:async';
import 'dart:collection';
import 'dart:io';

import 'package:flutter/material.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:get/get.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

import 'package:gaia/TestOtaView.dart';
import 'package:gaia/utils/StringUtils.dart';
import 'package:gaia/utils/gaia/ConfirmationType.dart';
import 'package:gaia/utils/gaia/GAIA.dart';
import 'package:gaia/utils/gaia/GaiaPacketBLE.dart';
import 'package:gaia/utils/gaia/OpCodes.dart';
import 'package:gaia/utils/gaia/ResumePoints.dart';
import 'package:gaia/utils/gaia/UpgradeStartCFMStatus.dart';
import 'package:gaia/utils/gaia/VMUPacket.dart';
import 'package:gaia/utils/gaia/rwcp/RWCPClient.dart';
import 'package:gaia/utils/gaia/rwcp/RWCPListener.dart';

class OtaServer extends GetxService implements RWCPListener {
  static const int _v3FeatureFramework = 0x00;
  static const int _v3FeatureUpgrade = 0x06;
  static const int _v3PacketTypeCommand = 0x00;
  static const int _v3PacketTypeNotification = 0x01;
  static const int _v3PacketTypeResponse = 0x02;
  static const int _v3PacketTypeError = 0x03;
  static const int _v3CmdAppVersion = 0x05;
  static const int _v3CmdUpgradeNotification = 0x00;
  static const int _v3CmdUpgradeConnect = 0x00;
  static const int _v3CmdUpgradeDisconnect = 0x01;
  static const int _v3CmdUpgradeControl = 0x02;
  static const int _v3CmdSetDataEndpointMode = 0x04;

  final flutterReactiveBle = FlutterReactiveBle();
  var logText = "".obs;
  final String TAG = "OtaServer";
  var devices = <DiscoveredDevice>[].obs;
  StreamSubscription<DiscoveredDevice>? _scanConnection;

  String connectDeviceId = "";
  Uuid otaUUID = Uuid.parse("00001100-d102-11e1-9b23-00025b00a5a5");
  Uuid notifyUUID = Uuid.parse("00001102-d102-11e1-9b23-00025b00a5a5");
  Uuid writeUUID = Uuid.parse("00001101-d102-11e1-9b23-00025b00a5a5");
  Uuid writeNoResUUID = Uuid.parse("00001103-d102-11e1-9b23-00025b00a5a5");
  StreamSubscription<ConnectionStateUpdate>? _connection;
  bool isDeviceConnected = false;

  /**
   * To know if the upgrade process is currently running.
   */
  bool isUpgrading = false;

  bool transFerComplete = false;

  /**
   * To know how many times we try to start the upgrade.
   */
  var mStartAttempts = 0;

  /**
   * The offset to use to upload data on the device.
   */
  var mStartOffset = 0;

  /**
   * The file to upload on the device.
   */
  List<int>? mBytesFile;

  List<int> writeBytes = [];

  /**
   * The maximum value for the data length of a VM upgrade packet for the data transfer step.
   */
  var mMaxLengthForDataTransfer = 16;

  var mPayloadSizeMax = 16;

  /**
   * To know if the packet with the operation code "UPGRADE_DATA" which was sent was the last packet to send.
   */
  bool wasLastPacket = false;

  int mBytesToSend = 0;

  int mResumePoint = -1;

  var mIsRWCPEnabled = false.obs;
  int sendPkgCount = 0;

  RxDouble updatePer = RxDouble(0);
  var versionBeforeUpgrade = "UNKNOWN".obs;
  var versionAfterUpgrade = "UNKNOWN".obs;

  /**
   * To know if we have to disconnect after any event which occurs as a fatal error from the board.
   */
  bool hasToAbort = false;

  final writeQueue = Queue<List<int>>();

  StreamSubscription<List<int>>? _subscribeConnection;

  StreamSubscription<List<int>>? _subscribeConnectionRWCP;

  String fileMd5 = "";
  var firmwarePath = "".obs;
  var rwcpStatusText = "未启用".obs;

  var percentage = 0.0.obs;

  Timer? _timer;
  Timer? _logFlushTimer;
  final ListQueue<String> _pendingLogs = ListQueue();
  bool _isLogFlushScheduled = false;
  static const int _maxLogLines = 800;
  String _lastLogDedupKey = "";
  int _lastLogRepeat = 0;
  StreamSubscription<BleStatus>? _bleStatusSubscription;
  static const bool _enableWriteTraceLog = false;

  var timeCount = 0.obs;

  //RWCP
  ListQueue<double> mProgressQueue = ListQueue();

  late RWCPClient mRWCPClient;

  int mTransferStartTime = 0;

  int writeRTCPCount = 0;

  File? file;
  final bool useDfuOnly = false;
  int _dfuPendingChunkSize = 0;
  bool _dfuWriteInFlight = false;
  Timer? _dfuResultTimer;
  bool _rwcpSetupInProgress = false;
  Timer? _upgradeWatchdogTimer;
  bool _autoReconnectEnabled = true;
  String _fatalUpgradeReason = "";
  static const String vendorModeAuto = "auto";
  static const String vendorModeV3 = "v3";
  static const String vendorModeV1V2 = "v1v2";
  var vendorMode = vendorModeV3.obs;
  int _activeVendorId = 0x001D;
  bool _isVendorReady = false;
  bool _isVendorDetecting = false;
  int _vendorProbeIndex = 0;
  Timer? _vendorProbeTimer;
  bool _vendorFallbackTried = false;
  final List<int> _vendorCandidates = [0x001D, GAIA.VENDOR_QUALCOMM];
  VoidCallback? _onVendorReady;
  VoidCallback? _onVendorFailed;
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

  static OtaServer get to => Get.find();

  @override
  void onInit() {
    super.onInit();
    mRWCPClient = RWCPClient(this);
    _initDefaultFirmwarePath();
    _bleStatusSubscription?.cancel();
    _bleStatusSubscription = flutterReactiveBle.statusStream.listen((event) {
      switch (event) {
        case BleStatus.ready:
          addLog("蓝牙打开");
          break;
        case BleStatus.poweredOff:
          addLog("蓝牙关闭");
          break;
        case BleStatus.unknown:
          addLog("蓝牙状态未知");
          break;
        default:
          addLog("蓝牙不可用");
          break;
      }
    });
  }

  void _initDefaultFirmwarePath() async {
    try {
      final filePath = await getApplicationDocumentsDirectory();
      firmwarePath.value = "${filePath.path}/1.bin";
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

  void connectDevice(String id) async {
    try {
      await _connection?.cancel();
      await _subscribeConnection?.cancel();
      await _subscribeConnectionRWCP?.cancel();
      _connection = null;
      _subscribeConnection = null;
      _subscribeConnectionRWCP = null;
      _autoReconnectEnabled = true;
      _isVendorReady = false;
      _vendorFallbackTried = false;
      addLog('开始连接$id');
      _connection = flutterReactiveBle
          .connectToDevice(
              id: id, connectionTimeout: const Duration(seconds: 5))
          .listen((connectionState) async {
        if (connectionState.connectionState ==
            DeviceConnectionState.connected) {
          isDeviceConnected = true;
          if (!isUpgrading) {
            rwcpStatusText.value = "待启用";
          }
          connectDeviceId = id;
          addLog("连接成功" + connectDeviceId);
          _startVendorProbe(
            onSuccess: () {
              addLog("Vendor探测成功: ${_vendorToHex(_activeVendorId)}");
            },
            onFailed: () {
              addLog(
                  "Vendor探测失败，继续使用默认Vendor ${_vendorToHex(_activeVendorId)}");
            },
          );
          //IOS BUG
          await flutterReactiveBle.discoverServices(id);
          Future.delayed(const Duration(seconds: 1))
              .then((value) => registerNotice());
          if (!isUpgrading) {
            Get.to(() => const TestOtaView());
          }
        } else if (connectionState.connectionState ==
            DeviceConnectionState.disconnected) {
          isDeviceConnected = false;
          rwcpStatusText.value = "连接断开";
          addLog('断开连接');
          if (isUpgrading) {
            _enterFatalUpgradeState("升级过程中蓝牙断链");
            return;
          }
          if (_autoReconnectEnabled && connectDeviceId.isNotEmpty) {
            Future.delayed(const Duration(seconds: 5))
                .then((value) => connectDevice(connectDeviceId));
          } else {
            addLog("自动重连已关闭，等待手动重连");
          }
        } else {
          isDeviceConnected = false;
          rwcpStatusText.value = "连接中断";
          addLog('断开${connectionState.connectionState}');
          if (isUpgrading) {
            _enterFatalUpgradeState(
                "升级过程中连接状态异常: ${connectionState.connectionState}");
          }
        }
      });
    } catch (e) {
      addLog('开始连接失败$e');
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

  bool _isV3VendorActive() => _activeVendorId == 0x001D;

  int _buildV3Command(int feature, int packetType, int commandId) {
    return ((feature & 0x7F) << 9) |
        ((packetType & 0x03) << 7) |
        (commandId & 0x7F);
  }

  int _upgradeConnectCommand() => _isV3VendorActive()
      ? _buildV3Command(
          _v3FeatureUpgrade, _v3PacketTypeCommand, _v3CmdUpgradeConnect)
      : GAIA.COMMAND_VM_UPGRADE_CONNECT;

  int _upgradeDisconnectCommand() => _isV3VendorActive()
      ? _buildV3Command(
          _v3FeatureUpgrade, _v3PacketTypeCommand, _v3CmdUpgradeDisconnect)
      : GAIA.COMMAND_VM_UPGRADE_DISCONNECT;

  int _upgradeControlCommand() => _isV3VendorActive()
      ? _buildV3Command(
          _v3FeatureUpgrade, _v3PacketTypeCommand, _v3CmdUpgradeControl)
      : GAIA.COMMAND_VM_UPGRADE_CONTROL;

  int _setDataEndpointModeCommand() => _isV3VendorActive()
      ? _buildV3Command(
          _v3FeatureUpgrade, _v3PacketTypeCommand, _v3CmdSetDataEndpointMode)
      : GAIA.COMMAND_SET_DATA_ENDPOINT_MODE;

  int _getApplicationVersionCommand() => _isV3VendorActive()
      ? _buildV3Command(
          _v3FeatureFramework, _v3PacketTypeCommand, _v3CmdAppVersion)
      : GAIA.COMMAND_GET_APPLICATION_VERSION;

  int _v3CommandFeature(int cmd) => (cmd >> 9) & 0x7F;
  int _v3CommandType(int cmd) => (cmd >> 7) & 0x03;
  int _v3CommandId(int cmd) => cmd & 0x7F;

  void setVendorMode(String mode) {
    if (mode != vendorModeAuto &&
        mode != vendorModeV3 &&
        mode != vendorModeV1V2) {
      return;
    }
    vendorMode.value = mode;
    _isVendorDetecting = false;
    _vendorProbeTimer?.cancel();
    _vendorFallbackTried = false;
    if (mode == vendorModeV3) {
      _activeVendorId = 0x001D;
      _isVendorReady = true;
      addLog("Vendor模式切换为V3，使用${_vendorToHex(_activeVendorId)}");
      return;
    }
    if (mode == vendorModeV1V2) {
      _activeVendorId = GAIA.VENDOR_QUALCOMM;
      _isVendorReady = true;
      addLog("Vendor模式切换为V1/V2，使用${_vendorToHex(_activeVendorId)}");
      return;
    }
    _activeVendorId = 0x001D;
    _isVendorReady = false;
    addLog("Vendor模式切换为自动探测（优先V3）");
  }

  void _startVendorProbe(
      {required VoidCallback onSuccess, required VoidCallback onFailed}) {
    if (vendorMode.value == vendorModeV3) {
      _activeVendorId = 0x001D;
      _isVendorReady = true;
      onSuccess();
      return;
    }
    if (vendorMode.value == vendorModeV1V2) {
      _activeVendorId = GAIA.VENDOR_QUALCOMM;
      _isVendorReady = true;
      onSuccess();
      return;
    }
    if (_isVendorReady) {
      onSuccess();
      return;
    }
    if (_isVendorDetecting) {
      _onVendorReady = onSuccess;
      _onVendorFailed = onFailed;
      return;
    }
    _isVendorDetecting = true;
    _vendorProbeIndex = 0;
    _onVendorReady = onSuccess;
    _onVendorFailed = onFailed;
    _probeNextVendor();
  }

  void _probeNextVendor() {
    _vendorProbeTimer?.cancel();
    if (_vendorProbeIndex >= _vendorCandidates.length) {
      _isVendorDetecting = false;
      _isVendorReady = false;
      rwcpStatusText.value = "Vendor探测失败";
      _onVendorFailed?.call();
      _onVendorReady = null;
      _onVendorFailed = null;
      return;
    }
    final candidate = _vendorCandidates[_vendorProbeIndex];
    addLog("探测Vendor ${_vendorToHex(candidate)}");
    final probeCommand = candidate == 0x001D
        ? _buildV3Command(
            _v3FeatureFramework, _v3PacketTypeCommand, _v3CmdAppVersion)
        : GAIA.COMMAND_GET_API_VERSION;
    final packet = _buildGaiaPacket(probeCommand, vendor: candidate);
    writeMsg(packet.getBytes());
    _vendorProbeTimer = Timer(const Duration(seconds: 2), () {
      if (!_isVendorDetecting) {
        return;
      }
      _vendorProbeIndex += 1;
      _probeNextVendor();
    });
  }

  void _onVendorProbeSuccess(int vendor) {
    _vendorProbeTimer?.cancel();
    _isVendorDetecting = false;
    _activeVendorId = vendor;
    _isVendorReady = true;
    rwcpStatusText.value = "Vendor ${_vendorToHex(vendor)} 就绪";
    final callback = _onVendorReady;
    _onVendorReady = null;
    _onVendorFailed = null;
    callback?.call();
  }

  void registerRWCP() async {
    if (!mIsRWCPEnabled.value) {
      return;
    }
    rwcpStatusText.value = "建立通道中";
    await _subscribeConnectionRWCP?.cancel();
    //IOS BUG
    await flutterReactiveBle.discoverServices(connectDeviceId);
    await Future.delayed(const Duration(seconds: 1));
    final characteristic = QualifiedCharacteristic(
        serviceId: otaUUID,
        characteristicId: writeNoResUUID,
        deviceId: connectDeviceId);
    _subscribeConnectionRWCP = flutterReactiveBle
        .subscribeToCharacteristic(characteristic)
        .listen((data) {
      //addLog("wenDataRec2>${StringUtils.byteToHexString(data)}");
      mRWCPClient.onReceiveRWCPSegment(data);
      // code to handle incoming data
    }, onError: (dynamic error) {
      // code to handle errors
    });
    addLog("isUpgrading$isUpgrading transFerComplete $transFerComplete");
    await Future.delayed(const Duration(seconds: 1));
    if (isUpgrading) {
      rwcpStatusText.value = "已启用";
      _rwcpSetupInProgress = false;
      if (!transFerComplete) {
        sendUpgradeConnect();
      }
      return;
    }
    if (!isUpgrading) {
      startUpdate();
    }
  }

  //注册通知
  void registerNotice() async {
    await _subscribeConnection?.cancel();
    //IOS需要先发现否则订阅失败
    await flutterReactiveBle.discoverServices(connectDeviceId);
    await Future.delayed(const Duration(seconds: 1));
    final characteristic = QualifiedCharacteristic(
        serviceId: otaUUID,
        characteristicId: notifyUUID,
        deviceId: connectDeviceId);
    _subscribeConnection = flutterReactiveBle
        .subscribeToCharacteristic(characteristic)
        .listen((data) {
      addLog("收到通知>${StringUtils.byteToHexString(data)}");
      handleRecMsg(data);
      // code to handle incoming data
    }, onError: (dynamic error) {
      // code to handle errors
    });
    await Future.delayed(const Duration(seconds: 1));
    if (!_isV3VendorActive()) {
      GaiaPacketBLE packet = _buildGaiaPacket(
        GAIA.COMMAND_REGISTER_NOTIFICATION,
        payload: [GAIA.VMU_PACKET],
      );
      writeMsg(packet.getBytes());
    }
    //如果开启RWCP那么需要在重连之后启用RWCP
    if (isUpgrading && transFerComplete && mIsRWCPEnabled.value) {
      //开启RWCP
      await Future.delayed(const Duration(seconds: 1));
      writeMsg(_buildGaiaPacket(_setDataEndpointModeCommand(), payload: [0x01])
          .getBytes());
    }
  }

  void startUpdate() async {
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
    isUpgrading = true;
    _dfuResultTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _rwcpSetupInProgress = false;
    _fatalUpgradeReason = "";
    _autoReconnectEnabled = true;
    _vendorFallbackTried = false;
    mIsRWCPEnabled.value = true;
    rwcpStatusText.value = "启用中";
    writeQueue.clear();
    resetUpload();
    _armUpgradeWatchdog();
    if (!useDfuOnly) {
      enableRwcpForUpgrade();
      return;
    }
    if (useDfuOnly) {
      final loaded = await loadFirmwareFile();
      if (!loaded) {
        isUpgrading = false;
        _timer?.cancel();
        return;
      }
      sendDfuRequest();
      return;
    }
    sendUpgradeConnect();
  }

  void startUpdateWithVersionCheck() {
    if (isUpgrading) {
      addLog("正在升级中，忽略重复开始请求");
      return;
    }
    _startVendorProbe(
      onSuccess: () {
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
      },
      onFailed: () {
        addLog("Vendor探测失败，升级取消");
      },
    );
  }

  void handleRecMsg(List<int> data) async {
    _touchUpgradeWatchdog();
    GaiaPacketBLE packet = GaiaPacketBLE.fromByte(data) ?? GaiaPacketBLE(0);
    if (packet.mVendorId == 0x001D) {
      _handleV3Packet(packet);
      return;
    }
    if (packet.isAcknowledgement()) {
      int status = packet.getStatus();
      if (status == GAIA.SUCCESS) {
        receiveSuccessfulAcknowledgement(packet);
      } else {
        receiveUnsuccessfulAcknowledgement(packet);
      }
    } else if (packet.getCommand() == GAIA.COMMAND_EVENT_NOTIFICATION) {
      final payload = packet.mPayload ?? [];
      //000AC0010012
      if (payload.isNotEmpty) {
        int event = packet.getEvent();
        if (event == GAIA.VMU_PACKET) {
          createAcknowledgmentRequest();
          await Future.delayed(const Duration(milliseconds: 1000));
          receiveVMUPacket(payload.sublist(1));
          return;
        } else {
          // not supported
          return;
        }
      } else {
        createAcknowledgmentRequest();
        await Future.delayed(const Duration(milliseconds: 1000));
        return;
      }
    }
  }

  void _handleV3Packet(GaiaPacketBLE packet) {
    final cmd = packet.getCommand();
    final feature = _v3CommandFeature(cmd);
    final packetType = _v3CommandType(cmd);
    final commandId = _v3CommandId(cmd);
    final payload = packet.mPayload ?? [];

    if (packetType == _v3PacketTypeResponse) {
      if (feature == _v3FeatureFramework && commandId == _v3CmdAppVersion) {
        if (_isVendorDetecting) {
          _onVendorProbeSuccess(packet.mVendorId);
          return;
        }
        onApplicationVersionAckV3(payload);
        return;
      }
      if (feature == _v3FeatureUpgrade &&
          commandId == _v3CmdSetDataEndpointMode) {
        if (mIsRWCPEnabled.value) {
          registerRWCP();
        } else {
          _rwcpSetupInProgress = false;
        }
        return;
      }
      if (feature == _v3FeatureUpgrade && commandId == _v3CmdUpgradeConnect) {
        if (isUpgrading) {
          resetUpload();
          sendSyncReq();
        }
        return;
      }
      if (feature == _v3FeatureUpgrade && commandId == _v3CmdUpgradeControl) {
        onSuccessfulTransmission();
        return;
      }
      if (feature == _v3FeatureUpgrade &&
          commandId == _v3CmdUpgradeDisconnect) {
        stopUpgrade(sendAbort: false, sendDisconnect: false);
        return;
      }
      return;
    }

    if (packetType == _v3PacketTypeNotification) {
      if (feature == _v3FeatureUpgrade &&
          commandId == _v3CmdUpgradeNotification) {
        receiveVMUPacket(payload);
      }
      return;
    }

    if (packetType == _v3PacketTypeError) {
      final status = payload.isNotEmpty ? payload.first : -1;
      addLog(
          "V3错误响应 feature=$feature cmdId=$commandId status=0x${status.toRadixString(16)} ${_gaiaStatusText(status)}");
      _reportDeviceError(
          "V3错误 feature=$feature cmdId=$commandId status=0x${status.toRadixString(16)}",
          triggerRecovery: !isUpgrading);

      if (feature == _v3FeatureFramework && commandId == _v3CmdAppVersion) {
        if (_isVendorDetecting) {
          _vendorProbeIndex += 1;
          _probeNextVendor();
          return;
        }
        _finishVersionQueryFailed(
            "$_currentVersionQueryTag版本查询失败 status=0x${status.toRadixString(16)}");
        return;
      }

      if (feature == _v3FeatureUpgrade &&
          commandId == _v3CmdUpgradeConnect &&
          vendorMode.value == vendorModeAuto &&
          !_vendorFallbackTried) {
        _vendorFallbackTried = true;
        _activeVendorId = _activeVendorId == GAIA.VENDOR_QUALCOMM
            ? 0x001D
            : GAIA.VENDOR_QUALCOMM;
        _isVendorReady = true;
        addLog("V3连接失败，切换Vendor后重试: ${_vendorToHex(_activeVendorId)}");
        _rwcpSetupInProgress = false;
        if (mIsRWCPEnabled.value) {
          enableRwcpForUpgrade();
        } else {
          sendUpgradeConnect();
        }
        return;
      }

      if (feature == _v3FeatureUpgrade &&
          commandId == _v3CmdSetDataEndpointMode) {
        _rwcpSetupInProgress = false;
        rwcpStatusText.value = "RWCP错误";
        _enterFatalUpgradeState("RWCP数据通道启用失败");
        return;
      }

      if (feature == _v3FeatureUpgrade &&
          (commandId == _v3CmdUpgradeConnect ||
              commandId == _v3CmdUpgradeControl ||
              commandId == _v3CmdUpgradeDisconnect)) {
        _enterFatalUpgradeState(
            "V3升级命令失败 cmdId=$commandId status=0x${status.toRadixString(16)}");
      }
    }
  }

  void receiveSuccessfulAcknowledgement(GaiaPacketBLE packet) {
    addLog(
        "receiveSuccessfulAcknowledgement ${StringUtils.intTo2HexString(packet.getCommand())}");
    switch (packet.getCommand()) {
      case GAIA.COMMAND_DFU_REQUEST:
        sendDfuBegin();
        break;
      case GAIA.COMMAND_DFU_BEGIN:
        sendNextDfuPacket();
        break;
      case GAIA.COMMAND_DFU_WRITE:
        onDfuWriteAck();
        break;
      case GAIA.COMMAND_DFU_COMMIT:
        onDfuCommitAck();
        break;
      case GAIA.COMMAND_DFU_GET_RESULT:
        onDfuGetResultAck(packet);
        break;
      case GAIA.COMMAND_GET_API_VERSION:
        if (_isVendorDetecting) {
          _onVendorProbeSuccess(packet.mVendorId);
        }
        break;
      case GAIA.COMMAND_GET_APPLICATION_VERSION:
        onApplicationVersionAck(packet);
        break;
      case GAIA.COMMAND_VM_UPGRADE_CONNECT:
        {
          if (isUpgrading) {
            resetUpload();
            sendSyncReq();
          } else {
            int size = mPayloadSizeMax;
            if (mIsRWCPEnabled.value) {
              size = mPayloadSizeMax - 1;
              size = (size % 2 == 0) ? size : size - 1;
            }
            mMaxLengthForDataTransfer =
                size - VMUPacket.REQUIRED_INFORMATION_LENGTH;
            addLog(
                "mMaxLengthForDataTransfer $mMaxLengthForDataTransfer mPayloadSizeMax $mPayloadSizeMax");
            //开始发送升级包
            startUpgradeProcess();
          }
        }
        break;
      case GAIA.COMMAND_VM_UPGRADE_DISCONNECT:
        stopUpgrade(sendAbort: false, sendDisconnect: false);
        break;
      case GAIA.COMMAND_VM_UPGRADE_CONTROL:
        onSuccessfulTransmission();
        break;
      case GAIA.COMMAND_SET_DATA_ENDPOINT_MODE:
        if (mIsRWCPEnabled.value) {
          registerRWCP();
        } else {
          _rwcpSetupInProgress = false;
          _subscribeConnectionRWCP?.cancel();
        }

        break;
    }
  }

  void receiveUnsuccessfulAcknowledgement(GaiaPacketBLE packet) {
    final cmd = packet.getCommand();
    final status = packet.getStatus();
    addLog(
        "命令发送失败 cmd=${StringUtils.intTo2HexString(cmd)}(${_gaiaCommandText(cmd)}) status=0x${status.toRadixString(16)} ${_gaiaStatusText(status)}");
    _reportDeviceError(
        "ACK失败 ${_gaiaCommandText(cmd)} status=0x${status.toRadixString(16)}",
        triggerRecovery: !isUpgrading);
    if (cmd == GAIA.COMMAND_DFU_REQUEST && useDfuOnly) {
      addLog("DFU_REQUEST不支持，尝试直接发送DFU_BEGIN");
      sendDfuBegin();
      return;
    }
    if (cmd == GAIA.COMMAND_DFU_BEGIN ||
        cmd == GAIA.COMMAND_DFU_WRITE ||
        cmd == GAIA.COMMAND_DFU_COMMIT) {
      _dfuWriteInFlight = false;
      stopUpgrade(sendAbort: false);
      return;
    }
    if (cmd == GAIA.COMMAND_DFU_GET_RESULT) {
      addLog("DFU_GET_RESULT失败，按提交成功处理（结果码不可得）");
      _finishDfuUpgrade("DFU提交完成（设备未返回结果码）", queryPostVersion: true);
      return;
    }
    if (cmd == _getApplicationVersionCommand()) {
      _finishVersionQueryFailed(
          "$_currentVersionQueryTag版本查询失败 status=0x${status.toRadixString(16)}");
      return;
    }
    if (cmd == GAIA.COMMAND_GET_API_VERSION && _isVendorDetecting) {
      _vendorProbeIndex += 1;
      _probeNextVendor();
      return;
    }
    if (packet.getCommand() == _upgradeConnectCommand() ||
        packet.getCommand() == _upgradeControlCommand()) {
      if (packet.getCommand() == _upgradeConnectCommand() &&
          vendorMode.value == vendorModeAuto &&
          !_vendorFallbackTried) {
        _vendorFallbackTried = true;
        _activeVendorId = _activeVendorId == GAIA.VENDOR_QUALCOMM
            ? 0x001D
            : GAIA.VENDOR_QUALCOMM;
        _isVendorReady = true;
        addLog("升级连接失败，切换Vendor后重试: ${_vendorToHex(_activeVendorId)}");
        _rwcpSetupInProgress = false;
        if (mIsRWCPEnabled.value) {
          enableRwcpForUpgrade();
        } else {
          sendUpgradeConnect();
        }
        return;
      }
      addLog("升级命令失败：${_gaiaCommandText(cmd)}，触发升级断开");
      _enterFatalUpgradeState(
          "升级命令失败：${_gaiaCommandText(cmd)} status=0x${status.toRadixString(16)}");
    } else if (packet.getCommand() == _upgradeDisconnectCommand()) {
      if (isUpgrading) {
        _enterFatalUpgradeState("升级断开命令失败");
      }
    } else if (packet.getCommand() == _setDataEndpointModeCommand() ||
        packet.getCommand() == GAIA.COMMAND_GET_DATA_ENDPOINT_MODE) {
      _rwcpSetupInProgress = false;
      rwcpStatusText.value = "RWCP错误";
      _enterFatalUpgradeState("RWCP数据通道启用失败");
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
    if (!isUpgrading) {
      isUpgrading = true;
      resetUpload();
      sendSyncReq();
    } else if (isUpgrading) {
      stopUpgrade();
      addLog("正在升级");
    } else {
      stopUpgrade();
      // mBytesFile == null
      addLog("升级文件不存在");
    }
  }

  /**
   * <p>To reset the file transfer.</p>
   */
  void resetUpload() {
    transFerComplete = false;
    mStartAttempts = 0;
    mBytesToSend = 0;
    mStartOffset = 0;
  }

  void stopUpgrade({bool sendAbort = true, bool sendDisconnect = true}) async {
    _clearUpgradeWatchdog();
    _vendorProbeTimer?.cancel();
    _isVendorDetecting = false;
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _pendingStartAfterVersionQuery = false;
    _rwcpSetupInProgress = false;
    if (!mIsRWCPEnabled.value) {
      rwcpStatusText.value = "未启用";
    }
    timeCount.value = 0;
    if (sendAbort && !useDfuOnly) {
      abortUpgrade();
    }
    resetUpload();
    writeRTCPCount = 0;
    updatePer.value = 0;
    isUpgrading = false;
    _dfuWriteInFlight = false;
    _dfuPendingChunkSize = 0;
    if (!useDfuOnly &&
        sendDisconnect &&
        isDeviceConnected &&
        connectDeviceId.isNotEmpty) {
      await Future.delayed(const Duration(milliseconds: 500));
      sendUpgradeDisconnect();
    }
  }

  Future<bool> loadFirmwareFile() async {
    String usePath = firmwarePath.value;
    if (usePath.isEmpty) {
      final filePath = await getApplicationDocumentsDirectory();
      usePath = "${filePath.path}/1.bin";
      firmwarePath.value = usePath;
    }
    file = File(usePath);
    if (!await file!.exists()) {
      addLog("升级文件不存在：$usePath");
      return false;
    }
    mBytesFile = await file!.readAsBytes();
    if ((mBytesFile ?? []).isEmpty) {
      addLog("升级文件为空：$usePath");
      return false;
    }
    fileMd5 = StringUtils.file2md5(mBytesFile ?? []).toUpperCase();
    addLog("读取到文件:$usePath");
    addLog("读取到文件MD5$fileMd5");
    return true;
  }

  void sendSyncReq() async {
    //A2305C3A9059C15171BD33F3BB08ADE4 MD5
    //000A0642130004BB08ADE4
    final loaded = await loadFirmwareFile();
    if (!loaded) {
      stopUpgrade();
      return;
    }
    final endMd5 = StringUtils.hexStringToBytes(fileMd5.substring(24));
    VMUPacket packet = VMUPacket.get(OpCodes.UPGRADE_SYNC_REQ, data: endMd5);
    sendVMUPacket(packet, false);
  }

  void sendDfuRequest() {
    addLog("发送DFU_REQUEST");
    _dfuWriteInFlight = false;
    _dfuPendingChunkSize = 0;
    mStartOffset = 0;
    mBytesToSend = mBytesFile?.length ?? 0;
    final packet = _buildGaiaPacket(GAIA.COMMAND_DFU_REQUEST);
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
    final packet = _buildGaiaPacket(GAIA.COMMAND_DFU_BEGIN, payload: payload);
    writeMsg(packet.getBytes());
  }

  void sendNextDfuPacket() {
    if (!isUpgrading || _dfuWriteInFlight) {
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
    final packet = _buildGaiaPacket(GAIA.COMMAND_DFU_WRITE, payload: payload);
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
    final packet = _buildGaiaPacket(GAIA.COMMAND_DFU_COMMIT);
    writeMsg(packet.getBytes());
  }

  void onDfuCommitAck() {
    updatePer.value = 100;
    sendDfuGetResult();
  }

  void sendDfuGetResult() {
    _dfuResultTimer?.cancel();
    addLog("发送DFU_GET_RESULT");
    final packet = _buildGaiaPacket(GAIA.COMMAND_DFU_GET_RESULT);
    writeMsg(packet.getBytes());
    _dfuResultTimer = Timer(const Duration(seconds: 3), () {
      if (!isUpgrading) {
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
    isUpgrading = false;
    _timer?.cancel();
    addLog(
        "DFU升级失败，结果码=0x${resultCode.toRadixString(16).padLeft(2, '0')} ${_dfuResultText(resultCode)}");
  }

  void _finishDfuUpgrade(String message, {bool queryPostVersion = false}) {
    _dfuWriteInFlight = false;
    isUpgrading = false;
    _timer?.cancel();
    _dfuResultTimer?.cancel();
    addLog(message);
    if (queryPostVersion) {
      _schedulePostUpgradeVersionQuery();
    }
  }

  String _gaiaStatusText(int status) {
    switch (status) {
      case 0:
        return "SUCCESS";
      case 1:
        return "NOT_SUPPORTED";
      case 2:
        return "NOT_AUTHENTICATED";
      case 3:
        return "INSUFFICIENT_RESOURCES";
      case 4:
        return "AUTHENTICATING";
      case 5:
        return "INVALID_PARAMETER";
      case 6:
        return "INCORRECT_STATE";
      case 7:
        return "IN_PROGRESS";
      default:
        return "UNKNOWN_STATUS";
    }
  }

  String _gaiaCommandText(int cmd) {
    if (cmd == _setDataEndpointModeCommand()) {
      return "SET_DATA_ENDPOINT_MODE";
    }
    if (cmd == _upgradeConnectCommand()) {
      return "VM_UPGRADE_CONNECT";
    }
    if (cmd == _upgradeControlCommand()) {
      return "VM_UPGRADE_CONTROL";
    }
    if (cmd == _upgradeDisconnectCommand()) {
      return "VM_UPGRADE_DISCONNECT";
    }
    if (cmd == _getApplicationVersionCommand()) {
      return "GET_APPLICATION_VERSION";
    }
    switch (cmd) {
      case GAIA.COMMAND_SET_DATA_ENDPOINT_MODE:
        return "SET_DATA_ENDPOINT_MODE";
      case GAIA.COMMAND_GET_DATA_ENDPOINT_MODE:
        return "GET_DATA_ENDPOINT_MODE";
      case GAIA.COMMAND_VM_UPGRADE_CONNECT:
        return "VM_UPGRADE_CONNECT";
      case GAIA.COMMAND_VM_UPGRADE_CONTROL:
        return "VM_UPGRADE_CONTROL";
      case GAIA.COMMAND_VM_UPGRADE_DISCONNECT:
        return "VM_UPGRADE_DISCONNECT";
      case GAIA.COMMAND_DFU_REQUEST:
        return "DFU_REQUEST";
      case GAIA.COMMAND_DFU_BEGIN:
        return "DFU_BEGIN";
      case GAIA.COMMAND_DFU_WRITE:
        return "DFU_WRITE";
      case GAIA.COMMAND_DFU_COMMIT:
        return "DFU_COMMIT";
      case GAIA.COMMAND_DFU_GET_RESULT:
        return "DFU_GET_RESULT";
      default:
        return "UNKNOWN_COMMAND";
    }
  }

  String _dfuResultText(int resultCode) {
    switch (resultCode) {
      case 0x00:
        return "SUCCESS";
      case 0x01:
        return "FAIL";
      default:
        return "UNKNOWN_RESULT";
    }
  }

  String _upgradeErrorText(int returnCode) {
    switch (returnCode) {
      case 0x21:
        return "电量过低";
      case 0x81:
        return "文件校验不通过";
      default:
        return "未知升级错误";
    }
  }

  void queryApplicationVersion({
    required String tag,
    required void Function(String version) onSuccess,
    required VoidCallback onFailed,
  }) {
    if (_isVersionQueryInFlight) {
      addLog("版本查询进行中，忽略重复请求");
      return;
    }
    if (!isDeviceConnected) {
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
    _versionQueryTimer = Timer(const Duration(seconds: 3), () {
      if (!_isVersionQueryInFlight) {
        return;
      }
      _finishVersionQueryFailed("$tag版本查询超时");
    });
  }

  void onApplicationVersionAck(GaiaPacketBLE packet) {
    if (!_isVersionQueryInFlight) {
      return;
    }
    _versionQueryTimer?.cancel();
    final version = _parseApplicationVersion(packet.mPayload ?? []);
    final tag = _currentVersionQueryTag;
    _isVersionQueryInFlight = false;
    _currentVersionQueryTag = "";
    addLog("$tag版本号: $version");
    final successCallback = _onVersionQuerySuccess;
    _onVersionQuerySuccess = null;
    _onVersionQueryFailed = null;
    successCallback?.call(version);
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

  String _parseApplicationVersion(List<int> payload) {
    if (payload.length <= 1) {
      return "UNKNOWN";
    }
    final raw = payload.sublist(1);
    final hex = StringUtils.byteToHexString(raw).toUpperCase();
    final printable = raw.every((b) => b >= 0x20 && b <= 0x7E);
    if (printable) {
      return "${String.fromCharCodes(raw)} (HEX:$hex)";
    }
    if (raw.length == 4) {
      final value = ((raw[0] & 0xFF) << 24) |
          ((raw[1] & 0xFF) << 16) |
          ((raw[2] & 0xFF) << 8) |
          (raw[3] & 0xFF);
      return "0x${value.toRadixString(16).padLeft(8, '0').toUpperCase()} (HEX:$hex)";
    }
    return "HEX:$hex";
  }

  String _parseApplicationVersionV3(List<int> payload) {
    if (payload.isEmpty) {
      return "UNKNOWN";
    }
    final hex = StringUtils.byteToHexString(payload).toUpperCase();
    final printable = payload.every((b) => b >= 0x20 && b <= 0x7E);
    if (printable) {
      return "${String.fromCharCodes(payload)} (HEX:$hex)";
    }
    return "HEX:$hex";
  }

  void _schedulePostUpgradeVersionQuery() {
    _postUpgradeVersionRetryTimer?.cancel();
    _postUpgradeVersionRetryCount = 0;
    _postUpgradeVersionRetryTimer =
        Timer.periodic(const Duration(seconds: 2), (timer) {
      if (_postUpgradeVersionRetryCount >= 10) {
        timer.cancel();
        addLog("升级后版本查询超时，无法自动对比");
        return;
      }
      _postUpgradeVersionRetryCount++;
      if (_isVersionQueryInFlight || isUpgrading) {
        return;
      }
      if (!isDeviceConnected) {
        addLog("等待设备重连后查询升级后版本(${_postUpgradeVersionRetryCount}/10)");
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
          if (_postUpgradeVersionRetryCount >= 10) {
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
      final packet = _buildGaiaPacket(_upgradeControlCommand(), payload: bytes);
      try {
        List<int> bytes = packet.getBytes();
        if (mTransferStartTime <= 0) {
          mTransferStartTime = DateTime.now().millisecond;
        }
        bool success = mRWCPClient.sendData(bytes);
        if (!success) {
          addLog(
              "Fail to send GAIA packet for GAIA command: ${packet.getCommandId()}");
        }
      } catch (e) {
        addLog(
            "Exception when attempting to create GAIA packet: " + e.toString());
      }
    } else {
      final pkg = _buildGaiaPacket(_upgradeControlCommand(), payload: bytes);
      writeMsg(pkg.getBytes());
    }
  }

  void receiveVMUPacket(List<int> data) {
    try {
      final packet = VMUPacket.getPackageFromByte(data);
      if (isUpgrading || packet!.mOpCode == OpCodes.UPGRADE_ABORT_CFM) {
        handleVMUPacket(packet);
      } else {
        addLog(
            "receiveVMUPacket Received VMU packet while application is not upgrading anymore, opcode received");
      }
    } catch (e) {
      addLog("receiveVMUPacket $e");
    }
  }

  ///创建回包
  void createAcknowledgmentRequest() {
    if (_isV3VendorActive()) {
      return;
    }
    final packet = _buildGaiaPacket(
      GAIA.COMMAND_EVENT_NOTIFICATION | GAIA.ACKNOWLEDGMENT_MASK,
      payload: [GAIA.SUCCESS],
    );
    writeMsg(packet.getBytes());
  }

  void handleVMUPacket(VMUPacket? packet) {
    switch (packet?.mOpCode) {
      case OpCodes.UPGRADE_SYNC_CFM:
        receiveSyncCFM(packet);
        break;
      case OpCodes.UPGRADE_START_CFM:
        receiveStartCFM(packet);
        break;
      case OpCodes.UPGRADE_DATA_BYTES_REQ:
        receiveDataBytesREQ(packet);
        break;
      case OpCodes.UPGRADE_ABORT_CFM:
        receiveAbortCFM();
        break;
      case OpCodes.UPGRADE_ERROR_WARN_IND:
        receiveErrorWarnIND(packet);
        break;
      case OpCodes.UPGRADE_IS_VALIDATION_DONE_CFM:
        receiveValidationDoneCFM(packet);
        break;
      case OpCodes.UPGRADE_TRANSFER_COMPLETE_IND:
        receiveTransferCompleteIND();
        break;
      case OpCodes.UPGRADE_COMMIT_REQ:
        receiveCommitREQ();
        break;
      case OpCodes.UPGRADE_COMPLETE_IND:
        receiveCompleteIND();
        break;
    }
  }

  void sendUpgradeConnect() async {
    GaiaPacketBLE packet = _buildGaiaPacket(_upgradeConnectCommand());
    writeMsg(packet.getBytes());
  }

  void cancelNotification() async {
    GaiaPacketBLE packet = _buildGaiaPacket(
      GAIA.COMMAND_CANCEL_NOTIFICATION,
      payload: [GAIA.VMU_PACKET],
    );
    writeMsg(packet.getBytes());
  }

  void sendUpgradeDisconnect() {
    GaiaPacketBLE packet = _buildGaiaPacket(_upgradeDisconnectCommand());
    writeMsg(packet.getBytes());
  }

  void receiveSyncCFM(VMUPacket? packet) {
    List<int> data = packet?.mData ?? [];
    if (data.length >= 6) {
      int step = data[0];
      addLog("上次传输步骤 step $step");
      if (step == ResumePoints.IN_PROGRESS) {
        setResumePoint(step);
      } else {
        mResumePoint = step;
      }
    } else {
      mResumePoint = ResumePoints.DATA_TRANSFER;
    }
    sendStartReq();
  }

  /**
   * To send an UPGRADE_START_REQ message.
   */
  void sendStartReq() {
    VMUPacket packet = VMUPacket.get(OpCodes.UPGRADE_START_REQ);
    sendVMUPacket(packet, false);
  }

  void receiveStartCFM(VMUPacket? packet) {
    List<int> data = packet?.mData ?? [];
    if (data.length >= 3) {
      if (data[0] == UpgradeStartCFMStatus.SUCCESS) {
        mStartAttempts = 0;
        // the device is ready for the upgrade, we can go to the resume point or to the upgrade beginning.
        switch (mResumePoint) {
          case ResumePoints.COMMIT:
            askForConfirmation(ConfirmationType.COMMIT);
            break;
          case ResumePoints.TRANSFER_COMPLETE:
            askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
            break;
          case ResumePoints.IN_PROGRESS:
            askForConfirmation(ConfirmationType.IN_PROGRESS);
            break;
          case ResumePoints.VALIDATION:
            sendValidationDoneReq();
            break;
          case ResumePoints.DATA_TRANSFER:
          default:
            sendStartDataReq();
            break;
        }
      }
    }
  }

  void receiveAbortCFM() {
    addLog("receiveAbortCFM");
    stopUpgrade(sendAbort: false, sendDisconnect: false);
  }

  void receiveErrorWarnIND(VMUPacket? packet) async {
    List<int> data = packet?.mData ?? [];
    if (data.length < 2) {
      addLog("receiveErrorWarnIND 升级失败，设备返回异常：错误码长度不足");
      _reportDeviceError("升级错误包长度异常", triggerRecovery: true);
      stopUpgrade();
      return;
    }
    sendErrorConfirmation(data); //
    int returnCode = StringUtils.extractIntFromByteArray(data, 0, 2, false);
    //A2305C3A9059C15171BD33F3BB08ADE4
    addLog(
        "receiveErrorWarnIND 升级失败 错误码0x${returnCode.toRadixString(16)} ${_upgradeErrorText(returnCode)} fileMd5$fileMd5");
    _reportDeviceError("升级错误码0x${returnCode.toRadixString(16)}",
        triggerRecovery: true);
    //noinspection IfCanBeSwitch
    if (returnCode == 0x81) {
      addLog("包不通过");
      askForConfirmation(ConfirmationType.WARNING_FILE_IS_DIFFERENT);
    } else if (returnCode == 0x21) {
      addLog("电量过低");
      askForConfirmation(ConfirmationType.BATTERY_LOW_ON_DEVICE);
    } else {
      _enterFatalUpgradeState("设备返回升级错误码0x${returnCode.toRadixString(16)}");
    }
  }

  void receiveValidationDoneCFM(VMUPacket? packet) {
    addLog("receiveValidationDoneCFM");
    List<int> data = packet?.getBytes() ?? [];
    if (data.length == 2) {
      final time = StringUtils.extractIntFromByteArray(data, 0, 2, false);
      Future.delayed(Duration(milliseconds: time))
          .then((value) => sendValidationDoneReq());
    } else {
      sendValidationDoneReq();
    }
  }

  void receiveTransferCompleteIND() {
    addLog("receiveTransferCompleteIND");
    transFerComplete = true;
    setResumePoint(ResumePoints.TRANSFER_COMPLETE);
    askForConfirmation(ConfirmationType.TRANSFER_COMPLETE);
  }

  void receiveCommitREQ() {
    addLog("receiveCommitREQ");
    setResumePoint(ResumePoints.COMMIT);
    askForConfirmation(ConfirmationType.COMMIT);
  }

  void receiveCompleteIND() {
    isUpgrading = false;
    _timer?.cancel();
    addLog("receiveCompleteIND 升级完成");
    _schedulePostUpgradeVersionQuery();
    disconnectUpgrade();
  }

  void sendValidationDoneReq() {
    VMUPacket packet = VMUPacket.get(OpCodes.UPGRADE_IS_VALIDATION_DONE_REQ);
    sendVMUPacket(packet, false);
  }

  void sendStartDataReq() {
    setResumePoint(ResumePoints.DATA_TRANSFER);
    VMUPacket packet = VMUPacket.get(OpCodes.UPGRADE_START_DATA_REQ);
    sendVMUPacket(packet, false);
  }

  void setResumePoint(int point) {
    mResumePoint = point;
  }

  void receiveDataBytesREQ(VMUPacket? packet) {
    List<int> data = packet?.mData ?? [];

    // Checking the data has the good length
    if (data.length == OpCodes.DATA_LENGTH) {
      // retrieving information from the received packet
      //REC 120300080000002400000000
      //SEND 000A064204000D0000030000FFFF0001FFFF0002
      var lengthByte = [data[0], data[1], data[2], data[3]];
      var fileByte = [data[4], data[5], data[6], data[7]];
      mBytesToSend =
          int.parse(StringUtils.byteToHexString(lengthByte), radix: 16);
      int fileOffset =
          int.parse(StringUtils.byteToHexString(fileByte), radix: 16);

      addLog(StringUtils.byteToHexString(data) +
          "本次发包: $fileOffset $mBytesToSend");
      // we check the value for the offset
      mStartOffset += (fileOffset > 0 &&
              fileOffset + mStartOffset < (mBytesFile?.length ?? 0))
          ? fileOffset
          : 0;

      // if the asked length doesn't fit with possibilities we use the maximum length we can use.
      mBytesToSend = (mBytesToSend > 0) ? mBytesToSend : 0;
      // if the requested length will look for bytes out of the array we reduce it to the remaining length.
      int remainingLength = mBytesFile?.length ?? 0 - mStartOffset;
      mBytesToSend =
          (mBytesToSend < remainingLength) ? mBytesToSend : remainingLength;
      if (mIsRWCPEnabled.value) {
        while (mBytesToSend > 0) {
          sendNextDataPacket();
        }
      } else {
        addLog("receiveDataBytesREQ: sendNextDataPacket");
        sendNextDataPacket();
      }
    } else {
      addLog("UpgradeError 数据传输失败");
      abortUpgrade();
    }
  }

  void abortUpgrade() {
    if (mRWCPClient.isRunningASession()) {
      mRWCPClient.cancelTransfer();
    }
    mProgressQueue.clear();
    sendAbortReq();
    isUpgrading = false;
  }

  void sendAbortReq() {
    VMUPacket packet = VMUPacket.get(OpCodes.UPGRADE_ABORT_REQ);
    sendVMUPacket(packet, false);
  }

  //主要发包逻辑
  void sendNextDataPacket() {
    if (!isUpgrading) {
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
      wasLastPacket = true;
      mBytesToSend = 0;
    } else {
      mStartOffset += bytesToSend;
      mBytesToSend -= bytesToSend;
    }

    sendData(lastPacket, dataToSend);
  }

  //计算进度
  void onFileUploadProgress() {
    double percentage = (mStartOffset * 100.0 / (mBytesFile ?? []).length);
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
    VMUPacket packet = VMUPacket.get(OpCodes.UPGRADE_DATA, data: dataToSend);
    sendVMUPacket(packet, true);
  }

  void onSuccessfulTransmission() {
    if (wasLastPacket) {
      if (mResumePoint == ResumePoints.DATA_TRANSFER) {
        wasLastPacket = false;
        setResumePoint(ResumePoints.VALIDATION);
        sendValidationDoneReq();
      }
    } else if (hasToAbort) {
      hasToAbort = false;
      abortUpgrade();
    } else {
      if (mBytesToSend > 0 &&
          mResumePoint == ResumePoints.DATA_TRANSFER &&
          !mIsRWCPEnabled.value) {
        sendNextDataPacket();
      }
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
      case ConfirmationType.COMMIT:
        {
          code = OpCodes.UPGRADE_COMMIT_CFM;
        }
        break;
      case ConfirmationType.IN_PROGRESS:
        {
          code = OpCodes.UPGRADE_IN_PROGRESS_RES;
        }
        break;
      case ConfirmationType.TRANSFER_COMPLETE:
        {
          code = OpCodes.UPGRADE_TRANSFER_COMPLETE_RES;
        }
        break;
      case ConfirmationType.BATTERY_LOW_ON_DEVICE:
        {
          sendSyncReq();
        }
        return;
      case ConfirmationType.WARNING_FILE_IS_DIFFERENT:
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
    VMUPacket packet =
        VMUPacket.get(OpCodes.UPGRADE_ERROR_WARN_RES, data: data);
    sendVMUPacket(packet, false);
  }

  void disconnectUpgrade() {
    if (!_isV3VendorActive()) {
      cancelNotification();
    }
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

  //一般命令写入通道
  Future<void> writeData(List<int> data) async {
    try {
      if (_enableWriteTraceLog) {
        addLog("wenDataWrite start>${StringUtils.byteToHexString(data)}");
      }
      await Future.delayed(const Duration(milliseconds: 100));
      final characteristic = QualifiedCharacteristic(
          serviceId: otaUUID,
          characteristicId: writeUUID,
          deviceId: connectDeviceId);
      await flutterReactiveBle.writeCharacteristicWithResponse(characteristic,
          value: data);
      _touchUpgradeWatchdog();
      if (_enableWriteTraceLog) {
        addLog("wenDataWrite end>${StringUtils.byteToHexString(data)}");
      }
    } catch (e) {
      addLog("写入失败(writeWithResponse): $e");
      _reportDeviceError("写通道异常(writeWithResponse)");
      if (isUpgrading) {
        _enterFatalUpgradeState("写入通道异常");
      }
    }
  }

  //RWCP写入通道
  void writeMsgRWCP(List<int> data) async {
    try {
      await Future.delayed(const Duration(milliseconds: 100));
      final characteristic = QualifiedCharacteristic(
          serviceId: otaUUID,
          characteristicId: writeNoResUUID,
          deviceId: connectDeviceId);
      await flutterReactiveBle
          .writeCharacteristicWithoutResponse(characteristic, value: data);
      _touchUpgradeWatchdog();
    } catch (e) {
      addLog("写入失败(writeWithoutResponse): $e");
      _reportDeviceError("写通道异常(writeWithoutResponse)");
      if (isUpgrading) {
        _enterFatalUpgradeState("RWCP写入异常");
      }
    }
  }

  void disconnect() {
    _vendorProbeTimer?.cancel();
    _isVendorDetecting = false;
    _connection?.cancel();
    _subscribeConnection?.cancel();
    _subscribeConnectionRWCP?.cancel();
  }

  Future<void> restPayloadSize() async {
    int mtu = await flutterReactiveBle.requestMtu(
        deviceId: connectDeviceId, mtu: 256);
    if (!mIsRWCPEnabled.value) {
      mtu = 23;
    }
    int dataSize = mtu - 3;
    mPayloadSizeMax = dataSize - 4;
    addLog("协商mtu $mtu mPayloadSizeMax $mPayloadSizeMax");
  }

  void addLog(String s) {
    debugPrint("wenTest " + s);
    final dedupKey = _normalizeLogKey(s);
    if (_lastLogDedupKey.isEmpty) {
      _lastLogDedupKey = dedupKey;
      _lastLogRepeat = 1;
      _pendingLogs.add(s);
      _scheduleLogFlush();
      return;
    }

    if (dedupKey == _lastLogDedupKey) {
      _lastLogRepeat += 1;
      _scheduleLogFlush();
      return;
    }

    _emitRepeatSummaryIfNeeded();
    _lastLogDedupKey = dedupKey;
    _lastLogRepeat = 1;
    _pendingLogs.add(s);
    _scheduleLogFlush();
  }

  String _normalizeLogKey(String message) {
    final withoutTimestamp = message.replaceFirst(
        RegExp(r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+\s+'), "");
    return withoutTimestamp.trim();
  }

  void _emitRepeatSummaryIfNeeded() {
    if (_lastLogRepeat > 1) {
      _pendingLogs.add("↳ 上一条重复 ${_lastLogRepeat - 1} 次");
      _lastLogRepeat = 1;
    }
  }

  void _scheduleLogFlush() {
    if (_isLogFlushScheduled) {
      return;
    }
    _isLogFlushScheduled = true;
    _logFlushTimer?.cancel();
    _logFlushTimer = Timer(const Duration(milliseconds: 120), _flushLogs);
  }

  void _flushLogs() {
    _isLogFlushScheduled = false;
    _emitRepeatSummaryIfNeeded();
    if (_pendingLogs.isEmpty) {
      return;
    }
    final builder = StringBuffer();
    while (_pendingLogs.isNotEmpty) {
      builder.writeln(_pendingLogs.removeFirst());
    }
    final merged = (logText.value + builder.toString());
    final lines = merged.split('\n');
    if (lines.length <= _maxLogLines) {
      logText.value = merged;
      return;
    }
    final start = lines.length - _maxLogLines;
    logText.value = lines.sublist(start).join('\n');
  }

  void _armUpgradeWatchdog() {
    _clearUpgradeWatchdog();
    if (!isUpgrading) {
      return;
    }
    _upgradeWatchdogTimer = Timer(const Duration(seconds: 15), () {
      if (!isUpgrading) {
        return;
      }
      _enterFatalUpgradeState("升级超时：15秒内未收到有效进展");
    });
  }

  void _touchUpgradeWatchdog() {
    if (!isUpgrading) {
      return;
    }
    _armUpgradeWatchdog();
  }

  void _clearUpgradeWatchdog() {
    _upgradeWatchdogTimer?.cancel();
    _upgradeWatchdogTimer = null;
  }

  void _enterFatalUpgradeState(String reason) {
    if (_fatalUpgradeReason == reason && !isUpgrading) {
      return;
    }
    _fatalUpgradeReason = reason;
    _autoReconnectEnabled = false;
    rwcpStatusText.value = "错误已退出";
    addLog("致命错误：$reason，已自动退出升级并关闭自动重连");
    if (isUpgrading) {
      stopUpgrade(sendAbort: false);
    } else {
      _clearUpgradeWatchdog();
    }
    _reportDeviceError(reason, triggerRecovery: true);
  }

  void _reportDeviceError(String reason, {bool triggerRecovery = false}) {
    if (!autoRecoveryEnabled.value) {
      return;
    }
    final now = DateTime.now();
    if (_lastErrorTime == null ||
        now.difference(_lastErrorTime!).inSeconds > 10) {
      _errorBurstCount = 0;
    }
    _lastErrorTime = now;
    _errorBurstCount += 1;
    addLog("错误累计($_errorBurstCount/3): $reason");
    if (triggerRecovery || _errorBurstCount >= 3) {
      _quickRecoverFromDeviceError("自动恢复触发: $reason");
    }
  }

  void quickRecoverNow() {
    _quickRecoverFromDeviceError("手动快速恢复");
  }

  void _quickRecoverFromDeviceError(String reason) async {
    if (_isRecovering) {
      addLog("恢复进行中，忽略重复触发");
      return;
    }
    final now = DateTime.now();
    _recoveryWindowStart ??= now;
    if (now.difference(_recoveryWindowStart!).inMinutes >= 1) {
      _recoveryWindowStart = now;
      _recoveryAttempts = 0;
    }
    if (_recoveryAttempts >= 3) {
      recoveryStatusText.value = "恢复受限";
      addLog("1分钟内恢复次数过多，暂停自动恢复");
      return;
    }
    _isRecovering = true;
    _recoveryAttempts += 1;
    _errorBurstCount = 0;
    recoveryStatusText.value = "恢复中";
    rwcpStatusText.value = "恢复中";
    addLog("执行快速恢复(${_recoveryAttempts}/3): $reason");
    try {
      stopUpgrade(sendAbort: false);
      await _subscribeConnection?.cancel();
      await _subscribeConnectionRWCP?.cancel();
      await _connection?.cancel();
      _subscribeConnection = null;
      _subscribeConnectionRWCP = null;
      _connection = null;
      isDeviceConnected = false;
      if (connectDeviceId.isNotEmpty) {
        await Future.delayed(const Duration(seconds: 2));
        connectDevice(connectDeviceId);
      } else {
        addLog("无连接设备ID，无法自动重连");
      }
      recoveryStatusText.value = "已恢复";
      rwcpStatusText.value = "待启用";
    } catch (e) {
      recoveryStatusText.value = "恢复失败";
      addLog("快速恢复失败: $e");
    } finally {
      _isRecovering = false;
    }
  }

  @override
  void onClose() {
    _bleStatusSubscription?.cancel();
    _logFlushTimer?.cancel();
    _upgradeWatchdogTimer?.cancel();
    _versionQueryTimer?.cancel();
    _postUpgradeVersionRetryTimer?.cancel();
    _vendorProbeTimer?.cancel();
    super.onClose();
  }

  void startScan() async {
    devices.clear();
    if (Platform.isAndroid) {
      Map<Permission, PermissionStatus> statuses = await [
        Permission.location,
        Permission.bluetooth,
        Permission.bluetoothScan,
        Permission.bluetoothConnect,
      ].request();
      var location = await Permission.location.status;
      var bluetooth = await Permission.bluetooth.status;
      var bluetoothScan = await Permission.bluetoothScan.status;
      var bluetoothConnect = await Permission.bluetoothConnect.status;
      if (location.isDenied) {
        addLog("location deny");
        return;
      }
      if (bluetoothScan.isDenied) {
        return;
      }
      if (bluetoothConnect.isDenied) {
        addLog("bluetoothConnect deny");
        return;
      }
    } else {
      var bluetooth = await Permission.bluetooth.status;
      if (bluetooth.isDenied) {
        addLog("bluetooth deny");
        return;
      }
    }
    try {
      await _scanConnection?.cancel();
      await _connection?.cancel();
    } catch (e) {}
    // Start scannin
    _scanConnection = flutterReactiveBle.scanForDevices(
        withServices: [],
        scanMode: ScanMode.lowLatency,
        requireLocationServicesEnabled: true).listen((device) {
      if (device.name.isNotEmpty) {
        final knownDeviceIndex = devices.indexWhere((d) => d.id == device.id);
        if (knownDeviceIndex >= 0) {
          devices[knownDeviceIndex] = device;
        } else {
          devices.add(device);
        }
      }
      //code for handling results
    });
  }
}
