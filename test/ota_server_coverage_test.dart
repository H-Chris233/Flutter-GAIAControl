import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:fake_async/fake_async.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ble_connection_manager.dart';
import 'package:gaia/controller/gaia_command_builder.dart';
import 'package:gaia/controller/ota_server.dart';
import 'package:gaia/controller/upgrade_state_machine.dart';
import 'package:gaia/utils/gaia/gaia.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';
import 'package:gaia/utils/gaia/confirmation_type.dart';
import 'package:gaia/utils/gaia/op_codes.dart';
import 'package:gaia/utils/gaia/resume_points.dart';
import 'package:gaia/utils/gaia/vmu_packet.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_client.dart';
import 'package:gaia/utils/gaia/rwcp/segment.dart';
import 'package:get/get.dart';
import 'package:path_provider_platform_interface/path_provider_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';
import 'package:reactive_ble_platform_interface/reactive_ble_platform_interface.dart'
    as reactive_ble;

class _NoopBleClient implements BleClient {
  @override
  Stream<BleStatus> get statusStream => const Stream<BleStatus>.empty();

  @override
  Stream<ConnectionStateUpdate> connectToDevice({
    required String id,
    Map<Uuid, List<Uuid>>? servicesWithCharacteristicsToDiscover,
    Duration? connectionTimeout,
  }) {
    return const Stream<ConnectionStateUpdate>.empty();
  }

  @override
  Future<void> discoverAllServices(String deviceId) async {}

  @override
  Future<List<Service>> getDiscoveredServices(String deviceId) async {
    return const <Service>[];
  }

  @override
  Future<int> requestMtu({required String deviceId, required int mtu}) async {
    return 23;
  }

  @override
  Stream<DiscoveredDevice> scanForDevices({
    required List<Uuid> withServices,
    ScanMode scanMode = ScanMode.balanced,
    bool requireLocationServicesEnabled = true,
  }) {
    return const Stream<DiscoveredDevice>.empty();
  }

  @override
  Stream<List<int>> subscribeToCharacteristic(
      QualifiedCharacteristic characteristic) {
    return const Stream<List<int>>.empty();
  }

  @override
  Future<void> writeCharacteristicWithResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {}

  @override
  Future<void> writeCharacteristicWithoutResponse(
    QualifiedCharacteristic characteristic, {
    required List<int> value,
  }) async {}
}

class _CoverageBleConnectionManager extends BleConnectionManager {
  _CoverageBleConnectionManager() : super(ble: _NoopBleClient());

  BleScanStartResult scanResult = BleScanStartResult.started;
  bool registerNotifyResult = true;
  bool registerRwcpResult = true;
  bool throwOnConnect = false;
  bool throwOnDisconnect = false;
  bool throwOnStopScan = false;
  bool throwWriteWithResponse = false;
  bool throwWriteWithoutResponse = false;
  int mtuResult = 185;
  int startScanCalled = 0;
  int stopScanCalled = 0;
  int disconnectCalled = 0;
  int registerNotifyCalled = 0;
  int registerRwcpCalled = 0;
  int cancelRwcpCalled = 0;
  final List<List<int>> writeWithResponsePayloads = <List<int>>[];
  final List<List<int>> writeWithoutResponsePayloads = <List<int>>[];
  final List<bool> autoReconnectEnabledHistory = <bool>[];
  VoidCallback? latestOnConnected;
  VoidCallback? latestOnDisconnected;
  void Function(Object error)? latestOnError;
  OnDataReceived? latestNotifyListener;
  OnDataReceived? latestRwcpListener;

  @override
  Future<BleScanStartResult> startScan() async {
    startScanCalled += 1;
    return scanResult;
  }

  @override
  Future<void> connect(
    String deviceId, {
    VoidCallback? onConnected,
    VoidCallback? onDisconnected,
    void Function(Object error)? onError,
  }) async {
    if (throwOnConnect) {
      throw StateError('connect failed');
    }
    latestOnConnected = onConnected;
    latestOnDisconnected = onDisconnected;
    latestOnError = onError;
  }

  @override
  Future<void> stopScan() async {
    stopScanCalled += 1;
    if (throwOnStopScan) {
      throw StateError('stopScan failed');
    }
  }

  @override
  Future<bool> registerNotifyChannel(OnDataReceived onDataReceived) async {
    registerNotifyCalled += 1;
    latestNotifyListener = onDataReceived;
    return registerNotifyResult;
  }

  @override
  Future<void> cancelRwcpChannel() async {
    cancelRwcpCalled += 1;
  }

  @override
  Future<bool> registerRwcpChannel(OnDataReceived onDataReceived) async {
    registerRwcpCalled += 1;
    latestRwcpListener = onDataReceived;
    return registerRwcpResult;
  }

  @override
  Future<void> writeWithResponse(List<int> data) async {
    if (throwWriteWithResponse) {
      throw StateError('writeWithResponse failed');
    }
    writeWithResponsePayloads.add(List<int>.from(data));
  }

  @override
  Future<void> writeWithoutResponse(List<int> data) async {
    if (throwWriteWithoutResponse) {
      throw StateError('writeWithoutResponse failed');
    }
    writeWithoutResponsePayloads.add(List<int>.from(data));
  }

  @override
  Future<int> requestMtu(int mtu) async {
    return mtuResult;
  }

  @override
  void setAutoReconnectEnabled(bool enabled) {
    autoReconnectEnabledHistory.add(enabled);
    super.setAutoReconnectEnabled(enabled);
  }

  @override
  void disconnect() {
    disconnectCalled += 1;
    if (throwOnDisconnect) {
      throw StateError('disconnect failed');
    }
    super.disconnect();
  }

  void emitDisconnected() {
    latestOnDisconnected?.call();
  }

  void emitError(Object error) {
    latestOnError?.call(error);
  }
}

