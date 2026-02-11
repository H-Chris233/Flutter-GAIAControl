import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/rwcp/RWCP.dart';
import 'package:gaia/utils/gaia/rwcp/RWCPClient.dart';
import 'package:gaia/utils/gaia/rwcp/RWCPListener.dart';

class _FakeRWCPListener implements RWCPListener {
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
  group('RWCPClient setMaximumWindowSize', () {
    test('rejects maximum window smaller than initial window', () {
      final client = RWCPClient(_FakeRWCPListener());

      final setInitial = client.setInitialWindowSize(10);
      final setMaximum = client.setMaximumWindowSize(9);

      expect(setInitial, isTrue);
      expect(setMaximum, isFalse);
      expect(client.getMaximumWindowSize(), RWCP.WINDOW_MAX);
    });

    test('accepts valid maximum window and updates value', () {
      final client = RWCPClient(_FakeRWCPListener());

      final result = client.setMaximumWindowSize(20);

      expect(result, isTrue);
      expect(client.getMaximumWindowSize(), 20);
    });
  });
}
