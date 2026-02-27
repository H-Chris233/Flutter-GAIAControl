import 'package:gaia/utils/gaia/confirmation_type.dart';
import 'package:gaia/utils/gaia/op_codes.dart';
import 'package:gaia/utils/gaia/resume_points.dart';
import 'package:gaia/utils/gaia/upgrade_start_cfm_status.dart';
import 'package:gaia/utils/gaia/vmu_packet.dart';
import 'dart:async';

/// 升级状态枚举
enum UpgradeState {
  /// 空闲状态
  idle,

  /// 同步中
  syncing,

  /// 启动中
  starting,

  /// 数据传输中
  transferring,

  /// 校验中
  validating,

  /// 提交中
  committing,

  /// 升级完成
  complete,

  /// 错误状态
  error,
}

/// 升级状态机委托接口
///
/// 用于状态机与外部组件（如 OtaServer）通信
abstract class UpgradeStateMachineDelegate {
  /// 发送 VMU 数据包
  void sendVmuPacket(VMUPacket packet, bool isTransferringData);

  /// 升级进度更新
  void onUpgradeProgress(double percent);

  /// 升级完成
  void onUpgradeComplete();

  /// 升级错误
  void onUpgradeError(String reason);

  /// 请求发送下一个数据包
  /// @param moveBy
  ///        DATA_BYTES_REQ 的第二个 u32 字段（Big-Endian），语义为 move_by（相对当前游标的移动量）。
  void onRequestNextDataPacket(int bytesToSend, int moveBy);

  /// 请求确认
  void onRequestConfirmation(int confirmationType);

  /// 日志输出
  void onLog(String message);
}

/// VMU 包处理结果
class VmuPacketResult {
  final bool success;
  final String? errorMessage;
  final UpgradeState? nextState;

  VmuPacketResult({
    required this.success,
    this.errorMessage,
    this.nextState,
  });

  factory VmuPacketResult.success({UpgradeState? nextState}) {
    return VmuPacketResult(success: true, nextState: nextState);
  }

  factory VmuPacketResult.error(String message) {
    return VmuPacketResult(success: false, errorMessage: message);
  }
}

/// 升级状态机
///
/// 负责管理 OTA 升级的状态流转和 VMU 包处理。
class UpgradeStateMachine {
  /// 当前状态
  UpgradeState state = UpgradeState.idle;

  /// 升级协议版本（来自 SYNC_CFM 的 PROTOCOL_VERSION 字段）。
  ///
  /// 参考 gaia-client-src/lib-upgrade：当前常见版本为 4/5/6。
  int protocolVersion = 0;

  /// 设备是否支持 Silent Commit（由 upgradeSilentCommitSupportedCfm 返回）。
  bool silentCommitSupported = false;

  /// 是否已收到 Silent Commit 支持性确认。
  bool isSilentCommitSupportKnown = false;

  /// 协议版本上限（与官方 lib-upgrade 对齐：V6）。
  static const int maxSupportedProtocolVersion = 0x06;

  /// Silent Commit 能力探测从协议版本 V4 开始支持。
  static const int _kProtocolVersionSilentCommit = 0x04;

  /// 委托对象
  final UpgradeStateMachineDelegate delegate;

  /// 断点续传恢复点
  int resumePoint = -1;

  /// 启动重试次数
  int startAttempts = 0;

  /// 最大启动重试次数
  static const int maxStartNotReadyRetries = 5;

  static const Duration _kStartNotReadyRetryDelay = Duration(seconds: 2);

  bool _waitingSilentCommitSupportedCfm = false;

  /// 传输是否完成
  bool transferComplete = false;

  /// 是否是最后一个包
  bool wasLastPacket = false;

  /// 是否需要中止
  bool hasToAbort = false;

  Timer? _validationPollTimer;

  /// 构造函数
  UpgradeStateMachine({required this.delegate});

  /// 重置状态机
  void reset() {
    _validationPollTimer?.cancel();
    _validationPollTimer = null;
    state = UpgradeState.idle;
    protocolVersion = 0;
    silentCommitSupported = false;
    isSilentCommitSupportKnown = false;
    _waitingSilentCommitSupportedCfm = false;
    resumePoint = -1;
    startAttempts = 0;
    transferComplete = false;
    wasLastPacket = false;
    hasToAbort = false;
  }

  /// 开始升级流程
  void startUpgrade() {
    state = UpgradeState.syncing;
    transferComplete = false;
    startAttempts = 0;
    wasLastPacket = false;
    // 发送 SYNC_REQ 由外部触发
  }

