import 'dart:convert';
import 'dart:io';

import 'package:path_provider/path_provider.dart';

class AppSettings {
  static const String _fileName = "gaia_settings.json";
  static const String _keyLastFirmwarePath = "lastFirmwarePath";

  static Future<Directory> _baseDir() async {
    try {
      final external = await getExternalStorageDirectory();
      if (external != null) {
        return external;
      }
    } catch (_) {}
    return getApplicationDocumentsDirectory();
  }

  static Future<File> _settingsFile() async {
    final dir = await _baseDir();
    return File("${dir.path}/$_fileName");
  }

  static Future<String?> readLastFirmwarePath() async {
    try {
      final file = await _settingsFile();
      if (!await file.exists()) {
        return null;
      }
      final raw = await file.readAsString();
      final json = jsonDecode(raw);
      if (json is! Map) {
        return null;
      }
      final value = json[_keyLastFirmwarePath];
      if (value is String && value.trim().isNotEmpty) {
        return value.trim();
      }
      return null;
    } catch (_) {
      return null;
    }
  }

  static Future<void> writeLastFirmwarePath(String path) async {
    final trimmed = path.trim();
    if (trimmed.isEmpty) {
      return;
    }
    try {
      final file = await _settingsFile();
      final payload = <String, Object?>{
        _keyLastFirmwarePath: trimmed,
        "updatedAt": DateTime.now().toIso8601String(),
      };
      await file.writeAsString(jsonEncode(payload), flush: true);
    } catch (_) {}
  }
}
