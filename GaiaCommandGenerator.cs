// 本文件为“上位机侧”准备的 GAIA v3/v4 指令生成器。
// 编码规则与本仓库 Rust 实现保持一致，支持两种输出：
// - RFCOMM framing bytes（经典蓝牙/RFCOMM）
// - GAIA PDU bytes（BLE/LE GATT）
//
// 刻意修了几个点：
// - GAIA PDU Header（vendor_id / command）使用 Big-Endian（BE，大端序）。
// - RFCOMM framing 的 len 字段表示“GAIA payload 的长度”（不包含 4 字节 GAIA PDU Header）。
// - framing v4 支持 length extension：当 payload_len > 0xFF 时，flags.bit1=1，len 使用 2 字节 BE。
// - 可选 XOR checksum：对“从 SOF 到 checksum 前一字节”的所有字节做 XOR，结果作为末尾校验字节。
//
// 说明：
// - 本文件既提供“发包生成”（GenerateCommand / GenerateApplicationVersionQuery），也提供一个最小的“回包解析/回调处理”
//   （仅覆盖 Application Version 响应：TryParseApplicationVersionResponse / HandleApplicationVersionQueryCallback）。
// - 回包解析函数要求输入是“一整帧 framing”（不是连续流、也不是粘包后的拼接数据）；流式解帧请在上位机另行实现。

#nullable enable

using System;
using System.Text;

namespace GaiaCommandGenerator
{
    public static class GaiaCommandGenerator
    {
        public const byte FrameworkFeatureId = 0x00;
        public const byte GetApplicationVersionCommandId = 0x05;
        public const byte UpgradeFeatureId = 0x06;
        public const byte UpgradeControlCommandId = 0x02;
        public const byte UpgradeNotificationCommandId = 0x00;

        private const byte Sof = 0xFF;
        private const byte FlagChecksum = 0x01;
        private const byte FlagLengthExtension = 0x02;

        /// <summary>
        /// 生成 UpgradeMessage bytes：
        /// <c>opcode(u8) + len(u16, BE) + content(len bytes)</c>。
        /// </summary>
        /// <param name="opcode">UpgradeMessage opcode。</param>
        /// <param name="content">content（可为 null，表示空 content）。</param>
        public static byte[] BuildUpgradeMessage(byte opcode, byte[]? content = null)
        {
            content ??= Array.Empty<byte>();
            if (content.Length > 0xFFFF)
                throw new ArgumentOutOfRangeException(nameof(content), "content too large for UpgradeMessage (u16 length).");

            var outBytes = new byte[3 + content.Length];
            outBytes[0] = opcode;
            WriteU16BE(outBytes, 1, (ushort)content.Length);
            if (content.Length != 0)
                Buffer.BlockCopy(content, 0, outBytes, 3, content.Length);
            return outBytes;
        }

        /// <summary>
        /// 生成一条 Upgrade Control（feature=0x06, commandId=0x02）的 GAIA PDU bytes（BLE/LE GATT 直接发送）。
        /// payload = <see cref="BuildUpgradeMessage"/> 的输出。
        /// </summary>
        public static byte[] GenerateUpgradeControlPdu(
            ushort vendorId,
            byte opcode,
            byte[]? content = null)
        {
            var upgradeMessage = BuildUpgradeMessage(opcode, content);
            return GenerateCommandPdu(
                vendorId: vendorId,
                featureId: UpgradeFeatureId,
                packetType: V3PacketType.COMMAND,
                commandId: UpgradeControlCommandId,
                payload: upgradeMessage);
        }

        /// <summary>
        /// 生成一条 GAIA v3 命令的“GAIA PDU bytes”（BLE/LE GATT 直接发送）。
        /// 输出格式：vendor_id(2, BE) + command(2, BE) + payload(N)。
        /// </summary>
        public static byte[] GenerateCommandPdu(
            ushort vendorId,
            byte featureId,
            V3PacketType packetType,
            byte commandId,
            byte[]? payload = null)
        {
            payload ??= Array.Empty<byte>();
            ushort cmd = CalculateCommandDescription(featureId, packetType, commandId);

            var pdu = new byte[4 + payload.Length];
            WriteU16BE(pdu, 0, vendorId);
            WriteU16BE(pdu, 2, cmd);
            if (payload.Length != 0)
                Buffer.BlockCopy(payload, 0, pdu, 4, payload.Length);
            return pdu;
        }

