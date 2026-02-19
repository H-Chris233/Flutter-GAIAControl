import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_client.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_listener.dart';
import 'package:gaia/utils/gaia/rwcp/segment.dart';

class _FakeRWCPListener implements RWCPListener {
  final List<List<int>> sentSegments = [];
  int progressAckCount = 0;
  bool transferFinished = false;
  bool transferFailed = false;
  bool sendSucceeds = true;

  @override
  bool sendRWCPSegment(List<int> bytes) {
    sentSegments.add(List<int>.from(bytes));
    return sendSucceeds;
  }

  @override
  void onTransferFailed() {
    transferFailed = true;
  }

  @override
  void onTransferFinished() {
    transferFinished = true;
  }

  @override
  void onTransferProgress(int acknowledged) {
    progressAckCount += acknowledged;
  }

  void reset() {
    sentSegments.clear();
    progressAckCount = 0;
    transferFinished = false;
    transferFailed = false;
    sendSucceeds = true;
  }
}

void main() {
  group('RWCPClient', () {
    late _FakeRWCPListener listener;
    late RWCPClient client;

    setUp(() {
      listener = _FakeRWCPListener();
      client = RWCPClient(listener);
    });

    group('timeout handling', () {
      test('timeout doubles data timeout up to max', () {
        client.mState = RWCPState.established;
        client.isTimeOutRunning = true;
        client.mDataTimeOutMs = 1200;

        client.onTimeOut();
        expect(client.mDataTimeOutMs, 2000);

        client.isTimeOutRunning = true;
        client.onTimeOut();
        expect(client.mDataTimeOutMs, 2000);
      });

      test('timeout does nothing when not running', () {
        client.mState = RWCPState.established;
        client.isTimeOutRunning = false;
        client.mDataTimeOutMs = 500;

        client.onTimeOut();

        expect(client.mDataTimeOutMs, 500);
      });

      test('timeout resets acknowledged segments counter', () {
        client.mState = RWCPState.established;
        client.isTimeOutRunning = true;
        client.mAcknowledgedSegments = 10;
        client.mSuccessfulAckStreak = 5;

        client.onTimeOut();

        expect(client.mAcknowledgedSegments, 0);
        expect(client.mSuccessfulAckStreak, 0);
      });
    });

    group('sequence number management', () {
      test('validateAckSequence handles wrap-around from 63 to 0', () {
        client.mLastAckSequence = 63;
        client.mNextSequence = 2;
        client.mWindow = 10;
        client.mCredits = 0;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 1));

        final acknowledged =
            client.validateAckSequence(RWCPOpCodeClient.data, 1);

        expect(acknowledged, 2);
        expect(client.mLastAckSequence, 1);
        expect(client.mUnacknowledgedSegments, isEmpty);
      });

      test('increaseSequenceNumber wraps at 63', () {
        expect(client.increaseSequenceNumber(0), 1);
        expect(client.increaseSequenceNumber(62), 63);
        expect(client.increaseSequenceNumber(63), 0);
      });

      test('decreaseSequenceNumber handles wrap-around', () {
        expect(client.decreaseSequenceNumber(5, 3), 2);
        expect(client.decreaseSequenceNumber(2, 3), 63);
        expect(client.decreaseSequenceNumber(0, 1), 63);
      });

      test('validateAckSequence rejects sequence outside window', () {
        client.mLastAckSequence = 5;
        client.mNextSequence = 10;
        client.mWindow = 10;
        // sequence 3 is before lastAck, should be rejected
        final result = client.validateAckSequence(RWCPOpCodeClient.data, 3);

        expect(result, -1);
      });

      test('validateAckSequence accepts sequence at boundary', () {
        client.mLastAckSequence = -1;
        client.mNextSequence = 5;
        client.mWindow = 10;
        client.mCredits = 5;
        for (int i = 0; i < 5; i++) {
          client.mUnacknowledgedSegments
              .add(Segment.get(RWCPOpCodeClient.data, i));
        }

        final result = client.validateAckSequence(RWCPOpCodeClient.data, 4);

        expect(result, 5);
        expect(client.mLastAckSequence, 4);
      });
    });

    group('timeout recovery', () {
      test('successful ACKs gradually recover timeout toward default', () {
        client.mState = RWCPState.established;
        client.mDataTimeOutMs = 400;
        client.mWindow = 16;
        client.mCredits = 0;
        client.mLastAckSequence = -1;
        client.mNextSequence = 9;
        for (int i = 0; i < 9; i++) {
          client.mUnacknowledgedSegments
              .add(Segment.get(RWCPOpCodeClient.data, i));
        }

        final handled =
            client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 7));

        expect(handled, isTrue);
        expect(client.mDataTimeOutMs, 300);
        expect(listener.progressAckCount, 8);
      });

      test('timeout stays at default when already at minimum', () {
        client.mState = RWCPState.established;
        client.mDataTimeOutMs = RWCP.dataTimeoutMsDefault;
        client.mWindow = 10;
        client.mCredits = 0;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));

        client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 0));

        expect(client.mDataTimeOutMs, RWCP.dataTimeoutMsDefault);
      });

      test('recovery requires 8 consecutive ACKs per step', () {
        client.mState = RWCPState.established;
        client.mDataTimeOutMs = 300;
        client.mWindow = 10;
        client.mCredits = 5;
        client.mLastAckSequence = -1;
        client.mNextSequence = 5;
        for (int i = 0; i < 5; i++) {
          client.mUnacknowledgedSegments
              .add(Segment.get(RWCPOpCodeClient.data, i));
        }
        // Add pending data to prevent rst trigger after all ACKs
        client.mPendingData.add([0x01]);

        // Only 5 ACKs, not enough for recovery
        client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 4));

        expect(client.mDataTimeOutMs, 300);
        expect(client.mSuccessfulAckStreak, 5);
      });
    });

    group('gap handling', () {
      test('receiveGAP decreases window and triggers resend', () {
        client.mState = RWCPState.established;
        client.mWindow = 16;
        client.mCredits = 0;
        client.mLastAckSequence = 2;
        client.mNextSequence = 6;
        for (int i = 3; i < 6; i++) {
          client.mUnacknowledgedSegments
              .add(Segment.get(RWCPOpCodeClient.data, i));
        }

        final gapSegment = Segment.get(RWCPOpCodeServer.gap, 3);
        final handled = client.receiveGAP(gapSegment);

        expect(handled, isTrue);
        // Window decreases: (16-1)/2 + 1 = 8
        expect(client.mWindow, 8);
      });

      test('receiveGAP ignores stale gap with sequence before lastAck', () {
        client.mState = RWCPState.established;
        client.mWindow = 16;
        client.mLastAckSequence = 10;
        final originalWindow = client.mWindow;

        final gapSegment = Segment.get(RWCPOpCodeServer.gap, 5);
        final handled = client.receiveGAP(gapSegment);

        expect(handled, isTrue);
        expect(client.mWindow, originalWindow);
      });

      test('receiveGAP returns false in listen state', () {
        client.mState = RWCPState.listen;

        final handled = client.receiveGAP(Segment.get(RWCPOpCodeServer.gap, 0));

        expect(handled, isFalse);
      });
    });

    group('window management', () {
      test('decreaseWindow halves window with minimum of 1', () {
        client.mWindow = 16;
        client.decreaseWindow();
        expect(client.mWindow, 8);

        client.mWindow = 4;
        client.decreaseWindow();
        expect(client.mWindow, 2);

        client.mWindow = 2;
        client.decreaseWindow();
        expect(client.mWindow, 1);

        client.mWindow = 1;
        client.decreaseWindow();
        expect(client.mWindow, 1);
      });

      test('increaseWindow increases after enough ACKs', () {
        client.mWindow = 10;
        client.mMaximumWindow = 32;
        client.mCredits = 10;
        client.mAcknowledgedSegments = 0;

        // Simulate 10 acknowledgements (equals window size)
        client.increaseWindow(10);

        expect(client.mWindow, 11);
        expect(client.mCredits, 11);
        expect(client.mAcknowledgedSegments, 0);
      });

      test('increaseWindow does not exceed maximum', () {
        client.mWindow = 32;
        client.mMaximumWindow = 32;
        client.mCredits = 32;
        client.mAcknowledgedSegments = 0;

        client.increaseWindow(32);

        expect(client.mWindow, 32);
      });

      test('setMaximumWindowSize fails during active session', () {
        client.mState = RWCPState.established;

        final result = client.setMaximumWindowSize(20);

        expect(result, isFalse);
      });

      test('setInitialWindowSize fails if greater than maximum', () {
        client.mMaximumWindow = 15;

        final result = client.setInitialWindowSize(20);

        expect(result, isFalse);
      });
    });

    group('session management', () {
      test('isRunningASession returns true when not in listen', () {
        expect(client.isRunningASession(), isFalse);

        client.mState = RWCPState.synSent;
        expect(client.isRunningASession(), isTrue);

        client.mState = RWCPState.established;
        expect(client.isRunningASession(), isTrue);

        client.mState = RWCPState.closing;
        expect(client.isRunningASession(), isTrue);
      });

      test('reset clears all session state', () {
        client.mState = RWCPState.established;
        client.mLastAckSequence = 10;
        client.mNextSequence = 15;
        client.mDataTimeOutMs = 500;
        client.mAcknowledgedSegments = 5;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));
        client.mPendingData.add([0x01, 0x02]);

        client.reset(true);

        expect(client.mState, RWCPState.listen);
        expect(client.mLastAckSequence, -1);
        expect(client.mNextSequence, 0);
        expect(client.mDataTimeOutMs, RWCP.dataTimeoutMsDefault);
        expect(client.mAcknowledgedSegments, 0);
        expect(client.mUnacknowledgedSegments, isEmpty);
        expect(client.mPendingData, isEmpty);
      });

      test('reset with complete=false preserves pending data', () {
        client.mPendingData.add([0x01, 0x02]);
        client.mPendingData.add([0x03, 0x04]);

        client.reset(false);

        expect(client.mPendingData.length, 2);
      });
    });

    group('receiveDataAck', () {
      test('returns false in listen state', () {
        client.mState = RWCPState.listen;

        final handled =
            client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 0));

        expect(handled, isFalse);
      });

      test('discards dataAck in closing state', () {
        client.mState = RWCPState.closing;

        final handled =
            client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 0));

        expect(handled, isTrue);
        expect(listener.progressAckCount, 0);
      });

      test('sends rst when all data acknowledged and no pending', () {
        client.mState = RWCPState.established;
        client.mWindow = 10;
        client.mCredits = 9;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));

        listener.reset();
        client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 0));

        expect(client.mState, RWCPState.closing);
        expect(listener.sentSegments.length, 1);
        // rst segment: sendRSTSegment calls reset() first which sets mNextSequence=0
        // So header = (2 << 6) | 0 = 128
        expect(listener.sentSegments[0][0], 128);
      });

      test('invalid ack keeps timeout running for retry path', () {
        client.mState = RWCPState.established;
        client.mWindow = 10;
        client.mCredits = 9;
        client.mDataTimeOutMs = 1000;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));
        client.startTimeOut(client.mDataTimeOutMs);

        client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 5));

        expect(client.isTimeOutRunning, isTrue);
        expect(client.mUnacknowledgedSegments.length, 1);
        client.cancelTimeOut();
      });
    });

    group('sendDataSegment reliability', () {
      test('send failure keeps pending data and starts timeout for retry', () {
        listener.sendSucceeds = false;
        client.mState = RWCPState.established;
        client.mWindow = 10;
        client.mCredits = 1;
        client.mDataTimeOutMs = 1000;
        client.mPendingData.add([0x01, 0x02]);
        client.mNextSequence = 7;

        client.sendDataSegment();

        expect(client.mPendingData.length, 1);
        expect(client.mUnacknowledgedSegments, isEmpty);
        expect(client.mNextSequence, 7);
        expect(client.mCredits, 1);
        expect(client.isTimeOutRunning, isTrue);
        client.cancelTimeOut();
      });
    });

    group('receiveSynAck', () {
      test('transitions to established on valid synAck', () {
        client.mState = RWCPState.synSent;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mCredits = 14;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.syn, 0));

        final handled =
            client.receiveSynAck(Segment.get(RWCPOpCodeServer.synAck, 0));

        expect(handled, isTrue);
        expect(client.mState, RWCPState.established);
      });

      test('terminates session on unexpected synAck sequence', () {
        client.mState = RWCPState.synSent;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.syn, 0));

        // Wrong sequence number
        client.receiveSynAck(Segment.get(RWCPOpCodeServer.synAck, 5));

        expect(listener.transferFailed, isTrue);
      });
    });

    group('additional branch coverage', () {
      test('showDebugLogs updates flag', () {
        client.showDebugLogs(false);
        expect(client.mShowDebugLogs, isFalse);
        client.showDebugLogs(true);
        expect(client.mShowDebugLogs, isTrue);
      });

      test('sendData starts session when in listen state', () {
        final started = client.sendData(<int>[0x01, 0x02]);

        expect(started, isTrue);
        expect(client.mState, RWCPState.closing);
        expect(client.mPendingData, isNotEmpty);
      });

      test('sendData sends immediately in established state without timeout',
          () {
        client.mState = RWCPState.established;
        client.mCredits = 1;

        final sent = client.sendData(<int>[0xAA]);

        expect(sent, isTrue);
        expect(client.mUnacknowledgedSegments, isNotEmpty);
      });

      test('sendData only queues when timeout is running', () {
        client.mState = RWCPState.established;
        client.isTimeOutRunning = true;

        final sent = client.sendData(<int>[0xAA]);

        expect(sent, isTrue);
        expect(client.mPendingData.length, 1);
        expect(client.mUnacknowledgedSegments, isEmpty);
      });

      test('cancelTransfer in listen state keeps state unchanged', () {
        client.mState = RWCPState.listen;

        client.cancelTransfer();

        expect(client.mState, RWCPState.listen);
      });

      test('cancelTransfer handles rst send failure by terminating session',
          () {
        listener.sendSucceeds = false;
        client.mState = RWCPState.established;

        client.cancelTransfer();

        expect(client.mState, RWCPState.listen);
      });

      test('onReceiveRWCPSegment returns false for null and short data', () {
        expect(client.onReceiveRWCPSegment(null), isFalse);
        expect(client.onReceiveRWCPSegment(<int>[]), isFalse);
      });

      test('window size getters return current values', () {
        client.mInitialWindow = 7;
        client.mMaximumWindow = 31;

        expect(client.getInitialWindowSize(), 7);
        expect(client.getMaximumWindowSize(), 31);
      });

      test('setInitialWindowSize fails during active session', () {
        client.mState = RWCPState.established;

        final ok = client.setInitialWindowSize(3);

        expect(ok, isFalse);
      });

      test('setMaximumWindowSize rejects out-of-range size', () {
        final ok = client.setMaximumWindowSize(RWCP.windowMax + 1);
        expect(ok, isFalse);
      });

      test('receiveRST in synSent state is ignored', () {
        client.mState = RWCPState.synSent;
        final handled = client.receiveRST(Segment.get(RWCPOpCodeServer.rst, 0));
        expect(handled, isTrue);
      });

      test('receiveRST in established state fails transfer', () {
        client.mState = RWCPState.established;
        final handled = client.receiveRST(Segment.get(RWCPOpCodeServer.rst, 0));
        expect(handled, isTrue);
        expect(listener.transferFailed, isTrue);
      });

      test('receiveRST in closing with no pending marks transfer finished', () {
        client.mState = RWCPState.closing;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.rst, 0));

        final handled = client.receiveRST(Segment.get(RWCPOpCodeServer.rst, 0));

        expect(handled, isTrue);
        expect(listener.transferFinished, isTrue);
      });

      test('receiveRST in closing with pending data sends SYN', () {
        client.mState = RWCPState.closing;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.rst, 0));
        client.mPendingData.add(<int>[0x01]);

        final handled = client.receiveRST(Segment.get(RWCPOpCodeServer.rst, 0));

        expect(handled, isTrue);
        expect(client.mState, RWCPState.synSent);
      });

      test('receiveRST in closing handles syn send failure', () {
        listener.sendSucceeds = false;
        client.mState = RWCPState.closing;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.rst, 0));
        client.mPendingData.add(<int>[0x01]);

        client.receiveRST(Segment.get(RWCPOpCodeServer.rst, 0));

        expect(listener.transferFailed, isTrue);
      });

      test('receiveRST returns false in listen state', () {
        client.mState = RWCPState.listen;
        final handled = client.receiveRST(Segment.get(RWCPOpCodeServer.rst, 0));
        expect(handled, isFalse);
      });

      test('startSession fails when already running', () {
        client.mState = RWCPState.established;
        expect(client.startSession(), isFalse);
      });

      test('startSession fails when sending rst fails', () {
        listener.sendSucceeds = false;
        client.mState = RWCPState.listen;
        expect(client.startSession(), isFalse);
      });

      test('startTimeOut timer invokes timeout callback', () async {
        client.mState = RWCPState.listen;
        client.startTimeOut(1);
        await Future<void>.delayed(const Duration(milliseconds: 10));
        expect(client.isTimeOutRunning, isFalse);
      });

      test('resendSegment returns early in established state', () {
        client.mState = RWCPState.established;
        client.resendSegment();
        expect(listener.sentSegments, isEmpty);
      });

      test('resendDataSegment returns early when not established', () {
        client.mState = RWCPState.listen;
        client.resendDataSegment();
        expect(listener.sentSegments, isEmpty);
      });

      test('resendDataSegment warns on non-data segment in queue tail', () {
        client.mState = RWCPState.established;
        client.mWindow = 1;
        client.mCredits = 0;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0, payload: <int>[0x01]));
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.rst, 1));

        client.resendDataSegment();

        expect(client.mUnacknowledgedSegments.length, 2);
      });

      test('receiveSynAck in established state can trigger resend', () {
        client.mState = RWCPState.established;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0, payload: <int>[0x01]));
        final handled =
            client.receiveSynAck(Segment.get(RWCPOpCodeServer.synAck, 0));
        expect(handled, isTrue);
      });

      test('receiveSynAck in listen state returns false', () {
        client.mState = RWCPState.listen;
        final handled =
            client.receiveSynAck(Segment.get(RWCPOpCodeServer.synAck, 0));
        expect(handled, isFalse);
      });

      test('validateAckSequence rejects invalid sequence bounds', () {
        expect(client.validateAckSequence(RWCPOpCodeClient.data, -1), -1);
        expect(
            client.validateAckSequence(
                RWCPOpCodeClient.data, RWCP.sequenceNumberMax + 1),
            -1);
      });

      test('removeSegmentFromQueue returns false for missing segment', () {
        final removed = client.removeSegmentFromQueue(RWCPOpCodeClient.data, 3);
        expect(removed, isFalse);
      });

      test('receiveDataAck starts timeout when waiting for more acks', () {
        client.mState = RWCPState.established;
        client.mWindow = 10;
        client.mCredits = 0;
        client.mDataTimeOutMs = 100;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mPendingData.add(<int>[0x01]);
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));

        final handled =
            client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 0));

        expect(handled, isTrue);
        expect(client.isTimeOutRunning, isTrue);
        client.cancelTimeOut();
      });

      test('receiveGAP in closing state is discarded', () {
        client.mState = RWCPState.closing;
        final handled = client.receiveGAP(Segment.get(RWCPOpCodeServer.gap, 0));
        expect(handled, isTrue);
      });

      test('decreaseWindow clamps to 1 when computed value is out of range',
          () {
        client.mWindow = 100;
        client.mMaximumWindow = 10;

        client.decreaseWindow();

        expect(client.mWindow, 1);
      });

      test('dispose resets state and pending data', () {
        client.mState = RWCPState.established;
        client.mPendingData.add(<int>[0x01, 0x02]);

        client.dispose();

        expect(client.mState, RWCPState.listen);
        expect(client.mPendingData, isEmpty);
      });

      test('onReceiveRWCPSegment dispatches by opcode', () {
        client.mState = RWCPState.synSent;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.syn, 0));
        expect(client.onReceiveRWCPSegment(<int>[0x40]), isTrue);

        client.mState = RWCPState.established;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mCredits = 0;
        client.mWindow = 10;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0));
        expect(client.onReceiveRWCPSegment(<int>[0x00]), isTrue);

        client.mState = RWCPState.synSent;
        expect(client.onReceiveRWCPSegment(<int>[0x80]), isTrue);

        client.mState = RWCPState.established;
        client.mLastAckSequence = 0;
        client.mNextSequence = 2;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 1, payload: <int>[0x01]));
        expect(client.onReceiveRWCPSegment(<int>[0xC1]), isTrue);
      });

      test('setMaximumWindowSize updates current window when needed', () {
        client.mState = RWCPState.listen;
        client.mInitialWindow = 5;
        client.mWindow = 10;

        final ok = client.setMaximumWindowSize(6);

        expect(ok, isTrue);
        expect(client.mWindow, 6);
        expect(client.mMaximumWindow, 6);
      });

      test('resendSegment sends syn rst and data with computed delay', () {
        client.mState = RWCPState.synSent;
        client.mWindow = 3;
        client.mCredits = 0;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.syn, 0));
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.rst, 1));
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 2, payload: <int>[0x01]));

        client.resendSegment();

        expect(listener.sentSegments.length, 3);
      });

      test('resendDataSegment moves overflowing data back to pending queue',
          () {
        client.mState = RWCPState.established;
        client.mWindow = 1;
        client.mCredits = 0;
        client.mNextSequence = 2;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 0, payload: <int>[0x11]));
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 1, payload: <int>[0x22]));

        client.resendDataSegment();

        expect(client.mUnacknowledgedSegments.length, 1);
        expect(client.mPendingData.first, <int>[0x22]);
      });

      test('receiveSynAck sends pending data when session established', () {
        client.mState = RWCPState.synSent;
        client.mLastAckSequence = -1;
        client.mNextSequence = 1;
        client.mCredits = 1;
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.syn, 0));
        client.mPendingData.add(<int>[0xAB]);

        final handled =
            client.receiveSynAck(Segment.get(RWCPOpCodeServer.synAck, 0));

        expect(handled, isTrue);
        expect(client.mUnacknowledgedSegments.isNotEmpty, isTrue);
      });

      test('validateAckSequence logs when acknowledged segment is missing', () {
        client.mLastAckSequence = -1;
        client.mNextSequence = 3;
        client.mWindow = 10;

        final validated = client.validateAckSequence(RWCPOpCodeClient.data, 1);

        expect(validated, 0);
      });

      test('receiveDataAck clamps recovered timeout to default', () {
        client.mState = RWCPState.established;
        client.mDataTimeOutMs = 150;
        client.mWindow = 10;
        client.mCredits = 0;
        client.mLastAckSequence = -1;
        client.mNextSequence = 8;
        for (int i = 0; i < 8; i++) {
          client.mUnacknowledgedSegments
              .add(Segment.get(RWCPOpCodeClient.data, i));
        }

        client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 7));

        expect(client.mDataTimeOutMs, RWCP.dataTimeoutMsDefault);
      });

      test('removeSegmentFromQueue removes existing entry', () {
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.data, 3));

        final removed = client.removeSegmentFromQueue(RWCPOpCodeClient.data, 3);

        expect(removed, isTrue);
        expect(client.mUnacknowledgedSegments, isEmpty);
      });

      test('receiveDataAck timeout branch hits credits equals zero path', () {
        client.mState = RWCPState.established;
        client.mWindow = 10;
        client.mCredits = 0;
        client.mDataTimeOutMs = 100;
        client.mLastAckSequence = -1;
        client.mNextSequence = 0;
        client.mPendingData.add(<int>[0x01]);

        final handled =
            client.receiveDataAck(Segment.get(RWCPOpCodeServer.dataAck, 0));

        expect(handled, isTrue);
        expect(client.isTimeOutRunning, isTrue);
        client.cancelTimeOut();
      });
    });
  });
}