  /// 处理 VMU 数据包
  void handleVmuPacket(VMUPacket? packet) {
    if (packet == null) {
      return;
    }

    switch (packet.mOpCode) {
      case OpCodes.upgradeSyncCfm:
        _handleSyncCfm(packet);
        break;
      case OpCodes.upgradeStartCfm:
        _handleStartCfm(packet);
        break;
      case OpCodes.upgradeDataBytesReq:
        _handleDataBytesReq(packet);
        break;
      case OpCodes.upgradeAbortCfm:
        _handleAbortCfm();
        break;
      case OpCodes.upgradeErrorWarnInd:
        _handleErrorWarnInd(packet);
        break;
      case OpCodes.upgradeIsValidationDoneCfm:
        _handleValidationDoneCfm(packet);
        break;
      case OpCodes.upgradeTransferCompleteInd:
        _handleTransferCompleteInd();
        break;
      case OpCodes.upgradeCommitReq:
        _handleCommitReq();
        break;
      case OpCodes.upgradeCompleteInd:
        _handleCompleteInd();
        break;
      case OpCodes.upgradeSilentCommitSupportedCfm:
        _handleSilentCommitSupportedCfm(packet);
        break;
      case OpCodes.upgradeSilentCommitCfm:
        _handleSilentCommitCfm();
        break;
      case OpCodes.upgradePutEarbudsInCaseReq:
        _handlePutEarbudsInCaseReq();
        break;
      case OpCodes.upgradeEarbudsInCaseCfm:
        _handleEarbudsInCaseCfm();
        break;
      case OpCodes.upgradeCompleteIndWithStatus:
        _handleCompleteIndWithStatus(packet);
        break;
    }
  }

  /// 处理 SYNC_CFM
  void _handleSyncCfm(VMUPacket packet) {
    final data = packet.mData ?? [];
    if (data.length >= 6) {
      final step = data[0];
      resumePoint = step;
      protocolVersion = data[5] & 0xFF;
      delegate.onLog(
          "SYNC_CFM: resumePoint=$step protocolVersion=$protocolVersion");
      if (protocolVersion > maxSupportedProtocolVersion) {
        state = UpgradeState.error;
        delegate.onUpgradeError(
            "不支持的升级协议版本: v$protocolVersion(最大支持v$maxSupportedProtocolVersion)");
        return;
      }
    } else {
      if (resumePoint < 0) {
        resumePoint = ResumePoints.dataTransfer;
      }
      delegate.onLog("SYNC_CFM 数据不足，继续沿用断点 step=$resumePoint");
    }
    state = UpgradeState.starting;
    // 发送 START_REQ
    final startReqPacket = VMUPacket.get(OpCodes.upgradeStartReq);
    delegate.sendVmuPacket(startReqPacket, false);
  }

  /// 处理 START_CFM
  void _handleStartCfm(VMUPacket packet) {
    final data = packet.mData ?? [];
    if (data.isEmpty) {
      state = UpgradeState.error;
      delegate.onUpgradeError("upgradeStartCfm 数据为空");
      return;
    }

    final status = data[0];
    if (status == UpgradeStartCFMStatus.success) {
      startAttempts = 0;
      _proceedBasedOnResumePoint();
      return;
    }

    if (status == UpgradeStartCFMStatus.errorAppNotReady) {
      delegate.onLog("设备应用未就绪(0x09)，准备重试");
      if (startAttempts < maxStartNotReadyRetries) {
        startAttempts += 1;
        delegate.onLog(
            "START_CFM未就绪：第$startAttempts/$maxStartNotReadyRetries次重试，延迟${_kStartNotReadyRetryDelay.inMilliseconds}ms");
        // 延迟后重新发送 START_REQ
        Future<void>.delayed(_kStartNotReadyRetryDelay, () {
          if (state == UpgradeState.starting) {
            final startReqPacket = VMUPacket.get(OpCodes.upgradeStartReq);
            delegate.sendVmuPacket(startReqPacket, false);
          }
        });
      } else {
        state = UpgradeState.error;
        delegate
            .onUpgradeError("设备持续未就绪(0x09)，超过重试上限($maxStartNotReadyRetries)");
      }
      return;
    }

    state = UpgradeState.error;
    delegate
        .onUpgradeError("upgradeStartCfm 异常状态: 0x${status.toRadixString(16)}");
  }

