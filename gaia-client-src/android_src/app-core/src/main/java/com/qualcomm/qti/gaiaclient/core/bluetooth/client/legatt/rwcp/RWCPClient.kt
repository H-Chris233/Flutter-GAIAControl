/*
 * ************************************************************************************************
 * Copyright 2018 Qualcomm Technologies International, Ltd.                                       *
 * © 2023-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.          *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.bluetooth.client.legatt.rwcp

import android.util.Log
import androidx.core.util.Pair
import com.qualcomm.qti.gaiaclient.core.utils.DEBUG
import com.qualcomm.qti.gaiaclient.core.utils.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ====== INNER CLASSES ====================================================================
interface RWCPListener {
    /**
     *
     * To send the bytes of a RWCP segment to a connected Server.
     *
     * @param bytes
     * The bytes to send.
     *
     * @return True if the sending could be handled.
     */
    fun sendRWCPSegment(bytes: ByteArray?): Boolean

    /**
     *
     * Called when the transfer with RWCP has failed. The transfer fails in the following cases:
     *
     *  * The sending of a segment fails at the transport layer.
     *  * The Server sent a [RST][RWCP.OpCode.Client.RST] segment.
     *
     */
    fun onTransferFailed()

    /**
     *
     * Called when the transfer of all the data given to this client had been successfully sent and
     * acknowledged.
     */
    fun onTransferFinished()

    /**
     *
     * Called when some new segments had been acknowledged to inform the listener.
     *
     * @param acknowledged
     * The number of segments which had been acknowledged.
     */
    fun onTransferProgress(acknowledged: Int)
}


class RWCPClient constructor(private val listener: RWCPListener, private val scope: CoroutineScope = GlobalScope) {

    private val TAG = "RWCPClient"
    private val showDebugLogs = DEBUG.Bluetooth.RWCP

    private var lastAckSequence: Int = -1
    private var nextSequence: Int = 0
    private var acknowledgedSegments: Int = 0
    private val initialWindow: Int = RWCP.WINDOW_DEFAULT
    private val maxWindow: Int = RWCP.WINDOW_MAX
    private var window: Int = initialWindow
    private var credits: Int = window
    private var isTimeoutRunning: Boolean = false
    private var isResendingData: Boolean = false
    private var dataTimeout: Int = RWCP.DATA_TIMEOUT_MS_DEFAULT

    private var timeoutJob: Job? = null

    @RWCP.State
    private var state: Int = RWCP.State.LISTEN

    private val pendingData: ArrayDeque<RWCPData> = ArrayDeque()
    private val unacknowledgedSegments: ArrayDeque<Segment> = ArrayDeque()

    fun sendData(data: ByteArray, onSent: () -> Unit): Boolean {
        synchronized(this) {

            pendingData.add(RWCPData(data, onSent))

            if (state == RWCP.State.LISTEN) {
                return startSession()
            } else if (state == RWCP.State.ESTABLISHED && !isTimeoutRunning) {
                sendDataSegment()
                return true
            }

            return true
        }
    }

    fun onReceiveRWCPSegment(data: ByteArray): Boolean {
        synchronized(this) {
            Logger.log(showDebugLogs, TAG, "onReceiveRWCPSegment", Pair("bytes", data))
            if (data.size < RWCP.Segment.REQUIRED_INFORMATION_LENGTH) return false
            val segment = Segment(data)

            return when (segment.operationCode.toByte()) {
                RWCP.OpCode.NONE -> false
                RWCP.OpCode.Server.SYN_ACK -> onSynAck(segment)
                RWCP.OpCode.Server.DATA_ACK -> onDataAck(segment)
                RWCP.OpCode.Server.RST -> onRst(segment)
                RWCP.OpCode.Server.GAP -> onGap(segment)
                else -> false
            }
        }
    }

    fun cancelTransfer() {
        logState("cancelTransfer")

        if (state == RWCP.State.LISTEN) {
            Log.i(TAG, "cancelTransfer: no ongoing transfer to cancel.")
            return
        }

        reset(true)

        if (!sendRSTSegment()) {
            Log.w(TAG, "Sending of RST segment has failed, terminating session.")
            terminate()
        }
    }

    private fun sendDataSegment() {
        while (state == RWCP.State.ESTABLISHED && credits > 0 && pendingData.isNotEmpty() && !isResendingData) {
            val data = pendingData.removeFirst()
            val segment = Segment(RWCP.OpCode.Client.DATA.toInt(), nextSequence, data)
            val success = send(segment, dataTimeout)
            unacknowledgedSegments.add(segment)
            nextSequence = incSeqNum(nextSequence)
            credits--
        }
    }

