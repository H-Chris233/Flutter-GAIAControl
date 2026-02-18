import 'dart:async';
import 'dart:collection';

import '../../log.dart';
import '../../string_utils.dart';
import 'rwcp.dart';
import 'rwcp_listener.dart';
import 'segment.dart';

class RWCPClient {
  /// <p>The tag to display for logs.</p>
  final String tag = "RWCPClient";

  /// <p>The listener to communicate with the application and send segments.</p>
  final RWCPListener mListener;

  /// The sequence number of the last sequence which had been acknowledged by the Server.
  int mLastAckSequence = 0;

  /// The next sequence number which will be send.
  int mNextSequence = 0;

  /// The window size to use when starting a transfer.
  int mInitialWindow = RWCP.windowDefault;

  /// The maximum size of the window to use when adjusting the window size.
  int mMaximumWindow = RWCP.windowMax;

  /// The window represents the maximum number of segments which can be sent simultaneously.
  int mWindow = RWCP.windowDefault;

  /// The credit number represents the number of segments which can still be send to fill the current window.
  int mCredits = RWCP.windowDefault;

  /// When receiving a gap or when an operation is timed out, this client resends the unacknowledged data and stops
  /// any other running operation.
  bool mIsResendingSegments = false;

  /// The state of the Client.
  int mState = RWCPState.listen;

  /// The queue of data which are waiting to be sent.

  var mPendingData = ListQueue<List<int>>();

  /// The queue of segments which have been sent but have not been acknowledged yet.

  var mUnacknowledgedSegments = ListQueue<Segment>();

  /// To know if a time out is running.
  bool isTimeOutRunning = false;

  /// The time used to time out the data segments.
  int mDataTimeOutMs = RWCP.dataTimeoutMsDefault;

  /// <p>To show the debug logs indicating when a method had been reached.</p>
  bool mShowDebugLogs = true;

  /// To know the number of segments which had been acknowledged in a row with dataAck.
  int mAcknowledgedSegments = 0;
  int mSuccessfulAckStreak = 0;
  static const int _timeoutRecoveryAckThreshold = 8;
  Timer? _timer;

  RWCPClient(this.mListener);

  bool isRunningASession() {
    return mState != RWCPState.listen;
  }

  void showDebugLogs(bool show) {
    mShowDebugLogs = show;
    Log.i(tag, "Debug logs are now ${show ? "activated" : "deactivated"}.");
  }

  bool sendData(List<int> bytes) {
    mPendingData.add(bytes);
    if (mState == RWCPState.listen) {
      return startSession();
    } else if (mState == RWCPState.established && !isTimeOutRunning) {
      sendDataSegment();
      return true;
    }

    return true;
  }

  void cancelTransfer() {
    logState("cancelTransfer");

    if (mState == RWCPState.listen) {
      Log.i(tag, "cancelTransfer: no ongoing transfer to cancel.");
      return;
    }

    reset(true);

    if (!sendRSTSegment()) {
      Log.w(tag, "Sending of rst segment has failed, terminating session.");
      terminateSession();
    }
  }

  bool onReceiveRWCPSegment(List<int>? bytes) {
    if (bytes == null) {
      Log.w(tag, "onReceiveRWCPSegment called with a null bytes array.");
      return false;
    }

    if (bytes.length < RWCPSegment.requiredInformationLength) {
      String message =
          "Analyse of RWCP Segment failed: the byte array does not contain the minimum "
          "required information.";
      if (mShowDebugLogs) {
        message += "\n\tbytes=${StringUtils.byteToHexString(bytes)}";
      }
      Log.w(tag, message);
      return false;
    }

    // getting the segment information from the bytes
    Segment segment = Segment.parse(bytes);
    int code = segment.getOperationCode();
    if (code == -1) {
      Log.w(tag,
          "onReceivedRWCPSegment failed to get a RWCP segment from given bytes: $code data->${StringUtils.byteToHexString(bytes)}");
      return false;
    }

    Log.d(tag, "onReceiveRWCPSegment code$code");
    // handling of a segment depends on the operation code.
    switch (code) {
      case RWCPOpCodeServer.synAck:
        return receiveSynAck(segment);
      case RWCPOpCodeServer.dataAck:
        return receiveDataAck(segment);
      case RWCPOpCodeServer.rst:
        /*case RWCP.OpCode.Server.rstAck:*/
        return receiveRST(segment);
      case RWCPOpCodeServer.gap:
        return receiveGAP(segment);
      default:
        Log.w(tag, "Received unknown operation code: $code");
        return false;
    }
  }