  /// 根据恢复点继续升级
  void _proceedBasedOnResumePoint() {
    switch (resumePoint) {
      case ResumePoints.commit:
        delegate.onRequestConfirmation(ConfirmationType.commit);
        break;
      case ResumePoints.transferComplete:
        if (protocolVersion >= _kProtocolVersionSilentCommit) {
          _sendSilentCommitSupportedReq();
        } else {
          delegate.onRequestConfirmation(ConfirmationType.transferComplete);
        }
        break;
      case ResumePoints.inProgress:
        delegate.onRequestConfirmation(ConfirmationType.inProgress);
        break;
      case ResumePoints.validation:
        state = UpgradeState.validating;
        final validationPacket =
            VMUPacket.get(OpCodes.upgradeIsValidationDoneReq);
        delegate.sendVmuPacket(validationPacket, false);
        break;
      case ResumePoints.postCommit:
        // 对齐 gaia-client-src/lib-upgrade：POST_COMMIT 阶段无需主动发送任何 Upgrade 消息，
        // 等待设备下发 COMPLETE_IND/COMPLETE_IND_WITH_STATUS。
        state = UpgradeState.committing;
        delegate.onLog("ResumePoint=POST_COMMIT，等待设备发送完成指示");
        break;
      case ResumePoints.dataTransfer:
      default:
        state = UpgradeState.transferring;
        resumePoint = ResumePoints.dataTransfer;
        final startDataPacket = VMUPacket.get(OpCodes.upgradeStartDataReq);
        delegate.sendVmuPacket(startDataPacket, false);
        break;
    }
  }

  /// 处理 DATA_BYTES_REQ
  void _handleDataBytesReq(VMUPacket packet) {
    final data = packet.mData ?? [];
    if (data.length != OpCodes.dataLength) {
      delegate.onLog("UpgradeError 数据传输失败");
      _sendAbortReq();
      return;
    }

    // 解析请求的字节数和偏移量
    // 约定：多字节整数为 Big-Endian。
    final bytesToSend = _extractIntFromByteArray(data, 0, 4);
    final moveBy = _extractInt32SignedFromByteArray(data, 4);

    delegate.onLog("DATA_BYTES_REQ: moveBy=$moveBy bytesToSend=$bytesToSend");
    delegate.onRequestNextDataPacket(bytesToSend, moveBy);
  }

  /// 处理 ABORT_CFM
  void _handleAbortCfm() {
    delegate.onLog("receiveAbortCFM");
    state = UpgradeState.idle;
  }

  /// 处理 ERROR_WARN_IND
  void _handleErrorWarnInd(VMUPacket packet) {
    final data = packet.mData ?? [];
    if (data.length < 2) {
      delegate.onLog("receiveErrorWarnIND 升级失败，设备返回异常：错误码长度不足");
      state = UpgradeState.error;
      delegate.onUpgradeError("设备返回升级异常：错误码长度不足");
      return;
    }

    // 发送错误确认
    final errorConfirmPacket =
        VMUPacket.get(OpCodes.upgradeErrorWarnRes, data: data);
    delegate.sendVmuPacket(errorConfirmPacket, false);

    int returnCode = _extractIntFromByteArray(data, 0, 2);
    delegate
        .onLog("receiveErrorWarnIND 升级失败 错误码0x${returnCode.toRadixString(16)}");

    if (returnCode == 0x81) {
      delegate.onLog("包不通过，固件文件与设备不匹配");
      delegate.onRequestConfirmation(ConfirmationType.warningFileIsDifferent);
    } else if (returnCode == 0x21) {
      delegate.onLog("设备电量过低，停止升级");
      state = UpgradeState.error;
      delegate.onUpgradeError("设备电量过低");
    } else if (returnCode == 0x23) {
      state = UpgradeState.error;
      delegate.onUpgradeError("设备处于错误状态(0x23)，建议断开重连后重新开始升级");
    } else {
      state = UpgradeState.error;
      delegate.onUpgradeError("设备返回升级错误码0x${returnCode.toRadixString(16)}");
    }
  }

  /// 处理 VALIDATION_DONE_CFM
  void _handleValidationDoneCfm(VMUPacket packet) {
    delegate.onLog("receiveValidationDoneCFM");
    final data = packet.mData ?? [];
    // 对齐 gaia-client-src/lib-upgrade:
    // - payload >= 2 时，按 WAITING_TIME(2 bytes, Big-Endian) 延迟再发送下一次轮询请求。
    // - payload 不足时，立即发送下一次轮询请求。
    if (data.length < 2) {
      final validationPacket =
          VMUPacket.get(OpCodes.upgradeIsValidationDoneReq);
      delegate.sendVmuPacket(validationPacket, false);
      return;
    }

    _validationPollTimer?.cancel();
    final delayMs = _extractIntFromByteArray(data, 0, 2);
    _validationPollTimer = Timer(Duration(milliseconds: delayMs), () {
      if (state != UpgradeState.validating) {
        return;
      }
      final validationPacket =
          VMUPacket.get(OpCodes.upgradeIsValidationDoneReq);
      delegate.sendVmuPacket(validationPacket, false);
    });
  }

