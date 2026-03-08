import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:isolate';
import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:path_provider/path_provider.dart';

import 'log.dart';

typedef CrashContextProvider = Map<String, Object?> Function();
typedef CrashLogSnapshotProvider = String Function();

class CrashReporter {
  CrashReporter._(this._reportsDir, this._pendingFile);

  static CrashReporter? _instance;
  static CrashReporter get instance => _instance!;
  static CrashReporter? get maybeInstance => _instance;

  final Directory _reportsDir;
  final File _pendingFile;

  CrashContextProvider? _contextProvider;
  CrashLogSnapshotProvider? _logSnapshotProvider;

  bool _isRecording = false;
  RawReceivePort? _isolateErrorPort;

  @visibleForTesting
  SendPort? get isolateErrorSendPortForTesting => _isolateErrorPort?.sendPort;

  static Future<void> init() async {
    final docs = await getApplicationDocumentsDirectory();
    var baseDir = docs;
    // release 模式下默认落盘到应用私有目录，避免敏感崩溃信息写入 external。
    // debug/profile 下若 external 可用，则优先使用（便于用户/开发者导出日志）。
    if (!kReleaseMode) {
      try {
        final external = await getExternalStorageDirectory();
        if (external != null) {
          baseDir = external;
        }
      } catch (e, s) {
        Log.w("CrashReporter", "getExternalStorageDirectory failed: $e\n$s");
      }
    }

    final reportsDir = Directory("${baseDir.path}/crash_reports");
    if (!await reportsDir.exists()) {
      await reportsDir.create(recursive: true);
    }
    final pendingFile = File("${reportsDir.path}/_pending.json");
    _instance = CrashReporter._(reportsDir, pendingFile);
  }

  void setContextProvider(CrashContextProvider provider) {
    _contextProvider = provider;
  }

  void setLogSnapshotProvider(CrashLogSnapshotProvider provider) {
    _logSnapshotProvider = provider;
  }

  void installGlobalHandlers() {
    FlutterError.onError = (details) {
      FlutterError.dumpErrorToConsole(details);
      unawaited(recordError(
        details.exception,
        details.stack ?? StackTrace.current,
        context: "FlutterError",
      ));
    };

    // 注意：使用 PlatformDispatcher.instance，避免依赖 WidgetsBinding 的初始化时机；
    // WidgetsBinding.instance.platformDispatcher 在实现上也会委托到该实例。
    bool handler(Object error, StackTrace stack) {
      unawaited(recordError(error, stack, context: "PlatformDispatcher"));
      return false;
    }
    PlatformDispatcher.instance.onError = handler;
    // 防御性同步：部分测试/嵌入环境可能通过 WidgetsBinding 读取该回调。
    try {
      WidgetsBinding.instance.platformDispatcher.onError = handler;
    } catch (e, s) {
      Log.w("CrashReporter", "bind WidgetsBinding platformDispatcher failed: $e\n$s");
    }

    try {
      if (_isolateErrorPort != null) {
        Isolate.current.removeErrorListener(_isolateErrorPort!.sendPort);
      }
    } catch (e, s) {
      Log.w("CrashReporter", "removeErrorListener failed: $e\n$s");
    }
    _isolateErrorPort?.close();
    _isolateErrorPort = RawReceivePort((dynamic message) {
      try {
        if (message is List && message.length >= 2) {
          final error = message[0];
          final stack = StackTrace.fromString("${message[1]}");
          unawaited(recordError(error, stack, context: "Isolate"));
        }
      } catch (e, s) {
        Log.w("CrashReporter", "isolate error handler failed: $e\n$s");
      }
    });
    Isolate.current.addErrorListener(_isolateErrorPort!.sendPort);
  }

  Future<String?> consumePendingReportPath() async {
    try {
      if (!await _pendingFile.exists()) {
        return null;
      }
      final raw = await _pendingFile.readAsString();
      final json = jsonDecode(raw);
      if (json is! Map) {
        await _pendingFile.delete();
        return null;
      }
      final path = json["path"];
      await _pendingFile.delete();
      if (path is String && path.isNotEmpty) {
        return path;
      }
      return null;
    } catch (e, s) {
      Log.w("CrashReporter", "consumePendingReportPath failed: $e\n$s");
      return null;
    }
  }

  Future<void> recordError(
    Object error,
    StackTrace stack, {
    required String context,
  }) async {
    if (_isRecording) {
      return;
    }
    _isRecording = true;
    try {
      final now = DateTime.now();
      final ts = now.toIso8601String();
      final ms = now.millisecondsSinceEpoch;
      final file = File("${_reportsDir.path}/crash_$ms.txt");

      Map<String, Object?> extra = {};
      try {
        extra = _contextProvider?.call() ?? {};
      } catch (e, s) {
        Log.w("CrashReporter", "contextProvider failed: $e\n$s");
        extra = {};
      }

      String logs = "";
      try {
        logs = _logSnapshotProvider?.call() ?? "";
      } catch (e, s) {
        Log.w("CrashReporter", "logSnapshotProvider failed: $e\n$s");
        logs = "";
      }

      final payload = <String, Object?>{
        "ts": ts,
        "context": context,
        "mode": kReleaseMode ? "release" : (kProfileMode ? "profile" : "debug"),
        "error": "$error",
        "stack": "$stack",
        "extra": extra,
      };

      final content = StringBuffer()
        ..writeln("=== GAIA Control Crash Report ===")
        ..writeln("ts: $ts")
        ..writeln("context: $context")
        ..writeln("mode: ${payload["mode"]}")
        ..writeln("")
        ..writeln("error:")
        ..writeln(payload["error"])
        ..writeln("")
        ..writeln("stack:")
        ..writeln(payload["stack"])
        ..writeln("")
        ..writeln("extra:")
        ..writeln(const JsonEncoder.withIndent("  ").convert(extra))
        ..writeln("")
        ..writeln("logs:")
        ..writeln(logs);

      try {
        await file.writeAsString(content.toString(), flush: true);
      } catch (e, s) {
        Log.w("CrashReporter", "write crash report failed: $e\n$s");
      }

      try {
        await _pendingFile.writeAsString(
          jsonEncode({"path": file.path, "ts": ts}),
          flush: true,
        );
      } catch (e, s) {
        Log.w("CrashReporter", "write pending file failed: $e\n$s");
      }
    } finally {
      _isRecording = false;
    }
  }

  void dispose() {
    try {
      if (_isolateErrorPort != null) {
        Isolate.current.removeErrorListener(_isolateErrorPort!.sendPort);
      }
    } catch (e, s) {
      Log.w("CrashReporter", "dispose removeErrorListener failed: $e\n$s");
    }
    _isolateErrorPort?.close();
    _isolateErrorPort = null;
  }
}