  int getInitialWindowSize() {
    return mInitialWindow;
  }

  bool setInitialWindowSize(int size) {
    logState("set initial window size to $size");

    if (mState != RWCPState.listen) {
      Log.w(
          tag,
          "FAIL to set initial window size to $size: not possible when there is an ongoing "
          "session.");
      return false;
    }

    if (size <= 0 || size > mMaximumWindow) {
      Log.w(tag, "FAIL to set initial window to $size: size is out of range.");
      return false;
    }

    mInitialWindow = size;
    mWindow =
        mInitialWindow; // not in an ongoing session, window is set up to the initial value
    return true;
  }

  int getMaximumWindowSize() {
    return mMaximumWindow;
  }

  bool setMaximumWindowSize(int size) {
    logState("set maximum window size to $size");

    if (mState != RWCPState.listen) {
      Log.w(
          tag,
          "FAIL to set maximum window size to $size: not possible when there is an ongoing "
          "session.");
      return false;
    }

    if (size <= 0 || size > RWCP.windowMax) {
      Log.w(tag, "FAIL to set maximum window to $size: size is out of range.");
      return false;
    }

    if (mInitialWindow > size) {
      Log.w(tag,
          "FAIL to set maximum window to $size: initial window is $mInitialWindow.");
      return false;
    }

    mMaximumWindow = size;
    if (mWindow > mMaximumWindow) {
      Log.i(tag,
          "window is updated to be less than the maximum window size ( $mInitialWindow).");
      mWindow = mMaximumWindow;
    }
    return true;
  }

  bool receiveRST(Segment segment) {
    if (mShowDebugLogs) {
      Log.d(tag,
          "Receive rst or rstAck for sequence ${segment.getSequenceNumber()}");
    }

    switch (mState) {
      case RWCPState.synSent:
        Log.i(
            tag,
            "Received rst (sequence ${segment.getSequenceNumber()}) in synSent state, ignoring "
            "segment.");
        return true;

      case RWCPState.established:
        // received rst
        Log.w(
            tag,
            "Received rst (sequence ${segment.getSequenceNumber()}) in established state, "
            "terminating session, transfer failed.");
        terminateSession();
        mListener.onTransferFailed();
        return true;

      case RWCPState.closing:
        // received rstAck
        cancelTimeOut();
        validateAckSequence(RWCPOpCodeClient.rst, segment.getSequenceNumber());
        reset(false);
        if (mPendingData.isNotEmpty) {
          // expected when starting a session: rst sent prior syn, sending syn to start the session
          if (!sendSYNSegment()) {
            Log.w(tag,
                "Start session of RWCP data transfer failed: sending of syn failed.");
            terminateSession();
            mListener.onTransferFailed();
          }
        } else {
          // rst is acknowledged: transfer is finished
          mListener.onTransferFinished();
        }
        return true;

      case RWCPState.listen:
      default:
        Log.w(tag,
            "Received unexpected rst segment with sequence=${segment.getSequenceNumber()} while in state ${RWCP.getStateLabel(mState)}");
        return false;
    }
  }

  bool sendSYNSegment() {
    bool done = false;
    mState = RWCPState.synSent;
    Segment segment = Segment.get(RWCPOpCodeClient.syn, mNextSequence);
    done = sendSegment(segment, RWCP.synTimeoutMs);
    if (done) {
      mUnacknowledgedSegments.add(segment);
      mNextSequence = increaseSequenceNumber(mNextSequence);
      mCredits--;
      logState("send syn segment");
    }
    return done;
  }

