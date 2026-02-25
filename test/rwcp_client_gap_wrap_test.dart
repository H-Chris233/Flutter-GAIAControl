import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_client.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_listener.dart';
import 'package:gaia/utils/gaia/rwcp/segment.dart';

class _FakeListener implements RWCPListener {
  @override
  void onTransferFailed() {}

  @override
  void onTransferFinished() {}

  @override
  void onTransferProgress(int acknowledged) {}

  @override
  bool sendRWCPSegment(List<int> bytes) => true;
}

void main() {
  test('receiveGAP handles sequence wrap-around (63 -> 0)', () {
    final client = RWCPClient(_FakeListener());
    client.mShowDebugLogs = false;

    client.mState = RWCPState.established;
    client.mWindow = 15;
    client.mMaximumWindow = 32;
    client.mCredits = client.mWindow;

    // Simulate wrap-around window: last ACK is 63, next to send is 5.
    client.mLastAckSequence = 63;
    client.mNextSequence = 5;

    final gap = Segment.get(RWCPOpCodeServer.gap, 0);
    final handled = client.receiveGAP(gap);

    expect(handled, isTrue);
    // GAP should not be ignored, so window should be decreased.
    expect(client.mWindow, 8);
  });
}
