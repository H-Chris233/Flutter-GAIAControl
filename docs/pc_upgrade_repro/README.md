# PC 端 GAIA v3 升级复现文档集（对齐 Flutter-GAIAControl）

本文档集用于把本仓库的 BLE OTA 升级流程**完整迁移到电脑端应用**（Windows/macOS/Linux 均可），目标是“字节级一致地复现核心链路”，便于同事独立实现与联调。

> 约束：本文档按你的要求只描述**一条通的 happy-path**（不展开错误处理/重连/异常分支），但会把协议里**必须做**的 ACK/状态推进写清楚。

## 参考实现与文件定位

- Flutter 端实现（本仓库）
  - 升级总控：`lib/controller/ota_server.dart`
  - 升级状态机（VMU 消息流转）：`lib/controller/upgrade_state_machine.dart`
  - GAIA v3 命令编码：`lib/controller/gaia_command_builder.dart`
  - GAIA PDU（Vendor+Cmd+Payload）：`lib/utils/gaia/gaia_packet_ble.dart`
  - VMU Packet（OpCode+Len+Data）：`lib/utils/gaia/vmu_packet.dart`
  - RWCP（可靠传输）：`lib/utils/gaia/rwcp/*`
- Qualcomm GAIA Client 源码（对照）
  - V3 升级插件：`gaia-client-src/android_src/app-core/.../v3/V3UpgradePlugin.java`
  - Upgrade 状态/消息：`gaia-client-src/android_src/lib-upgrade/.../UpgradeManagerImpl.java`
  - DataReader offset 语义：`gaia-client-src/android_src/lib-upgrade/.../data/DataReader.java`
- 官方手册（对照）
  - `80-CH482-1_REV_AH_Gaia_V3_Command_Reference_Manual.md`

## 你要实现的最小闭环（电脑端）

1. BLE 连接 + 发现服务/特征
2. 订阅 GAIA 通知特征（收 GAIA v3 Notification/Response/Error）
3. 发送 GAIA v3：注册 Upgrade 通知（Feature=0x06）
4. 发送 GAIA v3：设置 Data Endpoint Mode = RWCP（payload=0x01）
5. 订阅 RWCP 特征（收 RWCP 服务器 ACK 段）
6. 发送 GAIA v3：Upgrade Connect
7. 通过 GAIA Upgrade Control 通道下发 VMU 升级消息（SYNC/START/START_DATA/...）
8. 在数据阶段，用 RWCP 发送 GAIA(UPGRADE_CONTROL + VMU:UPGRADE_DATA)
9. 等 COMPLETE_IND 后，发送 Upgrade Disconnect（可选）
10. 查询升级前/后版本（GET_APPLICATION_VERSION）

## 文档目录

- `docs/pc_upgrade_repro/gaia_v3_wire_format.md`：GAIA v3 PDU 编码/解码与固定字节
- `docs/pc_upgrade_repro/vmu_upgrade_messages.md`：VMU（Upgrade library）消息结构与 OpCode 表
- `docs/pc_upgrade_repro/rwcp_wire_format.md`：RWCP 段结构与最小可用会话流程
- `docs/pc_upgrade_repro/upgrade_end_to_end_flow.md`：端到端升级步骤（每一步写什么/等什么/用哪些 bytes）
- `docs/pc_upgrade_repro/version_query_and_comparison.md`：当前版本轮询 + 升级前/后版本查询与冲突规避
- `docs/pc_upgrade_repro/manual_and_gaia_client_crosscheck.md`：对照手册/gaia-client-src 的一致性检查清单