  void logState(String label) {
    if (mShowDebugLogs) {
      String message =
          "$label\t\t\tstate=${RWCP.getStateLabel(mState)}\n\tWindow: \tcurrent = $mWindow \t\tdefault = $mInitialWindow \t\tcredits = $mCredits\n\tSequence: \tlast = $mLastAckSequence \t\tnext = $mNextSequence\n\tPending: \tPSegments = ${mUnacknowledgedSegments.length} \t\tPData = ${mPendingData.length}";
      Log.d(tag, message);
    }
  }

  bool startSession() {
    logState("startSession");

    if (mState != RWCPState.listen) {
      Log.w(tag, "Start RWCP session failed: already an ongoing session.");
      return false;
    }

    // it is recommended to send a rst and then a syn to make sure the Server side is in the right state.
    // This client first sends a rst segment, waits to get a rstAck segment and sends the syn segment.
    // The sending of the syn happens if there is some pending data waiting to be sent.
    if (sendRSTSegment()) {
      return true;
      // wait for receiveRST to be called.
    } else {
      Log.w(tag, "Start RWCP session failed: sending of rst segment failed.");
      terminateSession();
      return false;
    }
  }

  void terminateSession() {
    logState("terminateSession");
    reset(true);
  }

  bool sendRSTSegment() {
    if (mState == RWCPState.closing) {
      // rst already sent waiting to be acknowledged
      return true;
    }

    bool done = false;
    reset(false);
    mState = RWCPState.closing;
    Segment segment = Segment.get(RWCPOpCodeClient.rst, mNextSequence);
    done = sendSegment(segment, RWCP.rstTimeoutMs);
    if (done) {
      mUnacknowledgedSegments.add(segment);
      mNextSequence = increaseSequenceNumber(mNextSequence);
      mCredits--;
      logState("send rst segment");
    }
    return done;
  }

  bool sendSegment(Segment segment, int timeout) {
    List<int> bytes = segment.getBytes();
    if (mListener.sendRWCPSegment(bytes)) {
      startTimeOut(timeout);
      return true;
    }

    return false;
  }

  void startTimeOut(int delay) {
    if (isTimeOutRunning) {
      _timer?.cancel();
    }

    isTimeOutRunning = true;
    _timer = Timer(Duration(milliseconds: delay), () {
      onTimeOut();
    });
  }

  void onTimeOut() {
    if (isTimeOutRunning) {
      isTimeOutRunning = false;
      mIsResendingSegments = true;
      mAcknowledgedSegments = 0;
      mSuccessfulAckStreak = 0;

      if (mShowDebugLogs) {
        Log.i(tag, "TIME OUT > re sending segments");
      }

      if (mState == RWCPState.established) {
        // Timed out segments are data segments: increasing data time out value
        mDataTimeOutMs *= 2;
        if (mDataTimeOutMs > RWCP.dataTimeoutMsMax) {
          mDataTimeOutMs = RWCP.dataTimeoutMsMax;
        }

        resendDataSegment();
      } else {
        // syn or rst segments are timed out
        resendSegment();
      }
    }
  }

  void resendSegment() {
    if (mState == RWCPState.established) {
      Log.w(
          tag, "Trying to resend non data segment while in established state.");
      return;
    }

    mIsResendingSegments = true;
    mCredits = mWindow;

    // resend the unacknowledged segments corresponding to the window
    for (Segment segment in mUnacknowledgedSegments) {
      int delay = (segment.getOperationCode() == RWCPOpCodeClient.syn)
          ? RWCP.synTimeoutMs
          : (segment.getOperationCode() == RWCPOpCodeClient.rst)
              ? RWCP.rstTimeoutMs
              : mDataTimeOutMs;
      sendSegment(segment, delay);
      mCredits--;
    }
    logState("resend segments");

    mIsResendingSegments = false;
  }

