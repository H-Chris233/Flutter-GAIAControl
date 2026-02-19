import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/ble_constants.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';

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
  });
}