        /// <summary>
        /// 生成一条 Upgrade Control（feature=0x06, commandId=0x02）的 framing bytes（可直接下发设备）。
        /// payload = <see cref="BuildUpgradeMessage"/> 的输出。
        /// </summary>
        public static byte[] GenerateUpgradeControlFrame(
            ushort vendorId,
            byte opcode,
            byte[]? content = null,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            var upgradeMessage = BuildUpgradeMessage(opcode, content);
            return GenerateCommand(
                vendorId: vendorId,
                featureId: UpgradeFeatureId,
                packetType: V3PacketType.COMMAND,
                commandId: UpgradeControlCommandId,
                payload: upgradeMessage,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成一条 GAIA v3/v4 命令的“完整 RFCOMM framing bytes”（可直接下发设备）。
        /// </summary>
        /// <param name="vendorId">
        /// GAIA Vendor ID。
        /// 常见：QTIL/Qualcomm 使用 <c>0x001D</c>（本仓库默认 vendor）。
        /// </param>
        /// <param name="featureId">
        /// 功能 ID（Feature，7 bits）。
        /// 例如：framework=<c>0x00</c>，upgrade=<c>0x06</c>。
        /// </param>
        /// <param name="packetType">
        /// 包类型（2 bits）：COMMAND/NOTIFICATION/RESPONSE/ERROR。
        /// 注意：上位机发给设备通常是 COMMAND；设备回包通常是 RESPONSE 或 ERROR。
        /// </param>
        /// <param name="commandId">命令 ID（Command ID，7 bits）。</param>
        /// <param name="payload">
        /// GAIA payload（可为 null，表示空 payload）。
        /// 注意：这里的 payload 不包含 vendor/command 这 4 个字节头部。
        /// </param>
        /// <param name="protocolVersion">
        /// RFCOMM framing 版本：
        /// <list type="bullet">
        /// <item><description><c>3</c>：len 为 1 字节（本仓库默认）。</description></item>
        /// <item><description><c>4</c>：支持 length extension（payload_len &gt; 0xFF 时 len 为 2 字节）。</description></item>
        /// </list>
        /// </param>
        /// <param name="useChecksum">
        /// 是否启用 framing XOR checksum（flags.bit0）。
        /// 如果为 true，将在帧末尾追加 1 字节校验。
        /// </param>
        public static byte[] GenerateCommand(
            ushort vendorId,
            byte featureId,
            V3PacketType packetType,
            byte commandId,
            byte[]? payload = null,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            payload ??= Array.Empty<byte>();

            if (protocolVersion < 3)
                throw new ArgumentOutOfRangeException(nameof(protocolVersion), "protocolVersion must be 3 or 4.");

            int gaiaPayloadLen = payload.Length;

            // Repository-compatible guardrails:
            // - v3: payload length is limited (0xFE in Rust impl).
            // - v4: payload length up to 0xFFFF.
            if (protocolVersion >= 4)
            {
                if (gaiaPayloadLen > 0xFFFF)
                    throw new ArgumentOutOfRangeException(nameof(payload), "payload too large for v4 framing.");
            }
            else
            {
                if (gaiaPayloadLen > 0xFE)
                    throw new ArgumentOutOfRangeException(nameof(payload), "payload too large for v3 framing.");
            }

            ushort cmd = CalculateCommandDescription(featureId, packetType, commandId);

            // GAIA PDU bytes: vendor_id(2, BE) + command(2, BE) + payload(N)
            var pdu = new byte[4 + gaiaPayloadLen];
            WriteU16BE(pdu, 0, vendorId);
            WriteU16BE(pdu, 2, cmd);
            if (gaiaPayloadLen != 0)
                Buffer.BlockCopy(payload, 0, pdu, 4, gaiaPayloadLen);

            // RFCOMM framing header
            bool hasLengthExtension = protocolVersion >= 4 && gaiaPayloadLen > 0xFF;
            byte flags = 0;
            if (useChecksum) flags |= FlagChecksum;
            if (hasLengthExtension) flags |= FlagLengthExtension;

            int headerLen = 1 /* SOF */ + 1 /* version */ + 1 /* flags */ + (hasLengthExtension ? 2 : 1);
            int checksumLen = useChecksum ? 1 : 0;
            var frame = new byte[headerLen + pdu.Length + checksumLen];

            int i = 0;
            frame[i++] = Sof;
            frame[i++] = protocolVersion;
            frame[i++] = flags;

            if (hasLengthExtension)
            {
                // v4 length extension：2 字节 BE（只表示 GAIA payload 长度）
                frame[i++] = (byte)((gaiaPayloadLen >> 8) & 0xFF);
                frame[i++] = (byte)(gaiaPayloadLen & 0xFF);
            }
            else
            {
                frame[i++] = (byte)(gaiaPayloadLen & 0xFF);
            }

            Buffer.BlockCopy(pdu, 0, frame, i, pdu.Length);
            i += pdu.Length;

            if (useChecksum)
            {
                // XOR 校验：对“从 SOF 到 checksum 前一字节”的所有字节做 XOR
                frame[i] = CalculateXor(frame, 0, i);
            }

            return frame;
        }

        /// <summary>
        /// 生成“读取固件版本号（Application Version）”请求 PDU（BLE/LE GATT 直接发送）。
        /// GAIA framework feature：feature=<c>0x00</c>，commandId=<c>0x05</c>，payload 为空。
        /// </summary>
        public static byte[] BuildGetApplicationVersionRequestPdu(ushort vendorId = VendorIds.GAIA_V3)
        {
            return GenerateCommandPdu(
                vendorId: vendorId,
                featureId: FrameworkFeatureId,
                packetType: V3PacketType.COMMAND,
                commandId: GetApplicationVersionCommandId,
                payload: null);
        }

        /// <summary>
        /// 生成“读取固件版本号（Application Version）”请求帧。
        /// GAIA framework feature：feature=<c>0x00</c>，commandId=<c>0x05</c>，payload 为空。
        /// </summary>
        public static byte[] BuildGetApplicationVersionRequest(
            ushort vendorId = VendorIds.GAIA_V3,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GenerateCommand(
                vendorId: vendorId,
                featureId: FrameworkFeatureId,
                packetType: V3PacketType.COMMAND,
                commandId: GetApplicationVersionCommandId,
                payload: null,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成“查询固件版本号（Application Version）”请求 PDU（BLE/LE GATT 直接发送）。
        /// 等价于 <see cref="BuildGetApplicationVersionRequestPdu"/>。
        /// </summary>
        public static byte[] GenerateApplicationVersionQueryPdu(ushort vendorId = VendorIds.GAIA_V3)
        {
            return BuildGetApplicationVersionRequestPdu(vendorId);
        }

        /// <summary>
        /// 生成 RWCP Endpoint Mode 请求 PDU（feature=0x06, commandId=0x04, payload=[0x00/0x01]）。
        /// 适用于 BLE/LE GATT 直接发送。
        /// </summary>
        public static byte[] GenerateRwcpEndpointModeRequestPdu(
            bool enabled,
            ushort vendorId = VendorIds.GAIA_V3)
        {
            return GenerateCommandPdu(
                vendorId: vendorId,
                featureId: UpgradeFeatureId,
                packetType: V3PacketType.COMMAND,
                commandId: 0x04,
                payload: new[] { (byte)(enabled ? 0x01 : 0x00) });
        }

        /// <summary>
        /// 生成“查询固件版本号（Application Version）”的请求帧（更贴近业务语义的命名）。
        /// 等价于 <see cref="BuildGetApplicationVersionRequest"/>。
        /// </summary>
        public static byte[] GenerateApplicationVersionQuery(
            ushort vendorId = VendorIds.GAIA_V3,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return BuildGetApplicationVersionRequest(vendorId, protocolVersion, useChecksum);
        }

        /// <summary>
        /// 生成 RWCP Endpoint Mode 请求 framing（feature=0x06, commandId=0x04, payload=[0x00/0x01]）。
        /// </summary>
        public static byte[] GenerateRwcpEndpointModeRequest(
            bool enabled,
            ushort vendorId = VendorIds.GAIA_V3,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GenerateCommand(
                vendorId: vendorId,
                featureId: UpgradeFeatureId,
                packetType: V3PacketType.COMMAND,
                commandId: 0x04,
                payload: new[] { (byte)(enabled ? 0x01 : 0x00) },
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 处理 <see cref="GenerateApplicationVersionQuery"/> 对应的设备回包（回调版本）。
        /// </summary>
        /// <param name="frame">
        /// 一整帧 RFCOMM framing bytes（从 SOF=0xFF 开始，到可选 checksum 结束）。
        /// 注意：如果你传入的是“连续流/粘包拼接/半帧”，本函数会返回 false（不处理）。
        /// </param>
        /// <param name="onVersion">当该帧被识别为“Get Application Version 的 Response”时回调，参数为版本字符串（可能为空）。</param>
        /// <param name="expectedVendorId">期望匹配的 Vendor ID（默认 0x001D）。</param>
        /// <returns>若识别成功并触发回调则返回 true，否则返回 false。</returns>
        public static bool HandleApplicationVersionQueryCallback(
            byte[] frame,
            Action<string> onVersion,
            ushort expectedVendorId = VendorIds.GAIA_V3)
        {
            if (onVersion == null) throw new ArgumentNullException(nameof(onVersion));
            if (TryParseApplicationVersionResponse(frame, out string version, expectedVendorId))
            {
                onVersion(version);
                return true;
            }
            return false;
        }

        /// <summary>
        /// 尝试解析“Get Application Version”响应帧并提取版本字符串。
        /// </summary>
        /// <param name="frame">一整帧 RFCOMM framing bytes。</param>
        /// <param name="version">解析出的 UTF-8/ASCII 版本字符串（可能为空）。</param>
        /// <param name="expectedVendorId">期望匹配的 Vendor ID。</param>
        /// <returns>若该帧匹配“Application Version 响应”则返回 true，否则返回 false。</returns>
        public static bool TryParseApplicationVersionResponse(
            byte[] frame,
            out string version,
            ushort expectedVendorId = VendorIds.GAIA_V3)
        {
            version = string.Empty;
            if (!TryParseGaiaFrame(frame, expectedVendorId, out var cmd, out var payload))
                return false;

            byte feature = GetFeature(cmd);
            byte commandId = GetCommandId(cmd);
            V3PacketType ty = GetPacketType(cmd);

            // 期望：GAIA framework 的 Get Application Version 响应（Response）
            if (feature != FrameworkFeatureId) return false;
            if (commandId != GetApplicationVersionCommandId) return false;
            if (ty != V3PacketType.RESPONSE) return false;

            version = Encoding.UTF8.GetString(payload);
            return true;
        }

        /// <summary>
        /// 尝试从设备上行的“Upgrade Notification”帧中解析 <c>UPGRADE_DATA_BYTES_REQ</c>（opcode=0x03）的参数。
        /// </summary>
        /// <param name="frame">一整帧 RFCOMM framing bytes。</param>
        /// <param name="requested">设备请求的数据字节数（u32，Big-Endian）。</param>
        /// <param name="moveBy">设备要求的 offset 前移量（u32，Big-Endian）。</param>
        /// <param name="expectedVendorId">期望匹配的 Vendor ID（默认 0x001D）。</param>
        /// <returns>若该帧匹配并成功解析则返回 true，否则返回 false。</returns>
        public static bool TryParseUpgradeDataBytesReq(
            byte[] frame,
            out uint requested,
            out uint moveBy,
            ushort expectedVendorId = VendorIds.GAIA_V3)
        {
            requested = 0;
            moveBy = 0;

            if (!TryParseGaiaFrame(frame, expectedVendorId, out var cmd, out var gaiaPayload))
                return false;

            // 期望：GAIA upgrade 的 Notification（commandId=0x00）承载 UpgradeMessage
            byte feature = GetFeature(cmd);
            byte commandId = GetCommandId(cmd);
            V3PacketType ty = GetPacketType(cmd);
            if (feature != UpgradeFeatureId) return false;
            if (commandId != UpgradeNotificationCommandId) return false;
            if (ty != V3PacketType.NOTIFICATION) return false;

            if (!TryParseUpgradeMessage(gaiaPayload, out var opcode, out var content))
                return false;

            const byte UPGRADE_DATA_BYTES_REQ = 0x03;
            if (opcode != UPGRADE_DATA_BYTES_REQ) return false;
            if (content.Length != 8) return false;

            requested = ReadU32BE(content, 0);
            moveBy = ReadU32BE(content, 4);
            return true;
        }

        /// <summary>
        /// 根据设备上行的 <c>UPGRADE_DATA_BYTES_REQ</c>（opcode=0x03）参数（requested/moveBy），
        /// 从固件数据中切片生成 1..N 条 <c>UPGRADE_DATA</c>（opcode=0x04）并将所有 framing bytes 拼接（粘包）输出。
        /// </summary>
        /// <param name="vendorId">Vendor ID（常见为 0x001D）。</param>
        /// <param name="firmware">固件 bin 全量字节。</param>
        /// <param name="cursor">
        /// 主机侧维护的发送游标（offset）。初始通常为 0；每次调用后更新为 <paramref name="nextCursor"/>。
        /// </param>
        /// <param name="requested">设备请求发送的字节数（u32，Big-Endian 解析结果）。</param>
        /// <param name="moveBy">设备要求的 offset 前移量（u32，Big-Endian 解析结果）。</param>
        /// <param name="nextCursor">本次生成并“发送完成”后的游标。</param>
        /// <param name="maxDataBytesPerPacket">
        /// 单条 <c>UPGRADE_DATA</c> 的 data_bytes 最大长度（不含 1 字节 is_end）。
        /// v3/no-checksum 推荐 <= 250（否则可能超过 framing(v3) payload_len=0xFE 限制）。
        /// </param>
        /// <param name="protocolVersion">framing 版本（3 或 4）。</param>
        /// <param name="useChecksum">是否启用 framing XOR checksum。</param>
        /// <returns>可直接下发设备的 framing bytes（可能包含多帧拼接）。</returns>
        public static byte[] GenerateStickyUpgradeDataFrames(
            ushort vendorId,
            byte[] firmware,
            int cursor,
            uint requested,
            uint moveBy,
            out int nextCursor,
            int maxDataBytesPerPacket = 250,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            if (firmware == null) throw new ArgumentNullException(nameof(firmware));
            if (maxDataBytesPerPacket <= 0)
                throw new ArgumentOutOfRangeException(nameof(maxDataBytesPerPacket), "maxDataBytesPerPacket must be > 0.");

            // C# array index is int; normalize cursor into [0, firmware.Length].
            int cur = cursor;
            if (cur < 0) cur = 0;
            if (cur > firmware.Length) cur = firmware.Length;

            // Match gaia-client-src semantics: only move when (moveBy > 0 && cur + moveBy < len).
            if (moveBy > 0)
            {
                long moved = (long)cur + moveBy;
                if (moved < firmware.Length)
                    cur = (int)moved;
            }

            // v3 framing payload limit: 0xFE bytes for GAIA payload.
            // For UPGRADE_DATA: GAIA payload is UpgradeMessage(opcode+len+content) where content=[is_end]+data.
            // payload_len = 1(opcode)+2(len)+1(is_end)+data = 4 + data => data <= 250.
            if (protocolVersion < 4 && maxDataBytesPerPacket > 250)
                maxDataBytesPerPacket = 250;

            int remaining = firmware.Length - cur;
            int totalToSend = (int)Math.Min((long)requested, (long)remaining);

            const byte UPGRADE_DATA = 0x04;
            var outBytes = new byte[0];
            var buffer = new System.Collections.Generic.List<byte>(
                capacity: totalToSend == 0 ? 32 : (totalToSend / Math.Max(1, maxDataBytesPerPacket) + 1) * 32);

            bool sentAny = false;
            while (totalToSend > 0)
            {
                int chunk = Math.Min(totalToSend, maxDataBytesPerPacket);
                bool isEnd = (cur + chunk) == firmware.Length;

                var content = new byte[1 + chunk];
                content[0] = (byte)(isEnd ? 0x01 : 0x00);
                Buffer.BlockCopy(firmware, cur, content, 1, chunk);

                byte[] frame = GenerateUpgradeControlFrame(
                    vendorId: vendorId,
                    opcode: UPGRADE_DATA,
                    content: content,
                    protocolVersion: protocolVersion,
                    useChecksum: useChecksum);
                buffer.AddRange(frame);

                sentAny = true;
                cur += chunk;
                totalToSend -= chunk;
            }

            // Ensure at least one response even if requested == 0 (match gaia-client-src / Rust old-mode behaviour).
            if (!sentAny)
            {
                bool isEnd = cur == firmware.Length;
                var content = new[] { (byte)(isEnd ? 0x01 : 0x00) };
                byte[] frame = GenerateUpgradeControlFrame(
                    vendorId: vendorId,
                    opcode: UPGRADE_DATA,
                    content: content,
                    protocolVersion: protocolVersion,
                    useChecksum: useChecksum);
                buffer.AddRange(frame);
            }

            nextCursor = cur;
            outBytes = buffer.ToArray();
            return outBytes;
        }

        private static bool TryParseGaiaFrame(
            byte[] frame,
            ushort expectedVendorId,
            out ushort cmd,
            out byte[] payload)
        {
            cmd = 0;
            payload = Array.Empty<byte>();

            if (frame == null || frame.Length < 8) return false;
            if (frame[0] != Sof) return false;

            byte protocolVersion = frame[1];
            if (protocolVersion < 3) return false;

            byte flags = frame[2];
            bool hasChecksum = (flags & FlagChecksum) != 0;
            bool hasLengthExtension = protocolVersion >= 4 && (flags & FlagLengthExtension) != 0;

            int headerLen = 1 + 1 + 1 + (hasLengthExtension ? 2 : 1);
            if (frame.Length < headerLen + 4) return false;

            int payloadLen = hasLengthExtension
                ? (frame[3] << 8) | frame[4]
                : frame[3];

            int expectedLen = headerLen + 4 + payloadLen + (hasChecksum ? 1 : 0);
            if (frame.Length != expectedLen) return false;

            if (hasChecksum)
            {
                byte check = 0;
                for (int i = 0; i < frame.Length - 1; i++) check ^= frame[i];
                if (check != frame[frame.Length - 1]) return false;
            }

            int pduOffset = headerLen;
            ushort vendorId = ReadU16BE(frame, pduOffset + 0);
            if (vendorId != expectedVendorId) return false;

            cmd = ReadU16BE(frame, pduOffset + 2);

            if (payloadLen == 0)
            {
                payload = Array.Empty<byte>();
                return true;
            }

            payload = new byte[payloadLen];
            Buffer.BlockCopy(frame, pduOffset + 4, payload, 0, payloadLen);
            return true;
        }

        private static bool TryParseUpgradeMessage(byte[] bytes, out byte opcode, out byte[] content)
        {
            opcode = 0;
            content = Array.Empty<byte>();
            if (bytes == null || bytes.Length < 3) return false;

            opcode = bytes[0];
            int declaredLen = (bytes[1] << 8) | bytes[2];
            int actualLen = bytes.Length - 3;

            // Keep compatibility with tolerant parsers: allow mismatch, but require enough bytes to read.
            if (actualLen < declaredLen) return false;
            int take = declaredLen;
            if (take < 0) return false;

            if (take == 0)
            {
                content = Array.Empty<byte>();
                return true;
            }

            content = new byte[take];
            Buffer.BlockCopy(bytes, 3, content, 0, take);
            return true;
        }

        /// <summary>
        /// 计算 GAIA v3 的 command 字段：
        /// <c>value = (feature &lt;&lt; 9) | (type &lt;&lt; 7) | commandId</c>
        /// </summary>
        public static ushort CalculateCommandDescription(byte featureId, V3PacketType type, byte commandId)
        {
            int value = ((featureId & 0x7F) << 9) | (((int)type & 0x03) << 7) | (commandId & 0x7F);
            return (ushort)value;
        }

        public static string ToHexString(byte[] bytes)
        {
            if (bytes == null) return string.Empty;
            return BitConverter.ToString(bytes).Replace("-", " ");
        }

        private static void WriteU16BE(byte[] buf, int offset, ushort v)
        {
            buf[offset + 0] = (byte)((v >> 8) & 0xFF);
            buf[offset + 1] = (byte)(v & 0xFF);
        }

        private static ushort ReadU16BE(byte[] buf, int offset)
        {
            return (ushort)((buf[offset] << 8) | buf[offset + 1]);
        }

        private static uint ReadU32BE(byte[] buf, int offset)
        {
            return (uint)(
                (buf[offset + 0] << 24)
                | (buf[offset + 1] << 16)
                | (buf[offset + 2] << 8)
                | buf[offset + 3]);
        }

        private static byte GetFeature(ushort cmd)
        {
            return (byte)((cmd >> 9) & 0x7F);
        }

        private static byte GetCommandId(ushort cmd)
        {
            return (byte)(cmd & 0x7F);
        }

        private static V3PacketType GetPacketType(ushort cmd)
        {
            return (V3PacketType)((cmd >> 7) & 0x03);
        }

        private static byte CalculateXor(byte[] data, int offset, int len)
        {
            byte x = 0;
            for (int i = 0; i < len; i++)
                x ^= data[offset + i];
            return x;
        }
    }

    /// <summary>
    /// 面向 LabVIEW/.NET 调用的薄封装（可实例化），用于避免“static class”在某些上位机环境中调用不便。
    /// </summary>
    public sealed class GaiaCommandGeneratorApi
    {
        /// <summary>
        /// 生成 UpgradeMessage bytes：<c>opcode + len(u16, BE) + content</c>。
        /// </summary>
        public byte[] BuildUpgradeMessage(byte opcode, byte[]? content = null)
        {
            return GaiaCommandGenerator.BuildUpgradeMessage(opcode, content);
        }

        /// <summary>
        /// 生成 Upgrade Control（feature=0x06, commandId=0x02）的 framing bytes（可直接下发设备）。
        /// </summary>
        public byte[] GenerateUpgradeControlFrame(
            byte opcode,
            byte[]? content = null,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateUpgradeControlFrame(
                vendorId: VendorIds.GAIA_V3,
                opcode: opcode,
                content: content,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成 Upgrade Control（feature=0x06, commandId=0x02）的 GAIA PDU bytes（BLE/LE GATT 直接发送）。
        /// </summary>
        public byte[] GenerateUpgradeControlPdu(byte opcode, byte[]? content = null)
        {
            return GaiaCommandGenerator.GenerateUpgradeControlPdu(
                vendorId: VendorIds.GAIA_V3,
                opcode: opcode,
                content: content);
        }

        /// <summary>
        /// 生成 Upgrade Control（feature=0x06, commandId=0x02）的 GAIA PDU bytes（可指定 Vendor ID）。
        /// </summary>
        public byte[] GenerateUpgradeControlPduWithVendor(
            ushort vendorId,
            byte opcode,
            byte[]? content = null)
        {
            return GaiaCommandGenerator.GenerateUpgradeControlPdu(
                vendorId: vendorId,
                opcode: opcode,
                content: content);
        }

        /// <summary>
        /// 生成 Upgrade Control（feature=0x06, commandId=0x02）的 framing bytes（可直接下发设备，可指定 Vendor ID）。
        /// </summary>
        public byte[] GenerateUpgradeControlFrameWithVendor(
            ushort vendorId,
            byte opcode,
            byte[]? content = null,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateUpgradeControlFrame(
                vendorId: vendorId,
                opcode: opcode,
                content: content,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成“查询固件版本号（Application Version）”请求帧（可直接下发设备的 framing bytes）。
        /// </summary>
        public byte[] GenerateApplicationVersionQuery(byte protocolVersion = 0x03, bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateApplicationVersionQuery(
                vendorId: VendorIds.GAIA_V3,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成 RWCP Endpoint Mode 请求 framing（feature=0x06, commandId=0x04, payload=[0x00/0x01]）。
        /// </summary>
        public byte[] GenerateRwcpEndpointModeRequest(bool enabled, byte protocolVersion = 0x03, bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateRwcpEndpointModeRequest(
                enabled: enabled,
                vendorId: VendorIds.GAIA_V3,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成 RWCP Endpoint Mode 请求 framing（可指定 Vendor ID）。
        /// </summary>
        public byte[] GenerateRwcpEndpointModeRequestWithVendor(
            bool enabled,
            ushort vendorId,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateRwcpEndpointModeRequest(
                enabled: enabled,
                vendorId: vendorId,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成“查询固件版本号（Application Version）”请求 PDU（BLE/LE GATT 直接发送）。
        /// </summary>
        public byte[] GenerateApplicationVersionQueryPdu()
        {
            return GaiaCommandGenerator.GenerateApplicationVersionQueryPdu(
                vendorId: VendorIds.GAIA_V3);
        }

        /// <summary>
        /// 生成“查询固件版本号（Application Version）”请求 PDU（可指定 Vendor ID）。
        /// </summary>
        public byte[] GenerateApplicationVersionQueryPduWithVendor(ushort vendorId)
        {
            return GaiaCommandGenerator.GenerateApplicationVersionQueryPdu(vendorId);
        }

        /// <summary>
        /// 生成 RWCP Endpoint Mode 请求 PDU（feature=0x06, commandId=0x04, payload=[0x00/0x01]）。
        /// 适用于 BLE/LE GATT。
        /// </summary>
        public byte[] GenerateRwcpEndpointModeRequestPdu(bool enabled)
        {
            return GaiaCommandGenerator.GenerateRwcpEndpointModeRequestPdu(
                enabled: enabled,
                vendorId: VendorIds.GAIA_V3);
        }

        /// <summary>
        /// 生成 RWCP Endpoint Mode 请求 PDU（可指定 Vendor ID）。
        /// </summary>
        public byte[] GenerateRwcpEndpointModeRequestPduWithVendor(bool enabled, ushort vendorId)
        {
            return GaiaCommandGenerator.GenerateRwcpEndpointModeRequestPdu(enabled, vendorId);
        }

        /// <summary>
        /// 生成“查询固件版本号（Application Version）”请求帧（可指定 Vendor ID）。
        /// </summary>
        public byte[] GenerateApplicationVersionQueryWithVendor(
            ushort vendorId,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateApplicationVersionQuery(
                vendorId: vendorId,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成任意 GAIA v3/v4 命令的 framing bytes（可直接下发设备）。
        /// </summary>
        public byte[] GenerateCommand(
            ushort vendorId,
            byte featureId,
            V3PacketType packetType,
            byte commandId,
            byte[]? payload = null,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateCommand(
                vendorId: vendorId,
                featureId: featureId,
                packetType: packetType,
                commandId: commandId,
                payload: payload,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 生成任意 GAIA 命令的 PDU bytes（BLE/LE GATT 直接发送）。
        /// </summary>
        public byte[] GenerateCommandPdu(
            ushort vendorId,
            byte featureId,
            V3PacketType packetType,
            byte commandId,
            byte[]? payload = null)
        {
            return GaiaCommandGenerator.GenerateCommandPdu(
                vendorId: vendorId,
                featureId: featureId,
                packetType: packetType,
                commandId: commandId,
                payload: payload);
        }

        /// <summary>
        /// 尝试解析“Get Application Version”响应帧并提取版本字符串（可能为空）。
        /// </summary>
        public bool TryParseApplicationVersionResponse(byte[] frame, out string version)
        {
            return GaiaCommandGenerator.TryParseApplicationVersionResponse(
                frame: frame,
                version: out version,
                expectedVendorId: VendorIds.GAIA_V3);
        }

        /// <summary>
        /// 尝试解析设备上行的 <c>UPGRADE_DATA_BYTES_REQ</c>（opcode=0x03），输出 requested/moveBy。
        /// 注意：输入必须是一整帧 framing bytes（先切帧再解析）。
        /// </summary>
        public bool TryParseUpgradeDataBytesReq(byte[] frame, out uint requested, out uint moveBy)
        {
            return GaiaCommandGenerator.TryParseUpgradeDataBytesReq(
                frame: frame,
                requested: out requested,
                moveBy: out moveBy,
                expectedVendorId: VendorIds.GAIA_V3);
        }

        /// <summary>
        /// 根据设备请求（requested/moveBy）从固件中切片生成“粘包”的 UPGRADE_DATA framing bytes，并返回更新后的游标。
        /// </summary>
        public byte[] GenerateStickyUpgradeDataFrames(
            byte[] firmware,
            int cursor,
            uint requested,
            uint moveBy,
            out int nextCursor,
            int maxDataBytesPerPacket = 250,
            byte protocolVersion = 0x03,
            bool useChecksum = false)
        {
            return GaiaCommandGenerator.GenerateStickyUpgradeDataFrames(
                vendorId: VendorIds.GAIA_V3,
                firmware: firmware,
                cursor: cursor,
                requested: requested,
                moveBy: moveBy,
                nextCursor: out nextCursor,
                maxDataBytesPerPacket: maxDataBytesPerPacket,
                protocolVersion: protocolVersion,
                useChecksum: useChecksum);
        }

        /// <summary>
        /// 尝试解析设备上行的 <c>UPGRADE_DATA_BYTES_REQ</c>（opcode=0x03），可指定 Vendor ID。
        /// </summary>
        public bool TryParseUpgradeDataBytesReqWithVendor(
            byte[] frame,
            ushort expectedVendorId,
            out uint requested,
            out uint moveBy)
        {
            return GaiaCommandGenerator.TryParseUpgradeDataBytesReq(
                frame: frame,
                requested: out requested,
                moveBy: out moveBy,
                expectedVendorId: expectedVendorId);
        }

        /// <summary>
        /// 尝试解析“Get Application Version”响应帧并提取版本字符串（可指定 Vendor ID）。
        /// </summary>
        public bool TryParseApplicationVersionResponseWithVendor(
            byte[] frame,
            ushort expectedVendorId,
            out string version)
        {
            return GaiaCommandGenerator.TryParseApplicationVersionResponse(
                frame: frame,
                version: out version,
                expectedVendorId: expectedVendorId);
        }

        /// <summary>
        /// 解析“Get Application Version”响应帧：成功返回版本字符串；失败返回 null。
        /// 这个接口不使用 out 参数，通常更利于上位机（含 LabVIEW）直接调用。
        /// </summary>
        public string? ParseApplicationVersionResponseOrNull(byte[] frame, ushort expectedVendorId = VendorIds.GAIA_V3)
        {
            return GaiaCommandGenerator.TryParseApplicationVersionResponse(frame, out var v, expectedVendorId)
                ? v
                : null;
        }

        public string ToHexString(byte[] bytes)
        {
            return GaiaCommandGenerator.ToHexString(bytes);
        }
    }

    public enum V3PacketType : byte
    {
        COMMAND = 0b00,
        NOTIFICATION = 0b01,
        RESPONSE = 0b10,
        ERROR = 0b11
    }

    public static class VendorIds
    {
        public const ushort GAIA_V3 = 0x001D;
        public const ushort GAIA_V1_V2 = 0x000A;
    }
}
