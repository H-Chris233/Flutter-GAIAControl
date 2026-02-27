# 对照检查：手册 / gaia-client-src / Flutter 实现是否一致

本文件给同事做“复核用”：每一条都能在手册、gaia-client-src、以及本仓库 Flutter 代码里找到对应点。

## 1. GAIA v3 命令对照（手册 80-CH482-1 Rev.AH）

### 1.1 Framework Feature

- **Get Application Version（3.1.6）**
  - 手册：Command ID=5；响应 payload 为版本文本（可能为空）
  - gaia-client-src：`V3BasicPlugin` -> `V1_GET_APPLICATION_VERSION = 0x05`，解析为 `TextData`
  - Flutter：`OtaServer.queryApplicationVersion` 发送 `001D0005`，`onApplicationVersionAckV3` 解析 payload

- **Register Notification（3.1.8）**
  - 手册：payload=Feature ID
  - gaia-client-src：`V3BasicPlugin` -> `V1_REGISTER_NOTIFICATION = 0x07`
  - Flutter：`OtaServer.registerNotice` 发送 `001D000706`（feature=0x06）

### 1.2 Upgrade/DFU Feature（3.6）

手册 3.6.1/3.6.2/3.6.3/3.6.5/3.6.6/3.6.7/3.6.8：

- **Upgrade Connect（3.6.1）**
  - 手册：无参数
  - gaia-client-src：`V3UpgradePlugin#setUpgradeModeOn` -> `V1_UPGRADE_CONNECT=0x00`
  - Flutter：`OtaServer.sendUpgradeConnect` -> GAIA `001D0C00`

- **Upgrade Disconnect（3.6.2）**
  - 手册：无参数
  - gaia-client-src：`V3UpgradePlugin#setUpgradeModeOff` -> `V1_UPGRADE_DISCONNECT=0x01`
  - Flutter：`OtaServer.sendUpgradeDisconnect` -> GAIA `001D0C01`

- **Upgrade Control（3.6.3）**
  - 手册：用于隧道 Upgrade messages（VMU）
  - gaia-client-src：`V3UpgradePlugin#sendUpgradeMessage` -> `V1_UPGRADE_CONTROL=0x02`
  - Flutter：`OtaServer.sendVMUPacket` -> GAIA `001D0C02 + VMU bytes`

- **Set Data Endpoint Mode（3.6.5）**
  - 手册：payload 0/1（None/RWCP）
  - gaia-client-src：`V3UpgradePlugin#setUpgradeModeOn(useRwcp)`，useRwcp 时发 payload `{0x01}`
  - Flutter：`OtaServer.enableRwcpForUpgrade` 发 `001D0C0401`

- **Upgrade Data Indication（3.6.6）**
  - 手册：notification payload 为 Upgrade data
  - gaia-client-src：`V3UpgradePlugin#onNotification` case `V1_UPGRADE_DATA` -> `upgradeHelper.onUpgradeMessage(packet.getData())`
  - Flutter：`OtaServer._handleV3Packet` 命中 `0x0C80` 后 `receiveVMUPacket(payload)`

- **Upgrade Stop/Start Request（3.6.7 / 3.6.8）**
  - 手册 action 定义：
    - STOP: `0x00 Disconnect upgrade` / `0x01 Stop sending data (pause)`
    - START: `0x00 (Re)connect upgrade` / `0x01 Start sending data (resume)`
  - gaia-client-src：`V3UpgradePlugin#onUpgradeStopRequest/onUpgradeStartRequest`
  - Flutter：`OtaServer._handleV3Packet` 对应 `0x0C81/0x0C82` 分支

## 2. VMU 升级消息流对照（gaia-client-src/lib-upgrade）

主参考：`gaia-client-src/android_src/lib-upgrade/.../UpgradeManagerImpl.java`

关键一致点（与 `lib/controller/upgrade_state_machine.dart` 对齐）：

1. `SYNC_CFM -> START_REQ`
2. `START_CFM(SUCCESS) -> 按 resumePoint 继续`
3. `DATA_BYTES_REQ`：
   - 解析 `requestedBytes` + `offsetMove`
   - offsetMove 语义：`move > 0 && move + offset < data.length` 才前移（见 `DataReader#set`）
4. 数据发完（最后一包）后进入 `IS_VALIDATION_DONE_REQ` 轮询
5. `TRANSFER_COMPLETE_IND` 后：
   - 协议 v4+：先 `SILENT_COMMIT_SUPPORTED_REQ` 再 ask confirmation
   - 否则直接 ask confirmation
6. `resumePoint=POST_COMMIT`：不再发任何 upgrade message，等待 COMPLETE_IND

## 3. 已知的“实现策略差异”（不影响协议正确性）

这些差异是**工程取舍**，不改变 wire protocol：

- Flutter 在 RWCP 模式下对数据泵做了节流（避免 pending 队列过大导致 OOM），而 gaia-client-src 会在收到 DATA_BYTES_REQ 后用 while 尽可能一次性灌满。
- Flutter 明确只让 `UPGRADE_DATA` 走 RWCP，其余控制类 upgrade message 走 Write With Response，降低末期阶段抖动风险（与手册“Data Endpoint Mode 用于发送升级数据”的语义一致）。
- Flutter 默认选择 Interactive commit（`TRANSFER_COMPLETE_RES action=0x00`），未开放 silent commit（`0x02`）作为配置项。

