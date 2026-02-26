import 'dart:async';
import 'dart:convert';
import 'dart:io';
import 'dart:isolate';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:path_provider/path_provider.dart';

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

  static Future<void> init() async {
    final docs = await getApplicationDocumentsDirectory();
    final reportsDir = Directory("${docs.path}/crash_reports");
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
      recordError(
        details.exception,
        details.stack ?? StackTrace.current,
        context: "FlutterError",
      );
    };

    WidgetsBinding.instance.platformDispatcher.onError = (error, stack) {
      recordError(error, stack, context: "PlatformDispatcher");
      return false;
    };

    _isolateErrorPort?.close();
    _isolateErrorPort = RawReceivePort((dynamic message) {
      try {
        if (message is List && message.length >= 2) {
          final error = message[0];
          final stack = StackTrace.fromString("${message[1]}");
          recordError(error, stack, context: "Isolate");
        }
      } catch (_) {}
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
    } catch (_) {
      return null;
    }
  }

  void recordError(
    Object error,
    StackTrace stack, {
    required String context,
  }) {
    if (_instance == null) {
      return;
    }
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
      } catch (_) {
        extra = {};
      }

      String logs = "";
      try {
        logs = _logSnapshotProvider?.call() ?? "";
      } catch (_) {
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
        file.writeAsStringSync(content.toString(), flush: true);
      } catch (_) {}

      try {
        _pendingFile.writeAsStringSync(
          jsonEncode({"path": file.path, "ts": ts}),
          flush: true,
        );
      } catch (_) {}
    } finally {
      _isRecording = false;
    }
  }

  void dispose() {
    try {
      if (_isolateErrorPort != null) {
        Isolate.current.removeErrorListener(_isolateErrorPort!.sendPort);
      }
    } catch (_) {}
    _isolateErrorPort?.close();
    _isolateErrorPort = null;
  }
}
