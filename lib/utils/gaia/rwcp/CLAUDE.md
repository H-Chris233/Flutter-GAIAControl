[根目录](../../../../CLAUDE.md) > lib > utils > [gaia](../CLAUDE.md) > **rwcp**

# rwcp 模块 -- RWCP 可靠传输协议

## 模块职责

RWCP (Reliable Write Command Protocol) 是一种基于滑动窗口的可靠数据传输协议，运行在 BLE Write Without Response 特征之上。它通过序列号、窗口控制、超时重传和 GAP 检测来保证数据传输的可靠性和效率。

核心能力：
1. **滑动窗口控制** -- 支持窗口动态调整（默认 15，最大 32），实现流量控制和拥塞避免
2. **序列号管理** -- 6 位序列号（0~63），支持环形回绕
3. **会话管理** -- SYN/SYN_ACK 握手建立会话，RST/RST_ACK 终止会话
4. **超时重传** -- DATA 超时（100ms~2000ms 自适应）、SYN/RST 超时（1000ms）
5. **GAP 处理** -- 服务端检测到乱序时发送 GAP，客户端从缺失点重传
6. **数据分段** -- 将 payload 封装为带操作码和序列号的 Segment

---

## 关键文件

| 文件 | 大小 | 说明 |
|------|------|------|
| `RWCP.dart` | ~186行 | 协议常量定义：窗口参数、超时参数、序列号范围；状态枚举（LISTEN/SYN_SENT/ESTABLISHED/CLOSING）；客户端操作码（DATA/SYN/RST）；服务端操作码（DATA_ACK/SYN_ACK/RST_ACK/GAP）；段格式常量 |
| `RWCPClient.dart` | ~21KB (~740行) | RWCP 客户端核心实现：会话生命周期管理、数据发送与窗口控制、ACK 处理与窗口调整、GAP 处理与重传、超时管理 |
| `RWCPListener.dart` | ~34行 | 事件回调抽象接口：`sendRWCPSegment()`、`onTransferFailed()`、`onTransferFinished()`、`onTransferProgress()` |
| `Segment.dart` | ~174行 | 数据段封装：Header (1字节 = 6位序列号 + 2位操作码) + Payload；提供 `get()` 构建和 `parse()` 解析 |

---

## 对外接口

### RWCPClient -- 客户端核心

```dart
class RWCPClient {
  RWCPClient(RWCPListener listener);  // 注入回调监听器

  // 会话管理
  bool startSession();            // 发送 SYN 开始会话
  void terminateSession();        // 发送 RST 终止会话
  bool isRunningASession();       // 是否正在会话中
  void cancelTransfer();          // 取消当前传输

  // 数据传输
  bool sendData(List<int> bytes); // 发送数据（入队后按窗口发送）

  // 接收处理
  bool onReceiveRWCPSegment(List<int>? bytes); // 处理设备返回的 RWCP 段

  // 窗口配置
  int getInitialWindowSize();
  bool setInitialWindowSize(int size);
  int getMaximumWindowSize();
  bool setMaximumWindowSize(int size);

  // 调试
  void showDebugLogs(bool show);
  void logState(String label);
}
```

### RWCPListener -- 事件回调接口

```dart
abstract class RWCPListener {
  bool sendRWCPSegment(List<int> bytes);   // 将段字节写入 BLE
  void onTransferFailed();                  // 传输失败
  void onTransferFinished();                // 传输完成（全部 ACK）
  void onTransferProgress(int acknowledged); // 进度更新（已确认段数）
}
```

`OtaServer` 实现了 `RWCPListener` 接口，将 RWCP 段通过 BLE Write Without Response 特征发送给设备。

---

## 状态机

```
LISTEN  ----[startSession()/sendSYN]----> SYN_SENT
   ^                                          |
   |                                    [收到SYN_ACK]
   |                                          |
   |                                          v
   |                                    ESTABLISHED
   |                                     |        |
   |                               [sendData]  [terminateSession]
   |                                     |        |
   |                                     v        v
   |                               (数据传输)  CLOSING
   |                                              |
   |                                        [收到RST_ACK]
   |                                              |
   +----------------------------------------------+
```

### 状态说明

| 状态 | 值 | 说明 |
|------|----|------|
| `LISTEN` | 0 | 空闲状态，等待应用发起会话 |
| `SYN_SENT` | 1 | 已发送 SYN，等待服务端 SYN_ACK |
| `ESTABLISHED` | 2 | 会话建立，可发送数据 |
| `CLOSING` | 3 | 已发送 RST，等待服务端 RST_ACK |

---

## 滑动窗口机制

```
发送队列 (mPendingData):  [D5] [D6] [D7] [D8] [D9] ...
                            ^
                            | 下一个待发送

已发送未确认 (mUnacknowledgedSegments): [D1] [D2] [D3] [D4]
                                                       ^
                                                       | mWindow=4 已满

已确认: mLastAckSequence = 0
```

- **窗口增长**：收到连续 ACK 时窗口 +1（不超过 mMaximumWindow）
- **窗口缩减**：收到 GAP 或超时时窗口减半（不低于 1）
- **超时自适应**：DATA 超时从 100ms 开始，收到 GAP 或超时后翻倍（上限 2000ms）

---

## 段格式

```
字节:  0        1 ... n
      +--------+----------+
      | Header | Payload  |
      +--------+----------+

Header (1字节):
  bit 0~5: Sequence Number (0~63)
  bit 6~7: Operation Code
           00 = DATA / DATA_ACK
           01 = SYN / SYN_ACK
           10 = RST / RST_ACK
           11 = RESERVED / GAP
```

---

## 内部依赖关系

```
RWCPClient.dart --> RWCP.dart (常量、状态、操作码)
                --> RWCPListener.dart (回调接口)
                --> Segment.dart (数据段构建/解析)
                --> ../../Log.dart (日志)
                --> ../../StringUtils.dart (字节转16进制)

Segment.dart    --> RWCP.dart (段格式常量)
                --> ../../StringUtils.dart (toString格式化)
```

---

## 数据流

### 发送数据流

```
OtaServer.sendData(firmwareChunk)
    |
    v
RWCPClient.sendData(bytes)
    |  加入 mPendingData 队列
    v
sendDataSegment()
    |  从队列取数据，构建 Segment
    v
Segment.get(DATA, sequenceNumber, payload)
    |  序列化为字节
    v
mListener.sendRWCPSegment(segmentBytes)
    |  (OtaServer 实现)
    v
BLE WriteWithoutResponse 特征写入
```

### 接收确认流

```
BLE Notify 特征
    |
    v
OtaServer.handleRecMsg() --> 识别 RWCP 数据
    |
    v
RWCPClient.onReceiveRWCPSegment(bytes)
    |
    v
Segment.parse(bytes)
    |
    +-- SYN_ACK --> receiveSynAck() --> 状态切换到 ESTABLISHED
    +-- DATA_ACK --> receiveDataAck() --> 移除已确认段, 增大窗口, 继续发送
    +-- GAP --> receiveGAP() --> 缩小窗口, 重传未确认段
    +-- RST/RST_ACK --> receiveRST() --> 重置状态到 LISTEN
```

---

## 相关文件清单

- `lib/utils/gaia/rwcp/RWCP.dart`
- `lib/utils/gaia/rwcp/RWCPClient.dart`
- `lib/utils/gaia/rwcp/RWCPListener.dart`
- `lib/utils/gaia/rwcp/Segment.dart`

---

## 变更记录 (Changelog)

| 时间 | 操作 | 说明 |
|------|------|------|
| 2026-02-10 22:00:06 CST | 初始化创建 | 由架构初始化工具生成 |
