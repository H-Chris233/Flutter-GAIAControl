import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/VMUPacket.dart';

void main() {
  group('VMUPacket', () {
    test('serializes opcode, length and data', () {
      final packet = VMUPacket.get(0x21, data: [0xAA, 0xBB]);

      expect(packet.getBytes(), [0x21, 0x00, 0x02, 0xAA, 0xBB]);
    });

    test('parses valid packet bytes', () {
      final parsed =
          VMUPacket.getPackageFromByte([0x10, 0x00, 0x02, 0x01, 0x02]);

      expect(parsed, isNotNull);
      expect(parsed!.mOpCode, 0x10);
      expect(parsed.mData, [0x01, 0x02]);
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
  });
}