    private fun sendSYNSegment(): Boolean {
        state = RWCP.State.SYN_SENT
        val segment = Segment(RWCP.OpCode.Client.SYN.toInt(), nextSequence)
        val success = send(segment, RWCP.SYN_TIMEOUT_MS)
        if (success) {
            unacknowledgedSegments.add(segment)
            nextSequence = incSeqNum(nextSequence)
            credits--
            logState("send SYN segment")
        }
        return success
    }

    private fun sendRSTSegment(): Boolean {
        if (state == RWCP.State.CLOSING) {
            return true
        }

        reset()

        state = RWCP.State.CLOSING
        val segment = Segment(RWCP.OpCode.Client.RST.toInt(), nextSequence)
        val success = send(segment, RWCP.RST_TIMEOUT_MS)

        if (success) {
            unacknowledgedSegments.add(segment)
            nextSequence = incSeqNum(nextSequence)
            credits--
            logState("send RST segment")
        }
        return success
    }


    private fun send(segment: Segment, timeout: Int): Boolean {
        val data = segment.bytes
        Logger.log(showDebugLogs, TAG, "send data", Pair("seq", segment.sequenceNumber), Pair("data", segment.bytes))
        return if (listener.sendRWCPSegment(data)) {
            startTimeout(timeout)
            true
        } else {
            false
        }
    }

    private fun resendDataSegments() {
        if (state != RWCP.State.ESTABLISHED) {
            Log.w(TAG, "Trying to resend data segment while not in ESTABLISHED state.")
            return
        }
        isResendingData = true
        credits = window
        logState("reset credits")

        var moved = 0
        while (unacknowledgedSegments.size > credits) {
            val segment = unacknowledgedSegments.last()
            if (segment.operationCode == RWCP.OpCode.Client.DATA.toInt()) {
                removeSegmentFromQueue(segment)
                pendingData.addFirst(segment.payload)
                moved++
            } else {
                break
            }
        }

        nextSequence = decSeqNum(nextSequence, moved)

        for (segment in unacknowledgedSegments) {
            send(segment, dataTimeout)
            credits--
        }

        logState("resend DATA segments")

        isResendingData = false

        if (credits > 0) {
            sendDataSegment()
        }
    }

    private fun resendSegments() {
        if (state == RWCP.State.ESTABLISHED) {
            Log.w(TAG, "Trying to resend non data segment while in ESTABLISHED state.")
            return
        }

        isResendingData = true
        credits = window

        for (segment in unacknowledgedSegments) {
            val delay = when (segment.operationCode) {
                RWCP.OpCode.Client.SYN.toInt() -> {
                    RWCP.SYN_TIMEOUT_MS
                }
                RWCP.OpCode.Client.RST.toInt() -> {
                    RWCP.RST_TIMEOUT_MS
                }
                else -> {
                    dataTimeout
                }
            }

            send(segment, delay)
            credits--
        }
        logState("resend segments")
        isResendingData = false
    }

    private fun incSeqNum(num: Int): Int {
        return (num + 1) % (RWCP.SEQUENCE_NUMBER_MAX + 1)
    }

    private fun decSeqNum(num: Int, decrease: Int): Int {
        return (num - decrease + RWCP.SEQUENCE_NUMBER_MAX + 1) % (RWCP.SEQUENCE_NUMBER_MAX + 1)
    }

    private fun reset(complete: Boolean = false) {
        lastAckSequence = -1
        nextSequence = 0
        state = RWCP.State.LISTEN
        unacknowledgedSegments.clear()
        window = initialWindow
        acknowledgedSegments = 0
        credits = window
        cancelTimeout()

        if (complete) {
            pendingData.clear()
        }

        logState("reset")
    }

    private fun onSynAck(segment: Segment): Boolean {
        Logger.log(showDebugLogs, TAG, "receiveSynAck", Pair("SeqNumber", segment.sequenceNumber))
        return when (state) {
            RWCP.State.SYN_SENT -> {
                cancelTimeout()
                val validated = validateAckSeq(RWCP.OpCode.Client.SYN.toInt(), segment.sequenceNumber)
                if (validated >= 0) {
                    state = RWCP.State.ESTABLISHED
                    if (pendingData.isNotEmpty()) {
                        sendDataSegment()
                    }
                } else {
                    Log.w(TAG, "Receive SYN_ACK with unexpected sequence number")
                    terminate()
                    listener.onTransferFailed()
                }
                true
            }
            RWCP.State.ESTABLISHED -> {
                cancelTimeout()
                if (unacknowledgedSegments.isNotEmpty()) {
                    resendDataSegments()
                }
                true
            }
            else -> {
                Log.w(
                    TAG, "Received unexpected SYN_ACK segment with header " + segment.getHeader()
                            + " while in state ${RWCP.getStateLabel(state)}"
                )
                false
            }
        }
    }

