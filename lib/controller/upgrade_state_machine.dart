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

  /// 委托对象
  final UpgradeStateMachineDelegate delegate;

  /// 断点续传恢复点
  int resumePoint = -1;

  /// 启动重试次数
  int startAttempts = 0;

  /// 最大启动重试次数
  static const int maxStartNotReadyRetries = 3;

  /// 传输是否完成
  bool transferComplete = false;

  /// 是否是最后一个包
  bool wasLastPacket = false;

  /// 是否需要中止
  bool hasToAbort = false;

  static const int _kValidationPollMinDelayMs = 80;
  static const int _kValidationPollMaxDelayMs = 5000;
  Timer? _validationPollTimer;

  /// 构造函数
  UpgradeStateMachine({required this.delegate});

  /// 重置状态机
  void reset() {
    _validationPollTimer?.cancel();
    _validationPollTimer = null;
    state = UpgradeState.idle;
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
    }
  }

  /// 处理 SYNC_CFM
  void _handleSyncCfm(VMUPacket packet) {
    final data = packet.mData ?? [];
    if (data.length >= 6) {
      int step = data[0];
      delegate.onLog("上次传输步骤 step $step");
      resumePoint = step;
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
      startAttempts += 1;
      delegate.onLog("设备应用未就绪(0x09)，第$startAttempts次重试");
      if (startAttempts <= maxStartNotReadyRetries) {
        // 延迟后重新发送 START_REQ
        Future<void>.delayed(const Duration(milliseconds: 500), () {
          if (state == UpgradeState.starting) {
            final startReqPacket = VMUPacket.get(OpCodes.upgradeStartReq);
            delegate.sendVmuPacket(startReqPacket, false);
          }
        });
      } else {
        state = UpgradeState.error;
        delegate.onUpgradeError("设备持续未就绪(0x09)，超过重试上限");
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
        delegate.onRequestConfirmation(ConfirmationType.transferComplete);
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
    final moveBy = _extractIntFromByteArray(data, 4, 4);

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
    if (data.length < 2) {
      // 兼容旧行为：当 payload 不包含延迟信息时，立即发起下一次轮询。
      // 同时用最小间隔做节流，避免异常重复通知导致瞬时刷屏。
      if (_validationPollTimer != null) {
        return;
      }
      final validationPacket =
          VMUPacket.get(OpCodes.upgradeIsValidationDoneReq);
      delegate.sendVmuPacket(validationPacket, false);
      _validationPollTimer =
          Timer(const Duration(milliseconds: _kValidationPollMinDelayMs), () {
        _validationPollTimer = null;
      });
      return;
    }

    _validationPollTimer?.cancel();
    _validationPollTimer = null;
    var delayMs = _kValidationPollMinDelayMs;
    delayMs = _extractIntFromByteArray(data, 0, 2);
    if (delayMs < _kValidationPollMinDelayMs) {
      delayMs = _kValidationPollMinDelayMs;
    } else if (delayMs > _kValidationPollMaxDelayMs) {
      delayMs = _kValidationPollMaxDelayMs;
    }
    _validationPollTimer = Timer(Duration(milliseconds: delayMs), () {
      _validationPollTimer = null;
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
    delegate.onRequestConfirmation(ConfirmationType.transferComplete);
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
}
