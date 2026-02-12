import '../log.dart';
import '../string_utils.dart';

class VMUPacket {
  /// <p>The tag to display for logs.</p>
  final String tag = "VMUPacket";

  /// The number of bytes to define the packet length information.
  static final int lengthLength = 2;

  /// The number of bytes to define the packet operation code information.
  static final int opcodeLength = 1;

  /// The offset for the operation code information.
  static final int opcodeOffset = 0;

  /// The offset for the length information.
  static final int lengthOffset = opcodeOffset + opcodeLength;

  /// The offset for the data information.
  static final int dataOffset = lengthOffset + lengthLength;

  /// The packet operation code information.
  int mOpCode = -1;

  /// The packet data information.
  List<int>? mData;

  /// The minimum length a VMU packet should have to be a VMU packet.
  static final int requiredInformationLength = lengthLength + opcodeLength;

  static VMUPacket get(int opCode, {List<int>? data}) {
    VMUPacket vmuPacket = VMUPacket();
    vmuPacket.mOpCode = opCode;
    if (data != null) {
      vmuPacket.mData = data;
    }
    return vmuPacket;
  }

  static VMUPacket? getPackageFromByte(List<int> bytes) {
    int opCode = -1;
    if (bytes.length >= requiredInformationLength) {
      opCode = bytes[0];
      int length = StringUtils.byteListToInt([bytes[1], bytes[2]]);
      int dataLength = bytes.length - requiredInformationLength;
      if (length > dataLength) {
        Log.w("VMUPacket",
            "getPackageFromByte: declared length ($length) > actual data ($dataLength), packet incomplete");
        return null;
      } else if (length < dataLength) {
        Log.w("VMUPacket",
            "getPackageFromByte: declared length ($length) < actual data ($dataLength), trailing bytes will be ignored");
      }
      List<int> data = bytes.sublist(
          requiredInformationLength, requiredInformationLength + length);
      return VMUPacket.get(opCode, data: data);
    }
    return null;
  }

  List<int> getBytes() {
    //000AC0010012
    List<int> packet = [];
    packet.add(mOpCode);
    packet.addAll(StringUtils.intTo2List((mData ?? []).length));
    packet.addAll(mData ?? []);
    return packet;
  }
}