    private fun onDataAck(segment: Segment): Boolean {
        Logger.log(showDebugLogs, TAG, "receiveDataAck", Pair("SeqNumber", segment.sequenceNumber))
        return when (state) {
            RWCP.State.ESTABLISHED -> {
                cancelTimeout()
                val sequence = segment.sequenceNumber
                val validated = validateAckSeq(RWCP.OpCode.Client.DATA.toInt(), sequence)
                if (validated >= 0) {
                    if (credits > 0 && pendingData.isNotEmpty()) {
                        sendDataSegment()
                    } else if (pendingData.isEmpty() && unacknowledgedSegments.isEmpty()) {
                        sendRSTSegment()
                    } else if (pendingData.isEmpty() || credits == 0) {
                        startTimeout(dataTimeout)
                    }
                    listener.onTransferProgress(validated)
                }
                true
            }
            RWCP.State.CLOSING -> {
                if (showDebugLogs) {
                    Log.i(
                        TAG, "Received DATA_ACK(${segment.sequenceNumber}"
                                + ") segment while in state CLOSING: segment discarded."
                    )
                }
                true
            }
            else -> {
                Log.w(
                    TAG, "Received unexpected DATA_ACK segment with sequence " + segment.sequenceNumber
                            + " while in state " + RWCP.getStateLabel(state)
                )
                false
            }
        }
    }

    private fun onRst(segment: Segment): Boolean {

        Logger.log(showDebugLogs, TAG, "receiveRST", Pair("SeqNumber", segment.sequenceNumber))
        return when (state) {
            RWCP.State.SYN_SENT -> {
                Log.i(
                    TAG, "Received RST (sequence " + segment.sequenceNumber + ") in SYN_SENT state, ignoring " +
                            "segment."
                )
                true
            }
            RWCP.State.ESTABLISHED -> {
                Log.w(
                    TAG, "Received RST (sequence " + segment.sequenceNumber + ") in ESTABLISHED state, " +
                            "terminating session, transfer failed."
                )
                terminate()
                listener.onTransferFailed()
                true
            }
            RWCP.State.CLOSING -> {
                cancelTimeout()
                validateAckSeq(RWCP.OpCode.Client.RST.toInt(), segment.sequenceNumber)
                reset()
                if (pendingData.isNotEmpty()) {
                    if (!sendSYNSegment()) {
                        Log.w(TAG, "Start session of RWCP data transfer failed: sending of SYN failed.")
                        terminate()
                        listener.onTransferFailed()
                    }
                } else {
                    listener.onTransferFinished()
                }
                true
            }
            else -> {
                Log.w(
                    TAG, "Received unexpected RST segment with sequence=" + segment.sequenceNumber
                            + " while in state " + RWCP.getStateLabel(state)
                )
                false
            }
        }
    }

    private fun onGap(segment: Segment): Boolean {
        Logger.log(showDebugLogs, TAG, "receiveGAP", Pair("SeqNumber", segment.sequenceNumber))
        return when (state) {
            RWCP.State.ESTABLISHED -> {
                if (isGAPDiscarded(segment.sequenceNumber, lastAckSequence, window)) {
                    Log.i(
                        TAG, "Ignoring GAP (" + segment.sequenceNumber + ") as last ack sequence is "
                                + lastAckSequence + "."
                    )
                    return true
                }
                decreaseWindow()
                validateAckSeq(RWCP.OpCode.Client.DATA.toInt(), segment.sequenceNumber)

                cancelTimeout()
                resendDataSegments()
                true
            }
            RWCP.State.CLOSING -> {
                if (showDebugLogs) {
                    Log.i(
                        TAG, "Received GAP(" + segment.sequenceNumber
                                + ") segment while in state CLOSING: segment discarded."
                    )
                }
                true
            }
            else -> {
                Log.w(
                    TAG, "Received unexpected GAP segment with header " + segment.getHeader()
                            + " while in state " + RWCP.getStateLabel(state)
                )
                false
            }
        }
    }

