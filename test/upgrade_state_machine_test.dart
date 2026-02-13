import 'package:flutter_test/flutter_test.dart';
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
  });
}
