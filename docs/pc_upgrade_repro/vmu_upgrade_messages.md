# VMU（Upgrade library）消息：结构、固定 bytes 与状态推进

GAIA v3 的 Upgrade Control 只是“隧道”，真正的升级流程由 **VMU（Virtual Machine Upgrade）消息**驱动。

本仓库对应：

- 结构：`lib/utils/gaia/vmu_packet.dart`
- OpCodes：`lib/utils/gaia/op_codes.dart`
- 状态机：`lib/controller/upgrade_state_machine.dart`

## 1. VMU Packet 结构

VMU packet 的二进制结构固定为：

```
0      OpCode (u8)
1..2   Length (u16, Big-Endian)      // 仅 Data 的长度
3..    Data (Length bytes)
```

举例：`UPGRADE_START_REQ`（OpCode=0x01, Length=0）

```
01 00 00
```

## 2. 关键 OpCode 表（电脑端必须实现）

> 方向用 Host->Dev / Dev->Host 表示 VMU 消息的发起方。

### 2.1 初始化与恢复点

- `0x13 UPGRADE_SYNC_REQ`（Host->Dev）
  - Data：4 bytes（Upgrade ID，来自固件 MD5 的尾 4 bytes）
- `0x14 UPGRADE_SYNC_CFM`（Dev->Host）
  - Data：至少 6 bytes（常见）
    - Byte0: Resume Point（见下方）
    - Byte1..4: In-progress identifier
    - Byte5: Protocol version（v4/v5/v6 常见）
- Resume Point（与 gaia-client-src/lib-upgrade 一致）
  - `0x00` DATA_TRANSFER（START）
  - `0x01` VALIDATION（PRE_VALIDATE）
  - `0x02` TRANSFER_COMPLETE（PRE_REBOOT）
  - `0x03` IN_PROGRESS（POST_REBOOT）
  - `0x04` COMMIT
  - `0x05` POST_COMMIT

### 2.2 启动

- `0x01 UPGRADE_START_REQ`（Host->Dev）Data=空
- `0x02 UPGRADE_START_CFM`（Dev->Host）
  - Data[0]：Status
    - `0x00` SUCCESS
    - `0x09` ERROR_APP_NOT_READY（需要延迟重试）

### 2.3 数据传输

- `0x15 UPGRADE_START_DATA_REQ`（Host->Dev）Data=空
- `0x03 UPGRADE_DATA_BYTES_REQ`（Dev->Host）
  - Data 长度固定 8 bytes：
    - Byte0..3：`requestedBytes`（u32_be）
    - Byte4..7：`offsetMove`（u32_be；在 Java 实现中会落到 int，可能表现为有符号溢出）
- `0x04 UPGRADE_DATA`（Host->Dev）
  - Data：
    - Byte0：`is_last`（0x00/0x01）—— **是否整个固件的最后一包**
    - Byte1..：固件 bytes chunk
  - Data length = `1 + chunk_len`

> `is_last` 的语义不是“本次 requestedBytes 的最后一包”，而是“整个 image 的最后一包”。本仓库（以及 gaia-client-src）都按“是否到达文件尾”来置位。

### 2.4 校验与完成

- `0x16 UPGRADE_IS_VALIDATION_DONE_REQ`（Host->Dev）Data=空
- `0x17 UPGRADE_IS_VALIDATION_DONE_CFM`（Dev->Host）
  - 若 Data>=2：Byte0..1 为 `waiting_time_ms`（u16_be），Host 需要等待再发下一次 0x16
  - 若 Data<2：Host 立即重试 0x16
- `0x0B UPGRADE_TRANSFER_COMPLETE_IND`（Dev->Host）Data=空
- `0x0C UPGRADE_TRANSFER_COMPLETE_RES`（Host->Dev）
  - Data[0] Action：
    - `0x00` CONTINUE（Interactive commit）
    - `0x01` ABORT
    - `0x02` SILENT_COMMIT（协议 v4+）
- `0x20 UPGRADE_SILENT_COMMIT_SUPPORTED_REQ`（Host->Dev）Data=空（协议 v4+ 用于探测）
- `0x21 UPGRADE_SILENT_COMMIT_SUPPORTED_CFM`（Dev->Host）Data[0]=0/1
- `0x0E UPGRADE_PROCEED_TO_COMMIT`（Host->Dev，历史命名 inProgressRes）
  - Data[0] Action：`0x00` CONTINUE / `0x01` ABORT
- `0x0F UPGRADE_COMMIT_REQ`（Dev->Host）Data=空
- `0x10 UPGRADE_COMMIT_CFM`（Host->Dev）
  - Data[0] Action：`0x00` CONTINUE / `0x01` ABORT
- `0x12 UPGRADE_COMPLETE_IND`（Dev->Host）Data=空
- `0x25 UPGRADE_COMPLETE_IND_WITH_STATUS`（Dev->Host）Data>=2（status u16_be，可选）

## 3. Host 侧固定 bytes（直接可写）

以下只列 VMU bytes（最终还要包在 GAIA `00 1D 0C 02` 后面）：

- START_REQ：`01 00 00`
- START_DATA_REQ：`15 00 00`
- VALIDATION_DONE_REQ：`16 00 00`
- SILENT_COMMIT_SUPPORTED_REQ：`20 00 00`
- TRANSFER_COMPLETE_RES（继续）：`0C 00 01 00`
- PROCEED_TO_COMMIT（继续）：`0E 00 01 00`
- COMMIT_CFM（继续）：`10 00 01 00`

SYNC_REQ 与 DATA 是变量（见端到端流程文档）。

