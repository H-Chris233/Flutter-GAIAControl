import 'dart:io';

import 'package:flutter/foundation.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/crash_reporter.dart';
import 'package:path_provider_platform_interface/path_provider_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class _FakePathProviderPlatform extends PathProviderPlatform
    with MockPlatformInterfaceMixin {
  _FakePathProviderPlatform({
    required this.documentsPath,
    this.externalStoragePath,
    this.throwOnExternalStorage = false,
  });

  final String? documentsPath;
  final String? externalStoragePath;
  final bool throwOnExternalStorage;

  @override
  Future<String?> getApplicationDocumentsPath() async {
    return documentsPath;
  }

  @override
  Future<String?> getExternalStoragePath() async {
    if (throwOnExternalStorage) {
      throw StateError('external storage unavailable');
    }
    return externalStoragePath;
  }
}

Future<void> _drainMicrotasks([int times = 3]) async {
  for (var i = 0; i < times; i += 1) {
    await Future<void>.delayed(Duration.zero);
  }
}

void main() {
  group('CrashReporter', () {
    late Directory docsDir;
    late Directory externalDir;
    late PathProviderPlatform originalPathProvider;
    FlutterExceptionHandler? originalFlutterOnError;
    bool Function(Object, StackTrace)? originalDispatcherOnError;

    setUpAll(() {
      TestWidgetsFlutterBinding.ensureInitialized();
    });

    setUp(() async {
      CrashReporter.maybeInstance?.dispose();
      originalPathProvider = PathProviderPlatform.instance;
      docsDir = await Directory.systemTemp.createTemp('gaia_crash_docs_');
      externalDir = await Directory.systemTemp.createTemp('gaia_crash_ext_');
      originalFlutterOnError = FlutterError.onError;
      originalDispatcherOnError = WidgetsBinding.instance.platformDispatcher.onError;
    });

    tearDown(() async {
      FlutterError.onError = originalFlutterOnError;
      WidgetsBinding.instance.platformDispatcher.onError =
          originalDispatcherOnError;
      CrashReporter.maybeInstance?.dispose();
      PathProviderPlatform.instance = originalPathProvider;
      if (await docsDir.exists()) {
        await docsDir.delete(recursive: true);
      }
      if (await externalDir.exists()) {
        await externalDir.delete(recursive: true);
      }
    });

    test('init 会创建 crash_reports 目录（external 为 null 时使用 documents）',
        () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);

      await CrashReporter.init();

      expect(
        Directory('${docsDir.path}/crash_reports').existsSync(),
        isTrue,
      );
      expect(CrashReporter.maybeInstance, isNotNull);
    });

    test('recordError 会写入报告、pending，并包含 extra/logs', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      reporter.setContextProvider(() => <String, Object?>{'k': 1});
      reporter.setLogSnapshotProvider(() => 'LOG_SNAPSHOT');
      reporter.recordError(
        StateError('boom'),
        StackTrace.current,
        context: 'UnitTest',
      );

      final path = await reporter.consumePendingReportPath();
      expect(path, isNotNull);
      expect(File(path!).existsSync(), isTrue);

      final content = await File(path).readAsString();
      expect(content, contains('context: UnitTest'));
      expect(content, contains('boom'));
      expect(content, contains('LOG_SNAPSHOT'));
      expect(content, contains('"k": 1'));
    });

    test('recordError 会吞掉 provider 异常，并避免重入写入', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      reporter.setContextProvider(() {
        reporter.recordError(
          'inner',
          StackTrace.current,
          context: 'Inner',
        );
        throw StateError('provider fail');
      });
      reporter.setLogSnapshotProvider(() {
        throw StateError('log fail');
      });

      reporter.recordError(
        'outer',
        StackTrace.current,
        context: 'Outer',
      );

      final path = await reporter.consumePendingReportPath();
      expect(path, isNotNull);
      final content = await File(path!).readAsString();
      expect(content, contains('context: Outer'));
      expect(content, isNot(contains('Inner')));
    });

    test('installGlobalHandlers：Isolate 错误消息会触发 recordError', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      reporter.installGlobalHandlers();
      reporter.installGlobalHandlers();

      final sendPort = reporter.isolateErrorSendPortForTesting;
      expect(sendPort, isNotNull);

      sendPort!.send('ignored');
      sendPort.send(<Object>['only-one']);
      sendPort.send(<Object>[StateError('iso'), StackTrace.current.toString()]);
      await _drainMicrotasks();

      final path = await reporter.consumePendingReportPath();
      expect(path, isNotNull);
      final content = await File(path!).readAsString();
      expect(content, contains('context: Isolate'));
      expect(content, contains('iso'));
    });

    test('installGlobalHandlers：FlutterError 与 PlatformDispatcher 会触发记录',
        () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      reporter.installGlobalHandlers();

      FlutterError.onError?.call(FlutterErrorDetails(
        exception: StateError('flutter'),
        stack: StackTrace.current,
      ));
      final flutterPath = await reporter.consumePendingReportPath();
      expect(flutterPath, isNotNull);
      final flutterContent = await File(flutterPath!).readAsString();
      expect(flutterContent, contains('context: FlutterError'));
      expect(flutterContent, contains('flutter'));

      final platformResult = WidgetsBinding.instance.platformDispatcher.onError
          ?.call(StateError('dispatcher'), StackTrace.current);
      expect(platformResult, isFalse);
      final dispatcherPath = await reporter.consumePendingReportPath();
      expect(dispatcherPath, isNotNull);
      final dispatcherContent = await File(dispatcherPath!).readAsString();
      expect(dispatcherContent, contains('context: PlatformDispatcher'));
      expect(dispatcherContent, contains('dispatcher'));
    });

    test('consumePendingReportPath：不存在/无效 JSON/非 Map/空 path 均返回 null',
        () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      expect(await reporter.consumePendingReportPath(), isNull);

      final pending = File('${docsDir.path}/crash_reports/_pending.json');

      await pending.writeAsString('{', flush: true);
      expect(await reporter.consumePendingReportPath(), isNull);
      expect(pending.existsSync(), isTrue);

      await pending.writeAsString('[]', flush: true);
      expect(await reporter.consumePendingReportPath(), isNull);
      expect(pending.existsSync(), isFalse);

      await pending.writeAsString('{"path":""}', flush: true);
      expect(await reporter.consumePendingReportPath(), isNull);
      expect(pending.existsSync(), isFalse);
    });

    test('consumePendingReportPath：有效 path 会返回并删除 pending 文件', () async {
      PathProviderPlatform.instance = _FakePathProviderPlatform(
        documentsPath: docsDir.path,
        externalStoragePath: externalDir.path,
      );
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      final pending = File('${externalDir.path}/crash_reports/_pending.json');
      await pending.writeAsString(
        '{"path":"/tmp/x","ts":"${DateTime.now().toIso8601String()}"}',
        flush: true,
      );

      final value = await reporter.consumePendingReportPath();
      expect(value, '/tmp/x');
      expect(pending.existsSync(), isFalse);
    });

    test('dispose 可安全重复调用', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      reporter.installGlobalHandlers();
      reporter.dispose();
      reporter.dispose();

      expect(CrashReporter.maybeInstance, isNotNull);
    });

    test('isolateErrorSendPortForTesting 在未安装 handler 时为 null', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();

      expect(CrashReporter.instance.isolateErrorSendPortForTesting, isNull);
    });

    test('recordError 会写入 mode 字段（debug/profile/release）', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      await CrashReporter.init();
      final reporter = CrashReporter.instance;

      reporter.recordError(
        'mode',
        StackTrace.current,
        context: 'Mode',
      );
      final path = await reporter.consumePendingReportPath();
      expect(path, isNotNull);
      final content = await File(path!).readAsString();
      final expected = kReleaseMode ? 'release' : (kProfileMode ? 'profile' : 'debug');
      expect(content, contains('mode: $expected'));
    });
  });
}