  void resendDataSegment() {
    if (mState != RWCPState.established) {
      Log.w(
          tag, "Trying to resend data segment while not in established state.");
      return;
    }

    mIsResendingSegments = true;
    mCredits = mWindow;
    logState("reset credits");

    // if they are more unacknowledged segments than available credits, these extra segments are not anymore
    // unacknowledged but pending
    int moved = 0;
    while (mUnacknowledgedSegments.length > mCredits) {
      Segment segment = mUnacknowledgedSegments.last;
      if (segment.getOperationCode() == RWCPOpCodeClient.data) {
        mUnacknowledgedSegments.removeLast();
        mPendingData.addFirst(segment.getPayload());
        moved++;
      } else {
        Log.w(tag,
            "Segment $segment in pending segments but not a data segment.");
        break;
      }
    }

    // if some segments have been moved to the pending state, the next sequence number has changed.
    mNextSequence = decreaseSequenceNumber(mNextSequence, moved);

    // resend the unacknowledged segments corresponding to the window
    for (var segment in mUnacknowledgedSegments) {
      if (mCredits <= 0) break;
      sendSegment(segment, mDataTimeOutMs);
      mCredits--;
    }

    logState("Resend data segments");

    mIsResendingSegments = false;

    if (mCredits > 0) {
      sendDataSegment();
    }
  }

  void sendDataSegment() {
    while (mCredits > 0 &&
        mPendingData.isNotEmpty &&
        !mIsResendingSegments &&
        mState == RWCPState.established) {
      List<int> data = mPendingData.first;
      Segment segment =
          Segment.get(RWCPOpCodeClient.data, mNextSequence, payload: data);
      final sent = sendSegment(segment, mDataTimeOutMs);
      if (!sent) {
        Log.w(
            tag,
            "Failed to send data segment(sequence=${segment.getSequenceNumber()}), "
            "keeping pending data for retry.");
        if (!isTimeOutRunning) {
          startTimeOut(mDataTimeOutMs);
        }
        break;
      }
      mPendingData.removeFirst();
      mUnacknowledgedSegments.add(segment);
      mNextSequence = increaseSequenceNumber(mNextSequence);
      mCredits--;
    }
    logState("send data segments");
  }

  int increaseSequenceNumber(int sequence) {
    return (sequence + 1) % (RWCP.sequenceNumberMax + 1);
  }

  int decreaseSequenceNumber(int sequence, int decrease) {
    return (sequence - decrease + RWCP.sequenceNumberMax + 1) %
        (RWCP.sequenceNumberMax + 1);
  }

  void reset(bool complete) {
    mLastAckSequence = -1;
    mNextSequence = 0;
    mState = RWCPState.listen;
    mUnacknowledgedSegments.clear();
    mWindow = mInitialWindow;
    mAcknowledgedSegments = 0;
    mSuccessfulAckStreak = 0;
    mDataTimeOutMs = RWCP.dataTimeoutMsDefault;
    mCredits = mWindow;
    cancelTimeOut();
    if (complete) {
      mPendingData.clear();
    }
    logState("reset");
  }

  void cancelTimeOut() {
    if (isTimeOutRunning) {
      _timer?.cancel();
      isTimeOutRunning = false;
    }
  }

