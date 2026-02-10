/*
 * ************************************************************************************************
 * * © 2021-2024 Qualcomm Technologies, Inc. and/or its subsidiaries. All rights reserved.        *
 * ************************************************************************************************
 */

package com.qualcomm.qti.gaiaclient.core.gaia.core.sending

import com.qualcomm.qti.gaiaclient.core.gaia.core.RunningStatus
import com.qualcomm.qti.gaiaclient.core.gaia.core.v3.V3Plugin

/**
 * A class to hold all the parameters that can be set to send a [GaiaPacket] through a
 * [com.qualcomm.qti.gaiaclient.core.gaia.core.Plugin].
 */
data class Parameters(
    /**
     * To know if the packet should be held or sent immediately.
     */
    /**
     * To set if a packet should be held when a plugin status is [RunningStatus.ON_HOLD].
     * If the packet is not holdable, the packet is sent immediately.
     */
    /**
     * If a packet should be held when a plugin status is [RunningStatus.ON_HOLD]. If the packet is not
     * holdable it is sent immediately if the plugin status is not [RunningStatus.STOPPED].
     */
    val isHoldable: Boolean = true,
    /**
     * To know if the plugin should expect an acknowledgement.
     */
    /**
     * @param acknowledged
     * True if the packet is expected to be acknowledged, false otherwise.
     */
    /**
     * If the plugin should expect an acknowledging packet for the packet it is sending / has sent.
     */
    val isAcknowledged: Boolean = true,
    /**
     * To know if the plugin should force the system to flush the data.
     */
    /**
     * @param flushed
     * True to force the system to flush the data immediately to the device. This is a
     * blocking operation for the sending of data that can lead to a slower throughput of
     * data. If set to false, the system flushes the packets itself at the optimum time -
     * this can lead packets to be sent over a same stream. When using GAIA v1/v2, it is
     * recommended to flush the packets due to some delay on the device when packets are
     * on a same stream.
     */
    /**
     * If a GaiaSender should flush the packet.
     */
    val isFlushed: Boolean = false,

    /**
     * If a GaiaSender should use RWCP when available.
     */
    val useRwcp: Boolean = false,

    /**
     * The listener to notify when the packet is sent.
     */
    val listener: PacketSentListener? = null,
    /**
     * How long the plugin should wait for an acknowledgement.
     */
    /**
     * @param timeout
     * A value in milliseconds. This is ignored by the plugin if `acknowledged` is set to False.
     */
    /**
     * The timeout to wait for an acknowledgement.
     */
    val timeout: Long = V3Plugin.DEFAULT_RESPONSE_TIME_OUT_MS.toLong(),
)
