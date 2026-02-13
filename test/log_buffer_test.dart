import 'package:fake_async/fake_async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:get/get.dart';
import 'package:gaia/controller/log_buffer.dart';

void main() {
  group('LogBuffer', () {
    late LogBuffer logBuffer;
    late RxString logText;

    setUp(() {
      logText = ''.obs;
      logBuffer = LogBuffer(logText: logText);
    });

    tearDown(() {
      logBuffer.dispose();
    });

    test('addLog adds message to pending logs', () {
      fakeAsync((async) {
        logBuffer.addLog('test message');
        // 驱动定时器触发刷新
        async.elapse(const Duration(milliseconds: 150));
        expect(logText.value.contains('test message'), isTrue);
      });
    });

    test('addLog deduplicates repeated messages', () {
      fakeAsync((async) {
        logBuffer.addLog('repeated message');
        logBuffer.addLog('repeated message');
        logBuffer.addLog('repeated message');
        async.elapse(const Duration(milliseconds: 150));
        // 应该只有一条消息加上重复摘要
        expect(logText.value.contains('repeated message'), isTrue);
        expect(logText.value.contains('重复'), isTrue);
      });
    });

    test('clear resets all state', () {
      fakeAsync((async) {
        logBuffer.addLog('message 1');
        async.elapse(const Duration(milliseconds: 150));
        logBuffer.clear();
        expect(logText.value, isEmpty);
      });
    });

    test('respects maxLogLines limit', () {
      fakeAsync((async) {
        final smallBuffer = LogBuffer(logText: logText, maxLogLines: 5);
        for (int i = 0; i < 10; i++) {
          smallBuffer.addLog('line $i');
        }
        async.elapse(const Duration(milliseconds: 150));
        final lines =
            logText.value.split('\n').where((l) => l.isNotEmpty).toList();
        expect(lines.length, lessThanOrEqualTo(5));
        smallBuffer.dispose();
      });
    });

    test('flush does not emit when no pending logs', () {
      fakeAsync((async) {
        // 不添加任何日志，只触发定时器
        async.elapse(const Duration(milliseconds: 200));
        expect(logText.value, isEmpty);
      });
    });

    test('multiple flushes accumulate correctly', () {
      fakeAsync((async) {
        logBuffer.addLog('first');
        async.elapse(const Duration(milliseconds: 150));
        logBuffer.addLog('second');
        async.elapse(const Duration(milliseconds: 150));
        expect(logText.value.contains('first'), isTrue);
        expect(logText.value.contains('second'), isTrue);
      });
    });
  });
}
