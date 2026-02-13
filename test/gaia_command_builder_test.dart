import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/gaia_command_builder.dart';
import 'package:gaia/utils/gaia/gaia.dart';

void main() {
  group('GaiaCommandBuilder', () {
    late GaiaCommandBuilder builder;

    setUp(() {
      builder = GaiaCommandBuilder();
    });

    test('default vendor is V3', () {
      expect(builder.activeVendorId, GaiaCommandBuilder.vendorIdV3);
      expect(builder.isV3VendorActive, isTrue);
    });

    test('vendorToHex formats vendor ID correctly', () {
      expect(builder.vendorToHex(0x001D), '0x001D');
      expect(builder.vendorToHex(0x000A), '0x000A');
    });

    test('buildV3Command constructs correct command format', () {
      // V3 命令格式: [Feature(7bit)][PacketType(2bit)][CommandId(7bit)]
      final cmd = builder.buildV3Command(0x06, 0x00, 0x02);
      expect(builder.v3CommandFeature(cmd), 0x06);
      expect(builder.v3CommandType(cmd), 0x00);
      expect(builder.v3CommandId(cmd), 0x02);
    });

    test('upgradeConnectCommand returns V3 command when V3 active', () {
      final cmd = builder.upgradeConnectCommand();
      expect(builder.v3CommandFeature(cmd), GaiaCommandBuilder.v3FeatureUpgrade);
      expect(builder.v3CommandId(cmd), GaiaCommandBuilder.v3CmdUpgradeConnect);
    });

    test('upgradeConnectCommand returns V1/V2 command when V1/V2 active', () {
      builder.activeVendorId = GaiaCommandBuilder.vendorIdV1V2;
      expect(builder.isV3VendorActive, isFalse);
      final cmd = builder.upgradeConnectCommand();
      expect(cmd, GAIA.commandVmUpgradeConnect);
    });

    test('upgradeControlCommand returns correct command', () {
      final cmd = builder.upgradeControlCommand();
      expect(builder.v3CommandFeature(cmd), GaiaCommandBuilder.v3FeatureUpgrade);
      expect(builder.v3CommandId(cmd), GaiaCommandBuilder.v3CmdUpgradeControl);
    });

    test('setDataEndpointModeCommand returns correct command', () {
      final cmd = builder.setDataEndpointModeCommand();
      expect(builder.v3CommandFeature(cmd), GaiaCommandBuilder.v3FeatureUpgrade);
      expect(cmd, builder.setDataEndpointModeCommand());
    });

    test('getApplicationVersionCommand returns correct command', () {
      final cmd = builder.getApplicationVersionCommand();
      expect(builder.v3CommandFeature(cmd), GaiaCommandBuilder.v3FeatureFramework);
      expect(builder.v3CommandId(cmd), GaiaCommandBuilder.v3CmdAppVersion);
    });

    test('gaiaStatusText returns correct text for known statuses', () {
      expect(builder.gaiaStatusText(0), 'success');
      expect(builder.gaiaStatusText(1), 'notSupported');
      expect(builder.gaiaStatusText(2), 'notAuthenticated');
      expect(builder.gaiaStatusText(99), 'UNKNOWN_STATUS');
    });

    test('gaiaCommandText returns correct text for known commands', () {
      expect(builder.gaiaCommandText(GAIA.commandDfuRequest), 'DFU_REQUEST');
      expect(builder.gaiaCommandText(GAIA.commandDfuBegin), 'DFU_BEGIN');
      expect(builder.gaiaCommandText(GAIA.commandDfuWrite), 'DFU_WRITE');
    });

    test('dfuResultText returns correct text', () {
      expect(builder.dfuResultText(0x00), 'success');
      expect(builder.dfuResultText(0x01), 'FAIL');
      expect(builder.dfuResultText(0xFF), 'UNKNOWN_RESULT');
    });

    test('upgradeErrorText returns correct text', () {
      expect(builder.upgradeErrorText(0x21), '电量过低');
      expect(builder.upgradeErrorText(0x81), '文件校验不通过');
      expect(builder.upgradeErrorText(0xFF), '未知升级错误');
    });

    test('buildGaiaPacket creates packet with correct vendor', () {
      final packet = builder.buildGaiaPacket(0x1234, payload: [0x01, 0x02]);
      expect(packet.mVendorId, GaiaCommandBuilder.vendorIdV3);
      expect(packet.mPayload, [0x01, 0x02]);
    });

    test('buildGaiaPacket allows vendor override', () {
      final packet = builder.buildGaiaPacket(0x1234,
          payload: [0x01], vendor: GaiaCommandBuilder.vendorIdV1V2);
      expect(packet.mVendorId, GaiaCommandBuilder.vendorIdV1V2);
    });
  });
}
