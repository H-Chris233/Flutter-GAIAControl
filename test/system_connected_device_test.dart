import 'dart:typed_data';

import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/controller/ota_server.dart';

void main() {
  test('isSystemConnectedScanDevice uses device id instead of name', () {
    final ota = OtaServer();

    ota.systemConnectedDeviceIds
      ..clear()
      ..addAll(<String>[
        ota.normalizeBluetoothId('70:5A:6F:6C:BE:39'),
      ]);

    final connected = DiscoveredDevice(
      id: '70:5A:6F:6C:BE:39',
      name: 'EarFun Air Pro 4',
      serviceData: const <Uuid, Uint8List>{},
      serviceUuids: const <Uuid>[],
      manufacturerData: Uint8List(0),
      rssi: -40,
    );

    final sameNameDifferentId = DiscoveredDevice(
      id: 'AA:BB:CC:DD:EE:FF',
      name: 'EarFun Air Pro 4',
      serviceData: const <Uuid, Uint8List>{},
      serviceUuids: const <Uuid>[],
      manufacturerData: Uint8List(0),
      rssi: -50,
    );

    expect(ota.isSystemConnectedScanDevice(connected), isTrue);
    expect(ota.isSystemConnectedScanDevice(sameNameDifferentId), isFalse);
  });
}

