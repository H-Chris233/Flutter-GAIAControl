[根目录](../../CLAUDE.md) > lib > **controller**

# controller 模块 -- OTA 服务核心

## 模块职责

本模块包含 OTA 升级相关的核心组件，采用模块化架构设计：

1. **OtaServer** -- 核心协调器，整合 LogBuffer 和 GaiaCommandBuilder，暴露 UI 状态
2. **LogBuffer** -- 日志缓冲、去重、刷新 ✅ 已集成
3. **GaiaCommandBuilder** -- GAIA V3 命令构建、数据包封装 ✅ 已集成
4. **BleConnectionManager** -- BLE 设备扫描、连接管理、服务发现（独立组件，待集成）
5. **UpgradeStateMachine** -- 升级状态机、VMU 包处理（独立组件，待集成）

---

## 架构设计

```
┌─────────────────────────────────────────────────────────────────┐
│                      OtaServer (GetxService)                     │
│   - 协调各组件                                                    │
│   - 暴露 UI 状态 (updatePer, logText, etc.)                       │
│   - 实现 RWCPListener                                             │
└───────────────────────────┬─────────────────────────────────────┘
                            │ 组合
        ┌───────────────────┴───────────────────┐
        ▼ ✅已集成                              ▼ ✅已集成
┌───────────────┐                       ┌───────────────┐
│   LogBuffer   │                       │ GaiaCommand   │
│               │                       │   Builder     │
└───────────────┘                       └───────────────┘

独立组件（已创建，待未来集成）:
┌───────────────┐   ┌───────────────┐
│ BleConnection │   │  UpgradeState │
│   Manager     │   │    Machine    │
└───────────────┘   └───────────────┘
```

---

## 关键文件

| 文件 | 行数 | 集成状态 | 说明 |
|------|------|----------|------|
| `ota_server.dart` | ~1892 | - | OTA 服务协调器，使用 LogBuffer 和 GaiaCommandBuilder |
| `log_buffer.dart` | ~130 | ✅ 已集成 | 日志缓冲、去重、批量刷新 |
| `gaia_command_builder.dart` | ~208 | ✅ 已集成 | GAIA V3 命令构建、状态文本转换 |
| `ble_connection_manager.dart` | ~376 | 🔲 独立 | BLE 连接管理（有测试覆盖） |
| `upgrade_state_machine.dart` | ~399 | 🔲 独立 | 升级状态机、VMU 包处理（有测试覆盖） |

---

## 组件详情

### LogBuffer

日志缓冲组件，负责：
- 重复日志折叠（相邻重复消息自动合并）
- 批量刷新（120ms 防抖）
- 行数限制（默认 800 行）

### GaiaCommandBuilder

GAIA 协议命令构建器：
- V3 协议常量定义
- 命令码构建（V3 格式: Feature + PacketType + CommandId）
- 状态/错误码文本转换
- 支持 V3 (0x001D) 和 V1/V2 (0x000A) 双协议

### BleConnectionManager

BLE 连接管理器（独立组件）：
- 设备扫描
- 连接管理与自动重连
- 服务发现
- 通知/RWCP 通道注册
- 数据写入（带响应/无响应）

### UpgradeStateMachine

升级状态机：
- 状态管理（idle → syncing → starting → transferring → validating → committing → complete）
- VMU 包处理
- 通过 delegate 接口与外部组件通信

---

## 类结构

```
OtaServer extends GetxService implements RWCPListener
```

### 核心状态变量

