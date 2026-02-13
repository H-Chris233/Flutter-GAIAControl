import '../string_utils.dart';
import 'gaia.dart';

class GaiaPacketBLE {
  /// <p>The vendor ID of the packet.</p>
  int mVendorId = 0x001D;

  /// <p>This attribute contains the full command of the packet. If this packet is an acknowledgement packet, this
  /// attribute will contain the acknowledgement bit set to 1.</p>
  int mCommandId = 0;

  int getCommand() {
    return mCommandId & GAIA.commandMask;
  }

  /// <p>The payload which contains all values for the specified command.</p> <p>If the
  /// packet is an acknowledgement packet, the first <code>byte</code> of the packet corresponds to the status of the
  /// sent command.</p>
  List<int>? mPayload;

  /// <p>The bytes which represent this packet.</p>
  List<int>? mBytes;

  GaiaPacketBLE(this.mCommandId, {this.mPayload, int? mVendorId}) {
    if (mVendorId != null) {
      this.mVendorId = mVendorId;
    }
  }

  int getStatus() {
    final int statusOffset = 0;
    final int statusLength = 1;

    if (!isAcknowledgement() ||
        mPayload == null ||
        mPayload!.length < statusLength) {
      return GAIA.notStatus;
    } else {
      return mPayload![statusOffset];
    }
  }

  /// <p>A packet is an acknowledgement packet if its command contains the acknowledgement mask.</p>
  ///
  /// @return <code>true</code> if the command is an acknowledgement.
  bool isAcknowledgement() {
    return (mCommandId & GAIA.acknowledgmentMask) > 0;
  }

  /// <p>Gets the event found in byte zero of the payload if the packet is a notification event packet.</p>
  ///
  /// @return The event code according to `GAIA.NotificationEvents`
  int getEvent() {
    final int eventOffset = 0;
    final int eventLength = 1;

    if ((mCommandId & GAIA.commandsNotificationMask) < 1 ||
        mPayload == null ||
        mPayload!.length < eventLength) {
      return GAIA.notNotification;
    } else {
      return mPayload![eventOffset];
    }
  }

  /// <p>To get the bytes which correspond to this packet.</p>
  ///
  /// @return A new byte array if this packet has been created using its characteristics or the source bytes if this
  /// packet has been created from a source <code>byte</code> array.
  ///
  /// @throws GaiaException for types:
  /// <ul>
  ///     <li>`GaiaException.Type#PAYLOAD_LENGTH_TOO_LONG`</li>
  /// </ul>
  List<int> getBytes() {
    if (mBytes != null) {
      return mBytes ?? [];
    } else {
      mBytes = buildBytes(mCommandId, mPayload);
      return mBytes ?? [];
    }
  }

  static GaiaPacketBLE buildGaiaNotificationPacket(
      int commandID, int event, List<int>? data, int type,
      {int? mVendorId}) {
    List<int> payload = [];
    payload.add(event);
    if (data != null && data.isNotEmpty) {
      payload.addAll(data);
    }

    return GaiaPacketBLE(commandID, mPayload: payload, mVendorId: mVendorId);
  }

  /// <p>The maximum length for the packet payload.</p>
  /// <p>The ble data length maximum for a packet is 20.</p>
  static final int maxPayload = 16;

  /// <p>The offset for the bytes which represents the vendor id in the byte structure.</p>
  static final int offsetVendorId = 0;

  /// <p>The number of bytes which represents the vendor id in the byte structure.</p>
  static final int lengthVendorId = 2;

  /// <p>The offset for the bytes which represents the command id in the byte structure.</p>
  static final int offsetCommandId = 2;

  /// <p>The number of bytes which represents the command id in the byte structure.</p>
  static final int lengthCommandId = 2;

  /// <p>The offset for the bytes which represents the payload in the byte structure.</p>
  static final int offsetPayload = 4;

  /// <p>The number of bytes which contains the information to identify the type of packet.</p>
  static final int packetInformationLength = lengthCommandId + lengthVendorId;

  /// <p>The minimum length of a packet.</p>
  static final int minPacketLength = packetInformationLength;

  static GaiaPacketBLE? fromByte(List<int> source) {
    int payloadLength = source.length - packetInformationLength;
    if (payloadLength < 0) {
      return null;
    }
    int mVendorId = StringUtils.extractIntFromByteArray(
        source, offsetVendorId, lengthVendorId, false);
    int mCommandId = StringUtils.extractIntFromByteArray(
        source, offsetCommandId, lengthCommandId, false);
    List<int> mPayload = [];
    if (payloadLength > 0) {
      mPayload.addAll(source.sublist(packetInformationLength));
    }
    GaiaPacketBLE gaiaPacketBLE =
        GaiaPacketBLE(mCommandId, mPayload: mPayload, mVendorId: mVendorId);
    gaiaPacketBLE.mBytes = source;
    return gaiaPacketBLE;
  }

  List<int> buildBytes(int commandId, List<int>? payload) {
    List<int> bytes = [];
    bytes.addAll(StringUtils.intTo2List(mVendorId));
    bytes.addAll(StringUtils.intTo2List(commandId));
    if (payload != null) {
      bytes.addAll(payload);
    }

    return bytes;
  }

  int getCommandId() {
    return mCommandId;
  }
}
