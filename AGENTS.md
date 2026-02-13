# Repository Guidelines

## Project Structure & Module Organization
- `lib/` 是核心业务代码：`main.dart` 为入口，`test_ota_view.dart` 为 OTA 页面，`utils/gaia/` 与 `utils/gaia/rwcp/` 实现 GAIA/RWCP 协议细节。
- `lib/controller/ota_server.dart` 承载 BLE OTA 主要流程与状态管理。
- `test/` 放置测试代码；当前包含 `widget_test.dart`，新增测试请按功能模块拆分。
- `img/` 存放 README 展示图片；平台壳工程分别位于 `android/` 与 `ios/`。
- 根目录关键配置：`pubspec.yaml`（依赖与版本）、`analysis_options.yaml`（lint 规则）。

## Build, Test, and Development Commands
- `flutter pub get`：安装/同步依赖。
- `flutter analyze`：执行静态检查（基于 `flutter_lints`）。
- `flutter test`：运行全部测试。
- `flutter run -d android` 或 `flutter run -d ios`：本地调试运行。
- `flutter build apk --release`：构建 Android 发布包；iOS 使用 `flutter build ios --release`。

## Coding Style & Naming Conventions
- 使用 2 空格缩进，遵循 Dart 官方格式；提交前执行 `dart format lib test`。
- 类型使用 `PascalCase`，变量/方法使用 `lowerCamelCase`，常量使用 `lowerCamelCase` + `const`。
- 文件名使用 `snake_case.dart`；如需新增文件优先遵循该规则（历史文件命名可逐步迁移，不在一次 PR 中大改）。
- 保持实现简洁（KISS），避免重复逻辑（DRY），仅实现当前需求（YAGNI）。

## Testing Guidelines
- 测试框架：`flutter_test`。
- 测试文件命名：`*_test.dart`；测试描述建议采用“行为 + 预期结果”，如：`'startOta sends upgrade packet'`。
- 变更 `lib/utils/gaia/` 或 `OtaServer` 的协议/传输逻辑时，至少补充一个对应单元或组件测试。

## Commit & Pull Request Guidelines
- 当前提交历史以简短中文说明和 Conventional Commit 混用（如 `feat(android): ...`、`增加权限判断`）；建议统一为 Conventional Commit：`feat/fix/refactor/test/docs(scope): summary`。
- PR 需包含：变更目的、核心改动、验证步骤（命令与结果）、影响平台（Android/iOS）。
- 涉及 UI 或 OTA 流程变更时，附截图或关键日志片段，便于快速审阅与回归。

## Security & Configuration Tips
- 不要提交密钥、证书、真实设备标识或抓包敏感数据。
- 涉及蓝牙权限、存储路径与网络请求改动时，同步检查 Android Manifest、iOS `Info.plist` 与运行时权限逻辑。

## Architecture Notes
- 本项目以 Flutter UI 层 + OTA 服务层分离为主：页面负责交互展示，`OtaServer` 负责 BLE 通信、分包传输与升级状态机。
- 协议相关代码集中在 `lib/utils/gaia/`，新增协议能力时优先在该目录扩展，避免把协议细节散落到页面组件中。
- 引入新依赖前请评估是否可复用现有 `get`、`flutter_reactive_ble` 能力，减少包体积与维护成本。
- 若修改 OTA 流程，建议在 PR 描述中附上“连接设备 → 下发升级 → 结果校验”的最小复现步骤，便于审查与回归。