class _FakePathProviderPlatform extends PathProviderPlatform
    with MockPlatformInterfaceMixin {
  _FakePathProviderPlatform(this.documentsPath);

  final String? documentsPath;

  @override
  Future<String?> getApplicationDocumentsPath() async {
    return documentsPath;
  }
}

class _FakeReactiveBlePlatform extends reactive_ble.ReactiveBlePlatform
    with MockPlatformInterfaceMixin {
  final StreamController<reactive_ble.BleStatus> _statusController =
      StreamController<reactive_ble.BleStatus>.broadcast();

  @override
  Stream<reactive_ble.BleStatus> get bleStatusStream =>
      _statusController.stream;

  @override
  Future<void> initialize() async {
    _statusController.add(reactive_ble.BleStatus.ready);
  }

  @override
  Future<void> deinitialize() async {
    await _statusController.close();
  }
}

class _ThrowingUpgradeStateMachine extends UpgradeStateMachine {
  _ThrowingUpgradeStateMachine({required super.delegate});

  @override
  void handleVmuPacket(VMUPacket? packet) {
    throw StateError('state machine failed');
  }
}

class _ControlledRWCPClient extends RWCPClient {
  _ControlledRWCPClient(super.mListener,
      {this.sendReturns = true, this.throwOnSend = false});

  bool sendReturns;
  bool throwOnSend;
  bool cancelCalled = false;

  @override
  bool sendData(List<int> bytes) {
    if (throwOnSend) {
      throw StateError('rwcp send throws');
    }
    return sendReturns;
  }

  @override
  void cancelTransfer() {
    cancelCalled = true;
  }
}