    private fun validateAckSeq(code: Int, sequence: Int): Int {
        if (sequence < 0) {
            Log.w(TAG, "Received ACK sequence ($sequence) is less than 0.")
            return -1
        }
        if (sequence > RWCP.SEQUENCE_NUMBER_MAX) {
            Log.w(
                TAG,
                "Received ACK sequence ($sequence) is bigger than its maximum value (${RWCP.SEQUENCE_NUMBER_MAX})."
            )
            return -1
        }
        if (lastAckSequence < nextSequence && (sequence < lastAckSequence || sequence > nextSequence)) {
            Log.w(
                TAG,
                "Received ACK sequence ($sequence) is out of interval: last received is $lastAckSequence and next will be $nextSequence)"
            )
            return -1
        }
        if (lastAckSequence > nextSequence && sequence < lastAckSequence && sequence > nextSequence) {
            Log.w(
                TAG, "Received ACK sequence ($sequence) is out of interval: last received is $lastAckSequence and " +
                        "next will be $nextSequence"
            )
            return -1
        }

        var acknowledged = 0
        var nextAckSequence = lastAckSequence

        while (nextAckSequence != sequence) {
            nextAckSequence = incSeqNum(nextAckSequence)
            if (removeSegmentFromQueue(code, nextAckSequence)) {
                lastAckSequence = nextAckSequence
                if (credits < window) {
                    credits++
                }
                acknowledged++
            } else {
                Log.w(
                    TAG, "Error validating sequence $nextAckSequence : no corresponding segment in " +
                            "pending segments."
                )
            }
        }

        increaseWindow(acknowledged)

        return acknowledged
    }

    private fun isGAPDiscarded(sequence: Int, last: Int, window: Int): Boolean {
        val diff = (if (last > sequence) RWCP.SEQUENCE_NUMBER_MAX else 0) + sequence - last
        return diff in 1..window
    }

    private fun increaseWindow(acknowledged: Int) {
        acknowledgedSegments += acknowledged
        if (acknowledgedSegments > window && window < maxWindow) {
            acknowledgedSegments = 0
            window++
            credits++
        }

    }

    private fun decreaseWindow() {
        window = (window - 1) / 2 + 1
        if (window > maxWindow || window < 1) {
            window = 1
        }
        acknowledgedSegments = 0
        credits = window

        logState("decrease window to $window")
    }

    private fun removeSegmentFromQueue(code: Int, sequence: Int): Boolean {
        val segment = unacknowledgedSegments.firstOrNull {
            it.operationCode == code && it.sequenceNumber == sequence
        } ?: return false

        segment.payload.onSent?.invoke()
        return unacknowledgedSegments.remove(segment)
    }

    private fun removeSegmentFromQueue(segment: Segment): Boolean {
        return unacknowledgedSegments.remove(segment)
    }

    private fun startTimeout(timeout: Int) {
        if (isTimeoutRunning) {
            timeoutJob?.cancel()
        }
        isTimeoutRunning = true

        timeoutJob = scope.launch() {
            delay(timeout.toLong())
            onTimeout()
        }
    }

    private fun cancelTimeout() {
        if (isTimeoutRunning) {
            timeoutJob?.cancel()
            isTimeoutRunning = false
        }
    }

    private fun onTimeout() {
        synchronized(this) {
            if (!isTimeoutRunning) return

            isTimeoutRunning = false
            isResendingData = true
            acknowledgedSegments = 0

            if (state == RWCP.State.ESTABLISHED) {
                dataTimeout *= 2
                if (dataTimeout > RWCP.DATA_TIMEOUT_MS_MAX) {
                    dataTimeout = RWCP.DATA_TIMEOUT_MS_MAX
                }

                resendDataSegments()
            } else {
                resendSegments()
            }
        }
    }

    private fun startSession(): Boolean {
        if (state != RWCP.State.LISTEN) {
            return false
        }

        return if (sendRSTSegment()) {
            true
        } else {
            terminate()
            false
        }
    }

    private fun terminate() {
        reset(true)
    }

    private fun logState(label: String) {
        if (showDebugLogs) {
            val message = "$label \t\t\tstate= ${RWCP.getStateLabel(state)}" +
                    "\n\tWindow: \tcurrent = $window \t\tdefault = $initialWindow" +
                    " \t\tcredits = $credits" +
                    "\n\tSequence: \tlast = $lastAckSequence  \t\tnext = $nextSequence" +
                    "\n\tPending: \tPSegments = ${unacknowledgedSegments.size}" +
                    " \t\tPData = ${pendingData.size}"
            Logger.log(true, TAG, label, message)
        }
    }

}