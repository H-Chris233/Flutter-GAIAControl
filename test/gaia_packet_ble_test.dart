import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/gaia.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';

void main() {
  group('GaiaPacketBLE', () {
    group('getBytes', () {
      test('builds bytes using vendor, command and payload', () {
        final packet = GaiaPacketBLE(
          0x1234,
          mVendorId: 0x00FF,
          mPayload: [0xAA, 0xBB],
        );

        expect(packet.getBytes(), [0x00, 0xFF, 0x12, 0x34, 0xAA, 0xBB]);
      });

      test('uses default vendorQualcomm when mVendorId not specified', () {
        final packet = GaiaPacketBLE(0x0001);

        final bytes = packet.getBytes();
        // vendorQualcomm = 0x000A
        expect(bytes[0], 0x00);
        expect(bytes[1], 0x0A);
      });

      test('builds bytes with empty payload', () {
        final packet = GaiaPacketBLE(0x0001, mVendorId: 0x001D);

        expect(packet.getBytes(), [0x00, 0x1D, 0x00, 0x01]);
      });

      test('caches bytes after first build', () {
        final packet = GaiaPacketBLE(0x0001, mPayload: [0x55]);

        final first = packet.getBytes();
        final second = packet.getBytes();

        expect(identical(first, second), isTrue);
      });
    });

    group('fromByte', () {
      test('parses bytes to packet fields', () {
        final packet =
            GaiaPacketBLE.fromByte([0x00, 0x0A, 0x01, 0x02, 0x55, 0x66]);

        expect(packet, isNotNull);
        expect(packet!.mVendorId, 0x000A);
        expect(packet.getCommandId(), 0x0102);
        expect(packet.mPayload, [0x55, 0x66]);
      });

      test('returns null when bytes length is less than minimum', () {
        expect(GaiaPacketBLE.fromByte([0x00]), isNull);
        expect(GaiaPacketBLE.fromByte([0x00, 0x0A]), isNull);
        expect(GaiaPacketBLE.fromByte([0x00, 0x0A, 0x01]), isNull);
      });

      test('parses packet with no payload correctly', () {
        final packet = GaiaPacketBLE.fromByte([0x00, 0x1D, 0x80, 0x01]);

        expect(packet, isNotNull);
        expect(packet!.mVendorId, 0x001D);
        expect(packet.getCommandId(), 0x8001);
        expect(packet.mPayload, isEmpty);
      });

      test('preserves original bytes in mBytes', () {
        final originalBytes = [0x00, 0x0A, 0x01, 0x02, 0xAA];
        final packet = GaiaPacketBLE.fromByte(originalBytes);

        expect(packet!.getBytes(), originalBytes);
      });
    });

    group('acknowledgement handling', () {
      test('ack packet exposes status and event packet exposes event code', () {
        final ackPacket = GaiaPacketBLE(
          GAIA.acknowledgmentMask | 0x0001,
          mPayload: [0x09],
        );
        final notificationPacket = GaiaPacketBLE(
          GAIA.commandsNotificationMask | 0x0001,
          mPayload: [0x06, 0x77],
        );

        expect(ackPacket.isAcknowledgement(), isTrue);
        expect(ackPacket.getStatus(), 0x09);
        expect(notificationPacket.getEvent(), 0x06);
      });

      test('non-ack packet returns notStatus', () {
        final packet = GaiaPacketBLE(0x0001, mPayload: [0x01]);

        expect(packet.isAcknowledgement(), isFalse);
        expect(packet.getStatus(), GAIA.notStatus);
      });

      test('ack packet with empty payload returns notStatus', () {
        final packet = GaiaPacketBLE(GAIA.acknowledgmentMask | 0x0001);

        expect(packet.isAcknowledgement(), isTrue);
        expect(packet.getStatus(), GAIA.notStatus);
      });

      test('ack packet with null payload returns notStatus', () {
        final packet = GaiaPacketBLE(
          GAIA.acknowledgmentMask | 0x0001,
          mPayload: null,
        );

        expect(packet.isAcknowledgement(), isTrue);
        expect(packet.getStatus(), GAIA.notStatus);
      });
    });

    group('getCommand', () {
      test('extracts pure command without ack bit', () {
        final ackPacket = GaiaPacketBLE(GAIA.acknowledgmentMask | 0x1234);

        expect(ackPacket.getCommandId(), 0x8000 | 0x1234);
        expect(ackPacket.getCommand(), 0x1234);
      });

      test('returns same value for non-ack command', () {
        final packet = GaiaPacketBLE(0x0301);

        expect(packet.getCommand(), 0x0301);
        expect(packet.getCommandId(), 0x0301);
      });
    });

    group('getEvent', () {
      test('returns notNotification for non-notification packet', () {
        final packet = GaiaPacketBLE(0x0001, mPayload: [0x06]);

        expect(packet.getEvent(), GAIA.notNotification);
      });

      test('returns notNotification when payload is null', () {
        final packet = GaiaPacketBLE(
          GAIA.commandsNotificationMask | 0x0001,
          mPayload: null,
        );

        expect(packet.getEvent(), GAIA.notNotification);
      });

      test('returns notNotification when payload is empty', () {
        final packet = GaiaPacketBLE(
          GAIA.commandsNotificationMask | 0x0001,
          mPayload: [],
        );

        expect(packet.getEvent(), GAIA.notNotification);
      });
    });

    group('buildGaiaNotificationPacket', () {
      test('builds notification packet with event and data', () {
        final packet = GaiaPacketBLE.buildGaiaNotificationPacket(
          0x0401,
          0x06,
          [0x11, 0x22],
          0,
          mVendorId: 0x001D,
        );

        expect(packet.mVendorId, 0x001D);
        expect(packet.getCommandId(), 0x0401);
        expect(packet.mPayload, [0x06, 0x11, 0x22]);
      });

      test('builds notification packet with event only when data is null', () {
        final packet = GaiaPacketBLE.buildGaiaNotificationPacket(
          0x0401,
          0x06,
          null,
          0,
        );

        expect(packet.mPayload, [0x06]);
      });

      test('builds notification packet with event only when data is empty', () {
        final packet = GaiaPacketBLE.buildGaiaNotificationPacket(
          0x0401,
          0x06,
          [],
          0,
        );

        expect(packet.mPayload, [0x06]);
      });
    });
  });
}
