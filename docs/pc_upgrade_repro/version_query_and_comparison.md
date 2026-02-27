# 版本查询（当前/升级前/升级后）与“UNKNOWN”问题规避

本项目的版本查询统一使用 GAIA v3 Framework Feature 的 `GET_APPLICATION_VERSION`（手册 3.1.6）。

## 1. 固定 bytes

请求（固定）：

```
00 1D 00 05
```

响应：

- `Vendor=001D`
- `Command=0105`（feature=0x00, type=Response, id=0x05）
- `Payload=Application version text`（可能为空）

电脑端建议：

- payload 为空：显示 `UNKNOWN`
- payload 为可打印 ASCII：按文本显示（如 `"V1"`）
- payload 非文本：降级显示 HEX

## 2. 当前版本轮询（UI 用）

本仓库 UI 在 `TestOtaView` 顶部状态栏显示：

- `当前版本: <currentVersion>`

轮询策略（`OtaServer.startCurrentVersionPolling`）：

- 间隔：1s
- 条件：已连接 + 不在升级 + 当前没有 version query in-flight
- 行为：发送一次 GET_APPLICATION_VERSION，更新 `currentVersion`

电脑端建议用同样策略，避免升级过程干扰。

## 3. 升级前版本（Version Before）

升级按钮触发时先发一次 GET_APPLICATION_VERSION，记录为 `versionBefore`，然后再开始升级流程。

## 4. 升级后版本（Version After）与 UNKNOWN 的根因

你描述的现象：“查询后版本号一直是 UNKNOWN”，在本项目中常见根因是：

- **同一时刻有两个定时器/逻辑都在发 GET_APPLICATION_VERSION**（例如：当前版本轮询 + 升级后版本查询）
- 但实现层做了“同一时间只允许 1 个版本查询 in-flight”的保护（避免并发请求）
- 如果“当前版本轮询”的频率更高（1s），“升级后版本查询”（2s）可能长期抢不到机会；
  同时重试计数仍在递增，最后直接超时退出，导致 `versionAfter` 一直不更新

本仓库已做的修复（`lib/controller/ota_server.dart`）：

1. 在开始“升级后版本查询”窗口期间，**暂时抑制当前版本轮询**（`_suppressCurrentVersionPolling`）。
2. 升级后版本查询成功后，同时刷新 `currentVersion`（避免 UI 仍显示旧值）。

## 5. 电脑端实现建议（最小规约）

为了彻底杜绝 UNKNOWN：

- 全局只维护一个 “ApplicationVersionQuery” 通道：
  - 同一时刻只允许 1 个 outstanding request
  - 任何模块想查询版本都必须排队或被拒绝
- 或者更简单：当你处于“升级后版本查询窗口”时，暂停“当前版本轮询”

> 重点不是 UI，而是避免“同一个 response 被错误地路由到另一个请求的回调”或“重试次数被抢占消耗”。