| 变量 | 类型 | 说明 |
|------|------|------|
| `devices` | `RxList<DiscoveredDevice>` | 扫描到的 BLE 设备列表 |
| `isUpgrading` | `bool` | 是否正在升级 |
| `isDeviceConnected` | `bool` | 设备是否已连接 |
| `updatePer` | `RxDouble` | 升级进度百分比 (0~100) |
| `mIsRWCPEnabled` | `RxBool` | RWCP 是否已启用 |
| `vendorMode` | `Rx<String>` | Vendor 模式 ("v3" / "v1v2" / "auto") |
| `firmwarePath` | `Rx<String>` | 当前固件文件路径 |
| `mBytesFile` | `List<int>?` | 待上传的固件字节数据 |
| `mResumePoint` | `int` | 断点续传恢复点 |
| `mStartOffset` | `int` | 数据传输偏移量 |
| `versionBeforeUpgrade` | `Rx<String>` | 升级前固件版本 |
| `versionAfterUpgrade` | `Rx<String>` | 升级后固件版本 |

### BLE 特征值 UUID

| UUID | 用途 |
|------|------|
| `00001100-d102-11e1-9b23-00025b00a5a5` | OTA 服务 UUID |
| `00001101-d102-11e1-9b23-00025b00a5a5` | 写入特征 (Write with Response) |
| `00001102-d102-11e1-9b23-00025b00a5a5` | 通知特征 (Notify) |
| `00001103-d102-11e1-9b23-00025b00a5a5` | 无响应写入特征 (Write without Response) |

---

## 对外接口 (Public API)

### 生命周期

| 方法 | 说明 |
|------|------|
| `static OtaServer get to => Get.find()` | 获取单例实例 |
| `onInit()` | 初始化 RWCPClient、默认固件路径、BLE 状态监听 |
| `onClose()` | 释放所有订阅、定时器 |

### 设备管理

| 方法 | 说明 |
|------|------|
| `startScan()` | 开始 BLE 设备扫描 |
| `connectDevice(String id)` | 连接指定设备，成功后自动探测 Vendor |
| `disconnect()` | 断开当前设备连接 |

### OTA 升级

| 方法 | 说明 |
|------|------|
| `startUpdateWithVersionCheck()` | 开始升级（先查询版本再启动） |
| `startUpdate()` | 直接开始升级流程 |
| `stopUpgrade({sendAbort, sendDisconnect})` | 停止升级（可选发送 Abort/Disconnect） |
| `abortUpgrade()` | 中止升级 |

### 配置

| 方法 | 说明 |
|------|------|
| `setFirmwarePath(String path)` | 设置固件文件路径 |
| `setVendorMode(String mode)` | 设置 Vendor 模式 ("v3" / "v1v2" / "auto") |
| `quickRecoverNow()` | 手动触发快速恢复 |

### RWCPListener 实现

| 方法 | 说明 |
|------|------|
| `sendRWCPSegment(List<int> bytes)` | 发送 RWCP 数据段到设备 |
| `onTransferFailed()` | RWCP 传输失败回调 |
| `onTransferFinished()` | RWCP 传输完成回调 |
| `onTransferProgress(int acknowledged)` | RWCP 传输进度回调 |

---

## OTA 升级状态机流程

```
startUpdate()
    |
    v
sendUpgradeConnect()  --> 注册通知/RWCP --> sendSyncReq()
    |
    v
receiveSyncCFM()  --> 获取 ResumePoint
    |
    v
sendStartReq() / setResumePoint()
    |
    v
receiveStartCFM()  --> 根据 ResumePoint 跳转:
    |
    +-- DATA_TRANSFER --> sendStartDataReq() --> receiveDataBytesREQ()
    |                                               |
    |                                               v
    |                                      sendNextDataPacket() (循环)
    |                                               |
    |                                               v
    |                                      receiveDataBytesREQ() (直到传输完成)
    |
    +-- VALIDATION --> sendValidationDoneReq()
    +-- TRANSFER_COMPLETE --> askForConfirmation(TRANSFER_COMPLETE)
    +-- IN_PROGRESS --> askForConfirmation(IN_PROGRESS)
    +-- COMMIT --> askForConfirmation(COMMIT)
    |
    v
receiveTransferCompleteIND()  --> 确认继续
    |
    v
receiveValidationDoneCFM()  --> sendValidationDoneReq()
    |
    v
receiveCommitREQ()  --> 确认提交
    |
    v
receiveCompleteIND()  --> 升级完成 --> 断开连接
```

