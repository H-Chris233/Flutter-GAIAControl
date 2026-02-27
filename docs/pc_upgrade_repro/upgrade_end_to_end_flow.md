# 端到端升级流程（电脑端复现：一步一步、字节级）

本节把“能跑通升级”的最小步骤按时间线写出来：**每一步要发什么 bytes、从哪里收什么 bytes、收到后推进到下一步做什么**。

> 说明：
> - 所有 `hex` 都是连续大写表示（与本项目日志一致）。
> - GAIA 所有多字节字段按 Big-Endian。
> - 假设 BLE 连接稳定，不展开重连/恢复/错误处理。

## 0. BLE/GATT 前置（固定 UUID）

本项目使用的服务/特征（见 `lib/utils/ble_constants.dart`）：

- Service UUID：`00001100-d102-11e1-9b23-00025b00a5a5`
- GAIA Notify 特征（订阅，收 GAIA 包）：`00001102-d102-11e1-9b23-00025b00a5a5`
- GAIA Write With Response（写，发普通 GAIA 包）：`00001101-d102-11e1-9b23-00025b00a5a5`
- RWCP 特征（订阅 + Write Without Response，收/发 RWCP 段）：`00001103-d102-11e1-9b23-00025b00a5a5`

电脑端最小要求：

1. 连接设备
2. 订阅 `00001102`（GAIA 通知流）

## 1. 注册 Upgrade Feature 通知（必须）

发送 GAIA v3 `REGISTER_NOTIFICATION(feature=0x06)`：

```
00 1D 00 07 06
```

写入：`Write With Response (00001101)`

设备可能回 `REGISTER_NOTIFICATION_RSP`（payload=空），可忽略。

## 2. （可选）读“当前版本/升级前版本”

发送 `GET_APPLICATION_VERSION`：

```
00 1D 00 05
```

设备回 Response：

- Vendor：`001D`
- Command：`0105`（feature=0x00, type=Response, id=0x05）
- Payload：ASCII 文本（可能为空）

例：收到 `001D01055631`，payload=`56 31` -> `"V1"`

## 3. 启用 RWCP Data Endpoint Mode（升级前必须）

发送 `SET_DATA_ENDPOINT_MODE(RWCP=1)`：

```
00 1D 0C 04 01
```

写入：`Write With Response (00001101)`

收到 `SET_DATA_ENDPOINT_MODE_RSP (001D0D04)` 后：

1. 订阅 `00001103`（RWCP 服务器 ACK 段会从这里来）
2. 进入下一步（Upgrade Connect）

## 4. Upgrade Connect（把 GAIA transport 接到 upgrade library）

发送 `UPGRADE_CONNECT`：

```
00 1D 0C 00
```

写入：`Write With Response (00001101)`

等待 Response：`001D0D00`（payload=空）

## 5. SYNC：发送 UPGRADE_SYNC_REQ（变量：由固件生成）

### 5.1 生成 SYNC_REQ 的 4 bytes Upgrade ID

本项目对齐 gaia-client-src：取固件全文件 MD5 的 **最后 4 bytes**：

1. `md5 = MD5(firmware_bytes)`（16 bytes）
2. `upgrade_id = md5[12..15]`（4 bytes）

举例：如果 MD5 文本为 `... 9B5E7148`（最后 8 hex），则：

```
upgrade_id = 9B 5E 71 48
```

### 5.2 组装 VMU：`UPGRADE_SYNC_REQ`

VMU bytes：

```
13 00 04 <upgrade_id_4bytes>
```

### 5.3 组装 GAIA：`UPGRADE_CONTROL + VMU`

GAIA header 固定：`00 1D 0C 02`

最终写入（示例）：

```
00 1D 0C 02  13 00 04  9B 5E 71 48
```

写入：`Write With Response (00001101)`（控制类消息不走 RWCP）

## 6. 等待设备的 UPGRADE_DATA_INDICATION（VMU 下行主通道）

之后设备的 VMU 消息通过 GAIA v3 `UPGRADE_DATA_INDICATION_NTF` 下发：

- GAIA Command：`0C80`（feature=0x06, type=Notification, id=0x00）
- GAIA payload：`VMU bytes`

你需要在 `00001102` 的通知回调里：

1. 解析 GAIA v3
2. 当命中 `cmd=0x0C80` 时，把 `payload` 当 VMU bytes 解析

## 7. START：收到 SYNC_CFM 后发 START_REQ

设备发来 `UPGRADE_SYNC_CFM (0x14)`：

```
14 00 06  <resumePoint 1B> <identifier 4B> <protocolVersion 1B>
```

电脑端做两件事：

1. 记录 `resumePoint`、`protocolVersion`
2. 发送 `UPGRADE_START_REQ`（固定 VMU bytes：`01 00 00`）

即写入：

```
00 1D 0C 02  01 00 00
```

## 8. 数据阶段（最核心）：DATA_BYTES_REQ -> UPGRADE_DATA*（走 RWCP）

### 8.1 收到 START_CFM 后决定下一步

设备发来 `UPGRADE_START_CFM (0x02)`：

