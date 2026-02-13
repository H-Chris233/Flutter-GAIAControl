import 'package:gaia/utils/gaia/gaia.dart';
import 'package:gaia/utils/gaia/gaia_packet_ble.dart';

/// GAIA V3 命令构建器
///
/// 负责 GAIA 协议命令的构建和数据包封装。
/// 支持 V3 (Vendor 0x001D) 和 V1/V2 (Vendor 0x000A) 两种协议版本。
class GaiaCommandBuilder {
  // V3 协议常量
  static const int v3FeatureFramework = 0x00;
  static const int v3FeatureUpgrade = 0x06;
  static const int v3PacketTypeCommand = 0x00;
  static const int v3PacketTypeNotification = 0x01;
  static const int v3PacketTypeResponse = 0x02;
  static const int v3PacketTypeError = 0x03;
  static const int v3CmdAppVersion = 0x05;
  static const int v3CmdRegisterNotification = 0x07;
  static const int v3CmdCancelNotification = 0x08;
  static const int v3CmdUpgradeNotification = 0x00;
  static const int v3CmdUpgradeConnect = 0x00;
  static const int v3CmdUpgradeDisconnect = 0x01;
  static const int v3CmdUpgradeControl = 0x02;
  static const int v3CmdSetDataEndpointMode = 0x04;

  // V3 Vendor ID
  static const int vendorIdV3 = 0x001D;

  // V1/V2 Vendor ID (Qualcomm)
  static const int vendorIdV1V2 = 0x000A;

  /// 当前活动的 Vendor ID
  int activeVendorId;

  /// 构造函数
  ///
  /// [activeVendorId] 初始 Vendor ID，默认为 V3 (0x001D)
  GaiaCommandBuilder({this.activeVendorId = vendorIdV3});

  /// 是否使用 V3 协议
  bool get isV3VendorActive => activeVendorId == vendorIdV3;

  /// 将 Vendor ID 转换为十六进制字符串
  String vendorToHex(int vendor) {
    return "0x${vendor.toRadixString(16).padLeft(4, '0').toUpperCase()}";
  }

  /// 构建 GAIA 数据包
  GaiaPacketBLE buildGaiaPacket(int command,
      {List<int>? payload, int? vendor}) {
    return GaiaPacketBLE(command,
        mPayload: payload, mVendorId: vendor ?? activeVendorId);
  }

  /// 构建 V3 命令码
  ///
  /// V3 命令格式: [Feature(7bit)][PacketType(2bit)][CommandId(7bit)]
  int buildV3Command(int feature, int packetType, int commandId) {
    return ((feature & 0x7F) << 9) |
        ((packetType & 0x03) << 7) |
        (commandId & 0x7F);
  }

  /// 解析 V3 命令的 Feature 字段
  int v3CommandFeature(int cmd) => (cmd >> 9) & 0x7F;

  /// 解析 V3 命令的 PacketType 字段
  int v3CommandType(int cmd) => (cmd >> 7) & 0x03;

  /// 解析 V3 命令的 CommandId 字段
  int v3CommandId(int cmd) => cmd & 0x7F;

  // ==================== 升级相关命令 ====================

  /// 获取升级连接命令
  int upgradeConnectCommand() => isV3VendorActive
      ? buildV3Command(
          v3FeatureUpgrade, v3PacketTypeCommand, v3CmdUpgradeConnect)
      : GAIA.commandVmUpgradeConnect;

  /// 获取升级断开命令
  int upgradeDisconnectCommand() => isV3VendorActive
      ? buildV3Command(
          v3FeatureUpgrade, v3PacketTypeCommand, v3CmdUpgradeDisconnect)
      : GAIA.commandVmUpgradeDisconnect;

  /// 获取升级控制命令
  int upgradeControlCommand() => isV3VendorActive
      ? buildV3Command(
          v3FeatureUpgrade, v3PacketTypeCommand, v3CmdUpgradeControl)
      : GAIA.commandVmUpgradeControl;

