import 'package:flutter_test/flutter_test.dart';
import 'package:fake_async/fake_async.dart';
import 'package:gaia/controller/upgrade_state_machine.dart';
import 'package:gaia/utils/gaia/confirmation_type.dart';
import 'package:gaia/utils/gaia/op_codes.dart';
import 'package:gaia/utils/gaia/resume_points.dart';
import 'package:gaia/utils/gaia/upgrade_start_cfm_status.dart';
import 'package:gaia/utils/gaia/vmu_packet.dart';

class MockUpgradeDelegate implements UpgradeStateMachineDelegate {
  final List<String> logs = [];
  final List<VMUPacket> sentPackets = [];
  int? lastConfirmationType;
  int? lastBytesToSend;
  int? lastStartOffset;
  double? lastProgress;
  bool upgradeCompleted = false;
  String? upgradeError;

  @override
  void onLog(String message) {
    logs.add(message);
  }

  @override
  void sendVmuPacket(VMUPacket packet, bool isTransferringData) {
    sentPackets.add(packet);
  }

  @override
  void onRequestConfirmation(int confirmationType) {
    lastConfirmationType = confirmationType;
  }

  @override
  void onRequestNextDataPacket(int bytesToSend, int startOffset) {
    lastBytesToSend = bytesToSend;
    lastStartOffset = startOffset;
  }

  @override
  void onUpgradeProgress(double percent) {
    lastProgress = percent;
  }

  @override
  void onUpgradeComplete() {
    upgradeCompleted = true;
  }

  @override
  void onUpgradeError(String reason) {
    upgradeError = reason;
  }

  void reset() {
    logs.clear();
    sentPackets.clear();
    lastConfirmationType = null;
    lastBytesToSend = null;
    lastStartOffset = null;
    lastProgress = null;
    upgradeCompleted = false;
    upgradeError = null;
  }
}