  bool receiveSynAck(Segment segment) {
    if (mShowDebugLogs) {
      Log.d(tag, "Receive synAck for sequence ${segment.getSequenceNumber()}");
    }

    switch (mState) {
      case RWCPState.synSent:
        // expected behavior: start to send the data
        cancelTimeOut();
        int validated = validateAckSequence(
            RWCPOpCodeClient.syn, segment.getSequenceNumber());
        if (validated >= 0) {
          mState = RWCPState.established;
          if (mPendingData.isNotEmpty) {
            sendDataSegment();
          }
        } else {
          Log.w(tag,
              "Receive synAck with unexpected sequence number: ${segment.getSequenceNumber()}");
          terminateSession();
          mListener.onTransferFailed();
          sendRSTSegment();
        }
        return true;

      case RWCPState.established:
        // data might have been lost, resending them
        cancelTimeOut();
        if (mUnacknowledgedSegments.isNotEmpty) {
          resendDataSegment();
        }
        return true;

      case RWCPState.closing:
      case RWCPState.listen:
      default:
        Log.w(tag,
            "Received unexpected synAck segment with header ${segment.getHeader()} while in state ${RWCP.getStateLabel(mState)}");
        return false;
    }
  }

  int validateAckSequence(final int code, final int sequence) {
    final int notValidated = -1;

    if (sequence < 0) {
      Log.w(tag, "Received ACK sequence ($sequence) is less than 0.");
      return notValidated;
    }

    if (sequence > RWCP.sequenceNumberMax) {
      Log.w(
          tag,
          "Received ACK sequence ($sequence) is bigger than its maximum value ("
          "${RWCP.sequenceNumberMax}"
          ").");
      return notValidated;
    }

    if (!_isSequenceWithinAckWindow(sequence)) {
      Log.w(
          tag,
          "Received ACK sequence ($sequence) is out of interval: last received is "
          "$mLastAckSequence"
          " and next will be "
          "$mNextSequence");
      return notValidated;
    }

    int acknowledged = 0;
    int nextAckSequence = mLastAckSequence;
    while (nextAckSequence != sequence) {
      nextAckSequence = increaseSequenceNumber(nextAckSequence);
      if (removeSegmentFromQueue(code, nextAckSequence)) {
        mLastAckSequence = nextAckSequence;
        if (mCredits < mWindow) {
          mCredits++;
        }
        acknowledged++;
      } else {
        Log.w(
            tag,
            "Error validating sequence "
            "$nextAckSequence"
            ": no corresponding segment in "
            "pending segments.");
      }
    }

    logState("$acknowledged"
        " segment(s) validated with ACK sequence(code=$code seq=$sequence");

    // increase the window size if qualified.
    increaseWindow(acknowledged);

    return acknowledged;
  }

  bool _isSequenceWithinAckWindow(int sequence) {
    if (mLastAckSequence < 0) {
      return sequence <= mNextSequence;
    }
    final mod = RWCP.sequenceNumberMax + 1;
    final forwardToNext = (mNextSequence - mLastAckSequence + mod) % mod;
    final forwardToSequence = (sequence - mLastAckSequence + mod) % mod;
    return forwardToSequence <= forwardToNext;
  }

  void _recoverTimeoutAfterSuccess(int acknowledged) {
    if (acknowledged <= 0) {
      return;
    }
    if (mDataTimeOutMs <= RWCP.dataTimeoutMsDefault) {
      mDataTimeOutMs = RWCP.dataTimeoutMsDefault;
      mSuccessfulAckStreak = 0;
      return;
    }
    mSuccessfulAckStreak += acknowledged;
    while (mSuccessfulAckStreak >= _timeoutRecoveryAckThreshold &&
        mDataTimeOutMs > RWCP.dataTimeoutMsDefault) {
      mSuccessfulAckStreak -= _timeoutRecoveryAckThreshold;
      mDataTimeOutMs -= RWCP.dataTimeoutMsDefault;
      if (mDataTimeOutMs < RWCP.dataTimeoutMsDefault) {
        mDataTimeOutMs = RWCP.dataTimeoutMsDefault;
      }
    }
  }

  bool removeSegmentFromQueue(int code, int sequence) {
    Segment? target;
    for (final s in mUnacknowledgedSegments) {
      if (s.getOperationCode() == code && s.getSequenceNumber() == sequence) {
        target = s;
        break;
      }
    }
    if (target != null) {
      mUnacknowledgedSegments.remove(target);
      return true;
    }
    Log.w(tag,
        "Pending segments does not contain acknowledged segment: code=$code \tsequence=$sequence");
    return false;
  }

