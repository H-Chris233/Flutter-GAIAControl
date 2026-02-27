# GAIA v3 Wire Format（字节级）与关键命令固定 bytes

本节只讲“电脑端必须实现”的 GAIA v3 编码/解码规则，所有命令都以 **Vendor=0x001D（QTIL v3）** 为前提。

## 1. GAIA v3 PDU 基本结构

在 BLE GATT 上传输的 GAIA v3 数据包（PDU）为：

```
0..1   Vendor ID (u16, Big-Endian)
2..3   Command ID (u16, Big-Endian)  // v3: feature + pduType + specificId
4..    Payload (0..N bytes)
```

在本仓库对应实现：`lib/utils/gaia/gaia_packet_ble.dart`

### 固定 Vendor ID

- `Vendor ID = 0x001D`
- 写入 bytes（Big-Endian）：`00 1D`

## 2. v3 Command ID 位域与计算公式

GAIA v3 的 Command ID 是 16-bit，位域如下（与手册 2.2.1 表达一致）：

- `Feature ID`：7 bits
- `PDU Type`：2 bits
- `PDU Specific ID`：7 bits

本仓库的计算公式（`GaiaCommandBuilder.buildV3Command`）：

```
cmd = ((feature & 0x7F) << 9) | ((pduType & 0x03) << 7) | (specificId & 0x7F)
```

解析公式：

```
feature    = (cmd >> 9) & 0x7F
pduType    = (cmd >> 7) & 0x03
specificId = cmd & 0x7F
```

### PDU Type 枚举（必须实现）

- `0` Command
- `1` Notification
- `2` Response
- `3` Error

> 注意：很多命令的 “Response PDU contents = None”，但设备仍可能回一个 **Response PDU（payload 长度为 0）**，电脑端解析时不要把它当异常。

## 3. 电脑端必须用到的 Feature / Command 列表

### 3.1 Framework Feature（Feature=0x00）

#### (A) GET_APPLICATION_VERSION（Command）

- Feature: `0x00`
- PDU Type: `0` (Command)
- Specific ID: `0x05`
- `Command ID = 0x0005`
- **完整 GAIA bytes（固定）**：

```
00 1D 00 05
```

#### (B) REGISTER_NOTIFICATION（Command）— 注册 Upgrade Feature 的通知

手册 3.1.8：payload=Feature ID

- Feature: `0x00`
- PDU Type: `0`
- Specific ID: `0x07`
- `Command ID = 0x0007`
- Payload: `06`（Upgrade/DFU feature）
- **完整 GAIA bytes（固定）**：

```
00 1D 00 07 06
```

### 3.2 Upgrade/DFU Feature（Feature=0x06）

#### (A) UPGRADE_CONNECT（Command）

- Feature: `0x06`
- PDU Type: `0`
- Specific ID: `0x00`
- `Command ID = 0x0C00`
- **完整 GAIA bytes（固定）**：

```
00 1D 0C 00
```

#### (B) UPGRADE_DISCONNECT（Command）

- Specific ID: `0x01`
- `Command ID = 0x0C01`
- **固定**：

```
00 1D 0C 01
```

#### (C) UPGRADE_CONTROL（Command）— Upgrade library 消息隧道

- Specific ID: `0x02`
- `Command ID = 0x0C02`
- Payload: VMU bytes（见 `vmu_upgrade_messages.md`）
- **GAIA header（固定）**：

```
00 1D 0C 02
```

#### (D) SET_DATA_ENDPOINT_MODE（Command）— 设为 RWCP

手册 3.6.5：payload=0/1（None/RWCP）

- Specific ID: `0x04`
- `Command ID = 0x0C04`
- Payload: `01`
- **固定**：

```
00 1D 0C 04 01
```

## 4. 电脑端必须识别的 Upgrade Feature 通知（device -> host）

这三类来自手册 3.6.6/3.6.7/3.6.8。

### 4.1 UPGRADE_DATA_INDICATION（Notification）

- Feature: `0x06`
- PDU Type: `1` (Notification)
- Specific ID: `0x00`
- `Command ID = 0x0C80`
- Payload: `VMU bytes`

### 4.2 UPGRADE_STOP_REQUEST（Notification）

- Specific ID: `0x01`
- `Command ID = 0x0C81`
- Payload[0] = Action
  - `0x00` Disconnect upgrade
  - `0x01` Stop sending data (pause)

### 4.3 UPGRADE_START_REQUEST（Notification）

- Specific ID: `0x02`
- `Command ID = 0x0C82`
- Payload[0] = Action
  - `0x00` (Re)connect upgrade
  - `0x01` Start sending data (resume)

## 5. 推荐的解析流程（电脑端）

收到一帧 GAIA bytes 后：

1. `vendor = u16_be(bytes[0..1])`，不是 `0x001D` 直接忽略（本项目固定 v3）。
2. `cmd = u16_be(bytes[2..3])`
3. 用上面的位域拆出 `feature/pduType/specificId`
4. `payload = bytes[4..]`
5. 如果是 `feature=0x06 & pduType=Notification & id=0x00`：payload 作为 VMU packet 解析（升级主通道）

