import 'package:flutter_reactive_ble/flutter_reactive_ble.dart';

class BleConstants {
  static final BleConstants instance = BleConstants._();
  static final Uuid otaServiceUuid =
      Uuid.parse('00001100-d102-11e1-9b23-00025b00a5a5');
  static final Uuid notifyCharacteristicUuid =
      Uuid.parse('00001102-d102-11e1-9b23-00025b00a5a5');
  static final Uuid writeCharacteristicUuid =
      Uuid.parse('00001101-d102-11e1-9b23-00025b00a5a5');
  static final Uuid writeNoResponseCharacteristicUuid =
      Uuid.parse('00001103-d102-11e1-9b23-00025b00a5a5');

  const BleConstants._();
}