void main() {
  group('UpgradeStateMachine', () {
    late UpgradeStateMachine machine;
    late MockUpgradeDelegate delegate;

    setUp(() {
      delegate = MockUpgradeDelegate();
      machine = UpgradeStateMachine(delegate: delegate);
    });

    test('initial state is idle', () {
      expect(machine.state, UpgradeState.idle);
    });

    test('reset clears all state', () {
      machine.state = UpgradeState.transferring;
      machine.resumePoint = ResumePoints.validation;
      machine.startAttempts = 3;
      machine.transferComplete = true;

      machine.reset();

      expect(machine.state, UpgradeState.idle);
      expect(machine.resumePoint, -1);
      expect(machine.startAttempts, 0);
      expect(machine.transferComplete, isFalse);
    });

    test('startUpgrade sets syncing state', () {
      machine.startUpgrade();
      expect(machine.state, UpgradeState.syncing);
      expect(machine.transferComplete, isFalse);
    });

    group('handleVmuPacket', () {
      test('handles SYNC_CFM and sends START_REQ', () {
        machine.startUpgrade();
        final syncCfmData = [ResumePoints.dataTransfer, 0, 0, 0, 0, 0];
        final syncCfmPacket =
            VMUPacket.get(OpCodes.upgradeSyncCfm, data: syncCfmData);
        syncCfmPacket.mOpCode = OpCodes.upgradeSyncCfm;
        syncCfmPacket.mData = syncCfmData;

        machine.handleVmuPacket(syncCfmPacket);

        expect(machine.state, UpgradeState.starting);
        expect(delegate.sentPackets.length, 1);
        expect(delegate.sentPackets.first.mOpCode, OpCodes.upgradeStartReq);
      });

      test('handles START_CFM success and starts data transfer', () {
        machine.state = UpgradeState.starting;
        machine.resumePoint = ResumePoints.dataTransfer;

        final startCfmPacket = VMUPacket.get(OpCodes.upgradeStartCfm,
            data: [UpgradeStartCFMStatus.success]);
        startCfmPacket.mOpCode = OpCodes.upgradeStartCfm;
        startCfmPacket.mData = [UpgradeStartCFMStatus.success];

        machine.handleVmuPacket(startCfmPacket);

        expect(machine.state, UpgradeState.transferring);
        expect(delegate.sentPackets.length, 1);
        expect(delegate.sentPackets.first.mOpCode, OpCodes.upgradeStartDataReq);
      });

      test('handles START_CFM with app not ready and retries', () {
        machine.state = UpgradeState.starting;
        machine.resumePoint = ResumePoints.dataTransfer;

        final startCfmPacket = VMUPacket.get(OpCodes.upgradeStartCfm,
            data: [UpgradeStartCFMStatus.errorAppNotReady]);
        startCfmPacket.mOpCode = OpCodes.upgradeStartCfm;
        startCfmPacket.mData = [UpgradeStartCFMStatus.errorAppNotReady];

        machine.handleVmuPacket(startCfmPacket);

        expect(machine.startAttempts, 1);
        expect(machine.state, UpgradeState.starting);
        expect(delegate.logs.any((l) => l.contains('未就绪')), isTrue);
      });

      test('handles DATA_BYTES_REQ correctly', () {
        machine.state = UpgradeState.transferring;
        // 构造请求: 4字节长度 + 4字节偏移
        final data = [0x00, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00];
        final dataReqPacket =
            VMUPacket.get(OpCodes.upgradeDataBytesReq, data: data);
        dataReqPacket.mOpCode = OpCodes.upgradeDataBytesReq;
        dataReqPacket.mData = data;

        machine.handleVmuPacket(dataReqPacket);

        expect(delegate.lastBytesToSend, 0x0400); // 1024
        expect(delegate.lastStartOffset, 0);
      });

      test('handles VALIDATION_DONE_CFM with delay payload', () {
        fakeAsync((async) {
          machine.state = UpgradeState.validating;
          final validationPacket = VMUPacket.get(
            OpCodes.upgradeIsValidationDoneCfm,
            data: [0x00, 0x64], // 100ms
          );
          validationPacket.mOpCode = OpCodes.upgradeIsValidationDoneCfm;
          validationPacket.mData = [0x00, 0x64];

          machine.handleVmuPacket(validationPacket);
          expect(delegate.sentPackets, isEmpty);

          async.elapse(const Duration(milliseconds: 99));
          expect(delegate.sentPackets, isEmpty);

          async.elapse(const Duration(milliseconds: 1));
          expect(delegate.sentPackets.length, 1);
          expect(delegate.sentPackets.first.mOpCode,
              OpCodes.upgradeIsValidationDoneReq);
        });
      });

      test('VALIDATION_DONE_CFM delayed callback should respect state change',
          () {
        fakeAsync((async) {
          machine.state = UpgradeState.validating;
          final validationPacket = VMUPacket.get(
            OpCodes.upgradeIsValidationDoneCfm,
            data: [0x00, 0x32], // 50ms
          );
          validationPacket.mOpCode = OpCodes.upgradeIsValidationDoneCfm;
          validationPacket.mData = [0x00, 0x32];

          machine.handleVmuPacket(validationPacket);
          machine.state = UpgradeState.error;
          async.elapse(const Duration(milliseconds: 50));

          expect(delegate.sentPackets, isEmpty);
        });
      });

      test('handles ABORT_CFM and resets to idle', () {
        machine.state = UpgradeState.transferring;

        final abortPacket = VMUPacket.get(OpCodes.upgradeAbortCfm);
        abortPacket.mOpCode = OpCodes.upgradeAbortCfm;

        machine.handleVmuPacket(abortPacket);

        expect(machine.state, UpgradeState.idle);
        expect(delegate.logs.any((l) => l.contains('AbortCFM')), isTrue);
      });

      test('handles TRANSFER_COMPLETE_IND', () {
        machine.state = UpgradeState.transferring;

        final completeIndPacket =
            VMUPacket.get(OpCodes.upgradeTransferCompleteInd);
        completeIndPacket.mOpCode = OpCodes.upgradeTransferCompleteInd;

        machine.handleVmuPacket(completeIndPacket);

        expect(machine.transferComplete, isTrue);
        expect(machine.resumePoint, ResumePoints.transferComplete);
        expect(
            delegate.lastConfirmationType, ConfirmationType.transferComplete);
      });

      test('handles COMMIT_REQ', () {
        machine.state = UpgradeState.validating;

        final commitReqPacket = VMUPacket.get(OpCodes.upgradeCommitReq);
        commitReqPacket.mOpCode = OpCodes.upgradeCommitReq;

        machine.handleVmuPacket(commitReqPacket);

        expect(machine.state, UpgradeState.committing);
        expect(machine.resumePoint, ResumePoints.commit);
        expect(delegate.lastConfirmationType, ConfirmationType.commit);
      });

      test('handles COMPLETE_IND', () {
        machine.state = UpgradeState.committing;

        final completePacket = VMUPacket.get(OpCodes.upgradeCompleteInd);
        completePacket.mOpCode = OpCodes.upgradeCompleteInd;

        machine.handleVmuPacket(completePacket);

        expect(machine.state, UpgradeState.complete);
        expect(delegate.upgradeCompleted, isTrue);
      });
    });

    test('onSuccessfulTransmission transitions after last packet', () {
      machine.state = UpgradeState.transferring;
      machine.resumePoint = ResumePoints.dataTransfer;
      machine.wasLastPacket = true;

      machine.onSuccessfulTransmission();

      expect(machine.state, UpgradeState.validating);
      expect(machine.resumePoint, ResumePoints.validation);
      expect(machine.wasLastPacket, isFalse);
      expect(delegate.sentPackets.length, 1);
      expect(delegate.sentPackets.first.mOpCode,
          OpCodes.upgradeIsValidationDoneReq);
    });

    test('setWasLastPacket updates flag', () {
      expect(machine.wasLastPacket, isFalse);
      machine.setWasLastPacket(true);
      expect(machine.wasLastPacket, isTrue);
    });

    test('VmuPacketResult factories build success and error objects', () {
      final success = VmuPacketResult.success(nextState: UpgradeState.starting);
      final error = VmuPacketResult.error('boom');

      expect(success.success, isTrue);
      expect(success.nextState, UpgradeState.starting);
      expect(error.success, isFalse);
      expect(error.errorMessage, 'boom');
    });

    test('handleVmuPacket ignores null packet', () {
      machine.state = UpgradeState.transferring;
      machine.handleVmuPacket(null);
      expect(machine.state, UpgradeState.transferring);
    });

    test('SYNC_CFM with short payload uses fallback resume point', () {
      machine.resumePoint = -1;
      final packet = VMUPacket.get(OpCodes.upgradeSyncCfm, data: <int>[0x01]);
      packet.mOpCode = OpCodes.upgradeSyncCfm;
      packet.mData = <int>[0x01];

      machine.handleVmuPacket(packet);

      expect(machine.resumePoint, ResumePoints.dataTransfer);
      expect(machine.state, UpgradeState.starting);
      expect(delegate.sentPackets.last.mOpCode, OpCodes.upgradeStartReq);
      expect(delegate.logs.any((log) => log.contains('SYNC_CFM 数据不足')), isTrue);
    });

    test('START_CFM with empty payload enters error state', () {
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm, data: <int>[]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.error);
      expect(delegate.upgradeError, contains('数据为空'));
    });

    test('START_CFM retry callback sends start request when still starting', () {
      fakeAsync((async) {
        machine.state = UpgradeState.starting;
        final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
            data: <int>[UpgradeStartCFMStatus.errorAppNotReady]);
        packet.mOpCode = OpCodes.upgradeStartCfm;
        packet.mData = <int>[UpgradeStartCFMStatus.errorAppNotReady];

        machine.handleVmuPacket(packet);
        expect(delegate.sentPackets, isEmpty);

        async.elapse(const Duration(milliseconds: 500));
        expect(delegate.sentPackets.length, 1);
        expect(delegate.sentPackets.first.mOpCode, OpCodes.upgradeStartReq);
      });
    });

    test('START_CFM retry callback skips resend when state changed', () {
      fakeAsync((async) {
        machine.state = UpgradeState.starting;
        final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
            data: <int>[UpgradeStartCFMStatus.errorAppNotReady]);
        packet.mOpCode = OpCodes.upgradeStartCfm;
        packet.mData = <int>[UpgradeStartCFMStatus.errorAppNotReady];

        machine.handleVmuPacket(packet);
        machine.state = UpgradeState.error;
        async.elapse(const Duration(milliseconds: 500));

        expect(delegate.sentPackets, isEmpty);
      });
    });

    test('START_CFM exceeds retry limit and reports error', () {
      machine.state = UpgradeState.starting;
      machine.startAttempts = UpgradeStateMachine.maxStartNotReadyRetries;
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
          data: <int>[UpgradeStartCFMStatus.errorAppNotReady]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[UpgradeStartCFMStatus.errorAppNotReady];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.error);
      expect(delegate.upgradeError, contains('超过重试上限'));
    });

    test('START_CFM unknown status enters error state', () {
      machine.state = UpgradeState.starting;
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm, data: <int>[0x7F]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[0x7F];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.error);
      expect(delegate.upgradeError, contains('异常状态'));
    });

    test('START_CFM success with commit resume point requests commit confirm',
        () {
      machine.state = UpgradeState.starting;
      machine.resumePoint = ResumePoints.commit;
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
          data: <int>[UpgradeStartCFMStatus.success]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[UpgradeStartCFMStatus.success];

      machine.handleVmuPacket(packet);

      expect(delegate.lastConfirmationType, ConfirmationType.commit);
    });

    test(
        'START_CFM success with transferComplete resume point requests transfer complete confirm',
        () {
      machine.state = UpgradeState.starting;
      machine.resumePoint = ResumePoints.transferComplete;
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
          data: <int>[UpgradeStartCFMStatus.success]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[UpgradeStartCFMStatus.success];

      machine.handleVmuPacket(packet);

      expect(
          delegate.lastConfirmationType, ConfirmationType.transferComplete);
    });

    test('START_CFM success with inProgress resume point requests inProgress',
        () {
      machine.state = UpgradeState.starting;
      machine.resumePoint = ResumePoints.inProgress;
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
          data: <int>[UpgradeStartCFMStatus.success]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[UpgradeStartCFMStatus.success];

      machine.handleVmuPacket(packet);

      expect(delegate.lastConfirmationType, ConfirmationType.inProgress);
    });

    test('START_CFM success with validation resume point sends validation req',
        () {
      machine.state = UpgradeState.starting;
      machine.resumePoint = ResumePoints.validation;
      final packet = VMUPacket.get(OpCodes.upgradeStartCfm,
          data: <int>[UpgradeStartCFMStatus.success]);
      packet.mOpCode = OpCodes.upgradeStartCfm;
      packet.mData = <int>[UpgradeStartCFMStatus.success];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.validating);
      expect(delegate.sentPackets.single.mOpCode,
          OpCodes.upgradeIsValidationDoneReq);
    });

    test('DATA_BYTES_REQ invalid length sends abort request', () {
      final packet =
          VMUPacket.get(OpCodes.upgradeDataBytesReq, data: <int>[0x00, 0x01]);
      packet.mOpCode = OpCodes.upgradeDataBytesReq;
      packet.mData = <int>[0x00, 0x01];

      machine.handleVmuPacket(packet);

      expect(delegate.logs.any((log) => log.contains('数据传输失败')), isTrue);
      expect(delegate.sentPackets.single.mOpCode, OpCodes.upgradeAbortReq);
    });

    test('ERROR_WARN_IND with short payload sets error state', () {
      final packet =
          VMUPacket.get(OpCodes.upgradeErrorWarnInd, data: <int>[0x81]);
      packet.mOpCode = OpCodes.upgradeErrorWarnInd;
      packet.mData = <int>[0x81];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.error);
      expect(delegate.logs.any((log) => log.contains('错误码长度不足')), isTrue);
    });

    test('ERROR_WARN_IND 0x81 asks warning confirmation', () {
      final packet =
          VMUPacket.get(OpCodes.upgradeErrorWarnInd, data: <int>[0x00, 0x81]);
      packet.mOpCode = OpCodes.upgradeErrorWarnInd;
      packet.mData = <int>[0x00, 0x81];

      machine.handleVmuPacket(packet);

      expect(delegate.sentPackets.first.mOpCode, OpCodes.upgradeErrorWarnRes);
      expect(delegate.lastConfirmationType,
          ConfirmationType.warningFileIsDifferent);
    });

    test('ERROR_WARN_IND 0x21 reports battery low error', () {
      final packet =
          VMUPacket.get(OpCodes.upgradeErrorWarnInd, data: <int>[0x00, 0x21]);
      packet.mOpCode = OpCodes.upgradeErrorWarnInd;
      packet.mData = <int>[0x00, 0x21];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.error);
      expect(delegate.upgradeError, contains('电量过低'));
    });

    test('ERROR_WARN_IND other code reports generic error', () {
      final packet =
          VMUPacket.get(OpCodes.upgradeErrorWarnInd, data: <int>[0x01, 0x23]);
      packet.mOpCode = OpCodes.upgradeErrorWarnInd;
      packet.mData = <int>[0x01, 0x23];

      machine.handleVmuPacket(packet);

      expect(machine.state, UpgradeState.error);
      expect(delegate.upgradeError, contains('0x123'));
    });

    test('VALIDATION_DONE_CFM without delay payload sends request immediately',
        () {
      final packet =
          VMUPacket.get(OpCodes.upgradeIsValidationDoneCfm, data: <int>[0x00]);
      packet.mOpCode = OpCodes.upgradeIsValidationDoneCfm;
      packet.mData = <int>[0x00];

      machine.handleVmuPacket(packet);

      expect(delegate.sentPackets.single.mOpCode,
          OpCodes.upgradeIsValidationDoneReq);
    });

    test('onSuccessfulTransmission sends abort when hasToAbort is true', () {
      machine.hasToAbort = true;
      machine.onSuccessfulTransmission();

      expect(machine.hasToAbort, isFalse);
      expect(delegate.sentPackets.single.mOpCode, OpCodes.upgradeAbortReq);
    });

    test('onSuccessfulTransmission does nothing for non-dataTransfer last packet',
        () {
      machine.wasLastPacket = true;
      machine.resumePoint = ResumePoints.validation;
      machine.onSuccessfulTransmission();

      expect(delegate.sentPackets, isEmpty);
    });
  });
}
