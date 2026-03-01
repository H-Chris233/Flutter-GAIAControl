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

  /// 发送失败后的最小重试间隔。
  ///
  /// 说明：这是“底层暂不可写（GATT busy/平台栈忙/瞬时断链）”的恢复手段，
  /// 不等同于 RWCP 协议 TIMEOUT（TIMEOUT 表示段已发出但未收到 ACK/GAP）。
  static const Duration sendRetryBaseDelay = Duration(milliseconds: 40);

  /// 发送失败后的最大重试间隔（指数退避上限）。
  static const Duration sendRetryMaxDelay = Duration(milliseconds: 1000);

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

  /// Whether to automatically close the RWCP session (send RST) when all pending/unacked data are drained.
  ///
  /// 工程实践：
  /// - 数据传输阶段：保持默认 true（最后一轮 ACK 后自动收尾）
  /// - validation 轮询阶段：建议设为 false，避免频繁 RST/SYN 抖动导致设备断链
  bool mCloseSessionWhenIdle = true;

  /// To know the number of segments which had been acknowledged in a row with dataAck.
  int mAcknowledgedSegments = 0;
  int mSuccessfulAckStreak = 0;
  static const int _timeoutRecoveryAckThreshold = 8;
  Timer? _timer;
  Timer? _sendRetryTimer;
  void Function()? _retryAction;
  int _sendRetryConsecutiveFailures = 0;
  Duration _sendRetryDelay = sendRetryBaseDelay;

  RWCPClient(this.mListener);

  /// 当前窗口剩余可发送额度（用于上层节流/观测）。
  int get credits => mCredits;

  /// 当前窗口大小（用于上层节流/观测）。
  int get window => mWindow;

  /// 待发送数据队列长度（用于上层节流/观测）。
  int get pendingDataLength => mPendingData.length;

  /// 未确认段队列长度（用于上层节流/观测）。
  int get unacknowledgedLength => mUnacknowledgedSegments.length;

  /// 是否正在重传（用于上层节流/观测）。
  bool get isResendingSegments => mIsResendingSegments;

  /// 当前是否有协议级超时计时器在跑（用于上层节流/观测）。
  bool get isTimeoutRunning => isTimeOutRunning;

  bool isRunningASession() {
    return mState != RWCPState.listen;
  }

  void showDebugLogs(bool show) {
    mShowDebugLogs = show;
    Log.i(tag, "Debug logs are now ${show ? "activated" : "deactivated"}.");
  }

  void setCloseSessionWhenIdle(bool close) {
    mCloseSessionWhenIdle = close;
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

    Log.d(tag, "onReceiveRWCPSegment code$code");
    // operation code occupies 2 bits, valid values are 0..3
    if (code == RWCPOpCodeServer.synAck) {
      return receiveSynAck(segment);
    }
    if (code == RWCPOpCodeServer.dataAck) {
      return receiveDataAck(segment);
    }
    if (code == RWCPOpCodeServer.rst) {
      return receiveRST(segment);
    }
    return receiveGAP(segment);
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
      _onSendSucceeded();
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

  int _dataTimeoutForCurrentWindow() {
    // 关键背景：
    // - 上层 BLE 写入在部分机型会被串行化/排队（GATT busy）。
    // - 若 data timeout 过小，会在“实际写入尚未完成”时误触发 TIMEOUT，导致无意义重传，
    //   进一步放大 GAP/卡顿问题。
    //
    // 这里在不改变 RWCP 常量（保持单测/默认配置）的前提下，给出一个“窗口相关的下限”。
    final w = mWindow;
    final floor = (w <= 2)
        ? 120
        : (w <= 4)
            ? 160
            : (w <= 8)
                ? 220
                : (w <= 16)
                    ? 320
                    : 420;
    return (mDataTimeOutMs < floor) ? floor : mDataTimeOutMs;
  }

  void _resetSendRetryBackoff() {
    _sendRetryConsecutiveFailures = 0;
    _sendRetryDelay = sendRetryBaseDelay;
  }

  void _onSendSucceeded() {
    _sendRetryConsecutiveFailures = 0;
    // 避免“偶发成功 → 立刻回到 40ms”导致的周期性突发：成功时只逐步回退 backoff。
    if (_sendRetryDelay > sendRetryBaseDelay) {
      final baseMs = sendRetryBaseDelay.inMilliseconds;
      final reducedMs = _sendRetryDelay.inMilliseconds ~/ 2;
      _sendRetryDelay =
          Duration(milliseconds: (reducedMs < baseMs) ? baseMs : reducedMs);
    } else {
      _sendRetryDelay = sendRetryBaseDelay;
    }
    _sendRetryTimer?.cancel();
    _sendRetryTimer = null;
    _retryAction = null;
  }

  Duration _onSendFailedAndGetDelay(String reason) {
    final delay = _sendRetryDelay;
    _sendRetryConsecutiveFailures += 1;

    // 指数退避：快速把高频重试压到 <= 1s，避免暂停/断链/不可写阶段持续 25Hz 唤醒。
    final nextMs = _sendRetryDelay.inMilliseconds * 2;
    final maxMs = sendRetryMaxDelay.inMilliseconds;
    _sendRetryDelay = Duration(milliseconds: (nextMs > maxMs) ? maxMs : nextMs);

    if (mShowDebugLogs) {
      Log.d(
          tag,
          "schedule send retry: $reason "
          "(failures=$_sendRetryConsecutiveFailures "
          "delay=${delay.inMilliseconds}ms next=${_sendRetryDelay.inMilliseconds}ms)");
    }
    return delay;
  }

  void _scheduleRetry(void Function() action, String reason) {
    // 发送失败通常意味着“底层暂时不可写”（断链/队列拥堵/平台栈忙）。
    // 这不是协议层 TIMEOUT（没有真正发出 DATA 段），因此单独做重试，避免：
    // - 立刻进入 TIMEOUT->重传->更拥堵 的负反馈
    // - 没有后续 ACK 触发时，发送链路永久停住

    // 会话已结束或没有待处理内容时，不进行重试，避免无意义唤醒。
    if (mState == RWCPState.listen) {
      return;
    }
    if (mPendingData.isEmpty && mUnacknowledgedSegments.isEmpty) {
      return;
    }

    final delay = _onSendFailedAndGetDelay(reason);
    _retryAction = action;

    // 若已有 retry 在排队，则只更新 action 与 backoff，不重复创建 Timer。
    _sendRetryTimer ??= Timer(delay, () {
      _sendRetryTimer = null;
      final run = _retryAction;
      _retryAction = null;
      if (mState == RWCPState.listen) {
        return;
      }
      if (mPendingData.isEmpty && mUnacknowledgedSegments.isEmpty) {
        return;
      }
      try {
        run?.call();
      } catch (e, st) {
        if (mShowDebugLogs) {
          Log.w(tag, "retry action failed: $e\n$st");
        }
      }
    });
  }

  void _cancelSendRetry() {
    _sendRetryTimer?.cancel();
    _sendRetryTimer = null;
    _retryAction = null;
    _resetSendRetryBackoff();
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

        // TIMEOUT 通常意味着链路/对端处理跟不上：收缩窗口降低并发写入压力，
        // 以减少持续 GAP/重传风暴导致的“卡住”。
        decreaseWindow();
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
      if (mCredits <= 0) break;
      int delay = (segment.getOperationCode() == RWCPOpCodeClient.syn)
          ? RWCP.synTimeoutMs
          : (segment.getOperationCode() == RWCPOpCodeClient.rst)
              ? RWCP.rstTimeoutMs
              : _dataTimeoutForCurrentWindow();
      final sent = sendSegment(segment, delay);
      if (!sent) {
        // 底层不可写：保持重传状态并稍后重试，避免 credits 被错误消耗。
        _scheduleRetry(resendSegment, "resendSegment");
        logState("resend segments (send failed)");
        return;
      }
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
      final sent = sendSegment(segment, _dataTimeoutForCurrentWindow());
      if (!sent) {
        _scheduleRetry(resendDataSegment, "resendDataSegment");
        logState("Resend data segments (send failed)");
        return;
      }
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
      final sent = sendSegment(segment, _dataTimeoutForCurrentWindow());
      if (!sent) {
        Log.w(
            tag,
            "Failed to send data segment(sequence=${segment.getSequenceNumber()}), "
            "keeping pending data for retry.");
        _scheduleRetry(() {
          if (mState == RWCPState.established && !mIsResendingSegments) {
            sendDataSegment();
          }
        }, "sendDataSegment");
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
    _cancelSendRetry();
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

  int validateAckSequence(final int code, final int sequence,
      {bool adjustWindow = true}) {
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

    // GAP/重传阶段不应“边确认边增窗”，否则容易出现窗口振荡，诱发持续 GAP。
    if (adjustWindow) {
      increaseWindow(acknowledged);
    }

    return acknowledged;
  }

  bool _isSequenceWithinAckWindow(int sequence) {
    // ACK 的有效区间应当是：[lastAck, lastSent]（包含重复 ACK）。
    // lastSent = mNextSequence - 1。若允许 ACK == mNextSequence，会把“尚未发送”的序号
    // 当作可确认范围，导致队列与窗口状态出现异常日志/误判。
    final mod = RWCP.sequenceNumberMax + 1;
    final lastSent = decreaseSequenceNumber(mNextSequence, 1);

    // mLastAckSequence == -1 表示“尚未确认任何段”，此时合法 ACK 仅可能落在 [0..lastSent]。
    // 不能用 RWCP.sequenceNumberMax 代替 -1，否则会错误接受 sequence=63 等异常 ACK，
    // 触发 validateAckSequence 的长循环与告警风暴。
    if (mLastAckSequence < 0) {
      return sequence <= lastSent;
    }

    final lastAck = mLastAckSequence;
    final forwardToLastSent = (lastSent - lastAck + mod) % mod;
    final forwardToSequence = (sequence - lastAck + mod) % mod;
    return forwardToSequence <= forwardToLastSent;
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
            // no more data to send: optionally close session
            if (mCloseSessionWhenIdle) {
              sendRSTSegment();
            }
          } else if (mPendingData
                  .isEmpty /*&& !mUnacknowledgedSegments.isEmpty()*/
              ||
              mCredits == 0 /*&& !mPendingData.isEmpty()*/) {
            // no more data to send but still some waiting to be acknowledged
            // or no credits and still some data to send
            startTimeOut(_dataTimeoutForCurrentWindow());
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
        final gapSequence = segment.getSequenceNumber();
        // GAP 表示对端发现乱序/缺口，建议缩窗并触发重传。
        //
        // 兼容性说明：
        // 部分设备在 GAP 段里携带的是“缺口起点/下一期待序列号”，而不是“最后已确认序列号”。
        // 若直接 validateAckSequence(..., gapSequence) 会把缺口段误判为已确认，
        // 进而导致缺口段永远不会被重传，传输会卡死在持续 GAP 的状态中。
        //
        // 因此这里将 ACK 对齐到 gapSequence-1，并重传未确认 DATA 段。
        final ackSequence = decreaseSequenceNumber(gapSequence, 1);
        decreaseWindow();
        validateAckSequence(RWCPOpCodeClient.data, ackSequence,
            adjustWindow: false);
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
    _cancelSendRetry();
    reset(true);
  }
}
