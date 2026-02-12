import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/rwcp/RWCP.dart';
import 'package:gaia/utils/gaia/rwcp/Segment.dart';

void main() {
  group('Segment', () {
    group('get factory method', () {
      test('builds segment with correct header format', () {
        // DATA opcode = 0, sequence = 5
        final segment = Segment.get(RWCPOpCodeClient.DATA, 5);
        final bytes = segment.getBytes();

        // Header: (opcode << 6) | sequence = (0 << 6) | 5 = 5
        expect(bytes[0], 5);
        expect(segment.getOperationCode(), RWCPOpCodeClient.DATA);
        expect(segment.getSequenceNumber(), 5);
      });

      test('builds SYN segment correctly', () {
        // SYN opcode = 1, sequence = 0
        final segment = Segment.get(RWCPOpCodeClient.SYN, 0);
        final bytes = segment.getBytes();

        // Header: (1 << 6) | 0 = 64
        expect(bytes[0], 64);
        expect(segment.getOperationCode(), RWCPOpCodeClient.SYN);
        expect(segment.getSequenceNumber(), 0);
      });

      test('builds RST segment correctly', () {
        // RST opcode = 2, sequence = 10
        final segment = Segment.get(RWCPOpCodeClient.RST, 10);
        final bytes = segment.getBytes();

        // Header: (2 << 6) | 10 = 138
        expect(bytes[0], 138);
        expect(segment.getOperationCode(), RWCPOpCodeClient.RST);
        expect(segment.getSequenceNumber(), 10);
      });

      test('builds segment with payload', () {
        final segment = Segment.get(
          RWCPOpCodeClient.DATA,
          3,
          payload: [0xAA, 0xBB, 0xCC],
        );
        final bytes = segment.getBytes();

        expect(bytes[0], 3); // Header: (0 << 6) | 3
        expect(bytes.sublist(1), [0xAA, 0xBB, 0xCC]);
        expect(segment.getPayload(), [0xAA, 0xBB, 0xCC]);
      });

      test('builds segment with empty payload', () {
        final segment = Segment.get(RWCPOpCodeClient.DATA, 0, payload: []);

        expect(segment.getPayload(), isEmpty);
        expect(segment.getBytes().length, 1);
      });

      test('builds segment with max sequence number', () {
        // Max sequence = 63 (6 bits)
        final segment = Segment.get(RWCPOpCodeClient.DATA, 63);

        expect(segment.getSequenceNumber(), 63);
        // Header: (0 << 6) | 63 = 63
        expect(segment.getBytes()[0], 63);
      });
    });

    group('parse', () {
      test('parses segment from bytes', () {
        // DATA_ACK with sequence 7: (0 << 6) | 7 = 7
        final segment = Segment.parse([7, 0xAA, 0xBB]);

        expect(segment.getOperationCode(), RWCPOpCodeServer.DATA_ACK);
        expect(segment.getSequenceNumber(), 7);
        expect(segment.getPayload(), [0xAA, 0xBB]);
      });

      test('parses SYN_ACK segment', () {
        // SYN_ACK opcode = 1, sequence = 0: (1 << 6) | 0 = 64
        final segment = Segment.parse([64]);

        expect(segment.getOperationCode(), RWCPOpCodeServer.SYN_ACK);
        expect(segment.getSequenceNumber(), 0);
        expect(segment.getPayload(), isEmpty);
      });

      test('parses RST_ACK segment', () {
        // RST_ACK opcode = 2, sequence = 5: (2 << 6) | 5 = 133
        final segment = Segment.parse([133]);

        expect(segment.getOperationCode(), RWCPOpCodeServer.RST_ACK);
        expect(segment.getSequenceNumber(), 5);
      });

      test('parses GAP segment', () {
        // GAP opcode = 3, sequence = 10: (3 << 6) | 10 = 202
        final segment = Segment.parse([202]);

        expect(segment.getOperationCode(), RWCPOpCodeServer.GAP);
        expect(segment.getSequenceNumber(), 10);
      });

      test('handles null bytes', () {
        final segment = Segment.parse(null);

        expect(segment.getOperationCode(), -1);
        expect(segment.getSequenceNumber(), -1);
        expect(segment.getHeader(), -1);
      });

      test('handles empty bytes', () {
        final segment = Segment.parse([]);

        expect(segment.getOperationCode(), -1);
        expect(segment.getSequenceNumber(), -1);
      });

      test('preserves original bytes in mBytes', () {
        final originalBytes = [7, 0x11, 0x22];
        final segment = Segment.parse(originalBytes);

        expect(segment.getBytes(), originalBytes);
      });
    });

    group('getHeader', () {
      test('returns correct header value', () {
        final segment = Segment.get(RWCPOpCodeClient.RST, 15);

        // Header: (2 << 6) | 15 = 143
        expect(segment.getHeader(), 143);
      });

      test('returns -1 for invalid parsed segment', () {
        final segment = Segment.parse(null);

        expect(segment.getHeader(), -1);
      });
    });

    group('round-trip', () {
      test('segment survives build-parse cycle', () {
        final original = Segment.get(
          RWCPOpCodeClient.DATA,
          42,
          payload: [0x01, 0x02, 0x03],
        );
        final bytes = original.getBytes();
        final restored = Segment.parse(bytes);

        expect(restored.getOperationCode(), original.getOperationCode());
        expect(restored.getSequenceNumber(), original.getSequenceNumber());
        expect(restored.getPayload(), original.getPayload());
      });

      test('header-only segment survives round-trip', () {
        final original = Segment.get(RWCPOpCodeClient.SYN, 0);
        final restored = Segment.parse(original.getBytes());

        expect(restored.getOperationCode(), RWCPOpCodeClient.SYN);
        expect(restored.getSequenceNumber(), 0);
      });
    });

    group('getBits static method', () {
      test('extracts bits correctly', () {
        // Value: 0b11010101 = 213
        // Bits 0-5 (sequence): 010101 = 21
        // Bits 6-7 (opcode): 11 = 3
        expect(Segment.getBits(213, 0, 6), 21);
        expect(Segment.getBits(213, 6, 2), 3);
      });

      test('extracts single bit', () {
        expect(Segment.getBits(0x80, 7, 1), 1);
        expect(Segment.getBits(0x7F, 7, 1), 0);
      });
    });
  });
}
