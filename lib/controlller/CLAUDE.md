[根目录](../../CLAUDE.md) > lib > **controlller**

# controlller 模块 -- OTA 服务核心

## 模块职责

本模块包含 `OtaServer` 类，是整个应用的业务核心。它以 `GetxService` 形式注册到 GetX 依赖注入容器中，承担以下职责：

1. **BLE 设备管理** -- 蓝牙扫描、设备连接/断开、权限处理
2. **OTA 升级状态机** -- 完整的固件升级流程编排（同步、启动、数据传输、校验、提交、完成）
3. **分包传输** -- 固件文件分包发送，支持 DFU 直传和 RWCP 可靠传输两种模式
4. **协议适配** -- 代码层面定义了 V1/V2 (Vendor 0x000A) 和 V3 (Vendor 0x001D) 两种 GAIA 协议版本，**当前实现仅使用 V3 (0x001D)**
5. **Vendor 探测** -- `_vendorCandidates` 当前仅包含 `[0x001D]`，即仅探测 V3；如需支持 V1/V2 需扩展候选列表
6. **版本查询** -- 升级前后固件版本查询与对比
7. **异常恢复** -- 连接异常自动重连、升级看门狗、错误突发检测与快速恢复

---

## 关键文件

| 文件 | 大小 | 说明 |
|------|------|------|
| `OtaServer.dart` | ~61KB (~1999行) | OTA 服务核心，涵盖 BLE 管理、升级状态机、RWCP 集成、日志系统 |

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

**Q: 目录名 `controlller` 有三个 `l`，是拼写错误吗？**
A: 是的，这是历史遗留的拼写错误，为保持向后兼容而未修改。

**Q: V3 和 V1/V2 的区别是什么？**
A: V3 使用 Vendor ID 0x001D，命令格式包含 Feature + PacketType + CommandId 三段；V1/V2 使用 Vendor ID 0x000A (Qualcomm)，命令格式为标准 GAIA 命令。

**Q: RWCP 和 DFU 模式如何选择？**
A: OtaServer 连接设备后会尝试注册 RWCP 写入特征。如果注册成功且设备支持，则启用 RWCP（更快、有窗口控制）；否则回退到 DFU 直传模式。

---

## 相关文件清单

- `lib/controlller/OtaServer.dart`
- `lib/TestOtaView.dart` (UI 页面，依赖 OtaServer)
- `lib/main.dart` (GetX 注入 OtaServer)

---

## 变更记录 (Changelog)

| 时间 | 操作 | 说明 |
|------|------|------|
| 2026-02-10 22:00:06 CST | 初始化创建 | 由架构初始化工具生成 |
