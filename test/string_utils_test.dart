import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/string_utils.dart';

void main() {
  group('StringUtils', () {
    group('byteToString', () {
      test('should convert valid UTF-8 bytes to string', () {
        final bytes = [72, 101, 108, 108, 111]; // "Hello"
        expect(StringUtils.byteToString(bytes), equals('Hello'));
      });

      test('should convert Chinese characters correctly', () {
        final bytes = [228, 184, 173, 230, 150, 135]; // "中文"
        expect(StringUtils.byteToString(bytes), equals('中文'));
      });

      test('should return empty string for invalid UTF-8', () {
        final invalidBytes = [0xFF, 0xFE, 0xFD];
        expect(StringUtils.byteToString(invalidBytes), equals(''));
      });

      test('should return empty string for empty input', () {
        expect(StringUtils.byteToString([]), equals(''));
      });
    });

    group('byteToHexString', () {
      test('should convert bytes to hex string', () {
        final bytes = [0x00, 0x1A, 0xFF, 0xAB];
        expect(StringUtils.byteToHexString(bytes), equals('001AFFAB'));
      });

      test('should handle empty list', () {
        expect(StringUtils.byteToHexString([]), equals(''));
      });

      test('should handle single byte', () {
        expect(StringUtils.byteToHexString([0x0F]), equals('0F'));
      });

      test('should pad single digit hex values', () {
        expect(
            StringUtils.byteToHexString([0x01, 0x02, 0x03]), equals('010203'));
      });
    });

    group('hexStringToBytes', () {
      test('should convert hex string to bytes', () {
        expect(StringUtils.hexStringToBytes('001AFFAB'),
            equals([0x00, 0x1A, 0xFF, 0xAB]));
      });

      test('should handle lowercase hex', () {
        expect(
            StringUtils.hexStringToBytes('abcdef'), equals([0xAB, 0xCD, 0xEF]));
      });

      test('should handle odd length by padding', () {
        expect(StringUtils.hexStringToBytes('1AB'), equals([0x01, 0xAB]));
      });

      test('should handle empty string', () {
        expect(StringUtils.hexStringToBytes(''), equals([]));
      });
    });

    group('file2md5', () {
      test('should calculate MD5 hash', () {
        final input = [72, 101, 108, 108, 111]; // "Hello"
        // MD5 of "Hello" is 8b1a9953c4611296a827abf8c47804d7
        expect(StringUtils.file2md5(input),
            equals('8b1a9953c4611296a827abf8c47804d7'));
      });

      test('should handle empty input', () {
        // MD5 of empty string is d41d8cd98f00b204e9800998ecf8427e
        expect(StringUtils.file2md5([]),
            equals('d41d8cd98f00b204e9800998ecf8427e'));
      });
    });

    group('encode', () {
      test('should encode string to UTF-8 bytes', () {
        expect(StringUtils.encode('Hello'), equals([72, 101, 108, 108, 111]));
      });

      test('should encode Chinese characters', () {
        expect(
            StringUtils.encode('中文'), equals([228, 184, 173, 230, 150, 135]));
      });

      test('should handle empty string', () {
        expect(StringUtils.encode(''), equals([]));
      });
    });

    group('extractIntFromByteArray', () {
      test('should extract big endian int', () {
        final bytes = [0x00, 0x01, 0x02, 0x03];
        // Big endian: 0x00010203 = 66051
        expect(StringUtils.extractIntFromByteArray(bytes, 0, 4, false),
            equals(66051));
      });

      test('should extract little endian int', () {
        final bytes = [0x03, 0x02, 0x01, 0x00];
        // Little endian: read as 0x00010203 = 66051
        expect(StringUtils.extractIntFromByteArray(bytes, 0, 4, true),
            equals(66051));
      });

      test('should extract with offset', () {
        final bytes = [0xFF, 0x00, 0x01, 0x02, 0x03];
        expect(StringUtils.extractIntFromByteArray(bytes, 1, 4, false),
            equals(66051));
      });

      test('should extract 2 bytes', () {
        final bytes = [0x01, 0x02];
        expect(StringUtils.extractIntFromByteArray(bytes, 0, 2, false),
            equals(0x0102));
      });

      test('should return 0 for invalid length', () {
        final bytes = [0x01, 0x02];
        expect(StringUtils.extractIntFromByteArray(bytes, 0, -1, false),
            equals(0));
        expect(
            StringUtils.extractIntFromByteArray(bytes, 0, 9, false), equals(0));
      });
    });

    group('intTo2HexString', () {
      test('should convert int to 2-byte hex string', () {
        expect(StringUtils.intTo2HexString(0x0102), equals('0102'));
      });

      test('should handle zero', () {
        expect(StringUtils.intTo2HexString(0), equals('0000'));
      });

      test('should handle max 2-byte value', () {
        expect(StringUtils.intTo2HexString(0xFFFF), equals('FFFF'));
      });
    });

    group('intTo2List', () {
      test('should convert int to 2-byte list', () {
        expect(StringUtils.intTo2List(0x0102), equals([0x01, 0x02]));
      });

      test('should handle zero', () {
        expect(StringUtils.intTo2List(0), equals([0x00, 0x00]));
      });

      test('should handle max 2-byte value', () {
        expect(StringUtils.intTo2List(0xFFFF), equals([0xFF, 0xFF]));
      });
    });

    group('byteListToInt', () {
      test('should convert 2-byte list to int', () {
        expect(StringUtils.byteListToInt([0x01, 0x02]), equals(0x0102));
      });

      test('should handle zero', () {
        expect(StringUtils.byteListToInt([0x00, 0x00]), equals(0));
      });

      test('should handle max value', () {
        expect(StringUtils.byteListToInt([0xFF, 0xFF]), equals(0xFFFF));
      });
    });

    group('minToSecond', () {
      test('should convert MM:SS to seconds', () {
        expect(StringUtils.minToSecond('02:30'), equals(150));
      });

      test('should handle zero', () {
        expect(StringUtils.minToSecond('00:00'), equals(0));
      });

      test('should return 0 for empty string', () {
        expect(StringUtils.minToSecond(''), equals(0));
      });

      test('should return 0 for invalid format', () {
        expect(StringUtils.minToSecond('invalid'), equals(0));
      });
    });

    group('roundtrip tests', () {
      test('byteToHexString and hexStringToBytes should be inverse', () {
        final original = [0x00, 0x1A, 0xFF, 0xAB, 0x12, 0x34];
        final hex = StringUtils.byteToHexString(original);
        final result = StringUtils.hexStringToBytes(hex);
        expect(result, equals(original));
      });

      test('intTo2List and byteListToInt should be inverse', () {
        const original = 0x1234;
        final list = StringUtils.intTo2List(original);
        final result = StringUtils.byteListToInt(list);
        expect(result, equals(original));
      });
    });
  });
}