  /// 获取设置数据端点模式命令
  int setDataEndpointModeCommand() => isV3VendorActive
      ? buildV3Command(
          v3FeatureUpgrade, v3PacketTypeCommand, v3CmdSetDataEndpointMode)
      : GAIA.commandSetDataEndpointMode;

  // ==================== 框架相关命令 ====================

  /// 获取应用版本命令
  int getApplicationVersionCommand() => isV3VendorActive
      ? buildV3Command(v3FeatureFramework, v3PacketTypeCommand, v3CmdAppVersion)
      : GAIA.commandGetApplicationVersion;

  /// 获取注册通知命令
  int registerNotificationCommand() => isV3VendorActive
      ? buildV3Command(
          v3FeatureFramework, v3PacketTypeCommand, v3CmdRegisterNotification)
      : GAIA.commandRegisterNotification;

  /// 获取取消通知命令
  int cancelNotificationCommand() => isV3VendorActive
      ? buildV3Command(
          v3FeatureFramework, v3PacketTypeCommand, v3CmdCancelNotification)
      : GAIA.commandCancelNotification;

  // ==================== 状态文本转换 ====================

  /// GAIA 状态码转文本
  String gaiaStatusText(int status) {
    switch (status) {
      case 0:
        return "success";
      case 1:
        return "notSupported";
      case 2:
        return "notAuthenticated";
      case 3:
        return "insufficientResources";
      case 4:
        return "authenticating";
      case 5:
        return "invalidParameter";
      case 6:
        return "incorrectState";
      case 7:
        return "inProgress";
      default:
        return "UNKNOWN_STATUS";
    }
  }

  /// GAIA 命令码转文本
  String gaiaCommandText(int cmd) {
    if (cmd == setDataEndpointModeCommand()) {
      return "SET_DATA_ENDPOINT_MODE";
    }
    if (cmd == upgradeConnectCommand()) {
      return "VM_UPGRADE_CONNECT";
    }
    if (cmd == upgradeControlCommand()) {
      return "VM_UPGRADE_CONTROL";
    }
    if (cmd == upgradeDisconnectCommand()) {
      return "VM_UPGRADE_DISCONNECT";
    }
    if (cmd == getApplicationVersionCommand()) {
      return "GET_APPLICATION_VERSION";
    }
    if (cmd == registerNotificationCommand()) {
      return "REGISTER_NOTIFICATION";
    }
    if (cmd == cancelNotificationCommand()) {
      return "CANCEL_NOTIFICATION";
    }
    switch (cmd) {
      case GAIA.commandSetDataEndpointMode:
        return "SET_DATA_ENDPOINT_MODE";
      case GAIA.commandGetDataEndpointMode:
        return "GET_DATA_ENDPOINT_MODE";
      case GAIA.commandVmUpgradeConnect:
        return "VM_UPGRADE_CONNECT";
      case GAIA.commandVmUpgradeControl:
        return "VM_UPGRADE_CONTROL";
      case GAIA.commandVmUpgradeDisconnect:
        return "VM_UPGRADE_DISCONNECT";
      case GAIA.commandDfuRequest:
        return "DFU_REQUEST";
      case GAIA.commandDfuBegin:
        return "DFU_BEGIN";
      case GAIA.commandDfuWrite:
        return "DFU_WRITE";
      case GAIA.commandDfuCommit:
        return "DFU_COMMIT";
      case GAIA.commandDfuGetResult:
        return "DFU_GET_RESULT";
      default:
        return "UNKNOWN_COMMAND";
    }
  }

  /// DFU 结果码转文本
  String dfuResultText(int resultCode) {
    switch (resultCode) {
      case 0x00:
        return "success";
      case 0x01:
        return "FAIL";
      default:
        return "UNKNOWN_RESULT";
    }
  }

  /// 升级错误码转文本
  String upgradeErrorText(int returnCode) {
    switch (returnCode) {
      case 0x21:
        return "电量过低";
      case 0x81:
        return "文件校验不通过";
      default:
        return "未知升级错误";
    }
  }
}
