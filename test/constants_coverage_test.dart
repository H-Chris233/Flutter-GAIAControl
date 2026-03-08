import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/ble_constants.dart';
import 'package:gaia/utils/gaia/confirmation_type.dart';
import 'package:gaia/utils/gaia/gaia.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';
import 'package:gaia/utils/gaia/op_codes.dart';
import 'package:gaia/utils/gaia/resume_points.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_listener.dart';
import 'package:gaia/utils/gaia/upgrade_start_cfm_status.dart';

class _NoopRwcpListener implements RWCPListener {
  @override
  bool sendRWCPSegment(List<int> bytes) => true;

  @override
  void onTransferFailed() {}

  @override
  void onTransferFinished() {}

  @override
  void onTransferProgress(int acknowledged) {}
}

void main() {
  group('Constants coverage', () {
    test('BleConstants singleton can be constructed', () {
      expect(BleConstants.instance, isNotNull);
      expect(identical(BleConstants.instance, BleConstants.instance), isTrue);
    });

    test('RWCP unknown state returns fallback label', () {
      expect(RWCP.getStateLabel(99), 'Unknown state (99)');
    });

    test('GaiaPacketBLE minPacketLength equals packetInformationLength', () {
      expect(
        GaiaPacketBLE.minPacketLength,
        GaiaPacketBLE.packetInformationLength,
      );
    });

    test('legacy GAIA constant containers are reachable at runtime', () {
      expect(ConfirmationType.values(), contains(ConfirmationType.commit));
      expect(ResumePoints.values(), contains(ResumePoints.validation));
      expect(OpCodes.values(), contains(OpCodes.upgradeSyncReq));
      expect(
          UpgradeStartCFMStatus.values(), contains(UpgradeStartCFMStatus.success));
      expect(GAIA.runtimeProbeValues(), contains(GAIA.commandMask));
    });

    test('RWCP listener helper can identify listener instances', () {
      final listener = _NoopRwcpListener();
      expect(isRwcpListenerInstance(listener), isTrue);
      expect(isRwcpListenerInstance(Object()), isFalse);
    });
  });
}
