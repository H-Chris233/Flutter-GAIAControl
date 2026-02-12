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

  @override
  bool sendRWCPSegment(List<int> bytes) {
    sentSegments.add(List<int>.from(bytes));
    return true;
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
  });
}
