class RWCP {
  /// <p>The maximum size of the window.</p>
  static const int windowMax = 32;

  /// <p>The default size of the window.</p>
  static const int windowDefault = 15;

  /// <p>The delay in millisecond to time out a syn operation.</p>
  static const int synTimeoutMs = 1000;

  /// <p>The delay in millisecond to time out a rst operation.</p>
  static const int rstTimeoutMs = 1000;

  /// <p>The default delay in millisecond to time out a data operation.</p>
  static const int dataTimeoutMsDefault = 100;

  /// <p>The maximum delay in millisecond to time out a data operation.</p>
  static const int dataTimeoutMsMax = 2000;

  /// <p>The maximum number of a sequence is 63 which correspond to the maximum value represented by 6 bits.</p>
  static const int sequenceNumberMax = 63;

  /// <p>This method builds a human readable label corresponding to the given state value as "closing",
  /// "established", "listen" and "synSent". It returns "Unknown state" for any other value.</p>
  ///
  /// @param state
  ///          The state for which is required a human readable value.
  ///
  /// @return A human readable label for the given value.
  static String getStateLabel(int state) {
    switch (state) {
      case RWCPState.closing:
        return "closing";
      case RWCPState.established:
        return "established";
      case RWCPState.listen:
        return "listen";
      case RWCPState.synSent:
        return "synSent";
      default:
        return "Unknown state ($state)";
    }
  }
}

class RWCPState {
  /// The Client is ready for the application to request that a Write Command(s) be sent to the Server.
  static const int listen = 0;

  /// The Client has started a session and is waiting for the Server to acknowledge the start.
  static const int synSent = 1;

  /// The Client sends data to the Server.
  static const int established = 2;

  /// The Client has terminated the connection the connection and the Client is waiting for the Server to
  /// acknowledge the termination request.
  static const int closing = 3;
}

class RWCPOpCodeClient {
  /// Data sent to the Server by the Client.
  static const int data = 0;

  /// Used to synchronise and start a session by the Client.
  static const int syn = 1;

  /// rst is used by the Client to terminate a session.
  static const int rst = 2;

  /// Undefined operation code, to not be used.
  static const int reserved = 3;
}

class RWCPOpCodeServer {
  /// Used by the Server to acknowledge the data sent to the Server.
  static const int dataAck = 0;

  /// Used by the Server to acknowledge the syn segment.
  static const int synAck = 1;

  /// rst is used by the Server to terminate a session.
  /// rstAck is used by the Server to acknowledge the Client’s request to terminate a session.
  static const int rst = 2;

  static const int rstAck = 2;

  /// Used by the Server to indicate that the Server static consted a data segment that was out-of-sequence.
  static const int gap = 3;
}

class RWCPSegment {
  /// The offset for the header information.
  static const int headerOffset = 0;

  /// The number of bytes which contain the header.
  static const int headerLength = 1;

  /// The offset for the payload information.
  static const int payloadOffset = headerOffset + headerLength;

  /// The minimum length of a segment.
  static const int requiredInformationLength = headerLength;

/**
 * <p>The header of a RWCP segment contains the information to identify the segment: a sequence number and an
 * operation code. The header is contained in one byte for which the bits are allocated as follows:</p>
 * <blockquote><pre>
 * 0 bit     ...         6          7          8
 * +----------+----------+----------+----------+
 * |   SEQUENCE NUMBER   |   OPERATION CODE    |
 * +----------+----------+----------+----------+
 * </pre></blockquote>
 */
}

class SegmentHeader {
  /// The bit offset for the sequence number.
  static const int sequenceNumberBitOffset = 0;

  /// The number of bits which contain the sequence number information.
  static const int sequenceNumberBitsLength = 6;

  /// The bit offset for the operation code.
  static const int operationCodeBitOffset =
      sequenceNumberBitOffset + sequenceNumberBitsLength;

  /// The number of bits which contain the operation code.
  static const int operationCodeBitsLength = 2;
}
