import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/log.dart';

void main() {
  group('Log', () {
    setUp(() {
      Log.isLog = false;
    });

    test('isLog defaults to false', () {
      expect(Log.isLog, isFalse);
    });

    test('i/d/e/w methods exist and can be called without error', () {
      // These should not throw even when isLog is false
      expect(() => Log.i('TAG', 'info'), returnsNormally);
      expect(() => Log.d('TAG', 'debug'), returnsNormally);
      expect(() => Log.e('TAG', 'error'), returnsNormally);
      expect(() => Log.w('TAG', 'warning'), returnsNormally);
    });

    test('methods work when isLog is true', () {
      Log.isLog = true;
      // In test mode (kDebugMode is true), these should execute without error
      expect(() => Log.i('TAG', 'info message'), returnsNormally);
      expect(() => Log.d('TAG', 'debug message'), returnsNormally);
      expect(() => Log.e('TAG', 'error message'), returnsNormally);
      expect(() => Log.w('TAG', 'warning message'), returnsNormally);
    });

    test('handles empty strings', () {
      Log.isLog = true;
      expect(() => Log.i('', ''), returnsNormally);
      expect(() => Log.d('', ''), returnsNormally);
      expect(() => Log.e('', ''), returnsNormally);
      expect(() => Log.w('', ''), returnsNormally);
    });

    test('handles special characters', () {
      Log.isLog = true;
      expect(() => Log.i('TAG', '中文日志 🎉'), returnsNormally);
      expect(() => Log.d('TAG', 'line1\nline2'), returnsNormally);
      expect(() => Log.e('TAG', 'path/to/file.dart:123'), returnsNormally);
    });
  });

  group('LogLevel', () {
    test('has all expected values', () {
      expect(LogLevel.values, hasLength(4));
      expect(LogLevel.values, contains(LogLevel.info));
      expect(LogLevel.values, contains(LogLevel.debug));
      expect(LogLevel.values, contains(LogLevel.error));
      expect(LogLevel.values, contains(LogLevel.warning));
    });
  });
}
