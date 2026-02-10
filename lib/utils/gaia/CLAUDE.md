[根目录](../../../CLAUDE.md) > lib > utils > **gaia**

# gaia 模块 -- GAIA 协议实现

## 模块职责

本模块实现了 Qualcomm GAIA (Generic Application Interface Architecture) 蓝牙协议，主要用于 BLE OTA 固件升级场景。从 Android 版 GAIA Control 3.4.0.52 的 Java 代码移植而来。

核心职责：
1. **协议常量定义** -- GAIA 命令掩码、Vendor ID、命令编码、状态码
2. **BLE 数据包封装/解析** -- 将 GAIA 命令打包为 BLE 字节流，解析设备响应
3. **升级操作码管理** -- 定义 OTA 升级流程中的所有操作码（同步、数据请求、校验等）
4. **VMU 数据包处理** -- Virtual Machine Upgrade 数据包的构建与解析
5. **辅助枚举定义** -- 确认类型、恢复点、升级状态等枚举常量

---

## 关键文件

| 文件 | 大小 | 说明 |
|------|------|------|
| `GAIA.dart` | ~36KB | GAIA 协议核心常量：命令掩码、Vendor 定义、配置/控制/通知/数据传输命令、DFU 命令、状态码、事件通知 |
| `GaiaPacketBLE.dart` | 中等 | GAIA BLE 数据包类：封装 VendorId + CommandId + Payload，提供构建(`buildBytes`)、解析、ACK 判断、状态提取 |
| `OpCodes.dart` | 中等 | OTA 升级操作码常量：`UPGRADE_START_REQ/CFM`、`UPGRADE_DATA_BYTES_REQ`、`UPGRADE_DATA`、`UPGRADE_ABORT_REQ/CFM`、`UPGRADE_COMMIT_REQ/CFM` 等 |
| `VMUPacket.dart` | 小 | VMU (Virtual Machine Upgrade) 数据包：OpCode + Length + Data 三段结构，提供 `get()` 构建和 `buildBytes()` 序列化 |
| `ConfirmationType.dart` | 小 | 确认类型枚举：`TRANSFER_COMPLETE`(1)、`COMMIT`(2)、`IN_PROGRESS`(3)、`WARNING_FILE_IS_DIFFERENT`(4)、`BATTERY_LOW_ON_DEVICE`(5) |
| `ResumePoints.dart` | 小 | 断点续传恢复点：`DATA_TRANSFER`(0)、`VALIDATION`(1)、`TRANSFER_COMPLETE`(2)、`IN_PROGRESS`(3)、`COMMIT`(4) |
| `UpgradeStartCFMStatus.dart` | 小 | 升级启动确认状态：`SUCCESS`(0x00)、`ERROR_APP_NOT_READY`(0x09) |

---

## 子模块

| 子模块 | 路径 | 说明 |
|--------|------|------|
| RWCP | `rwcp/` | 可靠写入命令协议 (Reliable Write Command Protocol)，提供滑动窗口传输机制 |

详见 [rwcp/CLAUDE.md](./rwcp/CLAUDE.md)

---

## 对外接口

### GAIA 类 -- 协议常量

```dart
class GAIA {
  // 命令掩码
  static const int COMMAND_MASK = 0x7FFF;
  static const int ACKNOWLEDGMENT_MASK = 0x8000;

  // Vendor 定义
  static const int VENDOR_NONE = 0x7FFE;
  static const int VENDOR_QUALCOMM = 0x000A;    // V1/V2 Vendor
  // V3 Vendor (0x001D) 在 OtaServer 中定义

  // 命令分类 (0x01nn ~ 0x06nn)
  static const int COMMANDS_CONFIGURATION_MASK = 0x0100;  // 配置命令
  static const int COMMANDS_CONTROL_MASK = 0x0200;         // 控制命令
  static const int COMMANDS_DATA_TRANSFER_MASK = 0x0300;   // 数据传输命令
  static const int COMMANDS_NOTIFICATION_MASK = 0x0400;    // 通知命令

  // 状态码
  static const int NOT_STATUS = -1;
  // ... 更多常量详见源文件
}
```

### GaiaPacketBLE 类 -- 数据包

