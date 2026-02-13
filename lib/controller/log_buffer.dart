import 'dart:async';
import 'dart:collection';

import 'package:flutter/foundation.dart';
import 'package:get/get.dart';

/// 日志缓冲组件
///
/// 负责日志的缓冲、去重、刷新显示。
/// 支持重复日志折叠、行数限制、批量刷新优化。
class LogBuffer {
  /// UI 绑定的日志文本
  final RxString logText;

  /// 待刷新的日志队列
  final ListQueue<String> _pendingLogs = ListQueue();

  /// 日志刷新定时器
  Timer? _logFlushTimer;

  /// 是否已调度刷新
  bool _isLogFlushScheduled = false;

  /// 最大保留行数
  final int maxLogLines;

  /// 上一条日志去重 key
  String _lastLogDedupKey = "";

  /// 上一条日志重复次数
  int _lastLogRepeat = 0;

  /// 构造函数
  ///
  /// [logText] 外部传入的 RxString，用于 UI 绑定
  /// [maxLogLines] 最大保留行数，默认 800
  LogBuffer({
    required this.logText,
    this.maxLogLines = 800,
  });

  /// 添加日志
  ///
  /// 自动进行重复日志折叠和批量刷新
  void addLog(String message) {
    if (kDebugMode) {
      debugPrint(message);
    }
    final dedupKey = _normalizeLogKey(message);
    if (_lastLogDedupKey.isEmpty) {
      _lastLogDedupKey = dedupKey;
      _lastLogRepeat = 1;
      _pendingLogs.add(message);
      _scheduleLogFlush();
      return;
    }

    if (dedupKey == _lastLogDedupKey) {
      _lastLogRepeat += 1;
      _scheduleLogFlush();
      return;
    }

    _emitRepeatSummaryIfNeeded();
    _lastLogDedupKey = dedupKey;
    _lastLogRepeat = 1;
    _pendingLogs.add(message);
    _scheduleLogFlush();
  }

  /// 清空日志
  void clear() {
    logText.value = "";
    _pendingLogs.clear();
    _lastLogDedupKey = "";
    _lastLogRepeat = 0;
  }

  /// 释放资源
  void dispose() {
    _logFlushTimer?.cancel();
    _logFlushTimer = null;
  }

  /// 归一化日志 key（去除时间戳前缀）
  String _normalizeLogKey(String message) {
    final withoutTimestamp = message.replaceFirst(
        RegExp(r'^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d+\s+'), "");
    return withoutTimestamp.trim();
  }

  /// 如果有重复日志，输出折叠摘要
  void _emitRepeatSummaryIfNeeded() {
    if (_lastLogRepeat > 1) {
      _pendingLogs.add("↳ 上一条重复 ${_lastLogRepeat - 1} 次");
      _lastLogRepeat = 1;
    }
  }

  /// 调度日志刷新（防抖 120ms）
  void _scheduleLogFlush() {
    if (_isLogFlushScheduled) {
      return;
    }
    _isLogFlushScheduled = true;
    _logFlushTimer?.cancel();
    _logFlushTimer = Timer(const Duration(milliseconds: 120), _flushLogs);
  }

  /// 执行日志刷新
  void _flushLogs() {
    _isLogFlushScheduled = false;
    _emitRepeatSummaryIfNeeded();
    if (_pendingLogs.isEmpty) {
      return;
    }
    final builder = StringBuffer();
    while (_pendingLogs.isNotEmpty) {
      builder.writeln(_pendingLogs.removeFirst());
    }
    final merged = (logText.value + builder.toString());
    final lines = merged.split('\n');
    if (lines.length <= maxLogLines) {
      logText.value = merged;
      return;
    }
    final start = lines.length - maxLogLines;
    logText.value = lines.sublist(start).join('\n');
  }
}
