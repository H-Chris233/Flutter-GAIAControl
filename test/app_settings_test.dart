import 'dart:io';

import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/app_settings.dart';
import 'package:path_provider_platform_interface/path_provider_platform_interface.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class _FakePathProviderPlatform extends PathProviderPlatform
    with MockPlatformInterfaceMixin {
  _FakePathProviderPlatform({
    required this.documentsPath,
    this.externalStoragePath,
    this.throwOnExternalStorage = false,
    this.throwOnDocuments = false,
  });

  final String? documentsPath;
  final String? externalStoragePath;
  final bool throwOnExternalStorage;
  final bool throwOnDocuments;

  @override
  Future<String?> getApplicationDocumentsPath() async {
    if (throwOnDocuments) {
      throw StateError('documents path unavailable');
    }
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

void main() {
  group('AppSettings', () {
    late Directory docsDir;
    late Directory externalDir;
    late PathProviderPlatform originalPathProvider;

    setUp(() async {
      originalPathProvider = PathProviderPlatform.instance;
      docsDir = await Directory.systemTemp.createTemp('gaia_settings_docs_');
      externalDir =
          await Directory.systemTemp.createTemp('gaia_settings_external_');
    });

    tearDown(() async {
      PathProviderPlatform.instance = originalPathProvider;
      if (await docsDir.exists()) {
        await docsDir.delete(recursive: true);
      }
      if (await externalDir.exists()) {
        await externalDir.delete(recursive: true);
      }
    });

    test('读取不存在的设置文件返回 null', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);

      final path = await AppSettings.readLastFirmwarePath();

      expect(path, isNull);
    });

    test('写入会 trim 并可读回', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);

      await AppSettings.writeLastFirmwarePath('  /tmp/fw.bin  ');
      final value = await AppSettings.readLastFirmwarePath();

      expect(value, '/tmp/fw.bin');
      expect(File('${docsDir.path}/gaia_settings.json').existsSync(), isTrue);
    });

    test('写入空字符串会被忽略', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);

      await AppSettings.writeLastFirmwarePath('   ');
      final value = await AppSettings.readLastFirmwarePath();

      expect(value, isNull);
      expect(File('${docsDir.path}/gaia_settings.json').existsSync(), isFalse);
    });

    test('外部存储存在时优先使用外部存储目录', () async {
      PathProviderPlatform.instance = _FakePathProviderPlatform(
        documentsPath: docsDir.path,
        externalStoragePath: externalDir.path,
      );

      await AppSettings.writeLastFirmwarePath('/external/fw.bin');

      expect(
        File('${externalDir.path}/gaia_settings.json').existsSync(),
        isTrue,
      );
      expect(File('${docsDir.path}/gaia_settings.json').existsSync(), isFalse);
    });

    test('外部存储异常时回落到 documents 目录', () async {
      PathProviderPlatform.instance = _FakePathProviderPlatform(
        documentsPath: docsDir.path,
        throwOnExternalStorage: true,
      );

      await AppSettings.writeLastFirmwarePath('/docs/fw.bin');

      expect(File('${docsDir.path}/gaia_settings.json').existsSync(), isTrue);
    });

    test('读取时遇到非 Map JSON 返回 null', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      final file = File('${docsDir.path}/gaia_settings.json');
      await file.writeAsString('[]', flush: true);

      final value = await AppSettings.readLastFirmwarePath();

      expect(value, isNull);
    });

    test('读取时 key 缺失/为空白 返回 null；非空字符串会 trim', () async {
      PathProviderPlatform.instance =
          _FakePathProviderPlatform(documentsPath: docsDir.path);
      final file = File('${docsDir.path}/gaia_settings.json');

      await file.writeAsString('{}', flush: true);
      expect(await AppSettings.readLastFirmwarePath(), isNull);

      await file.writeAsString('{"lastFirmwarePath":"   "}', flush: true);
      expect(await AppSettings.readLastFirmwarePath(), isNull);

      await file.writeAsString('{"lastFirmwarePath":"  abc  "}', flush: true);
      expect(await AppSettings.readLastFirmwarePath(), 'abc');
    });

    test('PathProvider 异常会被吞掉并返回 null', () async {
      PathProviderPlatform.instance = _FakePathProviderPlatform(
        documentsPath: docsDir.path,
        throwOnDocuments: true,
      );

      final value = await AppSettings.readLastFirmwarePath();

      expect(value, isNull);
    });
  });
}

