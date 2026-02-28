# RWCP Wire Format（最小可用实现）与发送规则

RWCP（Reliable Write Command Protocol）用于在 BLE Write Without Response 上实现可靠传输。

在本项目里：

- RWCP 的 **payload 是完整 GAIA PDU bytes**（不是 VMU bytes）
- RWCP 只用于 **发送升级数据包 UPGRADE_DATA**（VMU OpCode=0x04），其余控制类消息走普通写通道（Write With Response）

对应实现：`lib/utils/gaia/rwcp/*`

## 1. RWCP Segment 结构（字节级）

每个 RWCP segment：

```
0      Header (u8)         // 6-bit seq + 2-bit opCode
1..    Payload (0..N)      // DATA 段才有 payload；ACK 段通常无 payload
```

Header 位域：

- bits 0..5：`sequence`（0..63）
- bits 6..7：`opCode`（2 bits）

构造公式：

```
header = (opCode << 6) | sequence
```

### OpCode 定义

- Client -> Server
  - `0` DATA
  - `1` SYN
  - `2` RST
- Server -> Client
  - `0` DATA_ACK
  - `1` SYN_ACK
  - `2` RST / RST_ACK
  - `3` GAP

## 2. 最小可用会话（假设“一条通”无丢包）

你可以用“单包往返”实现（最简单、吞吐低但可复现）：

1. **启动会话**
   - 发送 RST 段（payload 空）
   - 等待 RST_ACK
   - 发送 SYN 段（payload 空）
   - 等待 SYN_ACK
2. **发送数据**
   - 对每个要发送的 GAIA PDU（这里仅指 UpgradeData 对应的 GAIA `00 1D 0C 02 ...`）：
     - 发送 DATA 段：`[header][gaiaPduBytes...]`
     - 等待 DATA_ACK（确认同一个 seq）
     - seq++（mod 64）
3. **结束会话**
   - 所有数据发完后发送 RST
   - 等待 RST_ACK

> 你也可以不显式结束会话，但官方实现会在队列清空后发送 RST 收尾。

### Header 示例（便于抓包核对）

假设 `seq=0`：

- RST：`opCode=2` -> header = `0b10_000000` = `0x80`，bytes：`80`
- SYN：`opCode=1` -> header = `0b01_000000` = `0x40`，bytes：`40`
- DATA：`opCode=0` -> header = `0x00`，bytes：`00 <payload...>`

Server ACK：

- RST_ACK：`opCode=2` -> `0x80 | seq`
- SYN_ACK：`0x40 | seq`
- DATA_ACK：`0x00 | seq`

## 3. 与 GAIA/VMU 的关系（很关键）

在数据阶段你发送的是：

```
RWCP(DATA, seq, payload = GAIA_PDU)

GAIA_PDU = [00 1D] [0C 02] [VMU bytes]
VMU bytes = upgradeData(op=0x04, len, is_last + chunk)
```

也就是说：**VMU 永远被封装在 GAIA 里；GAIA（仅 upgradeData 这类）再被封装在 RWCP DATA 段里。**

---

## 4. 本项目实际的高吞吐实现（窗口 + 队列 + ACK 驱动）

上面的“单包往返”能跑通但吞吐很低：每发一个 DATA 就等待一个 DATA_ACK，速度基本被 RTT（你说的 ~100ms）锁死。

本项目实际用的是 RWCP 的**滑动窗口**能力：**允许同时在路上飞多个未确认 DATA 段**，靠 ACK/GAP/超时异步驱动确认与重传，不在每次写入上阻塞。

### 4.1 两级队列（应用层 / 协议层）

为了既快又稳，本项目有两层队列：

1. **应用层（OtaServer）**：按设备的 `DATA_BYTES_REQ` 从固件文件切片，把每个 `UPGRADE_DATA` 组装成 **GAIA PDU bytes**，然后喂给 RWCP（仅用于升级数据包）。
   - 保护：每次泵送最多生成 24 个升级包，避免 ACK 变慢时 pending 膨胀导致内存峰值（见 `_kRwcpPumpMaxPacketsPerTick`）。
2. **协议层（RWCPClient）**：
   - `mPendingData`：待发送的 GAIA PDU bytes
   - `mUnacknowledgedSegments`：已发送未确认的 RWCP DATA 段（含 seq + payload）

你可以把它理解成：

- 应用层只负责“**生产 payload**（切片+封装）”
- 协议层负责“**发送/重传/窗口流控**”

### 4.2 Window / Credits 的含义

- `window`：允许同时未确认（in-flight）的段数上限。
- `credits`：当前还能再发多少段（`credits == window - unackedCount` 的等价概念）。

发送侧不会“每包等 ACK”，而是：

- 只要 `credits > 0`，就持续发送 DATA 段并把段放进 `mUnacknowledgedSegments`
- 收到 ACK 后移除已确认段，释放 credits，再继续发送

### 4.3 为什么抓包看到 ACK 就是 `01 02 03 ...`