void main() {
  group('OtaServer coverage flows', () {
    late OtaServer server;
    late _CoverageBleConnectionManager bleManager;
    late GaiaCommandBuilder cmdBuilder;
    late Directory tempDir;
    late String firmwarePath;
    late PathProviderPlatform originalPathProvider;
    late reactive_ble.ReactiveBlePlatform originalReactiveBlePlatform;

    List<int> v3Packet({
      required int feature,
      required int packetType,
      required int commandId,
      List<int>? payload,
      int vendor = 0x001D,
    }) {
      final cmd = cmdBuilder.buildV3Command(feature, packetType, commandId);
      return GaiaPacketBLE(cmd, mPayload: payload, mVendorId: vendor)
          .getBytes();
    }

    DiscoveredDevice discoveredDevice(String id,
        {String name = 'GAIA', int rssi = -40}) {
      return DiscoveredDevice(
        id: id,
        name: name,
        serviceData: const <Uuid, Uint8List>{},
        manufacturerData: Uint8List(0),
        rssi: rssi,
        serviceUuids: const <Uuid>[],
      );
    }

    setUp(() async {
      Get.testMode = true;
      originalPathProvider = PathProviderPlatform.instance;
      originalReactiveBlePlatform = reactive_ble.ReactiveBlePlatform.instance;
      tempDir = await Directory.systemTemp.createTemp('gaia_cov_');
      firmwarePath = '${tempDir.path}/firmware.bin';
      bleManager = _CoverageBleConnectionManager();
      cmdBuilder = GaiaCommandBuilder();
      server = OtaServer(
        bleManagerOverride: bleManager,
        defaultFirmwarePathResolver: () async => firmwarePath,
      );
      server.onInit();
      server.autoRecoveryEnabled.value = false;
      server.connectDeviceId = 'device-1';
    });

    tearDown(() async {
      PathProviderPlatform.instance = originalPathProvider;
      reactive_ble.ReactiveBlePlatform.instance = originalReactiveBlePlatform;
      Get.reset();
      server.onClose();
      if (await tempDir.exists()) {
        await tempDir.delete(recursive: true);
      }
    });

    test('handleRecMsg ignores invalid packet and non V3 vendor', () {
      server.handleRecMsg(<int>[0x01]);
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[0x41],
        vendor: 0x000A,
      ));
      return Future<void>.delayed(const Duration(milliseconds: 150)).then((_) {
        expect(server.logText.value, contains('数据包解析失败'));
        expect(server.logText.value, contains('忽略非V3 Vendor包'));
      });
    });

    test('queryApplicationVersion succeeds via V3 response packet', () async {
      var version = '';
      var failed = false;
      server.isDeviceConnected.value = true;

      server.queryApplicationVersion(
        tag: '测试',
        onSuccess: (v) => version = v,
        onFailed: () => failed = true,
      );
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[0x56, 0x31],
      ));
      await Future<void>.delayed(Duration.zero);

      expect(failed, isFalse);
      expect(version, contains('V1'));
    });

    test('setDataEndpointMode response triggers registerRWCP when enabled',
        () async {
      bleManager.isDeviceConnected = true;
      server.mIsRWCPEnabled.value = true;
      server.enableRwcpForUpgrade();
      await Future<void>.delayed(Duration.zero);
      bleManager.writeWithResponsePayloads.clear();

      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdSetDataEndpointMode,
      ));
      await Future<void>.delayed(Duration.zero);

      expect(bleManager.cancelRwcpCalled, 1);
      expect(bleManager.registerRwcpCalled, 1);
    });

    test('setDataEndpointMode response does not register RWCP when disabled',
        () async {
      server.mIsRWCPEnabled.value = false;
      server.enableRwcpForUpgrade();
      await Future<void>.delayed(Duration.zero);

      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdSetDataEndpointMode,
      ));
      await Future<void>.delayed(Duration.zero);

      expect(bleManager.registerRwcpCalled, 0);
    });

    test('upgradeDisconnect response stops upgrading', () async {
      server.isUpgrading.value = true;
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdUpgradeDisconnect,
      ));
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
    });

    test('upgradeConnect response while upgrading triggers sync and file check',
        () async {
      server.isUpgrading.value = true;
      server.firmwarePath.value = '${tempDir.path}/missing.bin';

      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdUpgradeConnect,
      ));
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(server.logText.value, contains('升级文件不存在'));
      expect(server.isUpgrading.value, isFalse);
    });

    test('upgrade notification packet reaches VMU handler', () async {
      server.isUpgrading.value = true;
      final vmu = VMUPacket.get(OpCodes.upgradeTransferCompleteInd);

      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeNotification,
        commandId: GaiaCommandBuilder.v3CmdUpgradeNotification,
        payload: vmu.getBytes(),
      ));
      await Future<void>.delayed(Duration.zero);

      expect(server.transFerComplete, isTrue);
    });

    test('framework error packet finishes pending version query', () async {
      var failed = false;
      server.isDeviceConnected.value = true;
      server.queryApplicationVersion(
        tag: '测试失败',
        onSuccess: (_) {},
        onFailed: () => failed = true,
      );

      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeError,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[GAIA.incorrectState],
      ));
      await Future<void>.delayed(Duration.zero);

      expect(failed, isTrue);
    });

    test('upgrade error packet disables auto reconnect in fatal path',
        () async {
      server.isUpgrading.value = true;
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeError,
        commandId: GaiaCommandBuilder.v3CmdUpgradeConnect,
        payload: <int>[GAIA.incorrectState],
      ));
      await Future<void>.delayed(Duration.zero);

      expect(bleManager.autoReconnectEnabledHistory, contains(false));
      expect(server.isUpgrading.value, isFalse);
    });

    test('unknown V3 packet type while upgrading records device error',
        () async {
      server.isUpgrading.value = true;
      server.autoRecoveryEnabled.value = true;
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeCommand,
        commandId: GaiaCommandBuilder.v3CmdUpgradeControl,
      ));

      expect(server.errorCount.value, greaterThan(0));
    });

    test('loadFirmwareFile and sendSyncReq work with valid file', () async {
      final file = File(firmwarePath);
      await file.writeAsBytes(List<int>.generate(64, (i) => i));
      server.firmwarePath.value = firmwarePath;
      server.isUpgrading.value = true;

      final loaded = await server.loadFirmwareFile();
      await server.sendSyncReq();
      await Future<void>.delayed(Duration.zero);

      expect(loaded, isTrue);
      expect(server.fileMd5.isNotEmpty, isTrue);
      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('DFU request and begin send packets when file is loaded', () async {
      final file = File(firmwarePath);
      await file.writeAsBytes(List<int>.filled(48, 0xAB));
      server.firmwarePath.value = firmwarePath;
      await server.loadFirmwareFile();

      server.sendDfuRequest();
      server.sendDfuBegin();
      await Future<void>.delayed(Duration.zero);

      expect(
          bleManager.writeWithResponsePayloads.length, greaterThanOrEqualTo(2));
    });

    test('DFU begin with empty bytes stops upgrade', () async {
      server.mBytesFile = <int>[];
      server.isUpgrading.value = true;

      server.sendDfuBegin();
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
    });

    test('DFU data write flow updates progress and sends next packet',
        () async {
      server.isUpgrading.value = true;
      server.mPayloadSizeMax = 8;
      server.mBytesFile = List<int>.generate(16, (i) => i);
      server.sendNextDfuPacket();
      await Future<void>.delayed(Duration.zero);
      expect(bleManager.writeWithResponsePayloads, isNotEmpty);

      server.onDfuWriteAck();
      await Future<void>.delayed(Duration.zero);

      expect(server.mStartOffset, greaterThan(0));
      expect(server.updatePer.value, greaterThan(0));
    });

    test('DFU result ack handles success and failure payloads', () async {
      server.isUpgrading.value = true;
      server.onDfuGetResultAck(
          GaiaPacketBLE(GAIA.commandDfuGetResult, mPayload: <int>[0x00, 0x00]));
      await Future<void>.delayed(Duration.zero);
      expect(server.isUpgrading.value, isFalse);

      server.isUpgrading.value = true;
      server.onDfuGetResultAck(
          GaiaPacketBLE(GAIA.commandDfuGetResult, mPayload: <int>[0x00, 0x01]));
      await Future<void>.delayed(Duration.zero);
      expect(server.isUpgrading.value, isFalse);
    });

    test('queryApplicationVersion fails fast when device disconnected', () {
      var failed = false;
      server.isDeviceConnected.value = false;
      server.queryApplicationVersion(
        tag: '离线',
        onSuccess: (_) {},
        onFailed: () => failed = true,
      );
      expect(failed, isTrue);
    });

    test('askForConfirmation sends expected packets for normal types',
        () async {
      server.askForConfirmation(ConfirmationType.commit);
      server.askForConfirmation(ConfirmationType.inProgress);
      server.askForConfirmation(ConfirmationType.transferComplete);
      await Future<void>.delayed(Duration.zero);

      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('askForConfirmation batteryLow stops upgrade', () async {
      server.isUpgrading.value = true;
      server.askForConfirmation(ConfirmationType.batteryLowOnDevice);
      await Future<void>.delayed(Duration.zero);
      expect(server.isUpgrading.value, isFalse);

      server.isUpgrading.value = true;
      server.askForConfirmation(1 << 30); // unknown -> fallback path
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isTrue);
      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('sendErrorConfirmation sends VMU error response', () async {
      server.sendErrorConfirmation(<int>[0x81, 0x00]);
      await Future<void>.delayed(Duration.zero);
      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('restPayloadSize respects RWCP mode', () async {
      bleManager.mtuResult = 247;
      server.mIsRWCPEnabled.value = true;
      await server.restPayloadSize();
      expect(server.mPayloadSizeMax, 240);

      server.mIsRWCPEnabled.value = false;
      await server.restPayloadSize();
      expect(server.mPayloadSizeMax, 16);
    });

    test('writeData and writeMsgRWCP handle write exceptions', () async {
      server.isUpgrading.value = true;
      bleManager.throwWriteWithResponse = true;
      await server.writeData(<int>[0x00, 0x1D, 0x0C, 0x00]);
      await Future<void>.delayed(Duration.zero);

      bleManager.throwWriteWithoutResponse = true;
      await server.writeMsgRWCP(<int>[0x80, 0x00]);
      await Future<void>.delayed(Duration.zero);

      expect(bleManager.autoReconnectEnabledHistory, contains(false));
    });

    test('startScan handles denied and failed result mapping', () async {
      bleManager.scanResult = BleScanStartResult.locationDenied;
      await server.startScan();
      expect(server.deviceListUiState.value, DeviceListUiState.error);

      bleManager.scanResult = BleScanStartResult.bluetoothScanDenied;
      await server.startScan();
      expect(server.deviceListUiState.value, DeviceListUiState.error);

      bleManager.scanResult = BleScanStartResult.bluetoothConnectDenied;
      await server.startScan();
      expect(server.deviceListUiState.value, DeviceListUiState.error);

      bleManager.scanResult = BleScanStartResult.bluetoothDenied;
      await server.startScan();
      expect(server.deviceListUiState.value, DeviceListUiState.error);

      bleManager.scanResult = BleScanStartResult.failed;
      await server.startScan();
      expect(server.deviceListUiState.value, DeviceListUiState.error);
    });

    test('onTransferProgress updates percentage from queued progress', () {
      server.mProgressQueue.add(12.5);
      server.mProgressQueue.add(44.0);
      server.onTransferProgress(2);
      expect(server.updatePer.value, 44.0);
    });

    test('setFirmwarePath trims input and consumeUserMessage clears value',
        () async {
      server.setFirmwarePath('   ');
      server.setFirmwarePath('  $firmwarePath  ');
      server.userMessage.value = '提示消息';
      server.consumeUserMessage();
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(server.firmwarePath.value, firmwarePath);
      expect(server.userMessage.value, isNull);
      expect(server.logText.value, contains('固件路径不能为空'));
    });

    test('setVendorMode keeps V3 vendor and sends V3 command', () async {
      server.setVendorMode('legacy');
      await server.sendUpgradeConnect();
      await Future<void>.delayed(Duration.zero);

      final packet =
          GaiaPacketBLE.fromByte(bleManager.writeWithResponsePayloads.last);
      expect(server.vendorMode.value, OtaServer.vendorModeV3);
      expect(packet, isNotNull);
      expect(packet!.mVendorId, 0x001D);
    });

    test('device list worker updates ready and empty states', () async {
      server.isScanning.value = true;
      server.deviceListUiState.value = DeviceListUiState.scanning;
      server.devices.add(discoveredDevice('dev-1'));
      await Future<void>.delayed(Duration.zero);

      expect(server.isScanning.value, isFalse);
      expect(server.deviceListUiState.value, DeviceListUiState.ready);
      expect(server.deviceListHint.value, contains('发现 1 台设备'));

      server.devices.clear();
      await Future<void>.delayed(Duration.zero);

      expect(server.deviceListUiState.value, DeviceListUiState.empty);
      expect(server.deviceListHint.value, contains('未发现设备'));
    });

    test('connectDevice onConnected callback updates states', () async {
      await server.connectDevice('device-ok');
      bleManager.latestOnConnected?.call();
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(bleManager.stopScanCalled, 1);
      expect(server.isDeviceConnected.value, isTrue);
      expect(server.connectingDeviceId.value, '');
      expect(server.deviceListHint.value, '连接成功');
      expect(bleManager.registerNotifyCalled, greaterThan(0));
    });

    test('connectDevice onError callback maps to ui error', () async {
      await server.connectDevice('device-error');
      bleManager.emitError(StateError('boom'));
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.isDeviceConnected.value, isFalse);
      expect(server.deviceListUiState.value, DeviceListUiState.error);
      expect(server.userMessage.value, contains('连接失败'));
    });

    test('connectDevice catches connect exception', () async {
      bleManager.throwOnConnect = true;
      await server.connectDevice('device-fail');
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.isDeviceConnected.value, isFalse);
      expect(server.deviceListUiState.value, DeviceListUiState.error);
      expect(server.userMessage.value, contains('开始连接失败'));
    });

    test('registerNotice consumes callback and sends rwcp enable packet',
        () async {
      server.isUpgrading.value = true;
      server.transFerComplete = true;
      server.mIsRWCPEnabled.value = true;

      await server.registerNotice();
      bleManager.latestNotifyListener?.call(<int>[0x01]);
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(bleManager.latestNotifyListener, isNotNull);
      expect(
          bleManager.writeWithResponsePayloads.length, greaterThanOrEqualTo(2));
      expect(server.logText.value, contains('收到通知'));
    });

    test('registerNotice enters fatal state when notify not ready in upgrade',
        () async {
      server.isUpgrading.value = true;
      bleManager.registerNotifyResult = false;
      await server.registerNotice();
      await Future<void>.delayed(const Duration(milliseconds: 20));

      expect(server.isUpgrading.value, isFalse);
      expect(bleManager.autoReconnectEnabledHistory, contains(false));
    });

    test('registerRWCP callback forwards bytes to rwcp client', () async {
      server.mIsRWCPEnabled.value = true;
      bleManager.isDeviceConnected = true;
      await server.registerRWCP();
      bleManager.latestRwcpListener?.call(<int>[0x01]);

      expect(bleManager.registerRwcpCalled, 1);
      expect(bleManager.latestRwcpListener, isNotNull);
    });

    test('startUpdate duplicate request is ignored', () async {
      server.isUpgrading.value = true;
      server.startUpdate();
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.logText.value, contains('正在升级中'));
    });

    test('startUpdate timer increments time count', () async {
      server.startUpdate();
      await Future<void>.delayed(const Duration(milliseconds: 1100));

      expect(server.timeCount.value, greaterThanOrEqualTo(1));
    });

    test('startUpdateWithVersionCheck success starts upgrade', () async {
      server.isDeviceConnected.value = true;
      server.startUpdateWithVersionCheck();
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[0x56, 0x31],
      ));
      await Future<void>.delayed(const Duration(milliseconds: 80));

      expect(server.versionBeforeUpgrade.value, contains('V1'));
      expect(server.isUpgrading.value, isTrue);
    });

    test('startUpdateWithVersionCheck failure still starts upgrade', () async {
      server.isDeviceConnected.value = true;
      server.startUpdateWithVersionCheck();
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeError,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[GAIA.incorrectState],
      ));
      await Future<void>.delayed(const Duration(milliseconds: 80));

      expect(server.versionBeforeUpgrade.value, 'UNKNOWN');
      expect(server.isUpgrading.value, isTrue);
    });

    test('startUpdateWithVersionCheck ignores duplicate when upgrading',
        () async {
      server.isUpgrading.value = true;
      server.startUpdateWithVersionCheck();
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.logText.value, contains('正在升级中'));
    });

    test('queryApplicationVersion duplicate call and timeout', () async {
      var failedCount = 0;
      server.isDeviceConnected.value = true;
      server.queryApplicationVersion(
        tag: 'A',
        onSuccess: (_) {},
        onFailed: () => failedCount += 1,
      );
      server.queryApplicationVersion(
        tag: 'B',
        onSuccess: (_) {},
        onFailed: () => failedCount += 10,
      );
      await Future<void>.delayed(
          Duration(seconds: OtaServer.kVersionQueryTimeoutSeconds + 1));

      expect(failedCount, 1);
    });

    test('application version parser returns hex for non printable payload',
        () async {
      var version = '';
      server.isDeviceConnected.value = true;
      server.queryApplicationVersion(
        tag: 'hex',
        onSuccess: (v) => version = v,
        onFailed: () {},
      );
      server.onApplicationVersionAckV3(<int>[0x01, 0x02, 0x03]);
      await Future<void>.delayed(Duration.zero);

      expect(version, startsWith('HEX:'));
    });

    test('startUpgradeProcess handles first start and duplicate call',
        () async {
      await File(firmwarePath).writeAsBytes(List<int>.generate(24, (i) => i));
      server.firmwarePath.value = firmwarePath;
      server.startUpgradeProcess();
      await Future<void>.delayed(const Duration(milliseconds: 120));
      final firstState = server.isUpgrading.value;

      server.startUpgradeProcess();
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(firstState, isTrue);
      expect(server.logText.value, contains('正在升级'));
    });

    test('stopUpgrade sends disconnect when connected', () async {
      server.isUpgrading.value = true;
      server.isDeviceConnected.value = true;
      server.connectDeviceId = 'device-1';

      await server.stopUpgrade(sendAbort: false, sendDisconnect: true);
      await Future<void>.delayed(const Duration(milliseconds: 120));

      final hasDisconnect = bleManager.writeWithResponsePayloads.any((bytes) {
        final packet = GaiaPacketBLE.fromByte(bytes);
        return packet?.getCommand() == cmdBuilder.upgradeDisconnectCommand();
      });
      expect(hasDisconnect, isTrue);
    });

    test('loadFirmwareFile uses default path fallback and rejects empty file',
        () async {
      await File(firmwarePath).writeAsBytes(<int>[]);
      server.firmwarePath.value = '';
      final loaded = await server.loadFirmwareFile();

      expect(loaded, isFalse);
      expect(server.firmwarePath.value, firmwarePath);
    });

    test('DFU commit ack triggers result timeout fallback', () async {
      server.isUpgrading.value = true;
      server.sendDfuCommit();
      server.onDfuCommitAck();
      await Future<void>.delayed(
          Duration(seconds: OtaServer.kDfuResultQueryTimeoutSeconds + 1));

      expect(server.isUpgrading.value, isFalse);
    });

    test('DFU result ack with short payload treated as success', () async {
      server.isUpgrading.value = true;
      server.onDfuGetResultAck(
          GaiaPacketBLE(GAIA.commandDfuGetResult, mPayload: <int>[0x00]));
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
    });

    test('sendVMUPacket rwcp path and sendVmuPacket delegate', () async {
      server.mIsRWCPEnabled.value = true;
      server.mTransferStartTime = 0;
      server.sendVMUPacket(
          VMUPacket.get(OpCodes.upgradeData, data: <int>[0x00, 0xAA]), true);
      server.sendVmuPacket(VMUPacket.get(OpCodes.upgradeAbortReq), false);
      await Future<void>.delayed(Duration.zero);

      expect(server.mTransferStartTime, greaterThan(0));
      expect(bleManager.writeWithoutResponsePayloads, isNotEmpty);
      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('receiveVMUPacket handles parse fail and non-upgrading packet',
        () async {
      server.receiveVMUPacket(<int>[0x01]);
      server.isUpgrading.value = false;
      server
          .receiveVMUPacket(VMUPacket.get(OpCodes.upgradeCommitReq).getBytes());
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(server.logText.value, contains('无法解析VMU包'));
      expect(
          server.logText.value,
          contains(
              'receiveVMUPacket Received VMU packet while application is not upgrading anymore'));
    });

    test('data request clamps offset and rwcp loop drains bytes', () async {
      server.isUpgrading.value = true;
      server.mIsRWCPEnabled.value = true;
      server.mBytesFile = List<int>.generate(6, (i) => i);
      server.mMaxLengthForDataTransfer = 4;

      server.onRequestNextDataPacket(5, 0);
      await Future<void>.delayed(Duration.zero);
      expect(server.mBytesToSend, 0);

      server.onRequestNextDataPacket(3, 99);
      expect(server.mStartOffset, 6);
    });

    test('sendNextDataPacket while idle stops upgrade flow', () async {
      server.isUpgrading.value = false;
      server.sendNextDataPacket();
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
    });

    test('onFileUploadProgress uses progress queue in rwcp mode', () {
      server.mIsRWCPEnabled.value = true;
      server.mBytesFile = List<int>.filled(10, 1);
      server.mStartOffset = 5;
      server.onFileUploadProgress();

      expect(server.mProgressQueue, isNotEmpty);
    });

    test('successful transmission requests next data in non-rwcp mode',
        () async {
      server.isUpgrading.value = true;
      server.mIsRWCPEnabled.value = false;
      server.mBytesFile = List<int>.generate(20, (i) => i);
      server.resetUpload();
      server.receiveVMUPacket(VMUPacket.get(OpCodes.upgradeSyncCfm,
          data: <int>[ResumePoints.dataTransfer, 0, 0, 0, 0, 0]).getBytes());
      server.receiveVMUPacket(
          VMUPacket.get(OpCodes.upgradeStartCfm, data: <int>[0x00]).getBytes());
      server.mBytesToSend = 4;
      server.mStartOffset = 0;
      server.onSuccessfulTransmission();
      await Future<void>.delayed(Duration.zero);

      expect(server.sendPkgCount, greaterThan(0));
    });

    test('rwcp not supported and warning confirmation stop upgrade', () async {
      server.isUpgrading.value = true;
      server.onRWCPNotSupported();
      await Future<void>.delayed(Duration.zero);
      expect(server.isUpgrading.value, isFalse);

      server.isUpgrading.value = true;
      server.askForConfirmation(ConfirmationType.warningFileIsDifferent);
      await Future<void>.delayed(Duration.zero);
      expect(server.isUpgrading.value, isFalse);
    });

    test('transfer callbacks and clearLog update observable states', () async {
      server.mProgressQueue.add(10.0);
      server.mProgressQueue.add(20.0);
      server.onTransferFinished();
      expect(server.mProgressQueue, isEmpty);

      server.isUpgrading.value = true;
      server.onTransferFailed();
      await Future<void>.delayed(Duration.zero);
      expect(server.isUpgrading.value, isFalse);
      expect(bleManager.autoReconnectEnabledHistory, contains(false));

      server.onUpgradeProgress(77.7);
      expect(server.updatePer.value, 77.7);

      server.addLog('temp');
      await Future<void>.delayed(const Duration(milliseconds: 150));
      server.clearLog();
      expect(server.logText.value, isEmpty);
    });

    test('gaia and rwcp write logging branches run without exceptions',
        () async {
      await server.writeData(<int>[0x01]);

      server.sendPkgCount = 49;
      server.sendData(false, <int>[0x11, 0x22]);

      await server.writeMsgRWCP(
          Segment.get(RWCPOpCodeClient.data, 1, payload: <int>[0x99])
              .getBytes());

      final vmu = VMUPacket.get(OpCodes.upgradeData, data: <int>[0x01, 0xAB]);
      final controlPacket = GaiaPacketBLE(
        cmdBuilder.upgradeControlCommand(),
        mPayload: vmu.getBytes(),
      ).getBytes();
      await server.writeMsgRWCP(
          Segment.get(RWCPOpCodeClient.data, 2, payload: controlPacket)
              .getBytes());

      final connectPacket =
          GaiaPacketBLE(cmdBuilder.upgradeConnectCommand()).getBytes();
      await server.writeMsgRWCP(
          Segment.get(RWCPOpCodeClient.data, 3, payload: connectPacket)
              .getBytes());

      expect(bleManager.writeWithoutResponsePayloads.length,
          greaterThanOrEqualTo(3));
    });

    test('quick recovery branch handles no device id and attempt limit',
        () async {
      server.connectDeviceId = '';
      for (var i = 0; i < 4; i++) {
        server.quickRecoverNow();
        await Future<void>.delayed(const Duration(milliseconds: 120));
      }

      expect(server.recoveryStatusText.value, '恢复受限');
    });

    test('quick recovery sends abort when connected but idle', () async {
      server.connectDeviceId = '';
      server.isDeviceConnected.value = true;
      server.quickRecoverNow();
      await Future<void>.delayed(const Duration(milliseconds: 380));

      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('quick recovery catches disconnect exception', () async {
      server.connectDeviceId = '';
      bleManager.throwOnDisconnect = true;
      server.quickRecoverNow();
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.recoveryStatusText.value, '恢复失败');
    });

    test('quick recovery reconnect path triggers connect call', () async {
      server.connectDeviceId = 'device-1';
      server.quickRecoverNow();
      await Future<void>.delayed(
          Duration(seconds: OtaServer.kRecoveryDelaySeconds + 1));

      expect(bleManager.latestOnConnected, isNotNull);
    });

    test('post-upgrade version query waits for reconnect when disconnected',
        () async {
      server.isDeviceConnected.value = false;
      server.isUpgrading.value = true;
      server.onUpgradeComplete();
      await Future<void>.delayed(Duration(
          seconds: OtaServer.kPostUpgradeVersionRetryIntervalSeconds + 1));

      expect(server.versionAfterUpgrade.value, 'UNKNOWN');
    });

    test('post-upgrade version compare logs unchanged result', () async {
      server.versionBeforeUpgrade.value = 'V1 (HEX:5631)';
      server.isDeviceConnected.value = true;
      server.isUpgrading.value = true;
      server.onUpgradeComplete();
      await Future<void>.delayed(Duration(
          seconds: OtaServer.kPostUpgradeVersionRetryIntervalSeconds + 1));
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[0x56, 0x31],
      ));
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(server.logText.value, contains('版本对比结果：未变化'));
    });

    test('post-upgrade version compare logs changed result', () async {
      server.versionBeforeUpgrade.value = 'V1 (HEX:5631)';
      server.isDeviceConnected.value = true;
      server.isUpgrading.value = true;
      server.onUpgradeComplete();
      await Future<void>.delayed(Duration(
          seconds: OtaServer.kPostUpgradeVersionRetryIntervalSeconds + 1));
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[0x56, 0x32],
      ));
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(server.logText.value, contains('版本对比结果：已变化'));
    });

    test('post-upgrade version compare logs insufficient info', () async {
      server.versionBeforeUpgrade.value = 'UNKNOWN';
      server.isDeviceConnected.value = true;
      server.isUpgrading.value = true;
      server.onUpgradeComplete();
      await Future<void>.delayed(Duration(
          seconds: OtaServer.kPostUpgradeVersionRetryIntervalSeconds + 1));
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureFramework,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdAppVersion,
        payload: <int>[0x56, 0x33],
      ));
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(server.logText.value, contains('版本对比结果：信息不足'));
    });

    test('stopScan shows ready when device list is not empty', () async {
      server.devices.add(discoveredDevice('stop-scan-dev'));
      await server.stopScan();

      expect(server.deviceListUiState.value, DeviceListUiState.ready);
      expect(server.deviceListHint.value, '已停止扫描');
    });

    test('default firmware resolver success and default manager callback path',
        () async {
      server.onClose();
      final docsDir = await Directory.systemTemp.createTemp('gaia_docs_');
      PathProviderPlatform.instance = _FakePathProviderPlatform(docsDir.path);
      reactive_ble.ReactiveBlePlatform.instance = _FakeReactiveBlePlatform();
      final defaultServer = OtaServer();
      defaultServer.onInit();
      await Future<void>.delayed(const Duration(milliseconds: 200));

      defaultServer.bleManager.onConnectionStateChanged
          ?.call(DeviceConnectionState.connecting, 'device-connect');
      defaultServer.bleManager.onConnectionStateChanged
          ?.call(DeviceConnectionState.connected, 'device-ready');

      expect(defaultServer.firmwarePath.value, '${docsDir.path}/1.bin');
      expect(defaultServer.connectDeviceId, 'device-ready');
      defaultServer.onClose();
      await docsDir.delete(recursive: true);
    });

    test('default firmware resolver failure logs error', () async {
      server.onClose();
      final failingServer = OtaServer(
        bleManagerOverride: _CoverageBleConnectionManager(),
        defaultFirmwarePathResolver: () async => throw StateError('path fail'),
      );
      failingServer.onInit();
      await Future<void>.delayed(const Duration(milliseconds: 200));

      expect(failingServer.logText.value, contains('初始化默认固件路径失败'));
      failingServer.onClose();
    });

    test('upgrade control response and endpoint mode error branches', () async {
      server.isUpgrading.value = true;
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeResponse,
        commandId: GaiaCommandBuilder.v3CmdUpgradeControl,
      ));
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeError,
        commandId: GaiaCommandBuilder.v3CmdSetDataEndpointMode,
        payload: <int>[GAIA.incorrectState],
      ));
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
      expect(bleManager.autoReconnectEnabledHistory, contains(false));
    });

    test('upgrade control and disconnect error branches enter fatal state',
        () async {
      server.isUpgrading.value = true;
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeError,
        commandId: GaiaCommandBuilder.v3CmdUpgradeControl,
        payload: <int>[GAIA.incorrectState],
      ));
      server.isUpgrading.value = true;
      server.handleRecMsg(v3Packet(
        feature: GaiaCommandBuilder.v3FeatureUpgrade,
        packetType: GaiaCommandBuilder.v3PacketTypeError,
        commandId: GaiaCommandBuilder.v3CmdUpgradeDisconnect,
        payload: <int>[GAIA.incorrectState],
      ));
      await Future<void>.delayed(Duration.zero);

      expect(server.isUpgrading.value, isFalse);
    });

    test('DFU next packet covers commit and small-chunk branches', () async {
      server.isUpgrading.value = true;
      server.mBytesFile = List<int>.generate(4, (i) => i);
      server.mStartOffset = 4;
      server.sendNextDfuPacket();
      await Future<void>.delayed(Duration.zero);

      server.isUpgrading.value = true;
      server.mPayloadSizeMax = 10;
      server.mStartOffset = 0;
      server.mBytesFile = List<int>.generate(5, (i) => i);
      server.sendNextDfuPacket();
      await Future<void>.delayed(Duration.zero);

      expect(bleManager.writeWithResponsePayloads, isNotEmpty);
    });

    test('post-upgrade query timeout branch after max retries', () {
      fakeAsync((async) {
        server.isDeviceConnected.value = false;
        server.isUpgrading.value = true;
        server.onUpgradeComplete();
        async.elapse(Duration(
            seconds: OtaServer.kPostUpgradeVersionRetryIntervalSeconds *
                (OtaServer.kPostUpgradeVersionMaxRetries + 1)));
        async.elapse(const Duration(milliseconds: 200));
        async.flushMicrotasks();
      });

      expect(server.logText.value, contains('升级后版本查询超时'));
    });

    test('post-upgrade query onFailed branch after repeated timeouts', () {
      fakeAsync((async) {
        server.isDeviceConnected.value = true;
        server.isUpgrading.value = true;
        server.onUpgradeComplete();
        async.elapse(const Duration(seconds: 80));
        async.elapse(const Duration(milliseconds: 200));
        async.flushMicrotasks();
      });

      expect(server.logText.value, contains('升级后版本查询失败'));
    });

    test('sendVMUPacket rwcp failure and exception branches', () async {
      final controlled = _ControlledRWCPClient(server, sendReturns: false);
      server.mRWCPClient = controlled;
      server.mIsRWCPEnabled.value = true;
      server.sendVMUPacket(
          VMUPacket.get(OpCodes.upgradeData, data: <int>[0x00, 0x11]), true);
      await Future<void>.delayed(Duration.zero);

      controlled.throwOnSend = true;
      server.sendVMUPacket(
          VMUPacket.get(OpCodes.upgradeData, data: <int>[0x00, 0x22]), true);
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.logText.value, contains('Fail to send GAIA packet'));
      expect(server.logText.value, contains('Exception when attempting'));
    });

    test('receiveVMUPacket catch branch handles parser exception', () async {
      server.receiveVMUPacket(<int>[0x01, -1, -1]);
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.logText.value, contains('receiveVMUPacket'));
    });

    test('receiveVMUPacket catches state machine exception', () async {
      final throwingServer = OtaServer(
        bleManagerOverride: _CoverageBleConnectionManager(),
        upgradeStateMachineOverride: _ThrowingUpgradeStateMachine(
          delegate: server,
        ),
        defaultFirmwarePathResolver: () async => firmwarePath,
      );
      throwingServer.onInit();
      throwingServer.isUpgrading.value = true;
      final packet = VMUPacket.get(OpCodes.upgradeStartReq, data: <int>[0x00]);
      throwingServer.receiveVMUPacket(packet.getBytes());
      await Future<void>.delayed(const Duration(milliseconds: 150));

      expect(throwingServer.logText.value, contains('state machine failed'));
      throwingServer.onClose();
    });

    test('abortUpgrade cancels rwcp session when running', () {
      final controlled = _ControlledRWCPClient(server);
      controlled.mState = RWCPState.established;
      server.mRWCPClient = controlled;
      server.abortUpgrade();

      expect(controlled.cancelCalled, isTrue);
    });

    test('last packet path logs and marks final data packet', () async {
      server.isUpgrading.value = true;
      server.mIsRWCPEnabled.value = false;
      server.mBytesFile = <int>[0x01, 0x02, 0x03];
      server.mBytesToSend = 3;
      server.mStartOffset = 0;
      server.mMaxLengthForDataTransfer = 16;
      server.sendNextDataPacket();
      await Future<void>.delayed(const Duration(milliseconds: 120));

      expect(server.mBytesToSend, 0);
      expect(server.logText.value, contains('lastPackettrue'));
    });

    test('writeData trace logs and rwcp catch-upgrading branch', () async {
      await server.writeData(<int>[0x00, 0x1D, 0x0C, 0x00]);
      await Future<void>.delayed(const Duration(milliseconds: 150));
      expect(server.logText.value, contains('writeData start'));
      expect(server.logText.value, contains('writeData end'));

      server.isUpgrading.value = true;
      bleManager.throwWriteWithoutResponse = true;
      await server.writeMsgRWCP(<int>[0x80, 0x00]);
      await Future<void>.delayed(const Duration(milliseconds: 150));
      expect(bleManager.autoReconnectEnabledHistory, contains(false));
    });

    test('gaia and rwcp logging sample branches are covered', () async {
      server.mIsRWCPEnabled.value = false;
      server.sendPkgCount = 49;
      server.sendData(false, <int>[0x11, 0x22, 0x33]);
      await Future<void>.delayed(Duration.zero);

      final nonDataVmu =
          VMUPacket.get(OpCodes.upgradeAbortReq, data: <int>[0x01]);
      final controlPacket = GaiaPacketBLE(
        cmdBuilder.upgradeControlCommand(),
        mPayload: nonDataVmu.getBytes(),
      ).getBytes();
      await server.writeMsgRWCP(
          Segment.get(RWCPOpCodeClient.data, 6, payload: controlPacket)
              .getBytes());

      expect(bleManager.writeWithoutResponsePayloads, isNotEmpty);
    });

    test('watchdog timeout enters fatal state', () {
      fakeAsync((async) {
        server.startUpdate();
        async.elapse(
            Duration(seconds: OtaServer.kUpgradeWatchdogTimeoutSeconds + 1));
        async.flushMicrotasks();
      });

      expect(server.logText.value, contains('升级超时'));
    });

    test('error burst window reset and recovery in-progress branch', () {
      fakeAsync((async) {
        server.autoRecoveryEnabled.value = true;
        server.onUpgradeError('first');
        async.elapse(Duration(seconds: OtaServer.kErrorBurstWindowSeconds + 1));
        server.onUpgradeError('second');

        server.connectDeviceId = 'device-1';
        server.quickRecoverNow();
        server.quickRecoverNow();
        async.elapse(const Duration(milliseconds: 200));
        async.elapse(const Duration(milliseconds: 10));
        async.flushMicrotasks();
      });

      expect(server.logText.value, contains('恢复进行中，忽略重复触发'));
    });

    test('recovery window reset and upgrading-stop branch', () {
      fakeAsync((async) {
        server.isUpgrading.value = true;
        server.connectDeviceId = '';
        server.quickRecoverNow();
        async.flushMicrotasks();
        async.elapse(const Duration(milliseconds: 10));
        expect(server.isUpgrading.value, isFalse);

        server.quickRecoverNow();
        async.flushMicrotasks();
        async.elapse(
            Duration(minutes: OtaServer.kRecoveryWindowMinutes, seconds: 1));
        server.quickRecoverNow();
        async.flushMicrotasks();
      });
      expect(server.recoveryStatusText.value, isNot('恢复受限'));
    });

    test('recovery attempts reset after recovery window elapsed', () async {
      DateTime now = DateTime(2025, 1, 1, 0, 0, 0);
      final recoveryServer = OtaServer(
        bleManagerOverride: _CoverageBleConnectionManager(),
        defaultFirmwarePathResolver: () async => firmwarePath,
        nowProvider: () => now,
      );
      recoveryServer.onInit();
      recoveryServer.connectDeviceId = '';

      for (int i = 0; i < OtaServer.kMaxRecoveryAttemptsPerWindow + 1; i++) {
        recoveryServer.quickRecoverNow();
        await Future<void>.delayed(const Duration(milliseconds: 30));
      }
      expect(recoveryServer.recoveryStatusText.value, '恢复受限');

      now = now
          .add(Duration(minutes: OtaServer.kRecoveryWindowMinutes, seconds: 1));
      recoveryServer.quickRecoverNow();
      await Future<void>.delayed(const Duration(milliseconds: 30));

      expect(recoveryServer.recoveryStatusText.value, isNot('恢复受限'));
      recoveryServer.onClose();
    });

    test('sendRWCPSegment writes bytes and returns true', () {
      final ok = server.sendRWCPSegment(<int>[0x80, 0x00]);
      expect(ok, isTrue);
    });
  });
}
