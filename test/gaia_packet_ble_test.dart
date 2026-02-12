import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/GAIA.dart';
import 'package:gaia/utils/gaia/GaiaPacketBLE.dart';

void main() {
  group('GaiaPacketBLE', () {
    test('builds bytes using vendor, command and payload', () {
      final packet = GaiaPacketBLE(
        0x1234,
        mVendorId: 0x00FF,
        mPayload: [0xAA, 0xBB],
      );

      expect(packet.getBytes(), [0x00, 0xFF, 0x12, 0x34, 0xAA, 0xBB]);
    });

    test('parses bytes to packet fields', () {
      final packet =
          GaiaPacketBLE.fromByte([0x00, 0x0A, 0x01, 0x02, 0x55, 0x66]);

      expect(packet, isNotNull);
      expect(packet!.mVendorId, 0x000A);
      expect(packet.getCommandId(), 0x0102);
      expect(packet.mPayload, [0x55, 0x66]);
    });

    test('ack packet exposes status and event packet exposes event code', () {
      final ackPacket = GaiaPacketBLE(
        GAIA.ACKNOWLEDGMENT_MASK | 0x0001,
        mPayload: [0x09],
      );
      final notificationPacket = GaiaPacketBLE(
        GAIA.COMMANDS_NOTIFICATION_MASK | 0x0001,
        mPayload: [0x06, 0x77],
      );

      expect(ackPacket.isAcknowledgement(), isTrue);
      expect(ackPacket.getStatus(), 0x09);
      expect(notificationPacket.getEvent(), 0x06);
    });

    test('non-ack packet returns NOT_STATUS', () {
      final packet = GaiaPacketBLE(0x0001, mPayload: [0x01]);

      expect(packet.isAcknowledgement(), isFalse);
      expect(packet.getStatus(), GAIA.NOT_STATUS);
    });
  });
}