---

## 内部依赖关系

| 依赖目标 | 用途 |
|----------|------|
| `utils/gaia/GAIA.dart` | 协议常量、Vendor ID |
| `utils/gaia/GaiaPacketBLE.dart` | 构建和解析 GAIA BLE 数据包 |
| `utils/gaia/OpCodes.dart` | 升级操作码 |
| `utils/gaia/VMUPacket.dart` | VMU 数据包构建 |
| `utils/gaia/ConfirmationType.dart` | 确认类型枚举 |
| `utils/gaia/ResumePoints.dart` | 断点续传恢复点 |
| `utils/gaia/UpgradeStartCFMStatus.dart` | 升级启动确认状态 |
| `utils/gaia/rwcp/RWCPClient.dart` | RWCP 可靠传输客户端 |
| `utils/gaia/rwcp/RWCPListener.dart` | RWCP 事件回调接口 |
| `utils/StringUtils.dart` | 字节/字符串转换 |
| `TestOtaView.dart` | 页面导航（连接成功后跳转） |
| `flutter_reactive_ble` | BLE 底层操作 |
| `get` (GetX) | 状态管理、依赖注入、路由 |
| `path_provider` | 获取文档目录 |
| `permission_handler` | 蓝牙权限请求 |

---

## 数据流

```
[固件文件 .bin]
      |
      v
 OtaServer (读取文件, MD5校验)
      |
      +-- DFU 模式 --> writeMsg() --> BLE Write 特征
      |
      +-- RWCP 模式 --> RWCPClient.sendData()
                              |
                              v
                    sendRWCPSegment() --> writeMsgRWCP() --> BLE WriteNoResponse 特征
                              |
                              v
                    [设备 BLE Notify] --> handleRecMsg() --> onReceiveRWCPSegment()
                              |
                              v
                    onTransferProgress() / onTransferFinished()
```

---

## 常见问题 (FAQ)

**Q: V3 和 V1/V2 的区别是什么？**
A: V3 使用 Vendor ID 0x001D，命令格式包含 Feature + PacketType + CommandId 三段；V1/V2 使用 Vendor ID 0x000A (Qualcomm)，命令格式为标准 GAIA 命令。

**Q: RWCP 和 DFU 模式如何选择？**
A: OtaServer 连接设备后会尝试注册 RWCP 写入特征。如果注册成功且设备支持，则启用 RWCP（更快、有窗口控制）；否则回退到 DFU 直传模式。

---

## 相关文件清单

- `lib/controller/ota_server.dart` -- 核心协调器
- `lib/controller/log_buffer.dart` -- 日志缓冲组件
- `lib/controller/gaia_command_builder.dart` -- 命令构建器
- `lib/controller/ble_connection_manager.dart` -- BLE 连接管理器
- `lib/controller/upgrade_state_machine.dart` -- 升级状态机
- `lib/TestOtaView.dart` (UI 页面，依赖 OtaServer)
- `lib/main.dart` (GetX 注入 OtaServer)

---

## 测试文件

| 文件 | 说明 |
|------|------|
| `test/log_buffer_test.dart` | LogBuffer 单元测试（使用 fake_async） |
| `test/gaia_command_builder_test.dart` | GaiaCommandBuilder 单元测试 |
| `test/upgrade_state_machine_test.dart` | UpgradeStateMachine 单元测试 |

---

## 变更记录 (Changelog)

| 时间 | 操作 | 说明 |
|------|------|------|
| 2026-02-13 | 组件集成 | 将 LogBuffer 和 GaiaCommandBuilder 集成到 OtaServer，代码减少 ~180 行 |
| 2026-02-12 | 模块化重构 | 创建 5 个独立组件：LogBuffer、GaiaCommandBuilder、BleConnectionManager、UpgradeStateMachine |
| 2026-02-10 22:00:06 CST | 初始化创建 | 由架构初始化工具生成 |