- 若 status=0x00：按 `resumePoint` 决定继续点
  - Happy path：`resumePoint=0x00` -> 发送 `UPGRADE_START_DATA_REQ (15 00 00)`
- 若 status=0x09：延迟 2s 重试 START_REQ（本节不展开）

发送 START_DATA_REQ：

```
00 1D 0C 02  15 00 00
```

### 8.2 处理 DATA_BYTES_REQ（设备告诉你“要多少 + offset 怎么动”）

设备发来 `UPGRADE_DATA_BYTES_REQ (0x03)`，Data 固定 8 bytes：

```
03 00 08  <requestedBytes u32_be> <offsetMove u32_be>
```

电脑端维护两个变量：

- `offset`：当前文件游标（初始 0）
- `requestedRemaining`：本次设备请求的剩余 bytes（每次 DATA_BYTES_REQ 重新赋值）

规则（对齐 gaia-client-src `DataReader#set(move, requested)`）：

- 若 `offsetMove > 0` 且 `offset + offsetMove < fileLength`：`offset += offsetMove`
- `requestedRemaining = min(requestedBytes, fileLength - offset)`

### 8.3 计算每个 UPGRADE_DATA 的最大 chunk_len（由 MTU 决定）

为了不超过 ATT 长度限制，chunk_len 需要满足：

- RWCP 模式下：`chunk_len_max ≈ MTU - 12`

原因（见本仓库 `OtaServer.restPayloadSize`）：

- ATT 可用：`MTU - 3`
- RWCP header：`1`
- GAIA vendor+cmd：`4`
- VMU header(op+len)：`3`
- UPGRADE_DATA 的 `is_last`：`1`

合计开销：`1 + 4 + 3 + 1 = 9`，再加 ATT 的 `3` => `12`

所以每包：

```
chunk_len = min(requestedRemaining, chunk_len_max)
```

### 8.4 组装并发送 UPGRADE_DATA（注意 is_last）

是否是最后一包（is_last=0x01）的判断：

- `is_last = 1` 当且仅当 `offset + chunk_len == fileLength`
- 否则 `is_last = 0`

组装 VMU：

```
VMU = 04 <len u16_be> <is_last 1B> <chunk bytes...>
len = 1 + chunk_len
```

组装 GAIA：

```
GAIA = 00 1D 0C 02  + VMU
```

最终通过 RWCP DATA 段发送（写 `00001103`，Write Without Response）：

```
RWCP = <header 1B> + GAIA
```

（最小实现：每发一个 DATA 段都等待一个 DATA_ACK，再发下一段）

循环发送直到：

- `requestedRemaining == 0`（完成本次设备请求）
- 等下一条 `DATA_BYTES_REQ` 再继续

当你把**整个文件最后一包（is_last=1）**发送完并被 ACK 后，下一阶段进入校验。

## 9. 校验阶段：反复发送 IS_VALIDATION_DONE_REQ

发送（固定）：

```
00 1D 0C 02  16 00 00
```

设备回 `IS_VALIDATION_DONE_CFM (0x17)`：

- 若带 `waiting_time_ms`（u16_be）：等待再发下一次 0x16
- 否则立刻再发 0x16

直到设备发 `TRANSFER_COMPLETE_IND (0x0B)`。

## 10. TRANSFER_COMPLETE ->（可选 silent commit 探测）-> TRANSFER_COMPLETE_RES

设备发 `0x0B TRANSFER_COMPLETE_IND`（Data 为空）。

若 `protocolVersion >= 4`：

1. （可选）发 `0x20 SILENT_COMMIT_SUPPORTED_REQ`
2. 收 `0x21 ..._CFM`（data[0]=0/1）

然后发 `TRANSFER_COMPLETE_RES`：

- 本项目选择 Interactive：action=0x00
- VMU bytes 固定：`0C 00 01 00`

写入：

```
00 1D 0C 02  0C 00 01 00
```

此后设备通常会重启/断链（真正的恢复逻辑见 Flutter 代码；本节不展开）。

## 11. POST_REBOOT：resumePoint=inProgress 时发 PROCEED_TO_COMMIT

设备重连后，一般流程是：

1. 重新 Upgrade Connect
2. 再次 SYNC/START（你会收到新的 SYNC_CFM，resumePoint 变为 0x03）

当 `resumePoint == 0x03 (IN_PROGRESS/POST_REBOOT)`：

发送 `0x0E PROCEED_TO_COMMIT`（action=0x00）：

```
00 1D 0C 02  0E 00 01 00
```

## 12. COMMIT：收到 COMMIT_REQ 后发 COMMIT_CFM

设备发 `0x0F COMMIT_REQ`。

发送 `0x10 COMMIT_CFM`（action=0x00）：

```
00 1D 0C 02  10 00 01 00
```

## 13. 完成：收到 COMPLETE_IND（或 WITH_STATUS）

设备发：

- `0x12 COMPLETE_IND` 或
- `0x25 COMPLETE_IND_WITH_STATUS`

至此升级结束。

（可选）发送 GAIA `UPGRADE_DISCONNECT` 关闭通道：

```
00 1D 0C 01
```

## 14. 升级后版本查询（建议）

再次发送：

```
00 1D 00 05
```

拿到新的版本文本做对比即可。