```dart
class GaiaPacketBLE {
  int mVendorId;       // Vendor ID
  int mCommandId;      // 命令 ID (含 ACK 标志位)
  List<int>? mPayload; // 负载数据

  int getCommand();              // 获取纯命令码 (去除 ACK 位)
  int getStatus();               // 获取 ACK 状态码
  bool isAcknowledgement();      // 是否为确认包
  int getEvent();                // 获取通知事件码
  List<int> buildBytes(vendorId); // 构建字节流
}
```

### VMUPacket 类 -- VMU 数据包

```dart
class VMUPacket {
  int mOpCode;         // 操作码
  List<int>? mData;    // 数据

  static VMUPacket get(int opCode, {List<int>? data}); // 工厂方法
  List<int> buildBytes();                              // 序列化
}
```

### OpCodes -- 升级操作码

```dart
class OpCodes {
  static const UPGRADE_START_REQ = 0x01;
  static const UPGRADE_START_CFM = 0x02;
  static const UPGRADE_DATA_BYTES_REQ = 0x03;
  static const UPGRADE_DATA = 0x04;
  static const UPGRADE_ABORT_REQ = 0x07;
  static const UPGRADE_ABORT_CFM = 0x08;
  static const UPGRADE_TRANSFER_COMPLETE_IND = 0x0B;
  static const UPGRADE_TRANSFER_COMPLETE_RES = 0x0C;
  static const UPGRADE_COMMIT_REQ = 0x0F;
  static const UPGRADE_COMMIT_CFM = 0x10;
  static const UPGRADE_COMPLETE_IND = 0x14;
  // ... 完整列表详见源文件
}
```

---

## 内部依赖关系

```
GAIA.dart         <-- GaiaPacketBLE.dart (使用命令掩码、Vendor 常量)
                  <-- OtaServer.dart (使用 Vendor 常量)

GaiaPacketBLE.dart --> GAIA.dart (协议常量)
                   --> StringUtils.dart (字节转换)

VMUPacket.dart     --> StringUtils.dart (字节转换)

OpCodes.dart       <-- OtaServer.dart (升级操作码判断)

ConfirmationType.dart <-- OtaServer.dart (确认类型分发)
ResumePoints.dart     <-- OtaServer.dart (断点恢复)
UpgradeStartCFMStatus.dart <-- OtaServer.dart (升级启动判断)
```

---

## 数据流

### GAIA 命令发送流程

```
OtaServer
    |
    v
VMUPacket.get(opCode, data) --> buildBytes()
    |
    v
GaiaPacketBLE(commandId, payload: vmuBytes)
    |
    v
buildBytes(vendorId)  --> [VendorHigh, VendorLow, CmdHigh, CmdLow, LenHigh, LenLow, ...Payload]
    |
    v
BLE Write Characteristic
```

### GAIA 响应解析流程

```
BLE Notify Characteristic
    |
    v
OtaServer.handleRecMsg(data)
    |
    v
GaiaPacketBLE.fromBytes(data) --> 解析 VendorId, CommandId, Payload
    |
    v
isAcknowledgement()?
    +-- Yes --> receiveSuccessfulAcknowledgement() / receiveUnsuccessfulAcknowledgement()
    +-- No  --> 通知事件处理
    |
    v
VMUPacket 解析 --> handleVMUPacket() --> 根据 OpCode 分发处理
```

---

## 协议参考

本模块代码从 Qualcomm GAIA Control Android 应用 v3.4.0.52 移植。原始 Java 源码位于项目根目录 `gaia-client-src/` 供参考。

GAIA 协议是 Qualcomm (原 CSR) 定义的蓝牙通用应用接口架构，广泛用于蓝牙音频设备的配置管理和固件升级。

---

## 相关文件清单

- `lib/utils/gaia/GAIA.dart`
- `lib/utils/gaia/GaiaPacketBLE.dart`
- `lib/utils/gaia/OpCodes.dart`
- `lib/utils/gaia/VMUPacket.dart`
- `lib/utils/gaia/ConfirmationType.dart`
- `lib/utils/gaia/ResumePoints.dart`
- `lib/utils/gaia/UpgradeStartCFMStatus.dart`
- `lib/utils/gaia/rwcp/` (子模块)

---

## 变更记录 (Changelog)

| 时间 | 操作 | 说明 |
|------|------|------|
| 2026-02-10 22:00:06 CST | 初始化创建 | 由架构初始化工具生成 |