  /// 处理 TRANSFER_COMPLETE_IND
  void _handleTransferCompleteInd() {
    delegate.onLog("receiveTransferCompleteIND");
    transferComplete = true;
    resumePoint = ResumePoints.transferComplete;
    if (protocolVersion >= _kProtocolVersionSilentCommit) {
      _sendSilentCommitSupportedReq();
    } else {
      delegate.onRequestConfirmation(ConfirmationType.transferComplete);
    }
  }

  /// 处理 COMMIT_REQ
  void _handleCommitReq() {
    delegate.onLog("receiveCommitREQ");
    state = UpgradeState.committing;
    resumePoint = ResumePoints.commit;
    delegate.onRequestConfirmation(ConfirmationType.commit);
  }

  /// 处理 COMPLETE_IND
  void _handleCompleteInd() {
    state = UpgradeState.complete;
    delegate.onLog("receiveCompleteIND 升级完成");
    delegate.onUpgradeComplete();
  }

  void _handleCompleteIndWithStatus(VMUPacket packet) {
    final data = packet.mData ?? [];
    int? status;
    if (data.length >= 2) {
      status = _extractIntFromByteArray(data, 0, 2);
    }
    state = UpgradeState.complete;
    delegate.onLog(
        "receiveCompleteINDWithStatus 升级完成${status == null ? '' : ' status=0x${status.toRadixString(16)}'}");
    delegate.onUpgradeComplete();
  }

  void _sendSilentCommitSupportedReq() {
    if (_waitingSilentCommitSupportedCfm) {
      return;
    }
    _waitingSilentCommitSupportedCfm = true;
    isSilentCommitSupportKnown = false;
    silentCommitSupported = false;
    delegate.onLog("请求设备返回SilentCommit支持性(协议v$protocolVersion)");
    final req = VMUPacket.get(OpCodes.upgradeSilentCommitSupportedReq);
    delegate.sendVmuPacket(req, false);
  }

  void _handleSilentCommitSupportedCfm(VMUPacket packet) {
    final data = packet.mData ?? [];
    final support = data.isNotEmpty ? (data[0] & 0xFF) : 0x00;
    silentCommitSupported = support == 0x01;
    isSilentCommitSupportKnown = true;
    _waitingSilentCommitSupportedCfm = false;
    delegate.onLog(
        "SilentCommit支持性确认: ${silentCommitSupported ? 'SUPPORTED' : 'NOT_SUPPORTED'}");
    // 对齐官方：获取支持性结果后，再发起 TRANSFER_COMPLETE 的确认流程。
    delegate.onRequestConfirmation(ConfirmationType.transferComplete);
  }

  void _handleSilentCommitCfm() {
    // 对齐官方：Silent Commit 完成后不一定再收到 COMPLETE_IND，因此将其视为一次正常结束。
    state = UpgradeState.complete;
    delegate.onLog("receiveSilentCommitCFM 升级完成(Silent Commit)");
    delegate.onUpgradeComplete();
  }

  void _handlePutEarbudsInCaseReq() {
    delegate.onLog("收到PUT_EARBUDS_IN_CASE_REQ：请提示用户将耳机放回充电盒并合盖以继续升级");
  }

  void _handleEarbudsInCaseCfm() {
    delegate.onLog("收到EARBUDS_IN_CASE_CFM：设备确认动作完成，升级可继续");
  }

  /// 发送中止请求
  void _sendAbortReq() {
    final abortPacket = VMUPacket.get(OpCodes.upgradeAbortReq);
    delegate.sendVmuPacket(abortPacket, false);
  }

  /// 处理成功传输
  void onSuccessfulTransmission() {
    if (wasLastPacket) {
      if (resumePoint == ResumePoints.dataTransfer) {
        wasLastPacket = false;
        resumePoint = ResumePoints.validation;
        state = UpgradeState.validating;
        final validationPacket =
            VMUPacket.get(OpCodes.upgradeIsValidationDoneReq);
        delegate.sendVmuPacket(validationPacket, false);
      }
    } else if (hasToAbort) {
      hasToAbort = false;
      _sendAbortReq();
    }
  }

  /// 设置最后一个包标志
  void setWasLastPacket(bool value) {
    wasLastPacket = value;
  }

  /// 从字节数组提取整数
  int _extractIntFromByteArray(List<int> source, int offset, int length) {
    int result = 0;
    for (int i = 0; i < length; i++) {
      result = (result << 8) | (source[offset + i] & 0xFF);
    }
    return result;
  }

  int _extractInt32SignedFromByteArray(List<int> source, int offset) {
    final raw = _extractIntFromByteArray(source, offset, 4) & 0xFFFFFFFF;
    if ((raw & 0x80000000) != 0) {
      return raw - 0x100000000;
    }
    return raw;
  }
}
