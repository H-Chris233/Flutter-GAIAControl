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

