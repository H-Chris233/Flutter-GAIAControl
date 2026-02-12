import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/rwcp/RWCP.dart';
import 'package:gaia/utils/gaia/rwcp/RWCPClient.dart';
import 'package:gaia/utils/gaia/rwcp/RWCPListener.dart';
import 'package:gaia/utils/gaia/rwcp/Segment.dart';

class _FakeRWCPListener implements RWCPListener {
  final List<List<int>> sentSegments = [];
  int progressAckCount = 0;

  @override
  bool sendRWCPSegment(List<int> bytes) {
    sentSegments.add(List<int>.from(bytes));
    return true;
  }

  @override
  void onTransferFailed() {}

  @override
  void onTransferFinished() {}

  @override
  void onTransferProgress(int acknowledged) {
    progressAckCount += acknowledged;
  }
}

void main() {
  group('RWCPClient', () {
    test('timeout increases data timeout and caps at max', () {
      final listener = _FakeRWCPListener();
      final client = RWCPClient(listener);
      client.mState = RWCPState.ESTABLISHED;
      client.isTimeOutRunning = true;
      client.mDataTimeOutMs = 1200;

      client.onTimeOut();
      expect(client.mDataTimeOutMs, 2000);

      client.isTimeOutRunning = true;
      client.onTimeOut();
      expect(client.mDataTimeOutMs, 2000);
    });

    test('validateAckSequence handles wrap-around from 63 to 0', () {
      final listener = _FakeRWCPListener();
      final client = RWCPClient(listener);
      client.mLastAckSequence = 63;
      client.mNextSequence = 2;
      client.mWindow = 10;
      client.mCredits = 0;
      client.mUnacknowledgedSegments.add(Segment.get(RWCPOpCodeClient.DATA, 0));
      client.mUnacknowledgedSegments.add(Segment.get(RWCPOpCodeClient.DATA, 1));

      final acknowledged = client.validateAckSequence(RWCPOpCodeClient.DATA, 1);

      expect(acknowledged, 2);
      expect(client.mLastAckSequence, 1);
      expect(client.mUnacknowledgedSegments, isEmpty);
    });

    test('successful ACKs gradually recover timeout toward default', () {
      final listener = _FakeRWCPListener();
      final client = RWCPClient(listener);
      client.mState = RWCPState.ESTABLISHED;
      client.mDataTimeOutMs = 400;
      client.mWindow = 16;
      client.mCredits = 0;
      client.mLastAckSequence = -1;
      client.mNextSequence = 9;
      for (int i = 0; i < 9; i++) {
        client.mUnacknowledgedSegments
            .add(Segment.get(RWCPOpCodeClient.DATA, i));
      }

      final handled =
          client.receiveDataAck(Segment.get(RWCPOpCodeServer.DATA_ACK, 7));

      expect(handled, isTrue);
      expect(client.mDataTimeOutMs, 300);
      expect(listener.progressAckCount, 8);
    });
  });
}
