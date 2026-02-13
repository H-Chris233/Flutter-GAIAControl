import 'package:flutter/foundation.dart';

/// 日志级别枚举
enum LogLevel { info, debug, error, warning }

/// 简易日志工具类
///
/// 使用方式：
/// ```dart
/// Log.isLog = true; // 启用日志
/// Log.i("TAG", "info message");
/// Log.d("TAG", "debug message");
/// Log.e("TAG", "error message");
/// Log.w("TAG", "warning message");
/// ```
class Log {
  static bool isLog = false;

  /// 统一日志输出方法
  static void _log(LogLevel level, String tag, String msg) {
    if (!isLog || !kDebugMode) return;
    final prefix = _levelPrefix(level);
    debugPrint("$prefix$tag $msg");
  }

  static String _levelPrefix(LogLevel level) {
    switch (level) {
      case LogLevel.info:
        return '[I] ';
      case LogLevel.debug:
        return '[D] ';
      case LogLevel.error:
        return '[E] ';
      case LogLevel.warning:
        return '[W] ';
    }
  }

  /// Info 级别日志
  static void i(String tag, String msg) => _log(LogLevel.info, tag, msg);

  /// Debug 级别日志
  static void d(String tag, String msg) => _log(LogLevel.debug, tag, msg);

  /// Error 级别日志
  static void e(String tag, String msg) => _log(LogLevel.error, tag, msg);

  /// Warning 级别日志
  static void w(String tag, String msg) => _log(LogLevel.warning, tag, msg);
}