  void increaseWindow(int acknowledged) {
    mAcknowledgedSegments += acknowledged;
    if (mAcknowledgedSegments >= mWindow && mWindow < mMaximumWindow) {
      mAcknowledgedSegments = 0;
      mWindow++;
      mCredits++;
      logState("increase window to $mWindow");
    }
  }

  bool receiveDataAck(Segment segment) {
    if (mShowDebugLogs) {
      Log.d(tag, "Receive dataAck for sequence ${segment.getSequenceNumber()}");
    }

    switch (mState) {
      case RWCPState.established:
        int sequence = segment.getSequenceNumber();
        int validated = validateAckSequence(RWCPOpCodeClient.data, sequence);
        if (validated >= 0) {
          cancelTimeOut();
          _recoverTimeoutAfterSuccess(validated);
          if (mCredits > 0 && !mPendingData.isEmpty) {
            sendDataSegment();
          } else if (mPendingData.isEmpty && mUnacknowledgedSegments.isEmpty) {
            // no more data to send: close session
            sendRSTSegment();
          } else if (mPendingData
                  .isEmpty /*&& !mUnacknowledgedSegments.isEmpty()*/
              ||
              mCredits == 0 /*&& !mPendingData.isEmpty()*/) {
            // no more data to send but still some waiting to be acknowledged
            // or no credits and still some data to send
            startTimeOut(mDataTimeOutMs);
          }
          mListener.onTransferProgress(validated);
        }
        return true;

      case RWCPState.closing:
        // rst had been sent, wait for the rst time out or rst ACK
        if (mShowDebugLogs) {
          Log.i(tag,
              "Received dataAck(${segment.getSequenceNumber()}) segment while in state closing: segment discarded.");
        }
        return true;

      case RWCPState.synSent:
      case RWCPState.listen:
      default:
        Log.w(tag,
            "Received unexpected dataAck segment with sequence ${segment.getSequenceNumber()} while in state ${RWCP.getStateLabel(mState)}");
        return false;
    }
  }

  bool receiveGAP(Segment segment) {
    if (mShowDebugLogs) {
      Log.d(tag, "Receive gap for sequence ${segment.getSequenceNumber()}");
    }

    switch (mState) {
      case RWCPState.established:
        if (mLastAckSequence > segment.getSequenceNumber()) {
          Log.i(tag,
              "Ignoring gap (${segment.getSequenceNumber()}) as last ack sequence is $mLastAckSequence.");
          return true;
        }
        if (mLastAckSequence <= segment.getSequenceNumber()) {
          // Sequence number in gap implies lost DATA_ACKs
          // adjust window
          decreaseWindow();
          // validate the acknowledged segments if not known.
          validateAckSequence(
              RWCPOpCodeClient.data, segment.getSequenceNumber());
        }

        cancelTimeOut();
        resendDataSegment();
        return true;

      case RWCPState.closing:
        // rst had been sent, wait for the rst time out or rst ACK
        if (mShowDebugLogs) {
          Log.i(tag,
              "Received gap(${segment.getSequenceNumber()}) segment while in state closing: segment discarded.");
        }
        return true;

      case RWCPState.synSent:
      case RWCPState.listen:
      default:
        Log.w(tag,
            "Received unexpected gap segment with header ${segment.getHeader()} while in state ${RWCP.getStateLabel(mState)}");
        return false;
    }
  }

  void decreaseWindow() {
    mWindow = ((mWindow - 1) ~/ 2) + 1;
    if (mWindow > mMaximumWindow || mWindow < 1) {
      mWindow = 1;
    }

    mAcknowledgedSegments = 0;
    mCredits = mWindow;

    logState("decrease window to $mWindow");
  }

  /// Releases resources held by the client.
  /// Should be called when the client is no longer needed.
  void dispose() {
    cancelTimeOut();
    reset(true);
  }
}
