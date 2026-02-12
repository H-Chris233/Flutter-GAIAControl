import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/VMUPacket.dart';

void main() {
  group('VMUPacket', () {
    group('get factory method', () {
      test('creates packet with opcode and data', () {
        final packet = VMUPacket.get(0x21, data: [0xAA, 0xBB]);

        expect(packet.mOpCode, 0x21);
        expect(packet.mData, [0xAA, 0xBB]);
      });

      test('creates packet with opcode only', () {
        final packet = VMUPacket.get(0x01);

        expect(packet.mOpCode, 0x01);
        expect(packet.mData, isNull);
      });

      test('creates packet with empty data', () {
        final packet = VMUPacket.get(0x01, data: []);

        expect(packet.mOpCode, 0x01);
        expect(packet.mData, isEmpty);
      });
    });

    group('getBytes', () {
      test('serializes opcode, length and data', () {
        final packet = VMUPacket.get(0x21, data: [0xAA, 0xBB]);

        expect(packet.getBytes(), [0x21, 0x00, 0x02, 0xAA, 0xBB]);
      });

      test('serializes packet with no data as zero length', () {
        final packet = VMUPacket.get(0x01);

        expect(packet.getBytes(), [0x01, 0x00, 0x00]);
      });

      test('serializes packet with empty data as zero length', () {
        final packet = VMUPacket.get(0x01, data: []);

        expect(packet.getBytes(), [0x01, 0x00, 0x00]);
      });

      test('serializes large data length correctly', () {
        // 256 bytes of data: length = 0x0100
        final largeData = List<int>.filled(256, 0x55);
        final packet = VMUPacket.get(0x04, data: largeData);

        final bytes = packet.getBytes();
        expect(bytes[0], 0x04);
        expect(bytes[1], 0x01); // length high byte
        expect(bytes[2], 0x00); // length low byte
        expect(bytes.length, 3 + 256);
      });

      test('serializes max single byte length correctly', () {
        // 255 bytes of data: length = 0x00FF
        final data = List<int>.filled(255, 0xAA);
        final packet = VMUPacket.get(0x04, data: data);

        final bytes = packet.getBytes();
        expect(bytes[1], 0x00); // length high byte
        expect(bytes[2], 0xFF); // length low byte
      });
    });

    group('getPackageFromByte', () {
      test('parses valid packet bytes', () {
        final parsed =
            VMUPacket.getPackageFromByte([0x10, 0x00, 0x02, 0x01, 0x02]);

        expect(parsed, isNotNull);
        expect(parsed!.mOpCode, 0x10);
        expect(parsed.mData, [0x01, 0x02]);
      });

      test('parses packet with zero length data', () {
        final parsed = VMUPacket.getPackageFromByte([0x10, 0x00, 0x00]);

        expect(parsed, isNotNull);
        expect(parsed!.mOpCode, 0x10);
        expect(parsed.mData, isEmpty);
      });

      test('returns null when bytes too short', () {
        expect(VMUPacket.getPackageFromByte([]), isNull);
        expect(VMUPacket.getPackageFromByte([0x10]), isNull);
        expect(VMUPacket.getPackageFromByte([0x10, 0x00]), isNull);
      });

      test('returns null when declared length exceeds actual data', () {
        final parsed = VMUPacket.getPackageFromByte([0x10, 0x00, 0x03, 0xAA]);

        expect(parsed, isNull);
      });

      test('ignores trailing bytes when declared length is shorter', () {
        final parsed =
            VMUPacket.getPackageFromByte([0x10, 0x00, 0x02, 0x11, 0x22, 0x33]);

        expect(parsed, isNotNull);
        expect(parsed!.mData, [0x11, 0x22]);
      });

      test('parses packet with large length correctly', () {
        // Create packet with length = 0x0100 (256)
        final bytes = [0x04, 0x01, 0x00, ...List<int>.filled(256, 0x77)];
        final parsed = VMUPacket.getPackageFromByte(bytes);

        expect(parsed, isNotNull);
        expect(parsed!.mOpCode, 0x04);
        expect(parsed.mData!.length, 256);
        expect(parsed.mData!.every((b) => b == 0x77), isTrue);
      });
    });

    group('round-trip serialization', () {
      test('packet survives serialize-deserialize cycle', () {
        final original = VMUPacket.get(0x21, data: [0x01, 0x02, 0x03, 0x04]);
        final bytes = original.getBytes();
        final restored = VMUPacket.getPackageFromByte(bytes);

        expect(restored, isNotNull);
        expect(restored!.mOpCode, original.mOpCode);
        expect(restored.mData, original.mData);
      });

      test('empty data packet survives round-trip', () {
        final original = VMUPacket.get(0x0B, data: []);
        final bytes = original.getBytes();
        final restored = VMUPacket.getPackageFromByte(bytes);

        expect(restored, isNotNull);
        expect(restored!.mOpCode, 0x0B);
        expect(restored.mData, isEmpty);
      });
    });

    group('protocol constants', () {
      test('REQUIRED_INFORMATION_LENGTH is correct', () {
        // OpCode (1 byte) + Length (2 bytes) = 3 bytes
        expect(VMUPacket.REQUIRED_INFORMATION_LENGTH, 3);
      });

      test('DATA_OFFSET is after header', () {
        expect(VMUPacket.DATA_OFFSET, 3);
      });

      test('LENGTH_OFFSET follows opcode', () {
        expect(VMUPacket.LENGTH_OFFSET, 1);
      });
    });
  });
}