因为 `DATA_ACK` 的 opCode=0，高 2 bit 为 `00`，header 直接等于 6-bit `seq`：

| Server 段类型 | header 计算 | 例（seq=0x12） |
|---|---|---|
| `DATA_ACK` | `0x00 | seq` | `12` |
| `SYN_ACK` | `0x40 | seq` | `52` |
| `RST_ACK` | `0x80 | seq` | `92` |
| `GAP` | `0xC0 | seq` | `D2` |

所以当你看到回包是 `01`、`02`、`03`，它就是在确认对应的 seq。

> 这也解释了为什么 RWCP ACK 段通常只有 1 byte：只有 header，没有 payload。

### 4.4 ACK / GAP / Timeout 如何驱动重传与继续发送（核心逻辑）

#### ACK（累计确认）

本项目的 ACK 校验是“累计确认”语义：收到 `ackSeq` 会把 `(lastAck+1 .. ackSeq)` 这一段的已发送段都从 `mUnacknowledgedSegments` 移除，并释放 credits。

这样一条 ACK 可以一次确认多个段，吞吐就不再是 “1 个包 / RTT”。

#### GAP（缺口提示 + 重传）

设备发 GAP 表示它检测到缺口（乱序/丢包）。本项目做两件事：

1. **缩窗**（更保守）
2. **重传未确认段**

兼容性点（避免卡死）：部分设备的 `GAP.seq` 表示“缺口起点/下一期待 seq”，而不是“最后已确认 seq”。  
若把 `GAP.seq` 当 ACK，会把缺口段误判为已确认并移出队列，导致该段永远不会被重传，传输卡死。

因此本项目对齐为：

```
ackSeq = GAP.seq - 1   (mod 64)
validateAckSequence(DATA, ackSeq)
resendDataSegment()
```

#### Timeout（不等回执，但需要兜底）

协议层会为发送启动超时定时器：

- 超时后会 **增大 dataTimeout（上限 2000ms）** 并触发 `resendDataSegment()`
- 注意：timeout 并不会缩窗；缩窗发生在 GAP

---

## 5. 伪代码（按本项目实现方式：解析 + 队列 + 窗口）

> 说明：为了便于讲解，下面省略了错误日志/状态枚举细节，但“数据结构与推进方式”与项目一致。

```text
parseSegment(bytes):
  header = bytes[0]
  op  = (header >> 6) & 0x03
  seq = header & 0x3F
  payload = bytes[1..]
  return (op, seq, payload)

sendData(gaiaPduBytes):
  pending.pushBack(gaiaPduBytes)
  if state == LISTEN:
    sendRST()              # 对齐参考实现：RST -> 等 RST_ACK -> SYN
  else if state == ESTABLISHED and !timeoutRunning:
    sendDataSegment()

sendDataSegment():
  while credits > 0 and pending.notEmpty and state == ESTABLISHED and !isResending:
    payload = pending.popFront()
    seg = build(DATA, nextSeq, payload)
    ok = writeWithoutResponse(seg.bytes)
    if !ok:
      ensureTimeoutRunning(dataTimeoutMs)
      break
    unacked.pushBack(seg)
    nextSeq = (nextSeq + 1) % 64
    credits--
    startTimeout(dataTimeoutMs)   # 每次发送都会刷新定时器

onRx(bytes):
  (op, seq, payload) = parseSegment(bytes)
  if op == DATA_ACK: onDataAck(seq)
  if op == GAP:      onGap(seq)
  if op == SYN_ACK:  onSynAck(seq)
  if op == RST_ACK:  onRstAck(seq)

onDataAck(ackSeq):
  validated = validateAckSequence(DATA, ackSeq)   # 累计确认 lastAck+1..ackSeq
  if validated >= 0:
    cancelTimeout()
    if credits > 0 and pending.notEmpty:
      sendDataSegment()
    else if pending.empty and unacked.empty and closeWhenIdle:
      sendRST()
    else:
      startTimeout(dataTimeoutMs)
    listener.onTransferProgress(validated)

onGap(gapSeq):
  decreaseWindow()
  ackSeq = (gapSeq - 1 + 64) % 64
  validateAckSequence(DATA, ackSeq)
  cancelTimeout()
  resendDataSegment()

onTimeout():
  dataTimeoutMs = min(dataTimeoutMs * 2, 2000)
  resendDataSegment()

resendDataSegment():
  credits = window
  # 若 unacked > credits，把尾部段挪回 pending，并回退 nextSeq（保持 seq 连贯）
  moved = 0
  while unacked.size > credits:
    seg = unacked.popBack()
    pending.pushFront(seg.payload)
    moved++
  nextSeq = (nextSeq - moved + 64) % 64

  # 重发仍在 unacked 内的段（保持原 seq）
  for seg in unacked in order:
    if credits == 0: break
    writeWithoutResponse(seg.bytes)
    credits--

  if credits > 0:
    sendDataSegment()
```

